package example.util;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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


    private static BigDecimal reverseOrderQuantity = BigDecimal.ZERO;
    private static boolean inTrade = false;
    private static String startSide;

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
        LambdaLogger contextLogger = context.getLogger();
        contextLogger.log("=====================================================================");
        double percentage = Double.parseDouble(props.get("position-entry"));
        // uppercase
        String[] s = model.split("_");
        String side = s[0];
        String name = s[1];
        String tradeClose = s[2]; // entry/close

        if (!props.get("name").contains(name)) {
            contextLogger.log("Name not matched. " + name);
            return new OrderResult();
        }

//        if ("MARGIN".equals(type)) {
//            if (Boolean.parseBoolean(getProps().get("isolated"))) {
//                usdt = getIsolatedQuoteAsset("BTCUSDT");
//            } else {
//                usdt = getMarginAsset(asset);
//            }
//        }
        String type = props.get("type");
        Map<String, Asset> assetsMap = getAssetsMap(type, context);
        Asset btc = assetsMap.get("BTC");
        contextLogger.log(String.format("BTC: %s USDT value: %s", btc.getValue(), btc.getUsdtValue()));
        Asset usdt = assetsMap.get("USDT");
        contextLogger.log(String.format("USDT: %s", usdt.getValue()));

        BigDecimal btcUsdtValue = new BigDecimal(assetsMap.get("BTC").getUsdtValue());
        BigDecimal usdtValue = new BigDecimal(assetsMap.get("USDT").getValue());
        BigDecimal total = btcUsdtValue.add(usdtValue);
        BigDecimal quantity = total.multiply(BigDecimal.valueOf(percentage))
                .round(new MathContext(8));
        contextLogger.log("qValue: " + quantity.toPlainString());

        if (!inTrade /*&& reverseOrderQuantity.intValue() == 0*/) {
            inTrade = true;
            startSide = side;
            // check tradeClose "entry";
            reverseOrderQuantity = quantity;
            contextLogger.log("new trade, reverseOrderQuantity = quantity");
        } else {
            inTrade = false;
            quantity = reverseOrderQuantity;
            reverseOrderQuantity = BigDecimal.ZERO;

//            if (side.equals(startSide)) {
//                // missed close
//                side = reverseSide(side);  // close
//
//                // + send new trade order
//            }
            contextLogger.log("close trade, quantity = reverseOrderQuantity");
        }

        // percentage * total < 10 split balance and retry
        // response: {"code":-1013,"msg":"Filter failure: NOTIONAL"}
        BigDecimal notional = BigDecimal.TEN;


        boolean splitForNotional = usdtValue.subtract(notional).doubleValue() <= 0
                || btcUsdtValue.subtract(notional).doubleValue() <= 0;
        boolean splitForQuantity = usdtValue.subtract(quantity).doubleValue() <= 0
                || btcUsdtValue.subtract(quantity).doubleValue() <= 0;

        if (splitForNotional || splitForQuantity) {
            contextLogger.log("================ split ================ ");
            quantity = total.multiply(BigDecimal.valueOf(0.5)).round(new MathContext(8));
            inTrade = false;
            reverseOrderQuantity = BigDecimal.ZERO;
            side = reverseSide(side);
        }

        OrderResult orderResult = ApiClientUtil.sendOrder(side, quantity.toPlainString(), BTCUSDT, getProps(), context);

        if ("SPOT".equals(type) || type == null) {
            Map<String, Asset> assetsMapAfter = getAssetsMap(type, context);
            Asset btcAfter = assetsMapAfter.get("BTC");
            contextLogger.log(String.format("BTC: %s USDT value: %s", btcAfter.getValue(), btcAfter.getUsdtValue()));
            Asset usdtAfter = assetsMapAfter.get("USDT");
            contextLogger.log("USDT: " + usdtAfter.getValue());
        }

        return orderResult;
    }

    private static String reverseSide(String side) {
        side = "BUY".equals(side) ? "SELL" : "BUY";
        return side;
    }

    private Map<String, Asset> getAssetsMap(String type, Context context) throws IOException, InterruptedException {
        Map<String, Asset> assets = new HashMap<>();
        if ("SPOT".equals(type) || type == null) {
            String btcAsset = getAssetFreeBalance("BTC", context);
            BigDecimal usdtValueBtc = new BigDecimal(btcAsset).multiply(new BigDecimal(getPrice(context)));
            String usdtValue = usdtValueBtc.round(new MathContext(8)).toPlainString();
            assets.put("BTC", new Asset("BTC", btcAsset, usdtValue));

            String usdtAsset = getAssetFreeBalance("USDT", context);
            BigDecimal usdt = new BigDecimal(usdtAsset).round(new MathContext(8));
            String valueUsdt = usdt.toPlainString();
            assets.put("USDT", new Asset("USDT", valueUsdt, valueUsdt));
        }
        return assets;
    }

    private String getAssetFreeBalance(final String asset, Context context) throws IOException, InterruptedException {
        List<AccInfoResponse.Balance> spotBalance = getSpotBalance(context);
        AccInfoResponse.Balance assetBalance = spotBalance.stream()
                .filter(b -> asset.equals(b.getAsset())).findFirst().orElse(null);

        return assetBalance != null ? assetBalance.getFree() : null;
    }

    private String getPrice(Context context) throws IOException, InterruptedException {
        String priceResponse = ApiClientUtil.getSimple(
                "ticker/price",
                Collections.singletonMap("symbol", BTCUSDT),
                props);

        LambdaLogger contextLogger = context.getLogger();
        contextLogger.log("Current ticker/price " + priceResponse);
        HashMap<String, String> responseJson = OM.readValue(priceResponse, new TypeReference<HashMap<String, String>>() { });

        return responseJson.get("price");
    }

    public List<AccInfoResponse.Balance> getSpotBalance(Context context) throws IOException, InterruptedException {
        String body = ApiClientUtil.get("account", new HashMap<>(), getProps(), context);

        AccInfoResponse response = OM.readValue(body, AccInfoResponse.class);
        context.getLogger().log(String.format("/account response: AccountType %s UpdateTime %s",
                response.getAccountType(), response.getUpdateTime()));

        return response.getBalances();
    }

