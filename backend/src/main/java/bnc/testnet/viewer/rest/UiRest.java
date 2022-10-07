package bnc.testnet.viewer.rest;

//import bnc.testnet.viewer.services.AwsJarService;
//import bnc.testnet.viewer.services.MarketService;
//import model.OrderResult;

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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class UiRest {

//    @Autowired
//    MarketService marketService;
//
//    @Autowired
//    AwsJarService lambdaJarService;


    private static final Logger logger = LoggerFactory.getLogger(UiRest.class);

//
//    @RequestMapping(method = RequestMethod.GET, path = "/acc")
//    public String accInfo() throws IOException, InterruptedException {
//        return marketService.getInfo("account", new HashMap<>());
//    }
//
//    @RequestMapping(method = RequestMethod.GET, path = "/accType")
//    public HashMap<String, Object> accType() {
//        return marketService.getAccType();
//    }
//
//    @RequestMapping(method = RequestMethod.GET, path = "/myTrades")
//    public String accTradesList() throws IOException, InterruptedException {
//        HashMap<String, String> params = new HashMap<>();
//        params.put("symbol", "BTCUSDT");
//
//        String accTradesList = marketService.getAccTradesList(params);
////        logger.info("accTradesList {}", accTradesList);
//
//        return accTradesList;
//    }
//
//    @RequestMapping(method = RequestMethod.POST, path = "/saveProps")
//    public String saveProps(@RequestBody Map<String, Object> props) {
//        return marketService.saveProps(props);
//    }
//
//
//    @RequestMapping(method = RequestMethod.GET, path = "/jar", produces = "application/octet-stream")
//    public byte[] getJar() throws IOException {
//        Map<String, Object> props = marketService.getProps();
//
//        return lambdaJarService.getUpdatedJar(props);
//    }

//    @RequestMapping(method = RequestMethod.GET, path = "/jar/test", produces = "application/octet-stream")
//    public byte[] getJarTest() throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        String className = "bnc.testnet.viewer.services.Test";
//
//        Class<?> c = Class.forName(className);
//        ((Runnable) c.getConstructors()[0].newInstance() ).run();
//
//        return lambdaJarService.getUpdatedJarTest();
//    }

    @RequestMapping(method = RequestMethod.GET, path = "/test", produces = "application/octet-stream")
    public byte[] test() throws ClassNotFoundException, IOException, CompileException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        String srcJC = "commons-compiler-3.1.9-SNAPSHOT" + "-sources.jar";
//        List<Resource> srcJCResorces = getResourcesAsString(srcJC);
//        String srcJ = "janino-3.1.9-SNAPSHOT" + "-sources.jar";
//        List<Resource> srcJList = getResourcesAsString(srcJ);

        ICompiler compiler = new CompilerFactory().newCompiler();
//        ICompiler compiler = new org.codehaus.commons.compiler.jdk.Compiler();  // jdk

        String srcJarName = "demo-be-0.0.1-SNAPSHOT" + "-sources.jar";
        List<Resource> resourcesTarget = getLocalJarResourcesAsString(srcJarName);
//        String baseJarName = "java-basic-0.0.1-SNAPSHOT" + "-sources.jar";
//        List<Resource> resourcesBase = getLocalJarResourcesAsString(baseJarName);

//        resourcesTarget.addAll(srcJCResorces);
//        resourcesTarget.addAll(srcJList);
//        resourcesTarget.addAll(resourcesBase);

/*
        String thisFileName = "target/demo-be-0.0.1-SNAPSHOT.jar";
        String u = "jar:file:./" + thisFileName + "!/BOOT-INF/lib/commons-compiler-3.1.4.jar";
        JarURLConnection urlConnection = (JarURLConnection) (new URL(u)).openConnection();
        JarFile jarFile = urlConnection.getJarFile();
        JarEntry jarEntry = urlConnection.getJarEntry();

        InputStream inputStream = jarFile.getInputStream(jarEntry);
        Map<String, byte[]> localJarResources1 = getLocalJarResources(new JarInputStream(inputStream));
*/
//        java.lang.ClassNotFoundException:
//        Lkotlin/reflect/jvm/internal/impl/resolve/constants/CompileTimeConstant$Parameters;


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

        String srcUrl = location.toURI().toString();
        String suffix = "";
        if (srcUrl.contains("!")) {
            // get running jar
            srcUrl = srcUrl.substring(0, srcUrl.indexOf("!"));
            suffix = "!/";
        }
        URL url = new URL(srcUrl + suffix);

        Map<String, byte[]> localJarResourcesAll = new HashMap<>();
        if (scheme.equals("jar")) {

            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            List<JarEntry> entries = (List<JarEntry>) jarFile.stream().collect(Collectors.toList());

            for (JarEntry e : entries) {
                if (e.getName().endsWith(".class")) {
                    InputStream is = jarFile.getInputStream(e);
                    int size = (int) e.getSize();
                    byte[] bytes = new byte[size];
                    int read = is.read(bytes);
                    localJarResourcesAll.putIfAbsent(e.getName(), bytes);
                }
            }


//        JarInputStream zin = new JarInputStream(url.openConnection().getInputStream());
//        Map<String, byte[]> localJarResourcesAll = getLocalJarResources(zin);

//        URL url = new URL("jar:file:/C:/Users/Al/ws/bnc-testnet/backend/target/demo-be-0.0.1-SNAPSHOT.jar!/");

            Map<String, String> env = Collections.singletonMap("create", "true");
            FileSystem fileSystem = provider.newFileSystem(url.toURI(), env);
            Stream<Path> list = Files.list(fileSystem.getPath("BOOT-INF/lib/"));
            try {
                List<Path> paths = (List<Path>) list.collect(Collectors.toList());
//            paths.add(fileSystem.getPath("org"));

                for (Path path : paths) {
                    JarURLConnection jarURLConnection = (JarURLConnection) path.toUri().toURL().openConnection();
                    JarEntry e = jarURLConnection.getJarEntry();
                    InputStream is = jarURLConnection.getJarFile().getInputStream(e);

                    localJarResourcesAll.putAll(getLocalJarResources(new JarInputStream(is))); // e.getSize()
                }

            } catch (Exception ignored) {
            }

            fileSystem.close();

        } else {
//            compiler.setClassPath();
        }

        Map<String, byte[]> classesMap = new HashMap<String, byte[]>();


//        Map<String, byte[]> localJarResources = new HashMap<>(localJarResourcesAll);
        Set<String> keys = localJarResourcesAll.keySet();
        for (String k : keys) {
            String newKey = k.substring(0, k.lastIndexOf(".")) + ".java";
            classesMap.put(newKey, localJarResourcesAll.get(k));

        }
//        classesMap.get("ch/qos/logback/classic/LoggerContext.java");
//        classesMap.keySet().stream().filter(k -> k.startsWith("ch/qos/logback/")).collect(Collectors.toList())

        MapResourceFinder resourceFinder = new MapResourceFinder(classesMap);
        resourceFinder.setLastModified(System.currentTimeMillis() - 100_000);
        compiler.setSourceFinder(resourceFinder);


        MapResourceFinder mapResourceFinder = new MapResourceFinder(localJarResourcesAll);
        mapResourceFinder.setLastModified(System.currentTimeMillis());
        compiler.setClassFileFinder(mapResourceFinder);

        compiler.setClassFileCreator(new MapResourceCreator(localJarResourcesAll));

//        List<Resource> resourcesTarget = new ArrayList<>();
        resourcesTarget.add(new StringResource(
                "pkg1/A.java",
                "package pkg1; " +
                        "public class A implements Runnable {\n" +
                        "    @Override\n" +
                        "    public void run() {\n" +
                        // pkg2.B.meth();
                        "        System.out.println(\"test run 2\");\n" +
                        "    }\n" +
                        "}"
        ));
        resourcesTarget.add(new StringResource(
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

        boolean cont = true;
        while (cont) {

            try {
                Resource[] classes = (Resource[]) resourcesTarget.toArray(new Resource[0]);
                compiler.compile(classes);
                cont = false;
            } catch (Exception e) {
                String message;

                if (e.getCause() == null) {
                    throw e;
                } else {
                    message = e.getCause().getLocalizedMessage();

//                if (message.contains("org/reactivestreams/Subscription")) {
//                    System.out.println();
//                }
                    System.out.println(message);
                    message = message.replaceAll(";", "");

                    if (message.startsWith("[")) {
                        message = message.substring(1);
                    }
                    if (message.startsWith("L")) {
                        message = message.substring(1);
                    }
                    int lastIndexOf = message.lastIndexOf("/");

                    String pkg = message.substring(0, lastIndexOf);
                    String pkgName = pkg.replace('/', '.');
                    String name = message.substring(lastIndexOf + 1);
                    String inner = "";
                    String simpleName = name;
                    if (name.contains("$")) {
                        simpleName = name.substring(0, name.indexOf("$"));
                        String innerName = name.substring(name.indexOf("$") + 1);  // single inner
                        inner = String.format(" static class %s { }", innerName);
                    }
//                resourcesTarget get and append inner lastIndexOf("}")
                    String classPackagePath = String.format("%s/%s.java", pkg, simpleName);

                    Resource existing = null;
                    for (Resource r : resourcesTarget) {
                        if (r.getFileName().equals(classPackagePath)) {
                            existing = r;
                        }
                    }

                    if (existing != null) {

                        InputStream open = existing.open();
                        List<String> lines = (List<String>) new BufferedReader(new InputStreamReader(open)).lines().collect(Collectors.toList());

                        String line = (String) lines.get(0);
                        String plusInner = line.substring(0, line.lastIndexOf("}")) + inner + " }";

                        resourcesTarget.remove(existing);
                        resourcesTarget.add(new StringResource(
                                classPackagePath,
                                plusInner
                        ));
                    } else {
                        resourcesTarget.add(new StringResource(
                                classPackagePath,
                                String.format("package %s; public class %s { %s }", pkgName, simpleName, inner)
                        ));
                    }
//                Resource[] classes = (Resource[]) resourcesTarget.toArray(new Resource[0]);
//                compiler.compile(classes);
                }
            }
        }


        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(localJarResourcesAll);
        Class<?> a = byteArrayClassLoader.loadClass("pkg1.A");
//        Runnable.class.cast(a).run();
        Method run = a.getDeclaredMethod("run");
        Object invoke = run.invoke(a.getConstructors()[0].newInstance());

/*        ClassLoader cl = new ResourceFinderClassLoader(
                new MapResourceFinder(localJarResources),    // resourceFinder
//                ClassLoader.getSystemClassLoader() // parent
                this.getClass().getClassLoader() // parent
        );
        Class<?> a = cl.loadClass("pkg1.A");
//        Runnable.class.cast(a).run();
        Method run = a.getDeclaredMethod("run");
        Object invoke = run.invoke(a.getConstructors()[0].newInstance());*/

        return new byte[0];
    }

    private List<Resource> getResourcesAsString(String srcJC) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(srcJC);

        List<Resource> srcJCResorces = new ArrayList<>();
        Map<String, byte[]> srcJCList = getLocalJarResources(new JarInputStream(inputStream));

        for (String k : srcJCList.keySet()) {
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new ByteArrayInputStream((byte[]) srcJCList.get(k)))
                    );
            String classLines = (String) reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(classLines);
            StringResource resource = new StringResource(k, (String) classLines);
            srcJCResorces.add(resource);

        }
        return srcJCResorces;
    }

    private Map<String, byte[]> getLocalJarResources(JarInputStream zin) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(zin);
