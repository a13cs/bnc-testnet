package bnc.testnet.viewer.services;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class AwsJarService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);

    public byte[] getUpdatedJar(Map<String, Object> props) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("java-basic-0.0.1-SNAPSHOT.jar");

        return updateZipFile(inputStream, props, "application.properties");
    }

    public static byte[] updateZipFile(InputStream is, Map<String, Object> map, String newEntryName) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(is);
        ZipOutputStream out = new ZipOutputStream(o);

        for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
            if (newEntryName.equals(entry.getName())) {
                continue;
            }
            compressEntry(entry, out, zin);
        }
        zin.close();

        List<String> updated = new ArrayList<>();
//        map.forEach((k, v) -> updated.add(String.format("%s=%s",k,v)));
        for (String k : map.keySet()) {
            updated.add(String.format("%s=%s",k,map.get(k)));
        }

        String newContent = (String) updated.stream().collect(Collectors.joining(System.lineSeparator()));

        // Compress the file
        InputStream in = new ByteArrayInputStream(newContent.getBytes());
        compressEntry(new ZipEntry(newEntryName), out, in);
        in.close();

        // Complete the ZIP file
        out.close();

        return o.toByteArray();
    }

    private static void compressEntry(ZipEntry entry, ZipOutputStream out, InputStream in) throws IOException {
        out.putNextEntry(entry);
        IOUtils.copy(in,out);
        out.closeEntry();
    }

    public byte[] getUpdatedJarTest() throws IOException {
//        URL location = getClass()
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation();
//        InputStream inputStream = new FileInputStream((String)location.getFile());

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("java-service-0.0.1-SNAPSHOT.jar");

        byte[] bytes = null;
        try {
            String localPath = "target/classes";

            Path path = Paths.get(localPath + "/bnc/testnet/viewer/services/Test.class");
            bytes = Files.readAllBytes(path);

            path.toFile().deleteOnExit();
        } catch (Exception ignored) {

        }

        String className = "bnc.testnet.viewer.services.Test".replace('.', '/') + ".class";
        return updateZipFileClass(inputStream, bytes, "BOOT-INF/classes/" + className);
    }

    public static byte[] updateZipFileClass(InputStream is, byte[] bytes, String className) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        ZipInputStream zin = new ZipInputStream(is);
        ZipOutputStream out = new ZipOutputStream(o);

        for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
            if (className.equals(entry.getName())) {
                continue;
            }
            compressEntry(entry, out, zin);
        }
        zin.close();

        // Compress the file
        if (bytes != null) {
            InputStream in = new ByteArrayInputStream(bytes);
            compressEntry(new ZipEntry(className), out, in);
            in.close();
        }
        // Complete the ZIP file
        out.close();

        return o.toByteArray();
    }
}
