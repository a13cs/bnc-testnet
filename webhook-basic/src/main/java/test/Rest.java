package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import test.model.OrderResult;
import test.util.OrderService;

import java.io.IOException;
import java.util.Map;

@RestController
public class Rest {

    @Autowired
    OrderService orderService;
    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(method = RequestMethod.POST, path = "/execute")
    public String saveProps(@RequestBody Map<String, Object> props) throws IOException, InterruptedException {
        String action = String.valueOf(props.get("action"));
        OrderResult orderResult = orderService.processOrder(action);

        return mapper.writeValueAsString(orderResult);
    }


}
