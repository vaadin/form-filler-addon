package com.vaadin.flow.ai.formfiller.views;

import com.vaadin.flow.ai.formfiller.FormFiller;
import com.vaadin.flow.ai.formfiller.FormFillerResult;
import com.vaadin.flow.ai.formfiller.data.ReceiptItem;
import com.vaadin.flow.ai.formfiller.utils.OCRUtils;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.server.StreamResource;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("camera")
public class FormFillerCameraDemo extends Div {

    private static final Logger logger = LoggerFactory.getLogger(FormFillerCameraDemo.class);

    private FormLayout receiptForm;
    private Component previousPhoto;
    private Paragraph photoName;

    private FileBuffer fileBuffer = new FileBuffer();

    private Image image = new Image();

    public FormFillerCameraDemo() {
        createLayout();
        add(receiptForm);

        Upload upload = new Upload(fileBuffer);
        upload.setAcceptedFileTypes("image/*");
        // You can use the capture html5 attribute
        // https://caniuse.com/html-media-capture
        upload.getElement().setAttribute("capture", "environment");
        // If you don't compress the image, don't forget to increase the upload limit
        // and request size, or you will have an error
        // For a spring boot application the default request size is 10MB
        // and the default upload size is 1MB
        // you can set it in application.properties:
        // spring.servlet.multipart.max-request-size=30MB
        // spring.servlet.multipart.max-file-size=30MB
        Div output = new Div();

        upload.addSucceededListener(
                event -> {
                    Component component = createComponent(
                            event.getMIMEType(),
                            event.getFileName(),
                            fileBuffer.getInputStream()
                    );
                    showOutput(event.getFileName(), component, output);
                }
        );

        add(upload, output);
    }

    private Component createComponent(String mimeType, String fileName, InputStream stream) {
        if (mimeType.startsWith("image")) {
            try {
                byte[] bytes = IOUtils.toByteArray(stream);
                image
                        .getElement()
                        .setAttribute("src", new StreamResource(fileName, () -> new ByteArrayInputStream(bytes)));
                try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                    final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        try {
                            reader.setInput(in);
                            image.setMaxWidth("100%");
                        } finally {
                            reader.dispose();
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading image", e);
            }

            return image;
        }
        Div content = new Div();
        String text = String.format(
                "Mime type: '%s'\nSHA-256 hash: '%s'",
                mimeType,
                Arrays.toString(MessageDigestUtil.sha256(stream.toString()))
        );
        content.setText(text);
        return content;
    }

    private void createLayout() {
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.LARGE);

        receiptForm = new FormLayout();

        com.vaadin.flow.component.textfield.TextField nameField = new com.vaadin.flow.component.textfield.TextField("Store Name");
        nameField.setId("storeName");
        receiptForm.add(nameField);

        com.vaadin.flow.component.textfield.TextField addressField = new com.vaadin.flow.component.textfield.TextField("Store Address");
        addressField.setId("storeAddress");
        receiptForm.add(addressField);

        com.vaadin.flow.component.textfield.TextField phoneField = new com.vaadin.flow.component.textfield.TextField("Phone Number");
        addressField.setId("phoneNumber");
        receiptForm.add(phoneField);

        com.vaadin.flow.component.textfield.TextField websiteOrEmailField = new com.vaadin.flow.component.textfield.TextField("Website or Email");
        addressField.setId("websiteOrEmail");
        receiptForm.add(websiteOrEmailField);

        DateTimePicker dateCreationField = new DateTimePicker("Receipt Date");
        dateCreationField.setId("receiptDate");
        receiptForm.add(dateCreationField);

        com.vaadin.flow.component.textfield.TextArea orderDescription = new com.vaadin.flow.component.textfield.TextArea("Receipt Description");
        orderDescription.setId("receiptDescription");
        receiptForm.add(orderDescription);

        RadioButtonGroup<String> paymentMethod = new RadioButtonGroup<>("Payment Method");
        paymentMethod.setItems("Credit Card", "Cash", "Paypal");
        paymentMethod.setId("paymentMethod");
        receiptForm.add(paymentMethod);

        com.vaadin.flow.component.textfield.TextField typeService = new TextField("Type of Service");
        typeService.setId("typeService");
        receiptForm.add(typeService);

        Grid<ReceiptItem> receiptGrid = new Grid<>(ReceiptItem.class);
        receiptGrid.removeAllColumns();
        receiptGrid.addColumn(ReceiptItem::getItemQuantity).setHeader("Quantity").setKey("itemQuantity").setId("itemQuantity");
        receiptGrid.addColumn(ReceiptItem::getItemDescription).setHeader("Description").setKey("itemDescription").setId("itemDescription");
        receiptGrid.addColumn(ReceiptItem::getItemCost).setHeader("Cost").setKey("itemCost").setId("itemCost");
        receiptGrid.setId("receiptItems");

        receiptForm.add(receiptGrid);

        NumberField subtotalService = new NumberField("Subtotal Service");
        subtotalService.setId("subtotalCost");
        receiptForm.add(subtotalService);

        NumberField taxService = new NumberField("Taxes");
        taxService.setId("taxesCost");
        receiptForm.add(taxService);

        NumberField totalService = new NumberField("Total Service");
        totalService.setId("totalCost");
        receiptForm.add(totalService);

        receiptForm.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new FormLayout.ResponsiveStep("500px", 2));
        // Stretch the username field over 2 columns
        receiptForm.setColspan(receiptGrid, 2);

        Button fillDocumentButton = new Button("Fill Form From Document");
        fillDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillDocumentButton.addClickListener(event -> {
            try {
                clearForm();
                FileInputStream fileInputStream = null;
                fileInputStream = new FileInputStream(fileBuffer.getFileData().getFile().getAbsolutePath());

                String input = OCRUtils.getOCRText(fileInputStream);
                if (input != null && !input.isEmpty()) {
                    FormFiller formFiller = new FormFiller(receiptForm);
                    FormFillerResult result = formFiller.fill(input);
                }
            } catch (FileNotFoundException ex) {
                logger.error("Error reading file", ex);
            }
        });
        receiptForm.add(fillDocumentButton);
    }

    private void clearForm() {
        receiptForm.getChildren().forEach(component -> {
            if (component instanceof HasValue<?, ?>) {
                ((HasValue) component).clear();
            } else if (component instanceof Grid) {
                ((Grid) component).setItems(new ArrayList<>());
            }
        });
    }

    private void showOutput(String text, Component content, HasComponents outputContainer) {
        if (photoName != null) {
            outputContainer.remove(photoName);
        }
        if (previousPhoto != null) {
            outputContainer.remove(previousPhoto);
        }
        photoName = new Paragraph(text);
        outputContainer.add(photoName);
        previousPhoto = content;
        outputContainer.add(previousPhoto);
    }
}
