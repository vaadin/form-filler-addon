package org.vaadin.addons.ai.formfiller.utils;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.ai.formfiller.FormFiller;
import com.google.protobuf.util.JsonFormat;

public class JsonToAnnotateImageResponseConverter {

    private static final Logger logger = LoggerFactory.getLogger(FormFiller.class);

    public static AnnotateImageResponse convertToAnnotateImageResponse(JsonObject json) throws InvalidProtocolBufferException {
        AnnotateImageResponse.Builder builder = AnnotateImageResponse.newBuilder();

        JsonObject response = json.getAsJsonArray("responses").get(0).getAsJsonObject();

        try {
            JsonFormat.parser().merge(response.toString(), builder);
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error while parsing the JSON into AnnotateImageResponse.", e);
        }

        return builder.build();
    }
}
