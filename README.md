Check https://testnet.binance.vision/ for API KEY.

`mvn clean install`

`java -jar backend\target\demo-be-0.0.1-SNAPSHOT.jar`


`java-basic` posts market orders for `{"action":"sell"}` or `{"action":"buy"}` post payload. 
( tradingview webhook: https://wundertrading.com/en/tradingview-automated-trading )
Order quantity and symbol are set up in application.properties.