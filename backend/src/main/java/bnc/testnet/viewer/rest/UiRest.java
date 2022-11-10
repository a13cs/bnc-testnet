package bnc.testnet.viewer.rest;

import basic.model.OrderResult;
import bnc.testnet.viewer.parse.DslService;
import bnc.testnet.viewer.services.AwsJarService;
import bnc.testnet.viewer.services.CompService;
import bnc.testnet.viewer.services.MarketService;
import bnc.testnet.viewer.services.StrategyService;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.ICompiler;
import org.codehaus.commons.compiler.util.ResourceFinderClassLoader;
import org.codehaus.commons.compiler.util.resource.MapResourceFinder;
import org.codehaus.commons.compiler.util.resource.Resource;
import org.codehaus.commons.compiler.util.resource.StringResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class UiRest {

    @Autowired
    MarketService marketService;

    @Autowired
    AwsJarService lambdaJarService;

    @Autowired
    CompService compService;

    @Autowired
    StrategyService strategyService;

    @Autowired
    DslService dslService;


    private static final Logger logger = LoggerFactory.getLogger(UiRest.class);

    @RequestMapping(method = RequestMethod.GET, path = "/sub/kLines/{interval}")
    public Flux<String> subscribeKlines(@PathVariable(value = "interval") String interval) {
        return marketService.subscribeKlines(interval);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/close/kLines")
    public String unsubscribeKlines() throws IOException {
        return marketService.unsubscribeTrades();
    }
    @RequestMapping(method = RequestMethod.GET, path = "/acc")
    public String accInfo() throws IOException, InterruptedException {
        return marketService.getInfo("account", new HashMap<>());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/accType")
    public HashMap<String, Object> accType() {
        return marketService.getAccType();
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


    // POST
    @RequestMapping(method = RequestMethod.GET, path = "/jar/{name}", produces = "application/octet-stream")
    public byte[] getJar(@PathVariable(value = "name") String name) throws IOException {
        Map<String, Object> props = marketService.getProps();

        byte[] updatedJar = lambdaJarService.getUpdatedJar(props);

        //  + add separate button and endpoint
        lambdaJarService.copyJar(name, updatedJar);

        return updatedJar;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/jar/test", produces = "application/octet-stream")
    public byte[] getJarTest() throws IOException{
        // adds "bnc.testnet.viewer.services.Test" to java-service;

        return lambdaJarService.getUpdatedJarTest();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/test/{start}/{end}/{interval}")
    public Map<String, Object> test(
            @PathVariable(value = "start") String start,
            @PathVariable(value = "end") String end,
            @PathVariable(value = "interval") String interval,
            @RequestBody String test
    ) throws ClassNotFoundException, IOException, CompileException, URISyntaxException, InvocationTargetException, InstantiationException, IllegalAccessException, InterruptedException {

        String srcJarName = "demo-be-0.0.1-SNAPSHOT" + "-sources.jar";
        List<Resource> sources = compService.getLocalJarResourcesAsString(srcJarName);

        String cls = dslService.parse(test);
//        logger.info("test: {}", cls);
        // POST payload -> classText

        String classText = "package bnc.testnet.viewer.services.strategy;\n" +
                "\n" +
                "import org.ta4j.core.*;\n" +
                "import org.ta4j.core.indicators.EMAIndicator;\n" +
                "import org.ta4j.core.indicators.helpers.ClosePriceIndicator;\n" +
                "import org.ta4j.core.num.DecimalNum;\n" +
                "import org.ta4j.core.num.Num;\n" +
                "import org.ta4j.core.rules.CrossedDownIndicatorRule;\n" +
                "import org.ta4j.core.rules.CrossedUpIndicatorRule;\n" +
                "\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class TaStrategyImpl implements TaStrategy {\n" +
                "\n" +
                "    private final BarSeries series;\n" +
                "    private final Strategy strategy;\n" +
                "\n" +
                "    private final Map<String, Num> inputs = new HashMap<>();\n" +
                "    private final Map<String, Indicator<Num>> output = new HashMap<>();\n" +
                "\n" +
                "\n" +
                "    {\n" +
                "        inputs.putIfAbsent(\"emaPeriodShort\", DecimalNum.valueOf(30));\n" +
                "        inputs.putIfAbsent(\"emaPeriodLong\", DecimalNum.valueOf(80));\n" +
                "    }\n" +
                "\n" +
                "    public TaStrategyImpl(BarSeries barSeries) {\n" +
                "        this.series = barSeries;\n" +
                "\n" +
                "        ClosePriceIndicator close = new ClosePriceIndicator(series);\n" +
                "\n" +
                "        int emaPeriodShort = ((Num) inputs.get(\"emaPeriodShort\")).intValue();\n" +
                "        int emaPeriodLong = ((Num) inputs.get(\"emaPeriodLong\")).intValue();\n" +
                "\n" +
                "        EMAIndicator fastEma = new EMAIndicator(close, emaPeriodShort);\n" +
                "        EMAIndicator slowEma = new EMAIndicator(close, emaPeriodLong);\n" +
                "\n" +
                "        // + keep list to add indicator at the end\n" +
                "        // plot(fastEma)\n" +
                "        // plot(slowEma)\n" +
                "        output.putIfAbsent(\"fastEma\", fastEma);\n" +
                "        output.putIfAbsent(\"slowEma\", slowEma);\n" +
                "\n" +
                "        Rule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);\n" +
                "        Rule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);\n" +
                "\n" +
                "        strategy = new BaseStrategy(entryRule, exitRule);\n" +
                "    }\n" +
                "\n" +
                "    public Map<String, Num> getInputs() {\n" +
                "        return inputs;\n" +
                "    }\n" +
                "\n" +
                "    public Map<String, Indicator<Num>> getOutput() {\n" +
                "        return output;\n" +
                "    }\n" +
                "\n" +
                "    public Strategy getStrategy() {\n" +
                "        return strategy;\n" +
                "    }\n" +
                "\n" +
                "    public BarSeries getSeries() {\n" +
                "        return series;\n" +
                "    }\n" +
                "\n" +
                "    // + implement Strategy\n" +
                "    public boolean shouldEnter(int i) {\n" +
                "        return this.strategy.shouldEnter(i);\n" +
                "    }\n" +
                "\n" +
                "/*\n" +
                "\n" +
                "    // Strategy START\n" +
                "\n" +
                "\n" +
                "        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);\n" +
                "\n" +
                "        EMAIndicator fastEma = new EMAIndicator(closePriceIndicator, emaPeriodShort);\n" +
                "        EMAIndicator slowEma = new EMAIndicator(closePriceIndicator, emaPeriodLong);\n" +
                "\n" +
                "\n" +
                "        CrossedUpIndicatorRule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);\n" +
                "        CrossedDownIndicatorRule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);\n" +
                "\n" +
                "        Strategy strategy = new BaseStrategy(entryRule, exitRule);\n" +
                "\n" +
                "    // Strategy END\n" +
                "\n" +
                "*/\n" +
                "}\n";

        sources.add(new StringResource(
                "bnc/testnet/viewer/services/strategy/TaStrategyImpl.java",
                classText
        ));

        URL location = getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        String scheme = location.toURI().getScheme();

        FileSystemProvider provider = (FileSystemProvider) FileSystemProvider.installedProviders().get(0);
        for (FileSystemProvider p : FileSystemProvider.installedProviders()) {
            if (p.getScheme().equals(scheme)) {
                provider = p;
            }
        }

        URL url = compService.createUrl(location);
        String jarLibPath = "BOOT-INF/lib/";

        // + copy BOOT-INF/lib/
        // compiler.setClassPath(dir);

        Map<String, byte[]> jarClasses = compService.getClasses(scheme, provider, url, jarLibPath);
        ICompiler compiler = compService.getCompiler(sources, jarClasses);
        compService.compile(compiler, sources);

//        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(jarClasses);
        ClassLoader cl = new ResourceFinderClassLoader(
                new MapResourceFinder(jarClasses),    // resourceFinder
                //  ClassLoader.getSystemClassLoader() // parent
                this.getClass().getClassLoader() // parent
        );
        Class<?> strategy = cl.loadClass("bnc.testnet.viewer.services.strategy.TaStrategyImpl");

        String kLines = klines(start, end, interval);
        String barSeconds = interval
                .replaceAll("\\p{Alpha}", ""); // .replaceAll("[a-z]","");
        int seconds = Integer.parseInt(barSeconds) * 60;

        // buy/sell markers and indicator plots
        return strategyService.runTest(kLines, String.valueOf(seconds), strategy);
    }


    /*
        http://localhost:8080/klines/1663749692000/1663849692000/15m
        http://localhost:8080/klines/1664582400000/1672531200000/15m
        1s,1m,3m,5m,15m,30m,1h,2h,4h,6h,8h,12h,1d,3d,1w,1M
     */

    // + batch
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

        map.put("limit", "1000"); // max
        map.put("symbol", "BTCUSDT");
        map.put("interval", /*interval*/ interval);

        return marketService.getSimple("uiKlines", map);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/order/{side}/{symbol}/{quoteQty}")
    public OrderResult order(
            @PathVariable(value = "side") String side,
            @PathVariable(value = "symbol") String symbol,
            @PathVariable(value = "quoteQty") String quoteQty) throws IOException, InterruptedException {

        return marketService.sendOrder(side, new BigDecimal(quoteQty), symbol);
    }

}
