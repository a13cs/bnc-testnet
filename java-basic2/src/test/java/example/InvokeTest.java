package example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

class InvokeTest {
  private static final Logger logger = LoggerFactory.getLogger(InvokeTest.class);

  private static final ObjectMapper OM = new ObjectMapper();
  static {
    OM.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

  private static String loadJsonFile(String filePath) throws URISyntaxException {
    URL resource = InvokeTest.class.getClassLoader().getResource(filePath);
    URI uri = Objects.requireNonNull(resource).toURI();
    Path path = Paths.get(uri);

    StringBuilder stringBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8))
    {
      stream.forEach(stringBuilder::append);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return stringBuilder.toString();
  }

}
