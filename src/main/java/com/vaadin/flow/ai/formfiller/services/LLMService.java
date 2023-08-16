package com.vaadin.flow.ai.formfiller.services;

import com.vaadin.flow.component.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A LLM service that generates a response based on a prompt.
 * All responsabilities related to the model usage have to be
 * implemented in this service. This could be APIKEY providing,
 * parameter setting, prompt template generation, etc.
 */
public interface LLMService {

    /**
     * Generates a prompt based on the input, the target objectMap and extra instructions.
     */
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions);

    /**
     * Generates a response based on the input prompt from the AI module.
     */
    public String getGeneratedResponse(String prompt);

}
