package bnc.testnet.viewer.services;

import bnc.testnet.viewer.model.BarModel;
import bnc.testnet.viewer.services.strategy.TaStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.Num;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;

@Service
public class StrategyService {

    private static final Logger logger = LoggerFactory.getLogger(StrategyService.class);


    private static final ObjectMapper OM;

    private static final String[] color= {"green", "blue", "red","yellow","orange"};
    private static int currentColor = 0;

    static {
        OM = new ObjectMapper();

        OM.findAndRegisterModules();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);

    }

    public Map<String, Object> runTest(String klines, String interval, Class<?> c) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, Object> results = new HashMap<>();

        List<String[]> signals = new ArrayList<>();
        List<Map<String, Object>> indicators = new ArrayList<>();

        results.put("signals", signals);
        results.put("indicators", indicators);


        // may keep adding trades/bars and evaluateLogic
        BarSeries series = getSeries(klines, interval);

        Object inst = c.getConstructors()[0].newInstance(series);
        TaStrategy taStrategy = (TaStrategy) inst;

        Map<String, Num> inputs = taStrategy.getInputs();
        Map<String, Indicator<Num>> output = taStrategy.getOutput();

        for (String indicatorName : output.keySet()) {
            Map<String, Object> displayIndicator = new HashMap<>();
            displayIndicator.put("name", indicatorName);
            displayIndicator.put("values", new ArrayList<>());
            displayIndicator.put("color", color[++currentColor%color.length]);

            indicators.add(displayIndicator);
        }

        final int beginIndex = series.getBeginIndex();
        final int endIndex = series.getEndIndex();

        final int emaPeriodLong = ((Num) inputs.get("emaPeriodLong")).intValue();
        Predicate<Object> when = new Predicate<Object>() {
            @Override
            public boolean test(Object o) {
                int i = (Integer) o;
                /* order signal saving uses next open time for fixed series */
                return i > emaPeriodLong && i < endIndex;
            }
        };

        for (int i = beginIndex; i <= endIndex; i++) {
            boolean shouldEnter = taStrategy.shouldEnter(i);
            logger.debug("shouldEnter {}", shouldEnter);

            logBar(series.getBar(i));
            evaluateLogic(i, series, taStrategy.getStrategy(), signals, when.test(i));

            ZonedDateTime endTime = series.getBar(i).getEndTime();
            String barTime = Long.toString(endTime.toEpochSecond());

            for (String name : output.keySet()) {
                String value = ((Num) ((Indicator<Num>) output.get(name)).getValue(i)).intValue() + "";

                for (Map<String, Object> indicator : indicators) {
                    if (indicator.get("name").equals(name)) {
                        Object values = indicator.get("values");

                        ((List<String[]>) values).add(new String[]{barTime, value});
                    }
                }
            }

        }

        return results;
    }

    private void logBar(Bar bar) {
        logger.debug("open {} high {} low {} close {} vol {} trades {}",
                bar.getOpenPrice(),
                bar.getHighPrice(),
                bar.getLowPrice(),
                bar.getClosePrice(),
                bar.getVolume(),
                bar.getTrades()
        );
    }

    private void logLastBar(BarSeries series) {
        int i = series.getEndIndex();
        if (i <= 0) return;

        Bar bar = series.getBar(i);
        logger.debug("open {} high {} low {} close {} vol {} trades {}",
                bar.getOpenPrice(),
                bar.getHighPrice(),
                bar.getLowPrice(),
                bar.getClosePrice(),
                bar.getVolume(),
                bar.getTrades()
        );
    }

    public BaseBarSeries getSeries(String klines, String interval) throws IOException {
        ArrayList<ArrayList<String>> bars = (ArrayList<ArrayList<String>>) OM.readValue(klines, new TypeReference<ArrayList<ArrayList<String>>>() {
        });
        if (!bars.isEmpty()) bars.remove(0); // header

        BaseBarSeries series = new BaseBarSeriesBuilder().withName("test" + "_series").build();

        for (ArrayList<String> b : bars) {
            // closePrice
            double price = Math.abs(Double.parseDouble((String) b.get(4)));
            if (price > 0) {
                logger.debug("Price at {}: {}", b.get(1), price);

                // endTime
                BigDecimal decimalSeconds = new BigDecimal((String) b.get(6));  // 1622064025.004043001
                long seconds = decimalSeconds.longValue();
                long nanos = decimalSeconds.subtract(BigDecimal.valueOf(seconds))
                        .movePointRight(9)
                        .longValueExact();
                Instant inst = Instant.ofEpochSecond(seconds, nanos);
                logger.debug("Nanos for price {} {}", price, inst);  // 2021-05-26T21:20:25.004043001Z
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(inst, ZoneId.of("UTC"));

                Bar bar = new BaseBar(
                        Duration.ofSeconds(Long.parseLong(interval)),
                        dateTime,
                        (String) b.get(1),  // open
                        (String) b.get(2),  // high
                        (String) b.get(3),  // low
                        (String) b.get(4),  // close
                        (String) b.get(5),  // volume
                        (String) b.get(7)   // amount, quote asset volume
                );

                try {
                    series.addBar(bar);
                } catch (Exception e) {
                    logger.warn("Recreating series from json. Could not add bar (replacing last): {}", bar);
                    series.addBar(bar, true);
                    // Cannot add bar with end time <= previous bar end time
//                    logger.error("Recreating series from CSV", e);
                }
            }
        }
        return series;
    }


    private void evaluateLogic(int index, BarSeries series, Strategy strategy, List<String[]> signals, boolean when) {
        try {
            if (when) {
                ZonedDateTime endTime = series.getBar(index).getEndTime();
                // + check bar count
                ZonedDateTime nextOpenTime = series.getBar(index + 1).getBeginTime();
                if (strategy.shouldEnter(index)) {
                    // buy
                    logger.debug("BUY)");
                    // accService.sendOrder("buy", quantity, symbol);
                    signals.add(new String[]{Long.toString(nextOpenTime.toEpochSecond()), "B"});
                } else if (strategy.shouldExit(index)) {
                    //sell or close
                    logger.debug("SELL");
                    signals.add(new String[]{Long.toString(nextOpenTime.toEpochSecond()), "S"});
                    // accService.sendOrder("sell", quantity, symbol);
                }
            }
        } catch (NullPointerException npe) {
            //
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveBarToCSV(Bar b/*, BarSeries series*/) throws IOException {
//        int i = series.getEndIndex();
//        if(i < 1) return;
//        Bar bar = b == null ? series.getBar(i) : b;

        BarModel barModel = BarModel.fromBar(b);
        Map<String, String> barFields = (LinkedHashMap<String, String>)
                OM.readValue(
                        OM.writeValueAsString(barModel),
                        new TypeReference<LinkedHashMap<String, String>>() {
                        }
                );

//        writeToFile(fileName, barFields);
    }

}
