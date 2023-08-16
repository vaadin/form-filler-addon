package com.vaadin.flow.ai.formfiller.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OCRUtils {
    private static final Logger logger = LoggerFactory.getLogger(OCRUtils.class);

    private static String GOOGLE_VISION_API_KEY;

    static {

        // read apiKey from -D param variable
        GOOGLE_VISION_API_KEY = System.getProperty("GOOGLE_VISION_API_KEY");
        if (GOOGLE_VISION_API_KEY == null) {
            // read apiKey from environment variable
            GOOGLE_VISION_API_KEY = System.getenv("GOOGLE_VISION_API_KEY");
        }
        if(GOOGLE_VISION_API_KEY != null) {
            logger.info("GOOGLE_VISION_API_KEY was filled properly");
        } else {
            logger.error("GOOGLE_VISION_API_KEY was not filled properly");
        }
    }

    public static String getGoogleVisionApiKey() {
        return GOOGLE_VISION_API_KEY;
    }

    public static String getOCRText(InputStream inputStream) {

        String GOOGLE_VISION_API_KEY = getGoogleVisionApiKey();

        String apikey = GOOGLE_VISION_API_KEY;

        try {
            // Read the image data from the InputStream into a byte array
            // Replace the following lines with your own InputStream retrieval logic
            InputStream imageStream = inputStream;  // Get the InputStream for the image
            byte[] imageData = IOUtils.toByteArray(imageStream);

            // Convert the image data to Base64
            String base64Image = Base64.getEncoder().encodeToString(imageData);

            URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=" + apikey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Create the JSON request payload with the Base64 image data
            String payload = "{"
                    + "\"requests\":[{"
                    + "\"image\":{\"content\":\"" + base64Image + "\"},"
                    + "\"features\":[{\"type\":\"TEXT_DETECTION\"}]"
                    + "}]"
                    + "}";

            // Send the request
            conn.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Process the response
//            System.out.println("#########################");
//            System.out.println("RAW JSON RESPONSE:");
//            System.out.println("#########################");
//            System.out.println(response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            String extractedText = "";

            JsonNode responseJson = objectMapper.readTree(response.toString());
            JsonNode responsesArray = responseJson.get("responses");
            if (responsesArray != null && responsesArray.isArray() && !responsesArray.isEmpty()) {
                JsonNode firstResponse = responsesArray.get(0);
                JsonNode fullTextAnnotation = firstResponse.get("fullTextAnnotation");
                if (fullTextAnnotation != null) {
                    extractedText = fullTextAnnotation.get("text").asText();
                }
            }

            System.out.println("#########################");
            System.out.println("Extracted text:");
            System.out.println("#########################");
            System.out.println(extractedText);
            return extractedText;
        } catch (IOException e) {
            logger.error("Error while reading image", e);
        }
        return "";
    }
}
