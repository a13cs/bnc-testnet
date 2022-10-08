package bnc.testnet.viewer.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.ta4j.core.Bar;

import java.time.Duration;
import java.time.ZonedDateTime;

@Data
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BarModel {

    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private Duration timePeriod;
    private Double openPrice;
    private Double closePrice;
    private Double highPrice;
    private Double lowPrice;
    private Double amount;
    private Double volume;
    private Long trades;

//    private String dateName;
//    private Boolean bearish;
//    private Boolean bullish;


    public static BarModel fromBar(Bar bar) {

        double openPrice = bar.getOpenPrice() != null ? bar.getOpenPrice().doubleValue() : 0;
        double closePrice = bar.getClosePrice() != null ? bar.getClosePrice().doubleValue() : 0;
        double highPrice = bar.getHighPrice() != null ? bar.getHighPrice().doubleValue() : 0;
        double lowPrice = bar.getLowPrice() != null ? bar.getLowPrice().doubleValue() : 0;

        double amount = bar.getAmount() != null ? bar.getAmount().doubleValue() : 0;
        double volume = bar.getVolume() != null ? bar.getVolume().doubleValue() : 0;

        return new BarModel(
                bar.getBeginTime(),
                bar.getEndTime(),
                bar.getTimePeriod(),
                openPrice,
                closePrice,
                highPrice,
                lowPrice,
                amount,
                volume,
                bar.getTrades()
                // may not save
//                bar.getDateName(),
//                bar.isBearish(),
//                bar.isBullish()
        );
    }
/*
[
    [
    1499040000000,      // Kline open time
    "0.01634790",       // Open price
    "0.80000000",       // High price
    "0.01575800",       // Low price
    "0.01577100",       // Close price
    "148976.11427815",  // Volume
    1499644799999,      // Kline Close time
    "2434.19055334",    // Quote asset volume
    308,                // Number of trades
    "1756.87402397",    // Taker buy base asset volume
    "28.46694368",      // Taker buy quote asset volume
    "0"                 // Unused field, ignore.
    ]
]
*/

}
