package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import example.util.OrderResult;
import example.util.OrderService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Hello implements RequestHandler<Object, Response> {
    private final OrderService orderService = new OrderService();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Override
    public Response handleRequest(Object o, Context context) {
        Map<String, Object> event = (Map<String, Object>) o;
        LambdaLogger logger = context.getLogger();
        try {

            try {
                logger.log("CONTEXT: " + MAPPER.writeValueAsString(context));
                logger.log("EVENT: " + MAPPER.writeValueAsString(event));
                logger.log("EVENT TYPE: " + event.getClass());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Object body = event.get("body");

            HashMap<String, Object> bodyMap = new HashMap<>();
            try {
                String jsonEvent;
                String isBase64Encoded = event.get("isBase64Encoded").toString();
                if ("true".equalsIgnoreCase(isBase64Encoded)) {
                    byte[] decode = Base64.getDecoder().decode(body.toString());
                    jsonEvent = new String(decode, StandardCharsets.UTF_8);
                } else {
                    jsonEvent = body.toString();
                }

                bodyMap = MAPPER.readValue(jsonEvent, new TypeReference<HashMap<String, Object>>() {
                });
            } catch (Exception e) {
                context.getLogger().log("Event received as " + event);
                bodyMap.putAll(event);
            }
            String model = bodyMap.get("action").toString().toUpperCase();
            logger.log("action: " + model);


            OrderResult orderResult = orderService.processOrder(model, context);

            String result = MAPPER.writeValueAsString(orderResult);
            return new Response(result, 200);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}