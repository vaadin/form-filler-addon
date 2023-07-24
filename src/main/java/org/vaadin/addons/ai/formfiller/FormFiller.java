package org.vaadin.addons.ai.formfiller;

import com.googlecode.gentyref.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;
import com.vaadin.flow.component.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FormFiller component that using AI ChatGPT fills automatically
 * from a natural language input text a component or group of
 * components (layout).<br>
 * Components supported:
 * <ul>
 *     <li>TextField</li>
 *     <li>NumberField</li>
 *     <li>DatePicker</li>
 *     <li>DateTimePicker</li>
 *
 *     <li>TextArea</li>
 *     <li>Checkbox (WIP)</li>
 *     <li>RadioButtonGroup (WIP)</li>
 *
 *     <li>ComboBox (WIP)</li>
 *     <li>ListBox (WIP)</li>
 *     <li>MultiSelectListBox (WIP)</li>
 *
 *     <li>Grid</li>
 *
 * @author Vaadin Ltd.
 */
public class FormFiller {

    private static final Logger logger = LoggerFactory.getLogger(FormFiller.class);

    /**
     * The target component to fill.
     */
    private Component target;

    private HashMap<Component, String> componentInstructions = new HashMap<>();

    private ArrayList<String> contextInstructions = new ArrayList<>();

    /*
     * The JSON representation of the target components to fill.
     * Includes hierarchy map and value types of the components
     */
    ComponentUtils.ComponentsMapping mapping;

    private LLMService llmService;

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
     */
    public FormFiller(Component target, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions, LLMService llmService) {
        this.llmService = llmService;
        this.target = target;
        this.componentInstructions = componentInstructions;
        this.contextInstructions = contextInstructions;
    }

    public FormFiller(Component target, HashMap<Component, String> componentInstructions, ArrayList<String> contextInstructions) {
        this(target, componentInstructions, contextInstructions, new ChatGPTService());
    }

    public FormFiller(Component target, HashMap<Component, String> componentInstructions) {
        this(target, componentInstructions, new ArrayList<>(), new ChatGPTService());
    }

    public FormFiller(Component target, ArrayList<String> contextInstructions) {
        this(target, new HashMap<>(), contextInstructions, new ChatGPTService());
    }

    public FormFiller(Component target) {
        this(target, new HashMap<>(), new ArrayList<>(), new ChatGPTService());
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
        ComponentUtils.fillComponents(mapping.components(), promptJsonToMapHierarchyValues(aiResponse));

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
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> contentMap = new HashMap<>();
        try {
            contentMap = gson.fromJson(response.trim(), type);
        }
        catch (Exception e) {
            logger.error("Error parsing AI response to JSON Object: {}", e.getMessage());
        }
        return contentMap;
    }

    public HashMap<Component, String> getComponentInstructions() {
        return componentInstructions;
    }

    public ArrayList<String> getContextInstructions() {
        return contextInstructions;
    }

    public ComponentUtils.ComponentsMapping getMapping() {
        return mapping;
    }
}
