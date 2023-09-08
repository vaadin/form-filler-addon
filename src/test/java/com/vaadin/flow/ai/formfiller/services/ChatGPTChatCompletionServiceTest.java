package com.vaadin.flow.ai.formfiller.services;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.vaadin.flow.ai.formfiller.utils.ComponentUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class ChatGPTChatCompletionServiceTest {

    @Before
    public void setup() {
        //set environmental variable
        System.setProperty("OPENAI_TOKEN", "open-ai-key");
    }

    @Test
    public void testPromptGeneration() {
        ChatGPTChatCompletionService chatGPTChatCompletionService = new ChatGPTChatCompletionService();
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");

        NumberField numberField = new NumberField();
        numberField.setId("age");

        formLayout.add(textField);
        formLayout.add(numberField);
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(formLayout);

        String result = chatGPTChatCompletionService.getPromptTemplate("My name is John Doe, and I am 31 years old. I live in New York. " +
                        "I work as a software engineer. I am married and have two children.", componentsMapping.componentsJSONMap(), componentsMapping.componentsTypesJSONMap(),
                new HashMap<>(), new ArrayList<>());

//        Result:
//        Based on the user input:
//        "My name is John Doe, and I am 31 years old. I live in New York. I work as a software engineer. I am married and have two children.",
//        generate a JSON object according to these instructions: Never include duplicate keys,
//        in case of duplicate keys just keep the first occurrence in the response.
//        Fill out "N/A" in the JSON value if the user did not specify a value.
//        Return the result as a JSON object in this format: '{name=, age=}'.
//        Additional instructions about some of the JSON fields to be filled:
//        name: Format this JSON field as a String. age: Format this JSON field as a Number.

        assertTrue(result.contains("My name is John Doe, and I am 31 years old. I live in New York. " +
                "I work as a software engineer. I am married and have two children."));
        assertTrue(result.contains(" generate a JSON object according to these instructions: Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response." +
                " Fill out \"N/A\" in the JSON value if the user did not specify a value."));
        assertTrue(result.contains("Return the result as a JSON object in this format: '{name=, age=}'."));

        assertTrue(result.contains("Additional instructions about some of the JSON fields to be filled: "));
        assertTrue(result.contains("name: Format this JSON field as a String."));
        assertTrue(result.contains("age: Format this JSON field as a Number."));
    }

    @Test
    public void testGeneratedResponse() {
        ChatGPTChatCompletionService chatGPTChatCompletionService = Mockito.spy(new ChatGPTChatCompletionService());
        ChatCompletionResult mockedCompletionResult = getCompletionResult();
        Mockito.doReturn(mockedCompletionResult).when((OpenAiService) chatGPTChatCompletionService).createChatCompletion(any());
        VerticalLayout formLayout = buildFormLayout();
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(formLayout);

        String result = chatGPTChatCompletionService.getPromptTemplate("My name is John Doe, and I am 31 years old. I live in New York. " +
                        "I work as a software engineer. I am married and have two children.", componentsMapping.componentsJSONMap(), componentsMapping.componentsTypesJSONMap(),
                new HashMap<>(), new ArrayList<>());

        String generatedResponse = chatGPTChatCompletionService.getGeneratedResponse(result);
        System.out.println(generatedResponse);

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

    private static ChatCompletionResult getCompletionResult() {
        ChatCompletionResult mockedCompletionResult = new ChatCompletionResult();
        ChatCompletionChoice mockedCompletionChoice = new ChatCompletionChoice();
        ChatMessage mockedCompletionMessage = new ChatMessage();
        mockedCompletionMessage.setContent(" {\n" +
                "//            \"name\": \"John Doe\",\n" +
                "//                \"age\": 31,\n" +
                "//                \"location\": \"New York\",\n" +
                "//                \"occupation\": \"Software Engineer\",\n" +
                "//                \"maritalStatus\": \"Married\",\n" +
                "//                \"children\": 2\n" +
                "//        }");
        mockedCompletionChoice.setMessage(mockedCompletionMessage);
        mockedCompletionResult.setChoices(new ArrayList<>() {{
            add(mockedCompletionChoice);
        }});
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
