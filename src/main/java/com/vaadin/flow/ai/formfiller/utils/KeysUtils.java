package com.vaadin.flow.ai.formfiller.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeysUtils {

    private static final Logger logger = LoggerFactory.getLogger(KeysUtils.class);
    private static String OPEN_AI_KEY;

    static {
        // read apiKey from -D param variable
        OPEN_AI_KEY = System.getProperty("OPENAI_TOKEN");
        if (OPEN_AI_KEY == null) {
            // read apiKey from environment variable
            OPEN_AI_KEY = System.getenv("OPENAI_TOKEN");
        }
        if(OPEN_AI_KEY != null) {
            logger.info("OPENAI_TOKEN was filled properly");
        } else {
            logger.error("OPENAI_TOKEN was not filled properly");
        }

    }

    public static String getOpenAiKey() {
        return OPEN_AI_KEY;
    }
}
