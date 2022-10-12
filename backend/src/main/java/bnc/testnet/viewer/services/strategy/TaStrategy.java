package bnc.testnet.viewer.services.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.num.Num;

import java.util.Map;

public interface TaStrategy {

    boolean shouldEnter(int i);

    Map<String, Num> getInputs();

    Map<String, Indicator<Num>> getOutput();

    Strategy getStrategy();

}

