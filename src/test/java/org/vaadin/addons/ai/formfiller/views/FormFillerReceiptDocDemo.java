package org.vaadin.addons.ai.formfiller.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.addons.ai.formfiller.FormFiller;
import org.vaadin.addons.ai.formfiller.FormFillerResult;
import org.vaadin.addons.ai.formfiller.data.ReceiptItem;
import org.vaadin.addons.ai.formfiller.utils.ComponentUtils;
import org.vaadin.addons.ai.formfiller.utils.DebugTool;
import org.vaadin.addons.ai.formfiller.utils.ExtraInstructionsTool;
import org.vaadin.addons.ai.formfiller.utils.OCRUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

@Route("receipt")
public class FormFillerReceiptDocDemo extends Div {

    FormLayout receiptForm;

    Image preview = new Image();
    Upload imageFile = new Upload();
    Notification imageNotification = new Notification("Please select a file to upload or a predefined image");
    FileBuffer fileBuffer = new FileBuffer();

    ExtraInstructionsTool extraInstructionsTool = new ExtraInstructionsTool();

    public FormFillerReceiptDocDemo() {
        imageNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        imageNotification.setDuration(2000);
        imageNotification.setPosition(Notification.Position.MIDDLE);

        receiptForm = new FormLayout();

        TextField nameField = new TextField("Store Name");
        nameField.setId("storeName");
        receiptForm.add(nameField);

        TextField addressField = new TextField("Store Address");
        addressField.setId("storeAddress");
        receiptForm.add(addressField);

        DateTimePicker dateCreationField = new DateTimePicker("Receipt Date");
        dateCreationField.setId("receiptDate");
        receiptForm.add(dateCreationField);

        TextArea orderDescription = new TextArea("Receipt Description");
        orderDescription.setId("receiptDescription");
        receiptForm.add(orderDescription);

        RadioButtonGroup<String> paymentMethod = new RadioButtonGroup<>("Payment Method");
        paymentMethod.setItems("Credit Card", "Cash", "Paypal");
        paymentMethod.setId("paymentMethod");
        receiptForm.add(paymentMethod);

        TextField typeService = new TextField("Type of Service");
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

        add(receiptForm);

        VerticalLayout debugLayout = new VerticalLayout();
        debugLayout.setWidthFull();

        DebugTool debugTool = new DebugTool();

        imageFile.setReceiver(fileBuffer);

        ComboBox<String> images = new ComboBox<>("Select Image");
        images.setItems("Load my receipt...", "Receipt1", "Receipt2", "Receipt3", "Receipt4", "Receipt5", "Receipt6", "Receipt7", "Receipt8", "Receipt_HU_1");
        images.setValue("Load my receipt...");
        images.setAllowCustomValue(false);
        enableFileUpload();
        images.addValueChangeListener(e -> {
            if (e.getValue().equalsIgnoreCase("Load my receipt...")) {
                enableFileUpload();
            } else {
                imageFile.setVisible(false);
                StreamResource imageResource = new StreamResource(e.getValue()+".png",
                        () -> getClass().getResourceAsStream("/receipts/"+e.getValue()+".png"));
                preview.setSrc(imageResource);
            }
        });


        Button fillDocumentButton = new Button("Fill Form From Document");
        fillDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillDocumentButton.addClickListener(event -> {
            if (images.getValue().equalsIgnoreCase("Load my receipt..."))
            {
                if (fileBuffer.getFileData() == null) {
                    imageNotification.open();
                    return;
                }
            }
            try {
                FileInputStream fileInputStream = null;
                if (images.getValue().equalsIgnoreCase("Load my receipt...")) {
                    fileInputStream = new FileInputStream(fileBuffer.getFileData().getFile().getAbsolutePath());
                } else {
                    fileInputStream = new FileInputStream(getClass().getResource("/receipts/"+images.getValue()+".png").getFile());
                }
                debugTool.getDebugJsonTarget().setValue("");
                debugTool.getDebugTypesTarget().setValue("");
                debugTool.getDebugResponse().setValue("");
                String input = OCRUtils.getOCRText(fileInputStream);
                debugTool.getDebugInput().setValue(input);
                if (input != null && !input.isEmpty()) {
                    HashMap<Component, String> fieldsInstructions = new HashMap<>();
                    for (Component c : extraInstructionsTool.getExtraInstructions().keySet()) {
                        if (extraInstructionsTool.getExtraInstructions().get(c).getValue() != null && !extraInstructionsTool.getExtraInstructions().get(c).getValue().isEmpty())
                            fieldsInstructions.put(c, extraInstructionsTool.getExtraInstructions().get(c).getValue());
                    }
                    clearForm();
                    FormFiller formFiller = new FormFiller(receiptForm, fieldsInstructions);
                    FormFillerResult result = formFiller.fill(input);
                    debugTool.getDebugPrompt().setValue(result.getRequest());
                    debugTool.getDebugJsonTarget().setValue(String.format("%s", formFiller.getMapping().componentsJSONMap()));
                    debugTool.getDebugTypesTarget().setValue(String.format("%s", formFiller.getMapping().componentsTypesJSONMap()));
                    debugTool.getDebugResponse().setValue(result.getResponse());
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        imageFile.addStartedListener(e ->
                fillDocumentButton.setEnabled(false));

        imageFile.addFinishedListener(e -> {
            String documentPath = fileBuffer.getFileData().getFile().getAbsolutePath();
            fillDocumentButton.setEnabled(true);
            preview.setSrc(new StreamResource("img.png", new InputStreamFactory() {
                @Override
                public InputStream createInputStream() {
                    try {
                        return new FileInputStream(fileBuffer.getFileData().getFile());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }));
        });

        extraInstructionsTool.setComponents(ComponentUtils.getComponentInfo(receiptForm));
        extraInstructionsTool.setVisible(false);
        extraInstructionsTool.setExtraInstructions(nameField, "This is the name of the store");
        extraInstructionsTool.setExtraInstructions(receiptGrid.getColumnByKey("itemCost"), "This is the cost or price of the item");

        Button extraInstructionsButton = new Button("Show/Hide extra instructions");
        extraInstructionsButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        extraInstructionsButton.addClickListener(e -> {
            extraInstructionsTool.setVisible(!extraInstructionsTool.isVisible());
        });

        HorizontalLayout imagesLayout = new HorizontalLayout(images, imageFile, preview);
        VerticalLayout documentLayout = new VerticalLayout(fillDocumentButton, extraInstructionsButton, extraInstructionsTool, imagesLayout);
        debugLayout.add(documentLayout, debugTool);
        add(debugLayout);

    }

    private void enableFileUpload() {
        imageFile.setVisible(true);
        preview.setSrc("");
        imageFile.clearFileList();
        fileBuffer = new FileBuffer();
        imageFile.setReceiver(fileBuffer);
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
}
