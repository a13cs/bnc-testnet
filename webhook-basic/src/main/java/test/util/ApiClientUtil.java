package test.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.model.OrderResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ApiClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(ApiClientUtil.class);

    private static final String BTCUSDT = "BTCUSDT";

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public ApiClientUtil() {
        throw new RuntimeException("Utils");
    }

    public static String getServerTime(Map<String, String> props) throws IOException, InterruptedException {
        String time = getSimple("time", Collections.emptyMap(), props);
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() { });

//        logger.info("time " + time);


        return (String) responseJson.get("serverTime");
    }

    public static String getCurrentPrice(Map<String, String> props) throws IOException, InterruptedException {
        String priceResponse = getSimple(
                "ticker/price",
                Collections.singletonMap("symbol", BTCUSDT),
                props);

        logger.info("Current ticker/price " + priceResponse);
        HashMap<String, String> responseJson = OM.readValue(priceResponse, new TypeReference<HashMap<String, String>>() { });

        return (String) responseJson.get("price");
    }

    public static String get(
            String path,
            Map<String, String> queryParams,
            Map<String, String> props
    ) throws IOException, InterruptedException {

        String time = getServerTime(props);
        long milli = time != null ? Long.parseLong(time) : new Date().getTime();

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp=").append(milli);

        queryParams.put("recvWindow", (String) props.get("recv-window"));
//        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        for (String k : queryParams.keySet()) {
            sb.append("&").append(k).append("=").append((String) queryParams.get(k));
        }

        boolean isolated = Boolean.parseBoolean((String) props.get("isolated"));
        String param = Boolean.toString(isolated).toUpperCase();

        String url = null;
        if ("MARGIN".equals((String) props.get("type"))) {
            url = (String) props.get("rest-uri-margin") + path;
            if (path.contains("myTrades")) {
                sb.append("&").append("isIsolated=").append(param);
            }
        }
        if ("SPOT".equals((String) props.get("type")) || (String) props.get("type") == null) {
            url = (String) props.get("rest-uri") + path;
        }

        String signature = ApiClientUtil.createHmacSignature((String) props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        url += "?" + sb;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("X-MBX-APIKEY", (String) props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("response: " + response.body());

        return (String) response.body();
    }

    public static String getSimple(
            String path,
            Map<String, String> queryParams,
            Map<String, String> props
    ) throws IOException, InterruptedException {

        StringBuilder sb = new StringBuilder();
//        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        for (String k : queryParams.keySet()) {
            sb.append("&").append(k).append("=").append((String) queryParams.get(k));
        }

        String params = sb.length() > 0 ? sb.substring(1) : sb.toString();

        String url = (String) props.get("rest-uri") + path + "?" + params;

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static OrderResult sendOrder(
            String side,
            String quoteOrderQty,
            String symbol,
            Map<String, String> props
    ) throws IOException, InterruptedException {

        String serverTime = getServerTime(props);
        logger.info("Side {} quoteOrderQty {}", side, quoteOrderQty);

        StringBuilder sb = new StringBuilder()
                .append("timestamp=").append(serverTime)
                .append("&")
                .append("recvWindow=").append((String) props.get("recv-window"))  // 60_000
                .append("&")
                .append("side=").append(side)
                .append("&")
//                .append(buy.equals(side) ? "quoteOrderQty=" : "quantity=").append(buy.equals(side) ? quoteOrderQty : quantity) // or quantity
                .append("quoteOrderQty=").append(quoteOrderQty) // or quantity
                .append("&")
                .append("symbol=").append(symbol.toUpperCase(Locale.ROOT))
                .append("&")
                .append("type=").append("MARKET")
                .append("&")
                .append("newOrderRespType=").append("RESULT");


        boolean isolated = Boolean.parseBoolean((String) props.get("isolated"));
        String param = Boolean.toString(isolated).toUpperCase();

        String url = null;
        if ("MARGIN".equals((String) props.get("type"))) {
            url = (String) props.get("rest-uri-margin") + "order";
            sb.append("&").append("isIsolated=").append(param);
        }
        if ("SPOT".equals((String) props.get("type")) || (String) props.get("type") == null) {
            url = (String) props.get("rest-uri") + "order";
        }

        String signature = ApiClientUtil.createHmacSignature((String) props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        url += "?" + sb;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("X-MBX-APIKEY", (String) props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("response: " + response.body());

        return (OrderResult) OM.readValue(response.body(), OrderResult.class);
    }

    public static String createHmacSignature(String secret, String inputText) {
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
