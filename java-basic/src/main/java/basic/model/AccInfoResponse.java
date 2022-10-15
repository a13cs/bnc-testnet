package basic.model;

//import lombok.Data;

import java.util.List;

//@Data
public class AccInfoResponse {

//      GET /api/v3/account (HMAC SHA256)
//      param: timestamp LONG
/*    {
            "makerCommission": 15,
            "takerCommission": 15,
            "buyerCommission": 0,
            "sellerCommission": 0,
            "canTrade": true,
            "canWithdraw": true,
            "canDeposit": true,
            "updateTime": 123456789,
            "accountType": "SPOT",
            "balances": [
        {
            "asset": "BTC",
            "free": "4723846.89208129",
            "locked": "0.00000000"
        },
        {
            "asset": "LTC",
            "free": "4763368.68006011",
            "locked": "0.00000000"
        }
  ],
        "permissions": [
        "SPOT"
  ]
    }*/

    private int makerCommission;
    private int takerCommission;
    private int buyerCommission;
    private int sellerCommission;
    private boolean canTrade;
    private boolean canWithdraw;
    private boolean canDeposit;
    private long updateTime;
    private String accountType;

    private List<Balance> balances;
    private List<String> permissions;

    public AccInfoResponse() {
    }

    public AccInfoResponse(int makerCommission, int takerCommission, int buyerCommission, int sellerCommission, boolean canTrade, boolean canWithdraw, boolean canDeposit, long updateTime, String accountType, List<Balance> balances, List<String> permissions) {
        this.makerCommission = makerCommission;
        this.takerCommission = takerCommission;
        this.buyerCommission = buyerCommission;
        this.sellerCommission = sellerCommission;
        this.canTrade = canTrade;
        this.canWithdraw = canWithdraw;
        this.canDeposit = canDeposit;
        this.updateTime = updateTime;
        this.accountType = accountType;
        this.balances = balances;
        this.permissions = permissions;
    }

    public int getMakerCommission() {
        return makerCommission;
    }

    public void setMakerCommission(int makerCommission) {
        this.makerCommission = makerCommission;
    }

    public int getTakerCommission() {
        return takerCommission;
    }

    public void setTakerCommission(int takerCommission) {
        this.takerCommission = takerCommission;
    }

    public int getBuyerCommission() {
        return buyerCommission;
    }

    public void setBuyerCommission(int buyerCommission) {
        this.buyerCommission = buyerCommission;
    }

    public int getSellerCommission() {
        return sellerCommission;
    }

    public void setSellerCommission(int sellerCommission) {
        this.sellerCommission = sellerCommission;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

    public void setCanTrade(boolean canTrade) {
        this.canTrade = canTrade;
    }

    public boolean isCanWithdraw() {
        return canWithdraw;
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public boolean isCanDeposit() {
        return canDeposit;
    }

    public void setCanDeposit(boolean canDeposit) {
        this.canDeposit = canDeposit;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    //    @Data
    public static class Balance {
        String asset, free, locked;

        public Balance() {
        }

        public Balance(String asset, String free, String locked) {
            this.asset = asset;
            this.free = free;
            this.locked = locked;
        }

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }

        public String getLocked() {
            return locked;
        }

        public void setLocked(String locked) {
            this.locked = locked;
        }
    }

}
