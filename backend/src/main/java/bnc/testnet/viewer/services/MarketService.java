package bnc.testnet.viewer.services;

import model.OrderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.ApiClientUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);

    @Value("${name}")
    private String name;
    @Value("${quantity}")
    private String quantity;
    @Value("${recv-window}")
    private String recvWindow;
    @Value("${rest-uri}")
    private String baseUrl;
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


    public MarketService() {    }

    public OrderResult sendOrder(String side, BigDecimal quoteOrderQty, String symbol) throws IOException, InterruptedException {
        logger.info("Sending {} order.", side);

        return ApiClientUtil.sendOrder(
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
        return ApiClientUtil.get(urlPath, queryParams, null, getProps());
    }

    public String getAccTradesList(HashMap<String, String> queryParams) throws IOException, InterruptedException {
        return getInfo("myTrades", queryParams);
    }

    public String getSimple(String path, Map<String, String> queryParams) throws IOException, InterruptedException {
        return ApiClientUtil.getSimple(path, queryParams, getProps());
    }

    public String saveProps(Map<String, Object> props) {
        // todo save to temp
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

        return map;
    }

    public HashMap<String, Object> getAccType() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", getProps().get("type"));
        map.put("isolated", getProps().get("isolated"));

        return map;
    }

}