package util;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.OrderResult;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ApiClientUtil {

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

    public static String getServerTime(Context context, Map<String, String> props) throws IOException, InterruptedException {
        String time = getSimple("time", Collections.emptyMap(), props);
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        if (context != null) {
            context.getLogger().log("time " + time);
        }

        return responseJson.get("serverTime");
    }

    public static String getCurrentPrice(Context context, Map<String, String> props) throws IOException, InterruptedException {
        String priceResponse = getSimple(
                "ticker/price",
                Collections.singletonMap("symbol", BTCUSDT),
                props);

        if (context != null) {
            context.getLogger().log("Current ticker/price " + priceResponse);
        }
        HashMap<String, String> responseJson = OM.readValue(priceResponse, new TypeReference<HashMap<String, String>>() {
        });

        return responseJson.get("price");
    }

    public static String get(
            String path,
            Map<String, String> queryParams,
            Context context,
            Map<String, String> props
    ) throws IOException, InterruptedException {

        String time = getServerTime(context, props);
        long milli = time != null ? Long.parseLong(time) : new Date().getTime();

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp=").append(milli);

        queryParams.put("recvWindow", props.get("recv-window"));
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));

        boolean isolated = Boolean.parseBoolean(props.get("isolated"));
        String param = Boolean.toString(isolated).toUpperCase();

        String url = null;
        if ("MARGIN".equals(props.get("type"))) {
            url = props.get("rest-uri-margin") + path;
            if (path.contains("myTrades")) {
                sb.append("&").append("isIsolated=").append(param);
            }
        }
        if ("SPOT".equals(props.get("type")) || props.get("type") == null) {
            url = props.get("rest-uri") + path;
        }

        String signature = ApiClientUtil.createHmacSignature(props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        url += "?" + sb;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("X-MBX-APIKEY", props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (context != null) {
            context.getLogger().log("response: " + response.body());
        }

        return response.body();
    }

    public static String getSimple(
            String path,
            Map<String, String> queryParams,
            Map<String, String> props
    ) throws IOException, InterruptedException {

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

    public static OrderResult sendOrder(
            String side,
            String quoteOrderQty,
            String symbol,
            Context context,
            Map<String, String> props
    ) throws IOException, InterruptedException {

        String serverTime = getServerTime(context, props);


        StringBuilder sb = new StringBuilder()
                .append("timestamp=").append(serverTime)
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
                .append("newOrderRespType=").append("RESULT");


        boolean isolated = Boolean.parseBoolean(props.get("isolated"));
        String param = Boolean.toString(isolated).toUpperCase();

        String url = null;
        if ("MARGIN".equals(props.get("type"))) {
            url = props.get("rest-uri-margin") + "order";
            sb.append("&").append("isIsolated=").append(param);
        }
        if ("SPOT".equals(props.get("type")) || props.get("type") == null) {
            url = props.get("rest-uri") + "order";
        }

        String signature = ApiClientUtil.createHmacSignature(props.get("api-secret"), sb.toString());
        sb.append("&signature=").append(signature);

        url += "?" + sb;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("X-MBX-APIKEY", props.get("api-key"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (context != null) {
            context.getLogger().log("response: " + response.body());
        }

        return OM.readValue(response.body(), OrderResult.class);
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
