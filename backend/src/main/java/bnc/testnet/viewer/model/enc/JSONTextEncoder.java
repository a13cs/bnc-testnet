package bnc.testnet.viewer.model.enc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class JSONTextEncoder implements Encoder.Text<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
//        MAPPER.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);

        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
//        MAPPER.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
    }

    @Override
    public String encode(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}