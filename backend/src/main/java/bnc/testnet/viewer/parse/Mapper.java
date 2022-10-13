package bnc.testnet.viewer.parse;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public enum Mapper {


    // indicators
    close(TaType.INDICATOR, ClosePriceIndicator.class),
    ema(TaType.INDICATOR, EMAIndicator.class),

    // rules
    crossunder(TaType.RULE, CrossedDownIndicatorRule.class),
    crossover(TaType.RULE, CrossedUpIndicatorRule.class),

    strategy(TaType.STRATEGY, BaseStrategy.class);

    private final Class<?> cls;
    private final TaType type;

    Mapper(TaType type, Class<?> cls) {
        this.cls = cls;
        this.type = type;
    }


    public Class<?> getCls() {
        return cls;
    }

    public TaType getType() {
        return type;
    }

    enum TaType {
        INDICATOR,
        RULE,
        STRATEGY
    }
}
