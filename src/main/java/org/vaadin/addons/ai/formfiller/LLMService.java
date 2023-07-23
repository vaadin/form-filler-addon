package org.vaadin.addons.ai.formfiller;

/**
 * A LLM service that generates a response based on a prompt.
 * All responsabilities related to the model usage have to be
 * implemented in this service. This could be APIKEY providing,
 * parameter setting, prompt template generation, etc.
 */
public interface LLMService {

    public String getPromptTemplate();

    public String getGeneratedResponse(String prompt);

}
