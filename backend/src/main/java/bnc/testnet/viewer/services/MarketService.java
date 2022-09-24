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
        // uses props to fill query params

        // todo update path and params if MARGIN
        // GET /sapi/v1/accountSnapshot (HMAC SHA256)
/*
        type	    STRING	YES	"SPOT", "MARGIN", "FUTURES"
        startTime	LONG	NO
        endTime	    LONG	NO
        limit	    INT	    NO	min 7, max 30, default 7
        recvWindow	LONG	NO
        timestamp	LONG	YES
*/
//        POST /sapi/v3/asset/getUserAsset
/*
        asset	            STRING	NO	If asset is blank, then query all positive assets user have.
        needBtcValuation	BOOLEAN	NO	Whether need btc valuation or not.
        recvWindow	        LONG	NO
        timestamp	        LONG	YES
*/
        return ApiClientUtil.get(urlPath, queryParams, null, getProps());
    }

    public String getAccTradesList(HashMap<String, String> queryParams) throws IOException, InterruptedException {
        // todo margin
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
        map.put("rest-uri-margin",(T)  marginUrl);
        map.put("recv-window",(T)  recvWindow);
        map.put("name",(T)  name);

        return map;
    }
}