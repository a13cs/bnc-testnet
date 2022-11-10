package bnc.testnet.viewer.model.enc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.util.HashMap;

public class JSONTextDecoder implements Decoder.Text<Object> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
//        MAPPER.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);

        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
//        MAPPER.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
    }
//    private final Gson gson = new Gson();

    @Override
    public Object decode(String s) {
        try {
            return MAPPER.readValue(s, new TypeReference<HashMap<String, Object>>() { });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean willDecode(String s) {
            try {
                return MAPPER.readValue(s, new TypeReference<HashMap<String, Object>>() {}) != null;
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