package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.OrderResult;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ApiClientUtil;
import util.OrderService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.ApiClientUtil.getCurrentPrice;

class InvokeTest {
  private static final Logger logger = LoggerFactory.getLogger(InvokeTest.class);

  private static final ObjectMapper OM = new ObjectMapper();
  static {
    OM.configure(SerializationFeature.INDENT_OUTPUT, true);
  }
//  @Test
//  void invokeTest() throws IOException, URISyntaxException {

    // tradingview List of trades CSV

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
//
//  }

  // TODO: test margin, no sapi on testnet

// test send real order
  @Test
  void orderTest() throws IOException, URISyntaxException, InterruptedException {
    logger.info("Order TEST");
    Context context = new TestContext();

    balanceDiff(context);

    String json = loadJsonFile("wh.json");

    Map<String, Object> tvSignal = OM.readValue(json, new TypeReference<HashMap<String, Object>>(){});

    Map<String,Object> event = new HashMap<>();
    event.put("isBase64Encoded", false);
    event.put("body", tvSignal);
    String response = new Handler().handleRequest(tvSignal, context);

    String expected = tvSignal.get("action").toString().split("_")[0];
    String actual = OM.readValue(response, OrderResult.class).getSide();

    assertTrue(expected.equalsIgnoreCase(actual));
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
    String accType = getProps().get("type");
    OrderService orderService = new OrderService();

    String qCcy = "0";
    String ccy = "0";
    if ("SPOT".equals(accType)) {
      qCcy = orderService.getSpotAsset(context, "USDT");
      ccy = orderService.getSpotAsset(context, "BTC");
    } else if ("MARGIN".equals(accType)) {
      qCcy = orderService.getMarginAsset(context, "USDT");
      ccy = orderService.getMarginAsset(context, "BTC");
    }
    BigDecimal accBalanceUsdt = new BigDecimal(qCcy);
    BigDecimal accBalanceBtc = new BigDecimal(ccy);

    String p = getCurrentPrice(context, getProps());

    BigDecimal price = new BigDecimal(p);
    BigDecimal u = accBalanceBtc.multiply(price);
    BigDecimal diff = u.subtract(accBalanceUsdt).round(new MathContext(8, RoundingMode.UP));

    context.getLogger().log("Balance diff USDT: " + diff.toPlainString());

    // use quote/total < 0.4
    BigDecimal total = accBalanceUsdt.add(u);
    double threshold = total.multiply(new BigDecimal("0.45")).doubleValue();
    context.getLogger().log("Balance diff threshold: " + (long) threshold);

    if (diff.abs().doubleValue() > threshold) {
      String quoteOrderQty = diff.abs().multiply(new BigDecimal("0.5")).toPlainString();
      if (diff.signum() > 0) {
        ApiClientUtil.sendOrder("SELL", quoteOrderQty, "BTCUSDT", context, getProps());
      } else {
        ApiClientUtil.sendOrder("BUY", quoteOrderQty, "BTCUSDT", context, getProps());
      }
    }
  }

  private Map<String, String> getProps() {
    Map<String, String> props = new HashMap<>();

    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.properties");
    if (inputStream != null) {
      List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
      lines.forEach(l -> {
        String[] pair = l.split("=");
        // take first
        props.putIfAbsent(pair[0], pair[1]);
      });
    }
    return props;
  }

}