//        bis.mark(Integer.MAX_VALUE);
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jarName);
//        JarInputStream zin = new JarInputStream(is);

        Map<String, byte[]> sources = new HashMap<>();
        int read = 0;
        for (JarEntry entry = zin.getNextJarEntry(); entry != null; entry = zin.getNextJarEntry()) {
            String name = entry.getName();
            int size = (int) entry.getSize(); // TODO: size -1

            if (name.endsWith(".class") || name.endsWith(".java")) {
                int buffSize = 32;  // todo
                int s = size <= 0 ? 1024 * buffSize : size;
                byte[] buff = new byte[s];
                byte[] b = new byte[s];
                int off = 0;
                // check entry EOF
/*                while ((read = zin.read(buff, off, size - off)) > 0) {
                    off += read;
                    if (read == size) break;
                }*/
                try {
                    bis.mark(Integer.MAX_VALUE);
                    while ((read = bis.read(buff, 0, s)) > 0) {
                        System.arraycopy(buff, 0, b, off, read);
                        off += read;
//                        if (read == s) break;
                    }
                } catch (IndexOutOfBoundsException iout) {
                    bis.reset();
                    read = 0;
                    off = 0;
                    int sizeIncrease = 500; // todo inc
                    s = 1024 * buffSize * sizeIncrease;
                    buff = new byte[s];
                    b = new byte[s];

                    while ((read = bis.read(buff, 0, s)) > 0) {
                        System.arraycopy(buff, 0, b, off, read);
                        off += read;
                        if (read == s) break;
                    }
                }
//                ByteArrayInputStream bis = new ByteArrayInputStream(b);
//                byte[] bytes = bis.readAllBytes();
//                int m = new DataInputStream(new ByteArrayInputStream(b)).readInt();
//                if (m != -889275714) {
//                    throw new ClassFile.ClassFileException("Invalid magic number");
//                } else {
                buff = new byte[off];
                System.arraycopy(b, 0, buff, 0, off);
                sources.putIfAbsent(name, buff);
//                }
            }
        }
        zin.close();

        return sources;
    }

    private List<Resource> getLocalJarResourcesAsString(String jarName) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jarName);
        JarInputStream zin = new JarInputStream(inputStream);

        Map<String, String> sources = new HashMap<>();
        int read = 0;
        for (JarEntry entry = zin.getNextJarEntry(); entry != null; entry = zin.getNextJarEntry()) {
            String name = entry.getName();
            int size = (int) entry.getSize();

            if (name.endsWith(".java")) {
                byte[] buff = new byte[size];
                int off = 0;
                while ((read = zin.read(buff, off, size - off)) > 0) {
                    off += read;
                    if (read == size) break;
                }
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new ByteArrayInputStream(buff))
                        );
                String classLines = (String) reader.lines().collect(Collectors.joining(System.lineSeparator()));
                System.out.println(classLines);
                sources.putIfAbsent(name, classLines);
            }
        }
        zin.close();

        List<Resource> resources = new ArrayList<>();
        Set<String> keySet = sources.keySet();
        for (String k : keySet) {
            StringResource resource = new StringResource(k, (String) sources.get(k));
            resources.add(resource);
        }

        return resources;
    }

    /*
            http://localhost:8080/klines/1663749692000/1663849692000/15m
            http://localhost:8080/klines/1664582400000/1672531200000/15m
            1s,1m,3m,5m,15m,30m,1h,2h,4h,6h,8h,12h,1d,3d,1w,1M
     */
