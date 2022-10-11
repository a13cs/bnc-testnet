package bnc.testnet.viewer.services;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.ICompiler;
import org.codehaus.commons.compiler.util.resource.Resource;
import org.codehaus.commons.compiler.util.resource.StringResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;

@Service
public class CompService {

    private static final Logger logger = LoggerFactory.getLogger(CompService.class);


    public URL createUrl(URL location) throws URISyntaxException, MalformedURLException {
        String srcUrl = location.toURI().toString();
        String suffix = "";
        if (srcUrl.contains("!")) {
            // get running jar
            srcUrl = srcUrl.substring(0, srcUrl.indexOf("!"));
            suffix = "!/";
        }
        URL url = new URL(srcUrl + suffix);
        return url;
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
            logger.debug(classLines);
            StringResource resource = new StringResource(k, (String) classLines);
            srcJCResorces.add(resource);

        }
        return srcJCResorces;
    }

    public Map<String, byte[]> getLocalJarResources(JarInputStream zin) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(zin);

        Map<String, byte[]> sources = new HashMap<>();
        int read = 0;
        for (JarEntry entry = zin.getNextJarEntry(); entry != null; entry = zin.getNextJarEntry()) {
            String name = entry.getName();
            int size = (int) entry.getSize(); // TODO: size -1

            if (name.endsWith(".class") || name.endsWith(".java")) {
                int buffSize = 32;
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
                    int sizeIncrease = 500;
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

    public List<Resource> getLocalJarResourcesAsString(String jarName) throws IOException {
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
                logger.debug(classLines);
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


    public Map<String, byte[]> getClasses(String scheme, FileSystemProvider provider, URL url, String jarPath) throws IOException, URISyntaxException {
        Map<String, byte[]> jarClasses = new HashMap<>();
        if (scheme.equals("jar")) {
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            List<JarEntry> entries = (List<JarEntry>) jarFile.stream().collect(Collectors.toList());

            for (JarEntry e : entries) {
                if (e.getName().endsWith(".class")) {
                    InputStream is = jarFile.getInputStream(e);
                    int size = (int) e.getSize();
                    byte[] bytes = new byte[size];
                    int read = is.read(bytes);
                    logger.debug("Read {} bytes of class {}", read, e.getName());

                    jarClasses.putIfAbsent(e.getName(), bytes);
                }
            }

            FileSystem fileSystem = provider.newFileSystem(url.toURI(), singletonMap("create", "true"));
            Stream<Path> list = Files.list(fileSystem.getPath(jarPath));
            List<Path> paths = (List<Path>) list.collect(Collectors.toList());

            for (Path path : paths) {
                JarURLConnection jarURLConnection = (JarURLConnection) path.toUri().toURL().openConnection();
                JarEntry e = jarURLConnection.getJarEntry();
                InputStream is = jarURLConnection.getJarFile().getInputStream(e);

                jarClasses.putAll(getLocalJarResources(new JarInputStream(is))); // may use size
            }
            fileSystem.close();

        } else {
            // ide run
//            compiler.setClassPath();
        }
        return jarClasses;
    }

    public void compile(ICompiler compiler, List<Resource> resourcesTarget) throws CompileException, IOException {
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
                    logger.debug(message);

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
                    // resourcesTarget get and append inner at lastIndexOf("}")
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
                }
            }
        }
    }

}
