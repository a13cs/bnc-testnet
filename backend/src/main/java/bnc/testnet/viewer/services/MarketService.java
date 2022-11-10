package bnc.testnet.viewer.services;

import basic.model.OrderResult;
import basic.util.ApiClientUtil;
import bnc.testnet.viewer.model.ChannelSubscription;
import bnc.testnet.viewer.model.enc.JSONTextDecoder;
import bnc.testnet.viewer.model.enc.JSONTextEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.websocket.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@ClientEndpoint(
        encoders = {JSONTextEncoder.class},
        decoders = {JSONTextDecoder.class}
)
// - gson
public class MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);
    private WebSocketClient client = new ReactorNettyWebSocketClient();

    @Value("${name}")
    private String name;
    @Value("${quantity}")
    private String quantity;
    @Value("${recv-window}")
    private String recvWindow;
    @Value("${rest-uri}")
    private String baseUrl;

    @Value("${ws-uri}")
    private String wsUrl;
    @Value("${rest-uri-margin}")
    private String marginUrl;
    @Value("${type}")
    private String accType;
    @Value("${isolated}")
    private String isolated;
    @Value("${api-key}")
    private String apiKey;
    @Value("${api-secret}")
    private String apiSecret;

    @Value("${symbol}")
    private String symbol;
    private Session session;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
//        MAPPER.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);

        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
