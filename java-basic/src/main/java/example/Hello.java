package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.OrderResult;
import util.OrderService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Hello implements RequestHandler<Map<String, Object>, String>{
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final OrderService orderService = new OrderService();
  @Override
  public String handleRequest(Map<String, Object> event, Context context) {
    LambdaLogger logger = context.getLogger();

    logger.log("CONTEXT: " + gson.toJson(context));
    // process event
    String json = gson.toJson(event);
    logger.log("EVENT: " + json);
    logger.log("EVENT TYPE: " + event.getClass());

    // todo: text/plain

    try {
      Object body = event.get("body");

      HashMap<String, Object> bodyMap = new HashMap<>();
      try{
        String jsonEvent;
        String isBase64Encoded = event.get("isBase64Encoded").toString();
        if ("true".equalsIgnoreCase(isBase64Encoded)) {
          byte[] decode = Base64.getDecoder().decode(body.toString());
          jsonEvent = new String(decode, StandardCharsets.UTF_8);
        } else {
          jsonEvent = body.toString(); //  "body": "{\"action\":\"sell\"}"
        }

        /*HashMap<String, Object>*/ bodyMap = new ObjectMapper().readValue(jsonEvent, new TypeReference<HashMap<String, Object>>() {
        });
      } catch (Exception e) {
        context.getLogger().log("Event received as " + event);
        bodyMap.putAll(event);
      }
      String model = bodyMap.get("action").toString().toUpperCase();

      OrderResult orderResult = orderService.processOrder(model, context);
//      logger.log("OrderResult: " + gson.toJson(orderResult));

      return gson.toJson(orderResult);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }

  }



}