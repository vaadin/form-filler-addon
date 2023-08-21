package com.vaadin.flow.ai.formfiller.services;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.ai.formfiller.utils.ComponentUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ChatGPTServiceTest {
    @Before
    public void setup () {
       //set environmental variable
        System.setProperty("OPENAI_TOKEN", "open-ai-key");
    }

    @Test
    public void testPromptGeneration() {
        ChatGPTService chatGPTService = new ChatGPTService();
        VerticalLayout formLayout = buildFormLayout();
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(formLayout);

        String result = chatGPTService.getPromptTemplate("My name is John Doe, and I am 31 years old. I live in New York. " +
                        "I work as a software engineer. I am married and have two children.", componentsMapping.componentsJSONMap(), componentsMapping.componentsTypesJSONMap(),
                new HashMap<>(), new ArrayList<>());

//        Result:
//        Based on the user input: 'My name is John Doe, and I am 31 years old.
//        I live in New York. I work as a software engineer. I am married and have two children.',
//        generate a JSON object according to these instructions: Never include duplicate keys,
//        in case of duplicate keys just keep the first occurrence in the response.
//        Generate the JSON object with all keys being double quoted.
//        Fill out null value in the JSON value if the user did not specify a value.
//        Return the result as a JSON object in this format: '{name=, age=}'.
//        Perform any modification in the response to assure a valid JSON object.
//        Some Additional instructions about some of the fields to be filled:
//        name: Format this field as a String. age: Format this field as a Number.

        assertTrue(result.contains("My name is John Doe, and I am 31 years old. I live in New York. " +
                "I work as a software engineer. I am married and have two children."));
        assertTrue(result.contains("Generate the JSON object with all keys being double quoted."));
        assertTrue(result.contains(" generate a JSON object according to these instructions: Never include duplicate keys," +
                " in case of duplicate keys just keep the first occurrence in the response."));
        assertTrue(result.contains("Fill out null value in the JSON value if the user did not specify a value"));
        assertTrue(result.contains("Return the result as a JSON object in this format: '{name=, age=}'."));
        assertTrue(result.contains("Perform any modification in the response to assure a valid JSON object."));

        assertTrue(result.contains("Some Additional instructions about some of the fields to be filled: "));
        // Difference compared to the ChatGPTChatCompletionService (Json is missing from the `this field`),
        // and maybe 'Generate the JSON object with all keys being double quoted' would not be needed.
        assertTrue(result.contains("name: Format this field as a String"));
        assertTrue(result.contains("age: Format this field as a Number"));
    }

    @Test
    public void testGeneratedResponse() {
        ChatGPTService chatGPTService = Mockito.spy(new ChatGPTService());
        CompletionResult mockedCompletionResult = getCompletionResult();
        Mockito.doReturn(mockedCompletionResult).when((OpenAiService)chatGPTService).createCompletion(any());
        VerticalLayout formLayout = buildFormLayout();
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(formLayout);

        String result = chatGPTService.getPromptTemplate("My name is John Doe, and I am 31 years old. I live in New York. " +
                        "I work as a software engineer. I am married and have two children.", componentsMapping.componentsJSONMap(), componentsMapping.componentsTypesJSONMap(),
                new HashMap<>(), new ArrayList<>());

        String generatedResponse = chatGPTService.getGeneratedResponse(result);

//        Result from GPT on 2023-08-21:
//        {
//            "name": "John Doe",
//                "age": 31,
//                "location": "New York",
//                "occupation": "Software Engineer",
//                "maritalStatus": "Married",
//                "children": 2
//        }

        assertTrue(generatedResponse.contains("name"));
        assertTrue(generatedResponse.contains("age"));
        assertTrue(generatedResponse.contains("location"));
        assertTrue(generatedResponse.contains("occupation"));
        assertTrue(generatedResponse.contains("maritalStatus"));
        assertTrue(generatedResponse.contains("children"));

        assertTrue(generatedResponse.contains("John Doe"));
        assertTrue(generatedResponse.contains("31"));
        assertTrue(generatedResponse.contains("New York"));
        assertTrue(generatedResponse.contains("Software Engineer"));
        assertTrue(generatedResponse.contains("Married"));
        assertTrue(generatedResponse.contains("2"));
    }

    private static CompletionResult getCompletionResult() {
        CompletionResult mockedCompletionResult = new CompletionResult();
        CompletionChoice mockedCompletionChoice = new CompletionChoice();
        mockedCompletionChoice.setText(" {\n" +
                "//            \"name\": \"John Doe\",\n" +
                "//                \"age\": 31,\n" +
                "//                \"location\": \"New York\",\n" +
                "//                \"occupation\": \"Software Engineer\",\n" +
                "//                \"maritalStatus\": \"Married\",\n" +
                "//                \"children\": 2\n" +
                "//        }");
        mockedCompletionResult.setChoices(new ArrayList<>(){{add(mockedCompletionChoice);}});
        return mockedCompletionResult;
    }

    private static VerticalLayout buildFormLayout() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");

        NumberField numberField = new NumberField();
        numberField.setId("age");

        formLayout.add(textField);
        formLayout.add(numberField);
        return formLayout;
    }
}
