package ch.algotrader.ema.rest;

import ch.algotrader.ema.model.AccInfoResponse;
import ch.algotrader.ema.model.OrderResult;
import ch.algotrader.ema.services.AccService;
import ch.algotrader.ema.services.JarService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EmaRest {

    @Autowired
    AccService accService;

    @Autowired
    JarService jarService;


    private static final Logger logger = LoggerFactory.getLogger(EmaRest.class);

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/acc")
    public AccInfoResponse accInfo() throws IOException, InterruptedException {
        String body = accService.getInfo("account", new HashMap<>());
        AccInfoResponse response;
        try {
            return OM.readValue(body, AccInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/myTrades")
    public String accTradesList() throws IOException, InterruptedException {
        String accTradesList = accService.getAccTradesList();
        logger.info("accTradesList {}", accTradesList);

        return accTradesList;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/jar", produces = "application/octet-stream")
    public byte[] getJar() throws IOException {
        Map<String, String> props = new HashMap<>();
        props.put("api-key", "testKey");
        props.put("api-secret", "testSecret");

        return jarService.getUpdatedJar(props);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/klines/{start}/{end}/{interval}")
    public String klines(@PathVariable(value = "start") String start,
                         @PathVariable(value = "end") String end,
                         @PathVariable(value = "interval") String interval) throws IOException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();

        long epochSecondMinus = Instant.now().minus(2, ChronoUnit.DAYS).getEpochSecond();
        long epochSecond = Instant.now().getEpochSecond();
        if (start.equals("0") || end.equals("0")) {
            map.put("startTime", String.valueOf(epochSecondMinus));
            map.put("endTime", String.valueOf(epochSecond));
        } else {
            map.put("startTime", start);
            map.put("endTime", end);
        }

//        map.put("limit","1000");
        map.put("symbol", "BTCUSDT");
        map.put("interval", /*interval*/ interval);  // 1s,1m,3m,5m,15m,30m,1h,2h,4h,6h,8h,12h,1d,3d,1w,1M
        return accService.getSimple("uiKlines", map);

//        http://localhost:8080/klines/1663749692000/1663849692000/15m
//        http://localhost:8080/klines/1664582400000/1672531200000/15m

/*[
    [
        1499040000000,      // Kline open time
        "0.01634790",       // Open price
        "0.80000000",       // High price
        "0.01575800",       // Low price
        "0.01577100",       // Close price
        "148976.11427815",  // Volume
        1499644799999,      // Kline close time
        "2434.19055334",    // Quote asset volume
        308,                // Number of trades
        "1756.87402397",    // Taker buy base asset volume
        "28.46694368",      // Taker buy quote asset volume
        "0"                 // Unused field. Ignore.
    ]
]*/

    }

    @RequestMapping(method = RequestMethod.GET, path = "/order/{side}/{symbol}")
    public OrderResult order(@PathVariable(value = "side") String side, @PathVariable(value = "symbol") String symbol) throws IOException {
        BigDecimal q = BigDecimal.valueOf(0.002);
        return accService.sendOrder(side, q, symbol);
    }

}
