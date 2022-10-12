package bnc.testnet.viewer.services;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

public class Test implements Runnable {
    @Override
    public void run() {
        BarSeries barSeries = new BaseBarSeriesBuilder().build();

        System.out.println(barSeries.getName());
    }
}
