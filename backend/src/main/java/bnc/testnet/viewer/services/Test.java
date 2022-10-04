package bnc.testnet.viewer.services;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;

public class Test implements Runnable {
    @Override
    public void run() {
        //
        BarSeries barSeries = new BaseBarSeries();

        System.out.println("test run 2");
    }
}
