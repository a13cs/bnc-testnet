package bnc.testnet.viewer.services;

import bnc.testnet.viewer.rest.BarModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class StrategyService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);


    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();

        OM.findAndRegisterModules();
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OM.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OM.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

//    private BarSeries series;
//    private Strategy strategy;
//    private EMAIndicator fastEma;
//    private EMAIndicator slowEma;

    @Override
    public void afterPropertiesSet() {
//        series = new BaseBarSeriesBuilder().withName("bnc_test_series").build();
//
//        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
//        fastEma = new EMAIndicator(closePriceIndicator, this.emaPeriodShort);
//        slowEma = new EMAIndicator(closePriceIndicator, this.emaPeriodLong);
//
//        CrossedUpIndicatorRule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);
//        CrossedDownIndicatorRule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);
//        strategy = new BaseStrategy(entryRule, exitRule);
    }

    public Map<String,Object> runTest(String klines, String interval, String pine) throws IOException {
        Map<String,Object> results = new HashMap<>();

        List<String[]> signals = new ArrayList<>();
        List<Map<String, Object>> indicators = new ArrayList<>();

        results.put("signals", signals);
        results.put("indicators", indicators);

        int emaPeriodShort = 20;
        int emaPeriodLong  = 80;


        // may keep adding bars and evaluateLogic
        BarSeries series = getSeries(klines, interval);

        // Strategy START
        // + use pine

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        // + keep list to add indicator at the end
        EMAIndicator fastEma = new EMAIndicator(closePriceIndicator, emaPeriodShort);
        EMAIndicator slowEma = new EMAIndicator(closePriceIndicator, emaPeriodLong);


        CrossedUpIndicatorRule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);
        CrossedDownIndicatorRule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);
        Strategy strategy = new BaseStrategy(entryRule, exitRule);

        // Strategy END


        int beginIndex = series.getBeginIndex();
        int endIndex = series.getEndIndex();

        List<String[]> fastEmaValues = new ArrayList<>();
        List<String[]> slowEmaValues = new ArrayList<>();

        for (int i = beginIndex; i <= endIndex; i++) {
            logBar(series.getBar(i));
            evaluateLogic(i, series, strategy, signals, i > emaPeriodLong);

            ZonedDateTime endTime = series.getBar(i).getEndTime();
            String barTime = Long.toString(endTime.toEpochSecond());

            if (i > emaPeriodLong) {
                String fastEmaValue = ((Num)fastEma.getValue(i)).intValue() + "";
                fastEmaValues.add(new String[]{barTime, fastEmaValue});

                String slowEmaValue = ((Num)slowEma.getValue(i)).intValue() + "";
                slowEmaValues.add(new String[]{barTime, slowEmaValue});
            }
        }

        Map<String, Object> fastEma_Line_green = new HashMap<>();
        fastEma_Line_green.put("name", "fastEma_Line_green");
        fastEma_Line_green.put("color", "green");
        fastEma_Line_green.put("values", fastEmaValues);
        indicators.add(fastEma_Line_green);

        Map<String, Object> slowEma_Line_blue = new HashMap<>();
        slowEma_Line_blue.put("name", "slowEma_Line_blue");
        slowEma_Line_blue.put("color", "blue");
        slowEma_Line_blue.put("values", slowEmaValues);
        indicators.add(slowEma_Line_blue);

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
        if(i <= 0) return;

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
        ArrayList<ArrayList<String>> bars = OM.readValue(klines, new TypeReference<ArrayList<ArrayList<String>>>() { });
        if (!bars.isEmpty()) bars.remove(0); // header

        BaseBarSeries series = new BaseBarSeriesBuilder().withName("test" + "_series").build();

        bars.forEach(b -> {
            // closePrice
            double price = Math.abs(Double.parseDouble(b.get(4)));
            if (price > 0) {
                logger.debug("Price at {}: {}",b.get(1), price);

                // endTime
                BigDecimal decimalSeconds = new BigDecimal(b.get(6));  // 1622064025.004043001
                long seconds = decimalSeconds.longValue();
                long nanos = decimalSeconds.subtract(BigDecimal.valueOf(seconds))
                        .movePointRight(9)
                        .longValueExact();
                Instant inst = Instant.ofEpochSecond(seconds, nanos);
                logger.debug("Nanos for price {} {}",price, inst);  // 2021-05-26T21:20:25.004043001Z
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(inst, ZoneId.of("UTC"));

                Bar bar = new BaseBar(
                        Duration.ofSeconds(Long.parseLong(interval)),
                        dateTime,
                        b.get(1),  // open
                        b.get(2),  // high
                        b.get(3),  // low
                        b.get(4),  // close
                        b.get(5),  // volume
                        b.get(7)   // amount, quote asset volume
                );

                try{
                    series.addBar(bar);
                } catch (Exception e) {
                    logger.warn("Recreating series from json. Could not add bar (replacing last): {}", bar);
                    series.addBar(bar,true);
                    // Cannot add bar with end time <= previous bar end time
//                    logger.error("Recreating series from CSV", e);
                }
            }
        });
        return series;
    }


    private void evaluateLogic(int index, BarSeries series, Strategy strategy, List<String[]> signals, boolean when) {
        try {
            if (when) {
                ZonedDateTime endTime = series.getBar(index).getEndTime();
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
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    private void saveBarToCSV(Bar b/*, BarSeries series*/) throws IOException {
//        int i = series.getEndIndex();
//        if(i < 1) return;
//        Bar bar = b == null ? series.getBar(i) : b;

        BarModel barModel = BarModel.fromBar(b);
        Map<String, String> barFields =
                OM.readValue(
                        OM.writeValueAsString(barModel),
                        new TypeReference<LinkedHashMap<String, String>>() { }
                );

//        writeToFile(fileName, barFields);
    }

}
