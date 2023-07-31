package org.vaadin.addons.ai.formfiller.services;

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

    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions);

    public String getGeneratedResponse(String prompt);

}
