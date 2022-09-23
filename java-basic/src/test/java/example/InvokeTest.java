package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvokeTest {
  private static final Logger logger = LoggerFactory.getLogger(InvokeTest.class);

  private static final ObjectMapper OM = new ObjectMapper();
  static {
    OM.configure(SerializationFeature.INDENT_OUTPUT, true);
  }
  @Test
  void invokeTest() throws IOException, URISyntaxException {
    Context context = new TestContext();

    String json = loadJsonFile("event.json");

    WeatherData data = OM.readValue(json, WeatherData.class);

    String weatherData = new HandlerWeatherData().handleRequest(data, context);

    assertEquals(data, OM.readValue(weatherData, WeatherData.class));

//
//    List<Double> pr = new ArrayList<>();
//
//    URL resource = InvokeTest.class.getClassLoader().getResource("trades.csv");
//    URI uri = Objects.requireNonNull(resource).toURI();
//    Path path = Paths.get(uri);
//
//    BufferedReader reader = new BufferedReader(new FileReader("./s.csv"));
//    AtomicInteger headerPosition = new AtomicInteger();
//
//    final double firstEntry = 100;
//    final double[] initial = {firstEntry};
//
//    List<String> lines = reader.lines().collect(Collectors.toList());
//    String[] names = lines.get(0).split(",");
//    for (int i = 0;i < names.length; i++) {
//      if ("Profit %".equals(names[i])) {
//        headerPosition.set(i);
//      }
//    }
//    List<String> values = lines.subList(1, lines.size());
//    logger.info("Trades count: {}", values.size() / 2);
//
//    double min = 0;
//    double hit = 0;
//
//    int countMinus = 0;
//    int countPlus = 0;
//    for (int i = 0; i < values.size(); i+=2) {
//      String value = values.get(i).split(",")[headerPosition.get()];
//      double p = Double.parseDouble(value);
//
//      if (p < 0) {
//        if (p < min) min = p;
//        hit = initial[0] * p;
//        countMinus++;
////        continue;
//      } else {
//        countPlus++;
//      }
//      if( Math.abs(p) > 1) {
//        logger.info("x {}", p);
//      }
//
//      // apply 1% commission
////      p = p - 1;
//
////      p = p - 0.010110;
//
//      pr.add(p);
////      logger.info("=================");
////      logger.info("{} * {}",initial[0],value);
//      initial[0] = initial[0] + initial[0] * p * 0.01;
////      logger.info(String.valueOf(initial[0]));
//    }
//
//
//    logger.info("=================");
//    logger.info("countMinus: {}", countMinus);
//    logger.info("countPlus: {}", countPlus);
//    logger.info("End " + initial[0]);
//    logger.info("min " + min);
//    logger.info("hit " + hit);
//
//    double profit = initial[0] - firstEntry;
//    logger.info(" % " + (profit/firstEntry) * 100);


  }


//  @Test
//  void orderTest() throws IOException, URISyntaxException {
//    logger.info("Order TEST");
//    Context context = new TestContext();
//
//    String json = loadJsonFile("wh-simple.json");
//
//
//    Map<String, Object> tvSignal = OM.readValue(json, new TypeReference<HashMap<String, Object>>(){});
//
//    Map<String,Object> event = new HashMap<>();
//    event.put("isBase64Encoded", false);
//    event.put("body", tvSignal);
//    String response = new MainHandler().handleRequest(tvSignal, context);
//
//    assertEquals(
//            tvSignal.get("action").toString().toUpperCase(),
//            OM.readValue(response, OrderResult.class).getSide()
//    );
//  }

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
