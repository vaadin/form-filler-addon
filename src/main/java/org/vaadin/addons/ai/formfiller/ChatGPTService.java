package org.vaadin.addons.ai.formfiller;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;

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
    public String getPromptTemplate() {
        return null;
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
