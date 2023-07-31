package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

@Route("receipt")
public class FormFillerReceiptDocDemo extends Div {

    FormLayout receiptForm;

    Image preview = new Image();

    public FormFillerReceiptDocDemo() {
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
        FormLayout dataLayout = new FormLayout();
        dataLayout.setWidthFull();

        TextArea debugInput = new TextArea("Debug Input Source");
        debugInput.setWidthFull();
        debugInput.setHeight("600px");
        debugInput.setEnabled(false);

        TextArea debugJsonTarget = new TextArea("Debug JSON target");
        debugJsonTarget.setWidthFull();
        debugJsonTarget.setHeight("600px");

        TextArea debugTypesTarget = new TextArea("Debug Type target");
        debugTypesTarget.setWidthFull();
        debugTypesTarget.setHeight("600px");

        TextArea debugPrompt = new TextArea("Debug Prompt");
        debugPrompt.setWidthFull();
        debugPrompt.setHeight("600px");

        TextArea debugResponse = new TextArea("Debug Response");
        debugResponse.setWidthFull();
        debugResponse.setHeight("600px");

        dataLayout.add(debugInput, debugJsonTarget, debugTypesTarget, debugPrompt, debugResponse);

        Upload pdfDocument = new Upload();
        FileBuffer fileBuffer = new FileBuffer();
        pdfDocument.setReceiver(fileBuffer);

        Button fillDocumentButton = new Button("Fill Form From Document");
        fillDocumentButton.setEnabled(false);
        fillDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillDocumentButton.addClickListener(event -> {
            try {

                debugJsonTarget.setValue("");
                debugTypesTarget.setValue("");
                debugResponse.setValue("");
                String input = OCRUtils.getOCRText(new FileInputStream(fileBuffer.getFileData().getFile().getAbsolutePath()));
                debugInput.setValue(input);
                if (input != null && !input.isEmpty()) {
                    HashMap<Component, String> fieldsInstructions = new HashMap<>();
                    fieldsInstructions.put(nameField, "This is the name of the store");
                    fieldsInstructions.put(receiptGrid.getColumnByKey("itemCost"), "This is the cost or price of the item");
                    FormFiller formFiller = new FormFiller(receiptForm, fieldsInstructions);
                    FormFillerResult result = formFiller.fill(input);
                    debugPrompt.setValue(result.getRequest());
                    debugJsonTarget.setValue(String.format("%s", formFiller.getMapping().componentsJSONMap()));
                    debugTypesTarget.setValue(String.format("%s", formFiller.getMapping().componentsTypesJSONMap()));
                    debugResponse.setValue(result.getResponse());
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        pdfDocument.addStartedListener(e ->
                fillDocumentButton.setEnabled(false));

        pdfDocument.addFinishedListener(e -> {
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

        HorizontalLayout documentLayout = new HorizontalLayout(fillDocumentButton, pdfDocument, preview);

        debugLayout.add(documentLayout, dataLayout);
        add(debugLayout);

    }

    private void clearForm() {
        receiptForm.getChildren().forEach(component -> {
            if (component instanceof HasValue<?,?>) {
                ((HasValue) component).clear();
            } else if (component instanceof Grid) {
                ((Grid) component).setItems(new ArrayList<>());
            }
        });
    }
}
