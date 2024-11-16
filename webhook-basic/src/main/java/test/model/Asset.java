package test.model;

public class Asset {

    private String asset;
    private String value;
    private String usdtValue;

    public Asset() {
    }

    public Asset(String asset, String value, String usdtValue) {
        this.asset = asset;
        this.value = value;
        this.usdtValue = usdtValue;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUsdtValue() {
        return usdtValue;
    }

    public void setUsdtValue(String usdtValue) {
        this.usdtValue = usdtValue;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "asset='" + asset + '\'' +
                ", value='" + value + '\'' +
                ", usdtValue='" + usdtValue + '\'' +
                '}';
    }
}
