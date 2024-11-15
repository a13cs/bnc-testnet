package test.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import test.model.AccInfoResponse;
import test.model.OrderResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static BigDecimal reverseOrderQuantity = BigDecimal.valueOf(0);
    private static boolean inTrade = false;

    private final HashMap<String, String> props = new HashMap<>();


    private final ObjectMapper OM;

    private static final String BTCUSDT = "BTCUSDT";

    @Value("${name}")
    String name;

    public OrderService() {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        if (inputStream != null) {
            List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
            lines.forEach(l -> {
                String[] pair = l.split("=");
                // take first
                props.putIfAbsent(pair[0], pair[1]);
            });
        }
    }

    public OrderResult processOrder(String model) throws IOException, InterruptedException {
        double percentage = Double.parseDouble(props.get("position-entry"));
        // uppercase
        String side = model.split("_")[0];
        String name = model.split("_")[1];
        if (!name.equalsIgnoreCase(props.get("name"))) {
            logger.info("Name not matched. " + name);
            return new OrderResult();
        }

        String usdt = "0";
        String type = props.get("type");
        if ("MARGIN".equals(type)) {
            if (Boolean.parseBoolean(getProps().get("isolated"))) {
                usdt = getIsolatedQuoteAsset("BTCUSDT");
            } else {
                usdt = getMarginAsset("USDT");
            }
        }
        if ("SPOT".equals(type) || type == null) {
            usdt = getSpotAsset("USDT");
        }

        if (usdt == null) {
            logger.info("No free assets.");
            return new OrderResult();
        }

        BigDecimal freeUsdt = new BigDecimal(usdt);
        BigDecimal quantity = BigDecimal.valueOf(percentage).multiply(freeUsdt);

        if (!inTrade && reverseOrderQuantity.intValue() == 0) {
            inTrade = true;
            reverseOrderQuantity = quantity;
        }

        BigDecimal orderQty = inTrade ? reverseOrderQuantity : quantity;

        String qValue = orderQty
                .round(new MathContext(8))
                /*.movePointLeft(1)*/
                .toPlainString();

        logger.info("Quantity: " + qValue);

        // may need to enable before
        return ApiClientUtil.sendOrder(side, qValue, BTCUSDT, getProps());

    }

    public String getSpotAsset(String asset) throws IOException, InterruptedException {
        HashMap<String, String> queryParams = new HashMap<>();
        String body = ApiClientUtil.get("account", queryParams, getProps());

        AccInfoResponse response;
        try {
            response = OM.readValue(body, AccInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<AccInfoResponse.Balance> balances = response.getBalances();
        if (balances != null) {
            AccInfoResponse.Balance assetBalance = balances.stream().filter(b -> asset.equals(b.getAsset())).findFirst().orElse(null);
            if (assetBalance != null) {
                return assetBalance.getFree();
            }
        }

        return null;
    }

    public String getMarginAsset(String asset) throws IOException, InterruptedException {
        HashMap<String, String> queryParams = new HashMap<>();
        String resp = ApiClientUtil.get("account", queryParams, getProps());

        // get asset json
        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
        });
        String assetsJson = OM.writeValueAsString(responseJson.get("userAssets"));
        List< Object> userAssetsJson = OM.readValue(assetsJson, new TypeReference<List<Object>>() {
        });
        logger.info(userAssetsJson.get(0).toString());

        for(Object a : userAssetsJson) {
            if (a.toString().contains(asset)) {
                String[] pair = a.toString().split(",")[1].split("=");
                logger.info(pair[1]);

                return pair[1];
            }
        }

        return null;

    }

    public String getIsolatedQuoteAsset(String symbol) throws IOException, InterruptedException {
        HashMap<String, String> queryParams = new HashMap<>();

        String path = "account";
        String isolated = getProps().get("isolated");
        if (Boolean.parseBoolean(isolated)) {
            path = "isolated/" + path;
        }
        String resp = ApiClientUtil.get(path, queryParams, getProps());

        // get asset json
        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
        });
        String assetsJson = OM.writeValueAsString(responseJson.get("assets"));
        List<Map<String,Object>> assets = OM.readValue(assetsJson, new TypeReference<List<Map<String,Object>>>() {
        });
        logger.info(assets.get(0).toString());

        Map<String, Object> bySymbol = assets.stream().filter(a -> symbol.equals(a.get("symbol"))).findFirst().orElse(new HashMap<>());
        String quoteAsset = OM.writeValueAsString(bySymbol.get("quoteAsset"));
        Map<String,String> value = OM.readValue(quoteAsset, new TypeReference<Map<String, String>>() { });

        String usdt = value.get("asset");

        logger.info("Isolated USDT " + usdt);

        return usdt;

    }

    private Map<String, String> getProps() {
        return Collections.unmodifiableMap(props);
    }

}