//    public String getMarginAsset(String asset) throws IOException, InterruptedException {
//        HashMap<String, String> queryParams = new HashMap<>();
//        String resp = ApiClientUtil.get("account", queryParams, getProps());
//
//        // get asset json
//        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
//        });
//        List<LinkedHashMap> assets = (List<LinkedHashMap>) OM.readValue(resp, new TypeReference<HashMap<String, Object>>() { })
//                .get("userAssets");
//        LinkedHashMap usdt = assets.stream().filter(l -> l.containsValue("USDT")).collect(Collectors.toList()).get(0);
//
//        return (String) usdt.get("free");
//    }

//    public String getIsolatedQuoteAsset(String symbol) throws IOException, InterruptedException {
//        HashMap<String, String> queryParams = new HashMap<>();
//
//        String path = "account";
//        String isolated = getProps().get("isolated");
//        if (Boolean.parseBoolean(isolated)) {
//            path = "isolated/" + path;
//        }
//        String resp = ApiClientUtil.get(path, queryParams, getProps());
//
//        // get asset json
//        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
//        });
//        String assetsJson = OM.writeValueAsString(responseJson.get("assets"));
//        List<Map<String,Object>> assets = OM.readValue(assetsJson, new TypeReference<List<Map<String,Object>>>() {
//        });
//        logger.info(assets.get(0).toString());
//
//        Map<String, Object> bySymbol = assets.stream().filter(a -> symbol.equals(a.get("symbol"))).findFirst().orElse(new HashMap<>());
//        String quoteAsset = OM.writeValueAsString(bySymbol.get("quoteAsset"));
//        Map<String,String> value = OM.readValue(quoteAsset, new TypeReference<Map<String, String>>() { });
//
//        String usdt = value.get("asset");
//
//        logger.info("Isolated USDT " + usdt);
//
//        return usdt;
//
//    }

    private Map<String, String> getProps() {
        return Collections.unmodifiableMap(props);
    }

}
