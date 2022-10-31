package bnc.testnet.viewer.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

//@Builder
//@Data
public class ChannelSubscription {

    private String method;
    private List<String> params;
    private Integer id;


    public static ChannelSubscription trades(String pair) {
        ChannelSubscription cs = new ChannelSubscription();
        cs.setMethod("SUBSCRIBE");
        cs.setParams(Arrays.asList(
//                        pair + "@aggTrade",
                pair + "@trade",
                pair + "@depth")
        );
        int anInt = new Random(Instant.now().toEpochMilli()).nextInt(100);
        cs.setId(anInt);

        return cs;
    }

    public static ChannelSubscription list() {
        ChannelSubscription cs = new ChannelSubscription();
        cs.setMethod("LIST_SUBSCRIPTIONS");
        int anInt = new Random(Instant.now().toEpochMilli()).nextInt(100);
        cs.setId(anInt);

        return cs;
    }

    public static ChannelSubscription kLines(String pair, String interval) {
        ChannelSubscription cs = new ChannelSubscription();
        cs.setMethod("SUBSCRIBE");
        cs.setParams(Arrays.asList(pair + "@kline_" + interval)
        );
        int anInt = new Random(Instant.now().toEpochMilli()).nextInt(100);
        cs.setId(anInt);

        return cs;
    }

    // + pair@ticker


    public String getMethod() {
        return method;
    }

    public List<String> getParams() {
        return params;
    }

    public Integer getId() {
        return id;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
