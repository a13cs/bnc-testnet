package bnc.testnet.viewer.services.strategy;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.HashMap;
import java.util.Map;

public class TaStrategyImpl implements TaStrategy {

    private final BarSeries series;
    private final Strategy strategy;

    private final Map<String, Num> inputs = new HashMap<>();
    private final Map<String, Indicator<Num>> output = new HashMap<>();


    {
        inputs.putIfAbsent("emaPeriodShort", DecimalNum.valueOf(20));
        inputs.putIfAbsent("emaPeriodLong", DecimalNum.valueOf(80));
    }

    public TaStrategyImpl(BarSeries barSeries) {
        this.series = barSeries;

        ClosePriceIndicator close = new ClosePriceIndicator(series);

        int emaPeriodShort = ((Num) inputs.get("emaPeriodShort")).intValue();
        int emaPeriodLong = ((Num) inputs.get("emaPeriodLong")).intValue();

        EMAIndicator fastEma = new EMAIndicator(close, emaPeriodShort);
        EMAIndicator slowEma = new EMAIndicator(close, emaPeriodLong);

        // + keep list to add indicator at the end
        // plot(fastEma)
        // plot(slowEma)
        output.putIfAbsent("fastEma", fastEma);
        output.putIfAbsent("slowEma", slowEma);

        Rule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);
        Rule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);

        strategy = new BaseStrategy(entryRule, exitRule);
    }

    public Map<String, Num> getInputs() {
        return inputs;
    }

    public Map<String, Indicator<Num>> getOutput() {
        return output;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public BarSeries getSeries() {
        return series;
    }

    // + implement Strategy
    public boolean shouldEnter(int i) {
        return this.strategy.shouldEnter(i);
    }

/*

    // Strategy START


        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

        EMAIndicator fastEma = new EMAIndicator(closePriceIndicator, emaPeriodShort);
        EMAIndicator slowEma = new EMAIndicator(closePriceIndicator, emaPeriodLong);


        CrossedUpIndicatorRule entryRule = new CrossedUpIndicatorRule(fastEma, slowEma);
        CrossedDownIndicatorRule exitRule = new CrossedDownIndicatorRule(fastEma, slowEma);

        Strategy strategy = new BaseStrategy(entryRule, exitRule);

    // Strategy END

*/
}
