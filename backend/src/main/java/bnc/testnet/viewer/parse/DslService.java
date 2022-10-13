package bnc.testnet.viewer.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class DslService {

    private static final Logger logger = LoggerFactory.getLogger(DslService.class);


    public String parse(String s) {
        logger.info("Mapper.ema {}", Mapper.ema.getCls().getSimpleName());
        logger.info(s);

        return s;
    }

}
