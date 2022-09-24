package util;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.AccInfoResponse;
import model.OrderResult;

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


public class OrderService {

    private static BigDecimal reverseOrderQuantity = BigDecimal.valueOf(0);
    private static boolean inTrade = false;

    private final HashMap<String, String> props = new HashMap<>();


    private final ObjectMapper OM;

    private static final String BTCUSDT = "BTCUSDT";

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

    public OrderResult processOrder(String model, Context context) throws IOException, InterruptedException {
        double percentage = Double.parseDouble(props.get("position-entry"));
        // uppercase
        String side = model.split("_")[0];
        String name = model.split("_")[1];
        if (!name.equalsIgnoreCase(props.get("name"))) {
            context.getLogger().log("Name not matched. " + name);
            return new OrderResult();
        }

        String usdt = "0";
        if ("MARGIN".equals(props.get("type"))) {
            usdt = getMarginAsset(context, "USDT");
        }
        if ("SPOT".equals(props.get("type")) || props.get("type") == null) {
            usdt = getSpotAsset(context, "USDT");

        }
        if (usdt == null) {
            context.getLogger().log("No free assets.");
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

        context.getLogger().log("Quantity: " + qValue);

        // may need to enable before
//      HashMap<String, Object> enableAcc = enableAcc(context);
//      context.getLogger().log("enableAcc " + enableAcc.get("success"));

        return ApiClientUtil.sendOrder(side, qValue, BTCUSDT, context, getProps());
    }

    public String getSpotAsset(Context context, String asset) throws IOException, InterruptedException {
        // GET /sapi/v1/margin/isolated/account timestamp, recv
        HashMap<String, String> queryParams = new HashMap<>();
        String body = ApiClientUtil.get("account", queryParams, context, getProps());

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

    private String getMarginAsset(Context context, String asset) throws IOException, InterruptedException {
        String url = props.get("rest-uri-margin") + "account";
        HashMap<String, String> queryParams = new HashMap<>();
        String resp = ApiClientUtil.get(url, queryParams, context, getProps());

        // get asset json
        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
        });
        String assetsJson = OM.writeValueAsString(responseJson.get("userAssets"));
        List< Object> userAssetsJson = OM.readValue(assetsJson, new TypeReference<List<Object>>() {
        });
        context.getLogger().log(userAssetsJson.get(0).toString());

        for(Object a : userAssetsJson) {
            if (a.toString().contains(asset)) {
                String[] pair = a.toString().split(",")[1].split("=");
                context.getLogger().log(pair[1]);

                return pair[1];
            }
        }

        return null;
    }

    private Map<String, String> getProps() {
        return Collections.unmodifiableMap(props);
    }

}
