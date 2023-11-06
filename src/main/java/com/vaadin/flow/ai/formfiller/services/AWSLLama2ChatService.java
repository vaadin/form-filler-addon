package com.vaadin.flow.ai.formfiller.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AWSLLama2ChatService implements LLMService {

    public AWSLLama2ChatService() {
    }

    @Override
    public String getPromptTemplate(String input, Map<String, Object> objectMap, Map<String, String> typesMap, Map<Component, String> componentInstructions, List<String> contextInstructions) {
        String gptRequest = String.format(
                "Based on the user input: \n \"%s\", " +
                        "generate a JSON object according to these instructions: " +
                        "Never include duplicate keys, in case of duplicate keys just keep the first occurrence in the response. " +
                        "Fill out \"N/A\" in the JSON value if the user did not specify a value. " +
                        "Return the result as a JSON object in this format: '%s'."
                , input, objectMap);
        if (!componentInstructions.isEmpty() || !typesMap.isEmpty()) {
            gptRequest += "\nAdditional instructions about some of the JSON fields to be filled: ";
            for (Map.Entry<String, String> entry : typesMap.entrySet()) {
                gptRequest += "\n" + entry.getKey() + ": Format this JSON field as " + entry.getValue() + ".";
            }
            for (Map.Entry<Component, String> entry : componentInstructions.entrySet()) {
                if (entry.getKey().getId().isPresent())
                    gptRequest += "\n" + entry.getKey().getId().get() + ": " + entry.getValue() + ".";
            }
            if (!contextInstructions.isEmpty()) {
                gptRequest += "\nAdditional instructions about the context and desired JSON output response:  ";
                for (String contextInstruction : contextInstructions) {
                    gptRequest += " " + contextInstruction + ".";
                }
            }
        }
        return gptRequest;
    }

    String jsonData = "{" +
            "\"typeService\": [\"Software\", \"Hardware\", \"Consultancy\"]," +
            "\"address\": \"123 Avenue A, Metropolis\"," +
            "\"dueDate\": \"2022-12-30\"," +
            "\"orderEntity\": \"Person\"," +
            "\"creationDate\": \"2022-12-07T12:00:00\"," +
            "\"orderDescription\": \"Baby chicks, Heat lamps, Chicken roosts\"," +
            "\"orderTotal\": 291.90," +
            "\"isFinnishCustomer\": false," +
            "\"phone\": \"(123) 456-7890\"," +
            "\"name\": \"Henry Ross\"," +
            "\"paymentMethod\": \"Cash or check\"," +
            "\"orders\": [" +
            "{" +
            "\"itemName\": \"Baby chicks\"," +
            "\"orderId\": \"123\"," +
            "\"orderStatus\": \"New\"," +
            "\"orderCost\": 50.00," +
            "\"deliveryDate\": \"2022-12-30\"," +
            "\"orderDate\": \"2022-12-07\"" +
            "}," +
            "{" +
            "\"itemName\": \"Heat lamps\"," +
            "\"orderId\": \"2\"," +
            "\"orderStatus\": \"New\"," +
            "\"orderCost\": 24.00," +
            "\"deliveryDate\": \"2022-12-30\"," +
            "\"orderDate\": \"2022-12-07\"" +
            "}," +
            "{" +
            "\"itemName\": \"Chicken roosts\"," +
            "\"orderId\": \"3\"," +
            "\"orderStatus\": \"New\"," +
            "\"orderCost\": 30.00," +
            "\"deliveryDate\": \"2022-12-30\"," +
            "\"orderDate\": \"2022-12-07\"" +
            "}" +
            "]," +
            "\"email\": \"happiest@example.com\"" +
            "}";


    /**
     * Generates a response based on the input prompt from the AI module.
     *
     * @param prompt the prompt to be used by the AI module
     * @return
     */

    @Override
    public String getGeneratedResponse(String prompt) {
//        String response = null;
//        try {
//            here we use an AWS endpoint but only with Get so only worked for one example, of course for proper solution a Post with parameters is needed:
//            response = getGeneratedResponse2(prompt);
//        } catch (Exception e) {
//            System.out.println("Error getting response from AWS LLAMA 2");
//        }
//        return response;


        // As we shut down the hosted LLM so I copied here the response to test it for invoice happy restaurant case.
        return jsonData;
    }

    public static String getGeneratedResponse2(String prompt) throws Exception {
            String response = getResponseFromAWSLLAMA2();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arrayNode = mapper.readTree(response);

            // Get the 'content' field which contains your JSON
            String jsonString = arrayNode.get(0).get("generation").get("content").asText();

            // Find the starting and ending curly braces to extract the JSON object
            int startIndex = jsonString.indexOf("{");
            int endIndex = jsonString.lastIndexOf("}") + 1;

            jsonString = jsonString.substring(startIndex, endIndex);

        return jsonString;
    }

    // This was the endpoint for the Lambda:
    private static final String ENDPOINT = "https://w59qnzefe4.execute-api.us-east-1.amazonaws.com/production/textgeneration-llama-2-7b";

    //Just for testing:
    public static void main(String[] args) {
        try {
            String response = getGeneratedResponse2("Hello, I am a chatbot. I am here to help you fill out a form. Please tell me your name.");
            System.out.println(response);
            String extractedJson = extractJsonFromTheText(response);
            System.out.println("Extracted JSON:");
            System.out.println(extractedJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getResponseFromAWSLLAMA2() throws Exception {
        // Append your prompt to the URL if the endpoint requires it
        // Assuming the prompt is added as a query parameter. Update if the endpoint has a different structure
        URL url = new URL(ENDPOINT);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
//        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setRequestProperty("Accept", "application/json");

        // Set timeouts
        int timeoutMillis = 30000; // 10 seconds
        conn.setConnectTimeout(timeoutMillis);
        conn.setReadTimeout(timeoutMillis);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();

        // Parse the response into a JSON object and retrieve the desired data.
        // Here I'm just returning the entire JSON string, you may need to extract specific fields.
        System.out.println(response);

        // If you need to extract a specific field, do it here.
        // For example: String result = jsonObject.getString("fieldName");

        return response.toString();
    }

    private static String extractJsonFromTheText(String text) {
        // 1. Identify the starting and ending points of the JSON in the text
        Pattern pattern = Pattern.compile("\\{[^\\}]*\\}");
        Matcher matcher = pattern.matcher(text);

        // 2. Extract the portion of the text that corresponds to the JSON
        if (matcher.find()) {
            String jsonString = matcher.group();

            // 3. Parse the extracted text as JSON to verify it is valid
            try {
                System.out.println(jsonString);  // Pretty print JSON
                return jsonString;
            } catch (Exception e) {
                System.out.println("Extracted text is not a valid JSON");
            }
        } else {
            System.out.println("No JSON found in the given text");
            return "";
        }
        return "";
    }
}
