package basic.model.ref;

//import lombok.Data;

import java.time.LocalDateTime;

//@Data
public class AccTradesResponse {

    private String symbol;// "BNBBTC",
    private Integer id;// 28457,
    private Integer orderId;// 100234,
    private Integer orderListId;// -1, //Unless OCO, the value will always be -1
    private Double price;// "4.00000100",
    private Double qty;// "12.00000000",
    private Double quoteQty;// "48.000012",
    private Double commission;// "10.10000000",
    private String commissionAsset;// "BNB",
    private Long time;// 1499865549590,
    private LocalDateTime displayTime;
    private Boolean isBuyer;// true,
    private Boolean isMaker;// false,
    private Boolean isBestMatch;// true

    public AccTradesResponse() {
    }

    public AccTradesResponse(String symbol, Integer id, Integer orderId, Integer orderListId, Double price, Double qty, Double quoteQty, Double commission, String commissionAsset, Long time, LocalDateTime displayTime, Boolean isBuyer, Boolean isMaker, Boolean isBestMatch) {
        this.symbol = symbol;
        this.id = id;
        this.orderId = orderId;
        this.orderListId = orderListId;
        this.price = price;
        this.qty = qty;
        this.quoteQty = quoteQty;
        this.commission = commission;
        this.commissionAsset = commissionAsset;
        this.time = time;
        this.displayTime = displayTime;
        this.isBuyer = isBuyer;
        this.isMaker = isMaker;
        this.isBestMatch = isBestMatch;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getOrderListId() {
        return orderListId;
    }

    public void setOrderListId(Integer orderListId) {
        this.orderListId = orderListId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getQuoteQty() {
        return quoteQty;
    }

    public void setQuoteQty(Double quoteQty) {
        this.quoteQty = quoteQty;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public String getCommissionAsset() {
        return commissionAsset;
    }

    public void setCommissionAsset(String commissionAsset) {
        this.commissionAsset = commissionAsset;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public LocalDateTime getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(LocalDateTime displayTime) {
        this.displayTime = displayTime;
    }

    public Boolean getBuyer() {
        return isBuyer;
    }

    public void setBuyer(Boolean buyer) {
        isBuyer = buyer;
    }

    public Boolean getMaker() {
        return isMaker;
    }

    public void setMaker(Boolean maker) {
        isMaker = maker;
    }

    public Boolean getBestMatch() {
        return isBestMatch;
    }

    public void setBestMatch(Boolean bestMatch) {
        isBestMatch = bestMatch;
    }
}
