package org.vaadin.addons.ai.formfiller.services;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.component.Component;
import org.vaadin.addons.ai.formfiller.utils.KeysUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatGPTChatCompletionService extends OpenAiService implements LLMService {

    /**
     * ID of the model to use.
     */
    private String MODEL = "gpt-3.5-turbo-16k";
    /**
     * The maximum number of tokens to generate in the completion.
     */
    private Integer MAX_TOKENS = 12000;

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

    public ChatGPTChatCompletionService() {
        super(KeysUtils.getOpenAiKey(), Duration.ofSeconds(TIMEOUT));
    }

    @Override
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions) {
        String gptRequest = String.format(
                "Based on the user input: '%s', " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Fill out \"N/A\" in the JSON value if the user did not specify a value. " +
                        "Return the result as a JSON object in this format: '%s'."
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
        return gptRequest;
    }

    @Override
    public String getGeneratedResponse(String prompt) {

        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user",prompt));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model(MODEL).maxTokens(MAX_TOKENS).temperature(TEMPERATURE)
                .build();

        ChatCompletionResult completionResult = createChatCompletion(chatCompletionRequest);
        String aiResponse = completionResult.getChoices().get(0).getMessage().getContent();
        return aiResponse;
    }
}
