package test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import test.model.OrderResult;
import test.util.OrderService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
public class Rest {

    private static final Logger logger = LoggerFactory.getLogger(Rest.class);

    @Autowired
    OrderService orderService;

    @RequestMapping(method = RequestMethod.POST, path = "/execute")
    public Map<String,String> execute(@RequestBody Map<String, Object> json) throws IOException, InterruptedException {
        String action = String.valueOf(json.get("action"));
        OrderResult orderResult = orderService.processOrder(action);
        logger.info(orderResult.toString());

        return Collections.singletonMap("result", orderResult.getOrderId());
    }


}
