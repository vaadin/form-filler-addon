package com.vaadin.flow.ai.formfiller.services;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.ai.formfiller.utils.KeysUtils;
import com.vaadin.flow.component.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatGPTService extends OpenAiService implements LLMService {

    /**
     * ID of the model to use.
     */
    private String MODEL = "text-davinci-003";

    /**
     * The maximum number of tokens to generate in the completion.
     */
    private Integer MAX_TOKENS = 2048;

    /**
     * What sampling temperature to use, between 0 and 2.
     * Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     */
    private Double TEMPERATURE = 0d;

    /**
     * If true the input prompt is included in the response
     */
    private Boolean ECHO = false;

    /**
     * Timeout for AI module response in seconds
     */
    private static Integer TIMEOUT = 60;

    public ChatGPTService() {
        super(KeysUtils.getOpenAiKey(), Duration.ofSeconds(TIMEOUT));
    }

    @Override
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions) {
        String gptRequest = String.format(
                "Based on the user input: '%s', " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Generate the JSON object with all keys being double quoted." +
                        "Fill out null value in the JSON value if the user did not specify a value. " +
                        "Return the result as a JSON object in this format: '%s'. Perform any modification in the response to assure a valid JSON object."
                , input, objectMap);
        if (!componentInstructions.isEmpty() || !typesMap.isEmpty()) {
            gptRequest += "Some Additional instructions about some of the fields to be filled: ";
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                gptRequest += " " + entry.getKey() + ": Format this field as " + entry.getValue() + ".";
            }
            for (Map.Entry<Component, String> entry : componentInstructions.entrySet()) {
                if (entry.getKey().getId().isPresent())
                    gptRequest += " " + entry.getKey().getId().get() + ": " + entry.getValue() + ".";
            }
        }
        if (!contextInstructions.isEmpty()) {
            gptRequest += "Additional instructions about the context and desired JSON output response: ";
            for (String contextInstruction : contextInstructions) {
                gptRequest += " " + contextInstruction + ".";
            }
        }
        return gptRequest;
    }

    @Override
    public String getGeneratedResponse(String prompt) {

        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .model(MODEL).maxTokens(MAX_TOKENS).temperature(TEMPERATURE)
                .echo(false)
                .build();


        CompletionResult completion = createCompletion(completionRequest);
        String aiResponse = completion.getChoices().get(0).getText();
        return aiResponse;
    }
}
