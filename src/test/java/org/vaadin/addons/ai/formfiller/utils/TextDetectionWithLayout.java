//package org.vaadin.addons.ai.formfiller.utils;
//
//import com.google.cloud.vision.v1.*;
//import com.google.protobuf.ByteString;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//public class TextDetectionWithLayout {
//
//    public static void main(String[] args) {
//        String imagePath = "path/to/your/image.jpg"; // Replace this with the path to your image
//
//        try {
//            List<EntityAnnotation> layout = detectTextWithLayout(imagePath);
//            List<TextAnnotation> extractedTextAndLayout = extractTextAndLayout(layout);
//            for (TextAnnotation annotation : extractedTextAndLayout) {
//                System.out.println("Text: " + annotation.getText());
//                System.out.println("Bounding Box Vertices: " + annotation.getBoundingPoly().getVerticesList());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static List<EntityAnnotation> detectTextWithLayout(String imagePath) throws IOException {
//        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
//            Path path = Paths.get(imagePath);
//            byte[] data = Files.readAllBytes(path);
//            Image image = Image.newBuilder().setContent(ByteString.copyFrom(data)).build();
//
//            Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
//            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
//                    .addFeatures(feature)
//                    .setImage(image)
//                    .build();
//
//            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
//            return response.getResponses(0).getTextAnnotationsList();
//        }
//    }
//
//    private static List<TextAnnotation> extractTextAndLayout(List<EntityAnnotation> layout) {
////         Process the layout information and extract text and bounding box vertices
////         You can iterate through the layout elements and extract the required information
////         as shown in the previous example for Python.
////         The TextAnnotation class in the Java client library has similar methods to
////         get text and bounding box information.
////
////         For example:
//         List<TextAnnotation> result = new ArrayList<>();
//         for (EntityAnnotation block : layout) {
////             for (EntityAnnotation paragraph : block.getParagraphsList()) {
////                 for (EntityAnnotation word : paragraph.getWordsList()) {
////                     String wordText = word.getDescription();
////                     List<Vertex> vertices = word.getBoundingPoly().getVerticesList();
////                     result.add(new TextAnnotation(wordText, vertices));
////                 }
////                 result.add(new TextAnnotation("\n", null));
////             }
//         }
//         return result;
//
////         Note: The exact implementation may vary based on the structure of the Java client library
////         for the Google Cloud Vision API.
//        return null; // Replace this with the actual implementation
//    }
//
//    // You can create a custom class to store text and layout information
//    // For example:
//     static class TextAnnotation {
//         private final String text;
//         private final List<Vertex> boundingBoxVertices;
//
//         public TextAnnotation(String text, List<Vertex> boundingBoxVertices) {
//             this.text = text;
//             this.boundingBoxVertices = boundingBoxVertices;
//         }
//
//         public String getText() {
//             return text;
//         }
//
//         public List<Vertex> getBoundingBoxVertices() {
//             return boundingBoxVertices;
//         }
//     }
//}
//
