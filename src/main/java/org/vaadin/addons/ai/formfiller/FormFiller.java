package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FormFiller component that using AI ChatGPT fills automatically
 * from a natural language input text a component or group of
 * components (layout).
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
        String response = "";
        mapping = ComponentUtils.createMapping(target);
        logger.debug("Generated Components Hierarchy JSON: {}", mapping.componentsJSONMap());
        logger.debug("Generated Components Types JSON: {}", mapping.componentsTypesJSONMap());
        logger.debug("AI response: " + response);
        return new FormFillerResult(prompt, response);
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
