package util;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import main.SignalModel;
import model.AccInfoResponse;
import model.OrderResult;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class OrderService {

    // todo
    private static final String TYPE = "SPOT"; // MARGIN
    private static BigDecimal reverseOrderQuantity = BigDecimal.valueOf(0);
    private static boolean inTrade = false;

    private final HashMap<String, String> props = new HashMap<>();

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private static final String BTCUSDT = "BTCUSDT";

    public OrderService() {
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

    public OrderResult processOrder(SignalModel model, Context context) throws IOException, InterruptedException {
        double percentage = Double.parseDouble(props.get("position-entry")); // 0.25

//      overbuy/oversell

//        if (Boolean.parseBoolean(model.getExtra())) {
//            percentage = Double.parseDouble(props.get("position-entry-extra")); // 0.1
//            if (extraCount.get() > maxExtraEntry) {
//                return new OrderResult();
//            } else if ("buy".equalsIgnoreCase(model.getAction())) {
//                extraCount.incrementAndGet();
//                // model.getOrderId()
//                // save entry pos
//            } else {
//                extraCount.decrementAndGet();
////                model.getOrderId()
//                // save entry pos
//            }
//        }

        balanceDiff(context, getInfo(context));  // use margin

        AccInfoResponse acc = getInfo(context);
        List<AccInfoResponse.Balance> balances = acc.getBalances();

        if (balances != null) {
            AccInfoResponse.Balance balanceUsdt = balances.stream().filter(b -> "USDT".equals(b.getAsset())).findFirst().orElse(null);
            AccInfoResponse.Balance balanceBtc = balances.stream().filter(b -> "BTC".equals(b.getAsset())).findFirst().orElse(null);
            if (balanceUsdt != null && balanceBtc != null) {
                BigDecimal accBalanceUsdt = new BigDecimal(balanceUsdt.getFree());
//                BigDecimal accBalanceBtc = new BigDecimal(balanceBtc.getFree());

//                BigDecimal quantity = BigDecimal.valueOf(percentage).multiply(accBalanceUsdt);
                BigDecimal quantity = BigDecimal.valueOf(percentage).multiply(new BigDecimal(getMarginAsset(context, "USDT")));

                if (!inTrade && reverseOrderQuantity.intValue() == 0) {
                    inTrade = true;
                    reverseOrderQuantity = quantity;
                }

                String side = model.getAction().toUpperCase();
                BigDecimal orderQty = inTrade ? reverseOrderQuantity : quantity;

                String qValue = orderQty.round(new MathContext(8))/*.movePointLeft(1)*/.toPlainString();
                context.getLogger().log("Quantity: " + qValue);

//                HashMap<String, Object> enableAcc = enableAcc(context);
//                context.getLogger().log("enableAcc " + enableAcc.get("success"));

                return sendOrder(side, qValue, BTCUSDT, context);
            }
        }
//        OrderResult orderResult = new OrderResult();
//        orderResult.setSide(model.getAction().toUpperCase());
//        return orderResult;

        throw new RuntimeException("No balance for symbol " + BTCUSDT);
    }

    private String getMarginAsset(Context context, String asset) throws IOException, InterruptedException {
        String resp = getUrl("https://api.binance.com/sapi/v1/margin/account",
                Collections.singletonMap("recvWindow", props.get("recv-window")),
                context);
        HashMap<String, Object> responseJson = OM.readValue(resp, new TypeReference<HashMap<String, Object>>() {
        });
        String assetsJson = OM.writeValueAsString(responseJson.get("userAssets"));
        List< Object> userAssetsJson = OM.readValue(assetsJson, new TypeReference<List<Object>>() {
        });
        context.getLogger().log(userAssetsJson.get(0).toString());

        String response = "0";
        for(Object a : userAssetsJson) {
            if (a.toString().contains(asset)) {
                String[] pair = a.toString().split(",")[1].split("=");
                context.getLogger().log(pair[1]);

                return pair[1] != null ? pair[1] : response;
            }
        }
        return response;
    }

//    private HashMap<String, Object> enableAcc(Context context) throws IOException, InterruptedException {
//        String url = "https://api.binance.com/sapi/v1/margin/isolated/account";
//
//        String time = getSimple("time", Collections.emptyMap());
//        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
//        });
//        context.getLogger().log("time " + time);
////        long time = new Date().getTime();
//
//        StringBuilder sb = new StringBuilder()
//                .append("timestamp=").append(responseJson.get("serverTime"))
//                .append("&")
//                .append("recvWindow=").append(props.get("recv-window"))  // 60_000
//                .append("&")
//                .append("symbol=").append("BTCUSDT");
//
//
//        String signature = sb + "&signature=" + createHmacSignature(props.get("api-secret"), sb.toString());
//
//        HttpClient client = HttpClient.newBuilder().build();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url + "?" + signature))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .headers("X-MBX-APIKEY", props.get("api-key"))
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        context.getLogger().log("response: " + response.body());
//
//        return OM.readValue(response.body(), new TypeReference<HashMap<String, Object>>(){});
//    }

    // move to test
    private void balanceDiff(Context context, AccInfoResponse acc) throws IOException, InterruptedException {
        AccInfoResponse.Balance balanceUsdt = acc.getBalances().stream().filter(b -> "USDT".equals(b.getAsset())).findFirst().orElse(null);
        AccInfoResponse.Balance balanceBtc = acc.getBalances().stream().filter(b -> "BTC".equals(b.getAsset())).findFirst().orElse(null);
        BigDecimal accBalanceUsdt = BigDecimal.ZERO;
        BigDecimal accBalanceBtc = BigDecimal.ZERO;
        if (balanceUsdt != null && balanceBtc != null) {
            accBalanceUsdt = new BigDecimal(balanceUsdt.getFree());
            accBalanceBtc = new BigDecimal(balanceBtc.getFree());
        }

        String priceResponse = getSimple("ticker/price", Collections.singletonMap("symbol", BTCUSDT));
        HashMap<String, String> responseJson = OM.readValue(priceResponse, new TypeReference<HashMap<String, String>>() {
        });
        BigDecimal price = new BigDecimal(responseJson.get("price"));
        BigDecimal u = accBalanceBtc.multiply(price);
        BigDecimal diff = u.subtract(accBalanceUsdt).round(new MathContext(8, RoundingMode.UP));

        context.getLogger().log("Balance diff usdt: " + diff.toPlainString());

        // use quote/total < 0.4
//        double threshold = accBalanceUsdt.longValue() * 0.4;
        BigDecimal total = accBalanceUsdt.add(u);
        double threshold = total.multiply(new BigDecimal("0.45")).doubleValue();
        context.getLogger().log("Balance diff threshold: " + (long) threshold);

        if (diff.abs().doubleValue() > threshold) {
            String quoteOrderQty = diff.abs().multiply(new BigDecimal("0.5")).toPlainString();
            if (diff.signum() > 0) {
//                sendOrder("sell", quoteOrderQty, BTCUSDT, context);
            } else {
//                sendOrder("buy", quoteOrderQty, BTCUSDT, context);
            }
        }
    }

    public AccInfoResponse getInfo(Context context) throws IOException, InterruptedException {
        String time = getSimple("time", Collections.emptyMap());
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        context.getLogger().log("time " + time);

        HashMap<String, String> map = new HashMap<>();
        map.put("recvWindow", props.get("recv-window"));
        // GET /sapi/v1/margin/isolated/account timestamp, recv
        String body = get("account", map, responseJson.get("serverTime"), context);


        AccInfoResponse response;
        try {
            response = OM.readValue(body, AccInfoResponse.class);
//            LambdaLogger logger = context.getLogger();
//            logger.log("Get Acc info response: " + OM.writeValueAsString(response));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private String getUrl(String url, Map<String, String> queryParams, Context context) throws IOException, InterruptedException {
        String time = getSimple("time", Collections.emptyMap());
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        context.getLogger().log("time " + time);
        long milli = time != null ? Long.parseLong(responseJson.get("serverTime")) : new Date().getTime();
//        long milli = Instant.now().toEpochMilli();

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp=").append(milli);
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));

        String signature = createHmacSignature(props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create(url + "?" + sb);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .headers("X-MBX-APIKEY", props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        context.getLogger().log("response: " + response.body());

        return response.body();
    }

    private String get(String path, Map<String, String> queryParams, String time, Context context) throws IOException, InterruptedException {
        long milli = time != null ? Long.parseLong(time) : new Date().getTime();
//        long milli = Instant.now().toEpochMilli();

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp=").append(milli);
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));

        String signature = createHmacSignature(props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        String url = props.get("rest-uri") + path + "?" + sb;

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("X-MBX-APIKEY", props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        context.getLogger().log("response: " + response.body());

        return response.body();
    }

    private String getSimple(String path, Map<String, String> queryParams) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        String params = sb.length() > 0 ? sb.substring(1) : sb.toString();

        String url = props.get("rest-uri") + path + "?" + params;

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public OrderResult sendOrder(String side, String quoteOrderQty, String symbol, Context context) throws IOException, InterruptedException {
//        String url = props.get("rest-uri") + "order";
        String url = "https://api.binance.com/sapi/v1/margin/order";

        String time = getSimple("time", Collections.emptyMap());
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        context.getLogger().log("time " + time);
//        long time = new Date().getTime();

        StringBuilder sb = new StringBuilder()
                .append("timestamp=").append(responseJson.get("serverTime"))
                .append("&")
                .append("recvWindow=").append(props.get("recv-window"))  // 60_000
                .append("&")
                .append("side=").append(side)
                .append("&")
                .append("quoteOrderQty=").append(quoteOrderQty) // or quantity
                .append("&")
                .append("symbol=").append(symbol.toUpperCase(Locale.ROOT))
                .append("&")
                .append("type=").append("MARKET")
                .append("&")
                .append("isIsolated=").append("FALSE")
                .append("&")
                .append("newOrderRespType=").append("RESULT");


        String signature = sb + "&signature=" + createHmacSignature(props.get("api-secret"), sb.toString());

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?" + signature))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("X-MBX-APIKEY", props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        context.getLogger().log("response: " + response.body());

        return OM.readValue(response.body(), OrderResult.class);
    }

    private String createHmacSignature(String secret, String inputText) {
        String hmacSHA256 = "HmacSHA256";
        try {
            Mac mac = Mac.getInstance(hmacSHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), hmacSHA256);
            mac.init(key);

            return new String(Hex.encodeHex(mac.doFinal(inputText.getBytes(StandardCharsets.UTF_8))));

        } catch (Exception e) {
            throw new RuntimeException("cannot create " + hmacSHA256, e);
        }
    }
}
