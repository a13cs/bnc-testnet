package main;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

// Handler value: main.Handler
public class Handler implements RequestHandler<Map<String,Object>, String>{
  Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @Override
  public String handleRequest(Map<String,Object> event, Context context)
  {
    LambdaLogger logger = context.getLogger();
    String response = "200 OK";
    logger.log("CONTEXT: " + gson.toJson(context));
    // process event
    logger.log("EVENT: " + gson.toJson(event));
    logger.log("EVENT TYPE: " + event.getClass());
//    String[] s = new String[1];
//    event.forEach((k,v) -> {
//      logger.log(String.format("k -> %s, v -> %s", k, v));
//      if (String.valueOf(k).equals("isBase64Encoded")) s[0] = String.valueOf(v);
//    });
    Object body = event.get("body");
    byte[] decode = Base64.getDecoder().decode(body.toString());
    return new String(decode, StandardCharsets.UTF_8);
  }



}