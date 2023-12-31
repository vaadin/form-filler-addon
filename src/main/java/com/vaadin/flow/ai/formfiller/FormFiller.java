package com.vaadin.flow.ai.formfiller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.ai.formfiller.services.FormFillerStats;
import com.vaadin.flow.component.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.ai.formfiller.services.ChatGPTChatCompletionService;
import com.vaadin.flow.ai.formfiller.services.LLMService;
import com.vaadin.flow.ai.formfiller.utils.ComponentUtils;
import com.vaadin.flow.server.VaadinService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FormFiller component that using AI ChatGPT fills automatically
 * from a natural language input text a component or group of
 * components (layout).<br>
 * Components supported:
 * <ul>
 *     <li>TextField</li>
 *     <li>EmailField</li>
 *     <li>PasswordField</li>
 *     <li>NumberField</li>
 *     <li>IntegerField</li>
 *     <li>BigDecimalField</li>
 *     <li>DatePicker</li>
 *     <li>TimePicker</li>
 *     <li>DateTimePicker</li>
 *     <li>TextArea</li>
 *     <li>Checkbox</li>
 *     <li>CheckboxGroup</li>
 *     <li>RadioButtonGroup</li>
 *     <li>ComboBox</li>
 *     <li>MultiSelectComboBox</li>
 *     <li>Grid</li>
 * </ul>
 *
 * To make a component available for the FormFiller, the component
 * must have an id, and it should be meaningful to be understood by
 * the AI module.<br>
 * <br>
 * <code>
 *     TextField textField = new TextField();<br>
 *     textField.setId("name");<br>
 *     <br>
 *     FormLayout fl = new FormLayout();<br>
 *     fl.add(textField);<br>
 *     <br>
 *     FormFiller formFiller = new FormFiller(fl);<br>
 *     formFiller.fill("My name is John");<br>
 * </code>
 * <br>
 * The text field will be filled with "John".<br>
 *
 * @author Vaadin Ltd.
 */
public class FormFiller {

    /**
     * Reports the usage statistics of the FormFiller component.
     *
     * The reporting of the usage statistics will only run once per JVM,
     * as the UsageStatistics taking care of duplicate reports.
     */
    static {
        FormFillerStats.report();
    }


    private static final Logger logger = LoggerFactory.getLogger(FormFiller.class);

    static {
        logger.warn("The Form Filler Add-on is an experimental feature and it may be removed, " +
                "altered, or limited to commercial subscribers in future releases.");
    }

    /**
     * The target component to fill.
     */
    private final Component target;

    /**
     * Additional instructions for the AI module (i.e.: field format,
     * field context, etc...).
     * Use these instructions to provide additional information to the AI module about a specific field
     * when the response of the form filler is not accurate enough.
     *
     * Use the target component as key and the instruction as value.
     */
    private final Map<Component, String> componentInstructions;

    /**
     * Additional instructions for the AI module (i.e.: target language, vocabulary explanation, etc..).
     * Use these instructions to provide additional information to the AI module about the context of the
     * input source in general.<br>
     * Be careful to add inconsistent instructions, as the AI module will try to find a response that
     * matches all the instructions.
     *
     * Use the instruction as value.
     */
    private final List<String> contextInstructions;

    /**
     * The JSON representation of the target components to fill.
     * Includes hierarchy map and value types of the components
     */
    ComponentUtils.ComponentsMapping mapping;

    /**
     * The AI module service to use.
     */
    private final LLMService llmService;

    /**
     * Creates a FormFiller to fill the target component or
     * group of components (layout) and includes additional
     * instructions for the AI module (i.e.: field format,
     * field context, etc...)
     *
     * @param target the target component or group of components (layout)
     *               to fill
     * @param componentInstructions additional instructions for the AI module (i.e.: field format, field explanation, etc...).
     *                              Use these instructions to provide additional information to the AI module about a specific field
     *                              when the response of the form filler is not accurate enough.
     * @param contextInstructions additional instructions for the AI module (i.e.: target language, vocabulary explanation, etc..).
     *                            Use these instructions to provide additional information to the AI module about the context of the
     *                            input source in general.
     * @param llmService the AI module service to use. By default, this service would use OpenAI ChatGPT.
     * @throws ExperimentalFeatureException
     *             when the {@link FeatureFlags#FORM_FILLER_ADDON} feature is
     *             not enabled
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions, List<String> contextInstructions, LLMService llmService) {
        if (!FeatureFlags.get(VaadinService.getCurrent().getContext())
                .isEnabled(FeatureFlags.FORM_FILLER_ADDON)) {
            throw new ExperimentalFeatureException();
        }
        this.llmService = llmService;
        this.target = target;
        this.componentInstructions = componentInstructions;
        this.contextInstructions = contextInstructions;
    }

    /**
     * Creates a FormFiller with default llmService.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions, List<String> contextInstructions) {
        this(target, componentInstructions, contextInstructions, new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService and empty context instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, Map<Component, String> componentInstructions) {
        this(target, componentInstructions, new ArrayList<>(), new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService and empty field instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, List<String> contextInstructions) {
        this(target, new HashMap<>(), contextInstructions, new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with default llmService, empty field instructions and empty context instructions.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target) {
        this(target, new HashMap<>(), new ArrayList<>(), new ChatGPTChatCompletionService());
    }

    /**
     * Creates a FormFiller with empty context instructions and empty context instructions with the given llmService.
     * Check {@link #FormFiller(Component, Map, List, LLMService) FormFiller} for more information.
     */
    public FormFiller(Component target, LLMService llmService) {
        this(target, new HashMap<>(), new ArrayList<>(), llmService);
    }

    /**
     * Fills automatically the target component analyzing the input source
     *
     * @param input Text input to send to the AI module
     * @return result of the query including request and response
     */
    public FormFillerResult fill(String input) {
        String prompt = "";
        mapping = ComponentUtils.createMapping(target);
        prompt = llmService.getPromptTemplate(input, mapping.componentsJSONMap(), mapping.componentsTypesJSONMap(), componentInstructions, contextInstructions);

        String aiResponse = llmService.getGeneratedResponse(prompt);
        ComponentUtils.fillComponents(mapping.componentInfoList(), promptJsonToMapHierarchyValues(aiResponse));

        logger.debug("Generated Prompt: {}", prompt);
        logger.debug("Generated Components Hierarchy JSON: {}", mapping.componentsJSONMap());
        logger.debug("Generated Components Types JSON: {}", mapping.componentsTypesJSONMap());
        logger.debug("AI response: " + aiResponse.trim());

        return new FormFillerResult(prompt, aiResponse);
    }

    /**
     * Transforms the response of the AI module (should be a valid JSON)
     * to a map with the hierarchy of the components and its values.
     *
     * @param response response prompt from the AI module
     * @return Map with components and values
     */
    private Map<String, Object> promptJsonToMapHierarchyValues(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map<String, Object> contentMap = new HashMap<>();
        try {
            contentMap = objectMapper.readValue(response.trim(), new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("Error parsing AI response to JSON Object: {}", e.getMessage());
        }
        return contentMap;
    }

    public Map<Component, String> getComponentInstructions() {
        return componentInstructions;
    }

    public List<String> getContextInstructions() {
        return contextInstructions;
    }

    public ComponentUtils.ComponentsMapping getMapping() {
        return mapping;
    }
}
