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

import static org.junit.jupiter.api.Assertions.assertTrue;

class InvokeTest {
  private static final Logger logger = LoggerFactory.getLogger(InvokeTest.class);

  private static final ObjectMapper OM = new ObjectMapper();
  static {
    OM.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

/*
  @Test
  void invokeTest() throws IOException, URISyntaxException {
//  Cumulate percentage growth from tradingview List of trades CSV

    String resourceName = "other/72s_Strategy__Adaptive_Hull_Moving_Average+_pt.1_List_of_Trades_2022-09-14.csv";
    URL resource = InvokeTest.class.getClassLoader().getResource(resourceName);
    URI uri = Objects.requireNonNull(resource).toURI();
    Path path = Paths.get(uri);

    List<String> lines = Files.readAllLines(path);

//    AtomicInteger headerPosition = new AtomicInteger();
    int headerPosition = 0;
    final double firstEntry = 100;
    final double[] initial = {firstEntry};

    String[] names = lines.get(0).split(",");
    for (int i = 0;i < names.length; i++) {
      if ("Profit %".equals(names[i])) {
//        headerPosition.set(i);
        headerPosition = i;
      }
    }
    List<String> values = lines.subList(1, lines.size());
    logger.info("Trades count: {}", values.size() / 2);

    double min = 0;
    double hit = 0;

    int countMinus = 0;
    int countPlus = 0;
    for (int i = 0; i < values.size(); i+=2) {
      String value = values.get(i).split(",")[headerPosition];
      double p = Double.parseDouble(value);

      if (p < 0) {
        if (p < min) min = p;
        hit = initial[0] * p;
        countMinus++;
//        continue;
      } else {
        countPlus++;
      }
      if( Math.abs(p) > 1) {
        logger.info("x {}", p);
      }

      // apply 1% commission
//      p = p - 1;

//      logger.info("{} * {}",initial[0],value);
      initial[0] = initial[0] + initial[0] * p * 0.01;
    }


    logger.info("=================");
    logger.info("start: {}", firstEntry);
    logger.info("count loss: {}", countMinus);
    logger.info("count gain: {}", countPlus);
    logger.info("End " + initial[0]);
    logger.info("max loss " + min);
    logger.info("hit " + hit);

    double profit = initial[0] - firstEntry;
    double v = (profit / firstEntry) * 100;

    BigDecimal rounded = BigDecimal.valueOf(v).round(new MathContext(8, RoundingMode.DOWN));
    logger.info("Result % " + rounded);

  }
*/

// test send real order
  @Test
  void orderTest() throws IOException, URISyntaxException, InterruptedException {
//    logger.info("Order TEST");
//    Context context = new TestContext();
//
//    balanceDiff(context);
//
//    String json = loadJsonFile("wh.json");
//
//    Map<String, Object> tvSignal = OM.readValue(json, new TypeReference<HashMap<String, Object>>(){});
//
////    Map<String,Object> event = new HashMap<>();
////    event.put("isBase64Encoded", false);
////    event.put("body", tvSignal);
//    String response = new Hello().handleRequest(tvSignal, context);
//
//    String expected = tvSignal.get("action").toString().split("_")[0];
//    String actual = OM.readValue(response, OrderResult.class).getSide();
//
//    assertTrue(expected.equalsIgnoreCase(actual));
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

  // split assets
  private void balanceDiff(Context context) throws IOException, InterruptedException {
//    String accType = getProps().get("type");
//    OrderService orderService = new OrderService();
//
//    String qCcy = "0";
//    String ccy = "0";
//    if ("SPOT".equals(accType)) {
//      qCcy = orderService.getSpotAsset(context, "USDT");
//      ccy = orderService.getSpotAsset(context, "BTC");
//    } else if ("MARGIN".equals(accType)) {
//      qCcy = orderService.getMarginAsset(context, "USDT");
//      ccy = orderService.getMarginAsset(context, "BTC");
//    }
//    BigDecimal accBalanceUsdt = new BigDecimal(qCcy);
//    BigDecimal accBalanceBtc = new BigDecimal(ccy);
//
//    String p = getCurrentPrice(context, getProps());
//
//    BigDecimal price = new BigDecimal(p);
//    BigDecimal u = accBalanceBtc.multiply(price);
//    BigDecimal diff = u.subtract(accBalanceUsdt)
//            .round(new MathContext(8, RoundingMode.UP));
//
//    context.getLogger().log("Balance diff USDT: " + diff.toPlainString());
//
//    // use quote/total < 0.4
//    BigDecimal total = accBalanceUsdt.add(u);
//    double threshold = total.multiply(new BigDecimal("0.45")).doubleValue();
//    context.getLogger().log("Balance diff threshold: " + (long) threshold);
//
//    if (diff.abs().doubleValue() > threshold) {
//      String quoteOrderQty = diff.abs().multiply(new BigDecimal("0.5")).toPlainString();
//      if (diff.signum() > 0) {
//        ApiClientUtil.sendOrder("SELL", quoteOrderQty, "BTCUSDT", context, getProps());
//      } else {
//        ApiClientUtil.sendOrder("BUY", quoteOrderQty, "BTCUSDT", context, getProps());
//      }
//    }
  }

//  private Map<String, String> getProps() {
//    Map<String, String> props = new HashMap<>();
//
//    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
//    if (inputStream != null) {
//      List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
//      lines.forEach(l -> {
//        String[] pair = l.split("=");
//        // take first
//        props.putIfAbsent(pair[0], pair[1]);
//      });
//    }
//    return props;
//  }

}