//
//    @RequestMapping(method = RequestMethod.GET, path = "/klines/{start}/{end}/{interval}")
//    public String klines(@PathVariable(value = "start") String start,
//                         @PathVariable(value = "end") String end,
//                         @PathVariable(value = "interval") String interval) throws IOException, InterruptedException {
//        HashMap<String, String> map = new HashMap<>();
//
//        long epochSecondMinus = Instant.now().minus(2, ChronoUnit.DAYS).toEpochMilli();
//        long epochSecond = Instant.now().toEpochMilli();
//        if (start.equals("0") || end.equals("0")) {
//            map.put("startTime", String.valueOf(epochSecondMinus));
//            map.put("endTime", String.valueOf(epochSecond));
//        } else {
//            map.put("startTime", start);
//            map.put("endTime", end);
//        }
//
//        map.put("limit","1000"); // max
//        map.put("symbol", "BTCUSDT");
//        map.put("interval", /*interval*/ interval);
//
//        return marketService.getSimple("uiKlines", map);
//    }
//
//    @RequestMapping(method = RequestMethod.GET, path = "/order/{side}/{symbol}/{quoteQty}")
//    public OrderResult order(
//            @PathVariable(value = "side") String side,
//            @PathVariable(value = "symbol") String symbol,
//            @PathVariable(value = "quoteQty") String quoteQty) throws IOException, InterruptedException {
//
//        return marketService.sendOrder(side, new BigDecimal(quoteQty) , symbol);
//    }

}
