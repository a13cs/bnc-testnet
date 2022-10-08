package bnc.testnet.viewer.rest;

import bnc.testnet.viewer.services.AwsJarService;
import bnc.testnet.viewer.services.CompService;
import bnc.testnet.viewer.services.MarketService;
import bnc.testnet.viewer.services.StrategyService;
import model.OrderResult;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.ICompiler;
import org.codehaus.commons.compiler.util.reflect.ByteArrayClassLoader;
import org.codehaus.commons.compiler.util.resource.MapResourceCreator;
import org.codehaus.commons.compiler.util.resource.MapResourceFinder;
import org.codehaus.commons.compiler.util.resource.Resource;
import org.codehaus.commons.compiler.util.resource.StringResource;
import org.codehaus.janino.CompilerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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


    private static final Logger logger = LoggerFactory.getLogger(UiRest.class);


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


    @RequestMapping(method = RequestMethod.GET, path = "/jar", produces = "application/octet-stream")
    public byte[] getJar() throws IOException {
        Map<String, Object> props = marketService.getProps();

        return lambdaJarService.getUpdatedJar(props);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/jar/test", produces = "application/octet-stream")
    public byte[] getJarTest() throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String className = "bnc.testnet.viewer.services.Test";

        Class<?> c = Class.forName(className);
        ((Runnable) c.getConstructors()[0].newInstance()).run();
//        Class<?> a = cl.loadClass("pkg1.A");

        return lambdaJarService.getUpdatedJarTest();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/test", produces = "application/octet-stream")
    public byte[] test() throws ClassNotFoundException, IOException, CompileException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ICompiler compiler = new CompilerFactory().newCompiler();

        String srcJarName = "demo-be-0.0.1-SNAPSHOT" + "-sources.jar";
        List<Resource> sources = compService.getLocalJarResourcesAsString(srcJarName);

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
        Map<String, byte[]> jarClasses = compService.getClasses(scheme, provider, url);

        Map<String, byte[]> classesMap = new HashMap<String, byte[]>();
        Set<String> keys = jarClasses.keySet();
        for (String k : keys) {
            String newKey = k.substring(0, k.lastIndexOf(".")) + ".java";
            classesMap.put(newKey, jarClasses.get(k));

        }

        int lastUpdatedDiff = 100_000;  // jarClasses to be considered first
        MapResourceFinder resourceFinder = new MapResourceFinder(classesMap);
        resourceFinder.setLastModified(System.currentTimeMillis() - lastUpdatedDiff);

        MapResourceFinder classesMapFinder = new MapResourceFinder(jarClasses);
        classesMapFinder.setLastModified(System.currentTimeMillis());

        MapResourceCreator classFileCreator = new MapResourceCreator(jarClasses);

        compiler.setSourceFinder(resourceFinder);
        compiler.setClassFileFinder(classesMapFinder);
        compiler.setClassFileCreator(classFileCreator);


        sources.add(new StringResource(
                "pkg1/A.java",
                "package pkg1; " +
                        "public class A implements Runnable {\n" +
                        "    @Override\n" +
                        "    public void run() {\n" +
                        // pkg2.B.meth();
                        "        System.out.println(\"test run a\");\n" +
                        "    }\n" +
                        "}"
        ));
        sources.add(new StringResource(
                "pkg2/B.java",
                "package pkg2; " +
                        "import bnc.testnet.viewer.services.Test; " +

                        "public class B { " +
                        "public static void meth() { " +
                        "new Test().run(); " +
                        "} " +
                        "}"
        ));
        compiler.setSourceCharset(Charset.defaultCharset());
        compService.compile(compiler, sources);


        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(jarClasses);
        Class<?> a = byteArrayClassLoader.loadClass("pkg1.A");
        Object inst = a.getConstructors()[0].newInstance();
        ((Runnable) inst).run();
/*
        ClassLoader cl = new ResourceFinderClassLoader(
                new MapResourceFinder(localJarResources),    // resourceFinder
                //  ClassLoader.getSystemClassLoader() // parent
                this.getClass().getClassLoader() // parent
        );

        Class<?> a = cl.loadClass("pkg1.A");
        Method run = a.getDeclaredMethod("run");
        Object invoke = run.invoke(a.getConstructors()[0].newInstance());
*/

        return new byte[0];
    }


    /*
        http://localhost:8080/klines/1663749692000/1663849692000/15m
        http://localhost:8080/klines/1664582400000/1672531200000/15m
        1s,1m,3m,5m,15m,30m,1h,2h,4h,6h,8h,12h,1d,3d,1w,1M
     */

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

    @RequestMapping(method = RequestMethod.GET, path = "/testStrategy")
    public Map<String,Object> testStrategy() throws IOException, InterruptedException {

        String interval = "1m";
        String klines = klines("0", "0", interval);

        String barSeconds = interval
                .replaceAll("[A-Z]","")
                .replaceAll("[a-z]","");
        int seconds = Integer.parseInt(barSeconds) * 60;

        // buy/sell markers and indicator plots
        return strategyService.runTest(klines, String.valueOf(seconds));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/order/{side}/{symbol}/{quoteQty}")
    public OrderResult order(
            @PathVariable(value = "side") String side,
            @PathVariable(value = "symbol") String symbol,
            @PathVariable(value = "quoteQty") String quoteQty) throws IOException, InterruptedException {

        return marketService.sendOrder(side, new BigDecimal(quoteQty), symbol);
    }

}
