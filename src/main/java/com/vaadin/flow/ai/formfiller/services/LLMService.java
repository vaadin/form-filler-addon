package com.vaadin.flow.ai.formfiller.services;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;

/**
 * A LLM service that generates a response based on a prompt.
 * All responsibilities related to the model usage have to be
 * implemented in this service. This could be APIKEY providing,
 * parameter setting, prompt template generation, etc.
 */
public interface LLMService {

    /**
     * Generates a prompt based on the input, the target components and any
     * extra instruction.
     *
     * @param input the input text (e.g. "My name is John")
     * @param objectMap the objectMap containing the target components in a
     *                  hierarchical structure (keys = ids)
     * @param typesMap the map containing the desired type to fill with each
     *                 one of the target components (keys = ids)
     * @param componentInstructions the components (fields) instructions
     *                              containing additional information to understand
     *                              the field meaning.
     * @param contextInstructions the context instructions containing additional
     *                            information to understand the input or
     *                            providing some information not present in the input
     * @return the generated prompt to be sent to the AI module
     */
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions);

    /**
     * Generates a response based on the input prompt from the AI module.
     *
     * @param prompt the prompt to be used by the AI module
     * @return the generated response from the AI module. This response has to
     * be a valid JSON Object using target field IDs as keys and the value
     * correctly formatted according to the target component. An example is
     * the following format:
     * <br>
     * <pre>{@code
     *     {
     *     "field Id 1": "value 1", // TextField
     *     "field Id 2": 66, // NumberField or IntegerField
     *     "field Id 3": "2022-04-03", // DatePicker
     *     "field Id 4 ": [ // Grid
     *          {
     *         "inner item id1": "Value 1",
     *         "inner item id2": "Value 2",
     *         "inner item id3": "Value 3"
     *          },
     *          {
     *         "inner item id1": "Value 1",
     *         "inner item id2": "Value 2",
     *         "inner item id3": "Value 3"
     *          }
     * ],
     * "field Id 5 ": ["Value 1", "Value 2"], // MultiSelectComboBox
     * "field Id 6": true, // Checkbox
     * "field Id 7": 43.47 // BigDecimalField
     * }
     * }</pre> <br>
     */
    public String getGeneratedResponse(String prompt);

}
