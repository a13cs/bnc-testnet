package bnc.testnet.viewer.rest;

import bnc.testnet.viewer.services.AwsJarService;
import bnc.testnet.viewer.services.MarketService;
import model.OrderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UiRest {

    @Autowired
    MarketService marketService;

    @Autowired
    AwsJarService lambdaJarService;


    private static final Logger logger = LoggerFactory.getLogger(UiRest.class);


    @RequestMapping(method = RequestMethod.GET, path = "/acc")
    public String accInfo() throws IOException, InterruptedException {
        return marketService.getInfo("account", new HashMap<>());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/myTrades")
    public String accTradesList() throws IOException, InterruptedException {
        HashMap<String, String> params = new HashMap<>();
        params.put("symbol", "BTCUSDT");

        String accTradesList = marketService.getAccTradesList(params);
        logger.info("accTradesList {}", accTradesList);

        return accTradesList;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/saveProps")
    public String saveProps(@RequestBody Map<String, Object> props) {
        return marketService.saveProps(props);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/jar", produces = "application/octet-stream")
    public byte[] getJar() throws IOException {
        Map<String, Object> props = marketService.getProps();

        return lambdaJarService.getUpdatedJar(props);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/klines/{start}/{end}/{interval}")
    public String klines(@PathVariable(value = "start") String start,
                         @PathVariable(value = "end") String end,
                         @PathVariable(value = "interval") String interval) throws IOException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();

        long epochSecondMinus = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
        long epochSecond = Instant.now().toEpochMilli();
        if (start.equals("0") || end.equals("0")) {
            map.put("startTime", String.valueOf(epochSecondMinus));
            map.put("endTime", String.valueOf(epochSecond));
        } else {
            map.put("startTime", start);
            map.put("endTime", end);
        }

        map.put("limit","1000"); // max
        map.put("symbol", "BTCUSDT");
        map.put("interval", /*interval*/ interval);
        return marketService.getSimple("uiKlines", map);

//        1s,1m,3m,5m,15m,30m,1h,2h,4h,6h,8h,12h,1d,3d,1w,1M

//        http://localhost:8080/klines/1663749692000/1663849692000/15m
//        http://localhost:8080/klines/1664582400000/1672531200000/15m
    }

    @RequestMapping(method = RequestMethod.GET, path = "/order/{side}/{symbol}/{quoteQty}")
    public OrderResult order(
            @PathVariable(value = "side") String side,
            @PathVariable(value = "symbol") String symbol,
            @PathVariable(value = "quoteQty") String quoteQty) throws IOException, InterruptedException {
//        BigDecimal q = BigDecimal.valueOf(0.002);

        return marketService.sendOrder(side, new BigDecimal(quoteQty) , symbol);
    }

}
