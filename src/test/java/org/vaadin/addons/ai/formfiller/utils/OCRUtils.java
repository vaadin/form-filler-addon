package org.vaadin.addons.ai.formfiller.utils;

import com.google.cloud.vision.v1.*;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        if (GOOGLE_VISION_API_KEY != null) {
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

            AnnotateImageResponse annotateImageResponse = detectText(imageStream);

            URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=" + apikey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Create the JSON request payload with the Base64 image data
            String payload = "{"
                    + "\"requests\":[{"
                    + "\"image\":{\"content\":\"" + base64Image + "\"},"
                    + "\"features\":[{\"type\":\"DOCUMENT_TEXT_DETECTION\"}]"
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
            System.out.println("#########################");
            System.out.println("RAW JSON RESPONSE:");
            System.out.println("#########################");
            System.out.println(response.toString());

            JsonParser jsonParser = new JsonParser();
            JsonObject responseJson = jsonParser.parse(response.toString()).getAsJsonObject();

//            JsonObject properJsonObject = responseJson.get("responses").getAsJsonArray().get(0).getAsJsonObject();
//            AnnotateImageResponse annotateImageResponse = new Gson().fromJson(properJsonObject, AnnotateImageResponse.class);
            GoogleVisionLineSegmentationParser googleVisionLineSegmentationParser = new GoogleVisionLineSegmentationParser();
            googleVisionLineSegmentationParser.initLineSegmentation(annotateImageResponse);


            // Extract the 'text' field from the JSON response
            JsonArray responsesArray = responseJson.getAsJsonArray("responses");
            JsonObject firstResponse = responsesArray.get(0).getAsJsonObject();
            JsonObject fullTextAnnotation = firstResponse.getAsJsonObject("fullTextAnnotation");
            String extractedText = fullTextAnnotation.get("text").getAsString();

            System.out.println("#########################");
            System.out.println("Extracted text:");
            System.out.println("#########################");
            System.out.println(extractedText);
            return extractedText;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static AnnotateImageResponse detectText(InputStream inputStream) throws Exception {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(inputStream);

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.println("Error: " + res.getError().getMessage());
                    return null;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    System.out.println("Text: " + annotation.getDescription());
                    System.out.println("Position: " + annotation.getBoundingPoly());
                    return res;
                }
            }
        }
        return null;
    }
}
