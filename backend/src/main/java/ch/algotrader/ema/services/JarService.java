package ch.algotrader.ema.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class JarService {

    private static final Logger logger = LoggerFactory.getLogger(AccService.class);

    public byte[] getUpdatedJar(Map<String, String> props) throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/java-basic.jar");

        return updateZipFile(inputStream, props, "application.properties");
    }

    public static byte[] updateZipFile(InputStream is, Map<String, String> map, String newEntryName) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(is);
        ZipOutputStream out = new ZipOutputStream(o);

        for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
            if (newEntryName.equals(entry.getName())) {
                continue;
            }
            compressEntry(entry.getName(), buf, out, zin);
        }
        zin.close();

        List<String> updated = new ArrayList<>();
        map.forEach((k, v) -> updated.add(String.format("%s=%s",k,v)));
        String newContent = updated.stream().collect(Collectors.joining(System.lineSeparator()));

        // Compress the file
        InputStream in = new ByteArrayInputStream(newContent.getBytes());
        compressEntry(newEntryName, buf, out, in);
        in.close();

        // Complete the ZIP file
        out.close();

        return o.toByteArray();
    }

    private static void compressEntry(String entryName, byte[] buf, ZipOutputStream out, InputStream in) throws IOException {
        out.putNextEntry(new ZipEntry(entryName));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        // Complete the entry
        out.closeEntry();
    }

}
