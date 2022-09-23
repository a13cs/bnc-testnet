package main;

import java.util.Objects;

public class SignalModel {

    private String extra;  // overbuy,oversell
    private String action; // buy
    private String price; // 21379.27
    private String volume; // 0.00083
    private String time; // 2022-09-10T07:44:30Z
    private String orderId; // Close entry(s) order sel

    public SignalModel() {
    }

    public SignalModel(String extra, String action, String price, String volume, String time, String orderId) {
        this.extra = extra;
        this.action = action;
        this.price = price;
        this.volume = volume;
        this.time = time;
        this.orderId = orderId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignalModel that = (SignalModel) o;
        return action.equals(that.action) && price.equals(that.price) && volume.equals(that.volume) && time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, price, volume, time);
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
