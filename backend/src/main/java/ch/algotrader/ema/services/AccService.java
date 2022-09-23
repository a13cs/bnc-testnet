package ch.algotrader.ema.services;

import ch.algotrader.ema.model.AccTradesResponse;
import ch.algotrader.ema.model.OrderResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AccService {

    private static final Logger logger = LoggerFactory.getLogger(AccService.class);
    private static final String MARKET = "MARKET";
    private static final String RESULT = "RESULT";

    @Value("${quantity}")
    private String quantity;
    @Value("${recv-window}")
    private String recvWindow;
    @Value("${rest-uri}")
    private String baseUrl;
    @Value("${api-key}")
    private String apiKey;
    @Value("${api-secret}")
    private String apiSecret;

    private final RestTemplate restTemplate;
    private final ObjectMapper OM;

    public AccService() {
        this.restTemplate = new RestTemplate();

        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);
    }


    public String getAccTradesList() throws IOException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();
        map.put("symbol", "BTCUSDT");
        String body = getInfo("myTrades", map);
        logger.debug("Get my trades list: {}", body);

        TypeReference<ArrayList<AccTradesResponse>> valueTypeRef = new TypeReference<>() { };
        ArrayList<AccTradesResponse> accTradesResponses = OM.readValue(body, valueTypeRef);

//        List<AccTradesResponse> trades = accTradesResponses.stream()
////                .peek(t -> t.setDisplayTime(
////                        LocalDateTime.ofInstant(Instant.ofEpochMilli(t.getTime()), ZoneId.systemDefault()))
////                )
//                .sorted(Comparator.comparingLong(AccTradesResponse::getTime).reversed())
////                .limit(ACC_TRADES_LIMIT)
//                .collect(Collectors.toList());
//
//        logger.debug("Get my trades List<AccTradesResponse> : {}", accTradesResponses);
//        return accTradesResponses;

        return body;
    }

    public OrderResult sendOrder(String side, BigDecimal quoteOrderQty, String symbol) throws IOException {
        // if initFromCsv return
        long time = new Date().getTime();

        final UriComponents pathUri = UriComponentsBuilder.fromUriString("order").build();
        final URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl).uriComponents(pathUri)
                .build().toUri();
        RequestEntity.BodyBuilder bodyBuilder = RequestEntity.method(HttpMethod.POST, uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        bodyBuilder = bodyBuilder.header("X-MBX-APIKEY", this.apiKey);

        StringBuilder sb = new StringBuilder()
                .append("timestamp=").append(time)
                .append("&")
                .append("side=").append(side)
                .append("&")
//                .append("quantity=").append(quantity)  // retest
                .append("quoteOrderQty=").append(quoteOrderQty)
                .append("&")
                .append("symbol=").append(symbol.toUpperCase(Locale.ROOT))
                .append("&")
                .append("type=").append(MARKET)
                .append("&")
                .append("newOrderRespType=").append(RESULT);

        String signature = sb + "&signature=" + createHmacSignature(this.apiSecret, sb.toString());

        ResponseEntity<String> exchange = restTemplate.exchange(bodyBuilder.body(signature), String.class);

        String bodyResp = exchange.getBody();

        OrderResult orderResult = OM.readValue(bodyResp, OrderResult.class);
        logger.info("Order response: {}", bodyResp);

        return orderResult;
    }
    
//    ==========================================================================


    public String getInfo(String urlPath, Map<String, String> map) throws IOException, InterruptedException {
        String time = getSimple("time", Collections.emptyMap());
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        logger.info("time {}", time);

//        HashMap<String, String> map = new HashMap<>();
        map.put("recvWindow", recvWindow);
        // GET /sapi/v1/margin/isolated/account timestamp, recv
        return get(urlPath, map, responseJson.get("serverTime"));
    }

    private String get(String path, Map<String, String> queryParams, String time) throws IOException, InterruptedException {
        long milli = time != null ? Long.parseLong(time) : new Date().getTime();
//        long milli = Instant.now().toEpochMilli();

        StringBuilder sb = new StringBuilder();
        sb.append("timestamp=").append(milli);
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));

        String signature = createHmacSignature(apiSecret, sb.toString());
        sb.append("&signature=").append(signature);

        String url = baseUrl + path + "?" + sb;

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .headers("X-MBX-APIKEY", apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("response: {}", response.body());

        return response.body();
    }

    public String getSimple(String path, Map<String, String> queryParams) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        queryParams.forEach((k, v) -> sb.append("&").append(k).append("=").append(v));
        String params = sb.length() > 0 ? sb.substring(1) : sb.toString();

        String url = baseUrl + path + "?" + params;

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public OrderResult sendOrder(String side, String quoteOrderQty, String symbol, Context context) throws IOException, InterruptedException {
//        String url = baseUrl + "order";
        String url = "https://api.binance.com/sapi/v1/margin/order";

        String time = getSimple("time", Collections.emptyMap());
        HashMap<String, String> responseJson = OM.readValue(time, new TypeReference<HashMap<String, String>>() {
        });
        context.getLogger().log("time " + time);
//        long time = new Date().getTime();

        StringBuilder sb = new StringBuilder()
                .append("timestamp=").append(responseJson.get("serverTime"))
                .append("&")
                .append("recvWindow=").append(recvWindow)  // 60_000
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


        String signature = sb + "&signature=" + createHmacSignature(apiSecret, sb.toString());

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?" + signature))
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers("X-MBX-APIKEY", apiKey)
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

    public String saveProps(Map<String, Object> props) {
        // todo save temp
        apiKey = props.get("api-key").toString();
        apiSecret = props.get("api-secret").toString();
        baseUrl = props.get("rest-uri").toString();
        quantity = props.get("position-entry").toString();

        return Boolean.TRUE.toString();
    }

    public Map<String, Object> getProps() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("api-key", apiKey);
        map.put("api-secret", apiSecret);
        map.put("rest-uri", baseUrl);
        map.put("position-entry", quantity);

        return map;
    }
}