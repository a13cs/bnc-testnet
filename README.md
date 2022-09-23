
### Install
`mvn clean install`
`java -jar backend\target\demo-be-0.0.1-SNAPSHOT.jar`

API-KEY
=======

Check https://testnet.binance.vision/ for API KEY.

Module `java-basic` posts market orders for `{"action":"sell"}` or `{"action":"buy"}` post payload. (todo)

Tradingview webhook: 
- https://wundertrading.com/en/tradingview-automated-trading
- https://docs.aws.amazon.com/apigateway/latest/developerguide/getting-started.html
- https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/java-basic

A pine script example strategy
- https://www.tradingview.com/script/FrjeeWdl-72s-Strat-Backtesting-Adaptive-HMA-pt-1/


Order quantity and symbol are set up in application.properties (via 'Add api-key').
