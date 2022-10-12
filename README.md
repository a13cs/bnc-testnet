
### Install
`mvn clean install`

`java -jar backend\target\demo-be-0.0.1-SNAPSHOT.jar`

API-KEY
=======

Check https://testnet.binance.vision/ for API KEY.

Module `java-basic` posts market orders for `{"action":"SELL_BNC-1"}` or `{"action":"BUY_BNC-1"}` post payload.
- side (sell/buy) + `_` + name

Tradingview webhook: 
- https://wundertrading.com/en/tradingview-automated-trading
- https://docs.aws.amazon.com/apigateway/latest/developerguide/getting-started.html
- https://github.com/awsdocs/aws-lambda-developer-guide/tree/main/sample-apps/java-basic

A pine script example strategy
- https://www.tradingview.com/script/FrjeeWdl-72s-Strat-Backtesting-Adaptive-HMA-pt-1/


Order quantity, symbol and name are set up in application.properties (via 'Add api-key' button).


###### NOTE
- A page refresh is needed after the api-key is changed.
- Clone and build https://github.com/janino-compiler/janino and see janino-change.txt. Maven uses local repo.  
- Can use bnc-testnet/frontend/node/yarn/dist/bin/yarn to install or add new dev dependencies.