//        MAPPER.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
    }

    public MarketService() {    }

    public OrderResult sendOrder(String side, BigDecimal quoteOrderQty, String symbol) throws IOException, InterruptedException {
        logger.info("Sending {} order.", side);

        return (OrderResult) ApiClientUtil.sendOrder(
                side,
                quoteOrderQty.toPlainString(),
                symbol,
                null,
                getProps()  // use current service props
        );
    }

    public String getInfo(String urlPath, Map<String, String> queryParams) throws IOException, InterruptedException {
        String isolated = getProps().get("isolated").toString();
        if (Boolean.parseBoolean(isolated) && "account".equals(urlPath)) {
            urlPath = "isolated/" + urlPath;
        }
        // uses props to fill query params
        return (String) ApiClientUtil.get(urlPath, queryParams, null, getProps());
    }

    public String getAccTradesList(HashMap<String, String> queryParams) throws IOException, InterruptedException {
        return (String) getInfo("myTrades", queryParams);
    }

    public String getSimple(String path, Map<String, String> queryParams) throws IOException, InterruptedException {
        return (String) ApiClientUtil.getSimple(path, queryParams, getProps());
    }

    // + use post with props for jar
    public String saveProps(Map<String, Object> props) {
        // todo save temp
        // check first
        apiKey = props.get("api-key").toString();
        apiSecret = props.get("api-secret").toString();
        baseUrl = props.get("rest-uri").toString();
        marginUrl = props.get("rest-uri-margin").toString();
        quantity = props.get("position-entry").toString();
        accType = props.get("type").toString();
        isolated = props.get("isolated").toString();
        recvWindow = props.get("recv-window").toString();
        name = props.get("name").toString();

        return Boolean.TRUE.toString();
    }

    public <T> Map<String, T> getProps() {
        HashMap<String, T> map = new HashMap<>();

        map.put("api-key", (T) apiKey);
        map.put("api-secret",(T)  apiSecret);
        map.put("rest-uri",(T)  baseUrl);
        map.put("position-entry",(T)  quantity);
        map.put("type",(T)  accType);
        map.put("isolated",(T)  isolated);
        map.put("rest-uri-margin",(T)  marginUrl);
        map.put("recv-window",(T)  recvWindow);
        map.put("name",(T)  name);

        return (Map<String, T>) map;
    }

    public HashMap<String, Object> getAccType() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", getProps().get("type"));
        map.put("isolated", getProps().get("isolated"));

        return (HashMap<String, Object>) map;
    }

    public String unsubscribeTrades() throws IOException {
//        subscribe.dispose();

        if (this.session != null && this.session.isOpen()) {
            session.close();

            return session.getId();
        }
        return null;
    }

    public Flux<String> subscribeKlines(String interval) {

//        WebSocketHandler handler = session -> {
//            Flux<String> tradesFlux = session.receive()
//                    .map(WebSocketMessage::getPayloadAsText)
//                    .map(this::acceptKline)
//                    .doOnNext(logger::info);
//
//            return tradesFlux.then();
//        };
//
//        client = new ReactorNettyWebSocketClient();
//        Disposable subscribe = client.execute(
//                URI.create("wss://stream.binance.com:9443/ws/btcusdt@kline_" + interval),
//                handler
//        ).subscribe();
//
//        return getFlux();

        Flux<String> flux = Flux.empty();
        try {
            if (this.session == null || !this.session.isOpen()) {
                initSession(interval);
            }
            flux = getFlux();

            ChannelSubscription subscription = ChannelSubscription.kLines(symbol, interval);
            final String sub = MAPPER.writeValueAsString(subscription);

            logger.info("sending " + sub);
            this.session.getBasicRemote().sendText(sub);

//                 list active
            String list = MAPPER.writeValueAsString(ChannelSubscription.list());
            this.session.getBasicRemote().sendText(list);
            logger.info("sending " + list);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return flux;
    }

    private String acceptKline(String message) {
        try {
            HashMap<String, Object> filteredMessage = new HashMap<>();
            HashMap<String, Object> mapMessage = (HashMap<String, Object>) MAPPER.readValue( message, new TypeReference<HashMap<String, Object>>(){} );
            if (mapMessage.containsKey("k")) {
                HashMap<String, Object> k = (HashMap<String, Object>) MAPPER.readValue(MAPPER.writeValueAsString(mapMessage.get("k")), new TypeReference<HashMap<String, Object>>() {
                });
                consumer.accept(k);
            }
        } catch (JsonProcessingException jpe) {
            // ignore
        } catch (Exception e) {
            logger.warn("Could not read exchange message {}. Err: {}", message, e);
        }
        return  message;
    }


    public Consumer<Object> consumer;
//    public Consumer<HashMap<String, Object>> consumer;

    public Flux<String> getFlux() {
        Consumer<Object> sinkConsumer = new Consumer<Object>() {
            @Override
            public void accept( final Object sink) {
                consumer = new Consumer<Object>() {
                    @Override
                    public void accept(Object i) {
                        HashMap<String, Object> items = (HashMap<String, Object>) i;
                        final StringBuilder sb = new StringBuilder();
                        sb.append("{");
                        for (Map.Entry<String, Object> entry : items.entrySet()) {
                            String k = (String) entry.getKey();
                            Object v = entry.getValue();
                            if (Arrays.asList("t", "T", "o", "h", "l", "c").contains(k)) {
                                sb.append(String.format("\"%s\" : %s,", k, v));
                            }
                        }
                        sb.deleteCharAt(sb.length() - 1);  // last ','
                        sb.append("}");

                        ((FluxSink<String>)sink).next(sb.toString());
                    }
                };
            }
        };
        return Flux.create(sinkConsumer);
    }
    @OnMessage
    public void onMessage(Session session, String msg) {
        acceptKline(msg);
    }
    private void initSession(String interval) {
        Session ssn;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            String url = "wss://stream.binance.com:9443/ws/";
            ssn = container.connectToServer(this, URI.create(/*wsUrl*/ url + "btcusdt" + "@kline_" + interval));
            logger.info("session open: " + ssn.isOpen());

            this.session = ssn;
        } catch (DeploymentException e) {
            logger.warn("Could not initialize websocket session. ", e);
//            SpringApplication.exit(context);
        } catch (IOException e) {
            logger.warn("Could not initialize websocket session. ", e);
//            SpringApplication.exit(context);
        } catch (UnresolvedAddressException e) {
            logger.warn("Could not initialize websocket session. ", e);
//            SpringApplication.exit(context);
        }
    }

}