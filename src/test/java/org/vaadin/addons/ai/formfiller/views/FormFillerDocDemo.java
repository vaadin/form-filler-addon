package org.vaadin.addons.ai.formfiller.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import org.vaadin.addons.ai.formfiller.FormFiller;
import org.vaadin.addons.ai.formfiller.FormFillerResult;
import org.vaadin.addons.ai.formfiller.data.OrderItem;
import org.vaadin.addons.ai.formfiller.utils.OCRUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

@Route("doc")
public class FormFillerDocDemo extends Div {

    FormLayout customerOrdersForm;

    public FormFillerDocDemo() {
        customerOrdersForm = new FormLayout();

        TextField nameField = new TextField("Name");
        nameField.setId("name");
        customerOrdersForm.add(nameField);

        TextField addressField = new TextField("Address");
        addressField.setId("address");
        customerOrdersForm.add(addressField);

        TextField phoneField = new TextField("Phone");
        phoneField.setId("phone");
        customerOrdersForm.add(phoneField);

        TextField emailField = new TextField("Email");
        emailField.setId("email");
        customerOrdersForm.add(emailField);

        DateTimePicker dateCreationField = new DateTimePicker("Creation Date");
        dateCreationField.setId("creationDate");
        customerOrdersForm.add(dateCreationField);

        DatePicker dueDateField = new DatePicker("Due Date");
        dueDateField.setId("dueDate");
        customerOrdersForm.add(dueDateField);

        ComboBox<String> orderEntity = new ComboBox<>("Order Entity");
        orderEntity.setId("orderEntity");
        orderEntity.setItems("Person", "Company");
        customerOrdersForm.add(orderEntity);

        NumberField orderTotal = new NumberField("Order Total");
        orderTotal.setId("orderTotal");
        customerOrdersForm.add(orderTotal);

        TextArea orderDescription = new TextArea("Order Description");
        orderDescription.setId("orderDescription");
        customerOrdersForm.add(orderDescription);

        RadioButtonGroup<String> paymentMethod = new RadioButtonGroup<>("Payment Method");
        paymentMethod.setItems("Credit Card", "Cash", "Paypal");
        paymentMethod.setId("paymentMethod");
        customerOrdersForm.add(paymentMethod);

        Checkbox isFinnishCustomer = new Checkbox("Is Finnish Customer");
        isFinnishCustomer.setId("isFinnishCustomer");
        customerOrdersForm.add(isFinnishCustomer);

        CheckboxGroup<String> typeService = new CheckboxGroup<>("Type of Service");
        typeService.setItems("Software", "Hardware", "Consultancy");
        typeService.setId("typeService");
        customerOrdersForm.add(typeService);

        Grid<OrderItem> orderGrid = new Grid<>(OrderItem.class);
        orderGrid.removeAllColumns();
        orderGrid.addColumn(OrderItem::getOrderId).setHeader("Order Id").setKey("orderId").setId("orderId");
        orderGrid.addColumn(OrderItem::getItemName).setHeader("Item Name").setKey("itemName").setId("itemName");
        orderGrid.addColumn(OrderItem::getOrderDate).setHeader("Order Date").setKey("orderDate").setId("orderDate");
        orderGrid.addColumn(OrderItem::getOrderStatus).setHeader("Order Status").setKey("orderStatus").setId("orderStatus");
        orderGrid.addColumn(OrderItem::getOrderTotal).setHeader("Order Cost").setKey("orderCost").setId("orderCost");
        orderGrid.setId("orders");

        customerOrdersForm.add(orderGrid);

        add(customerOrdersForm);

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
                    fieldsInstructions.put(nameField, "Format this field in Uppercase");
                    fieldsInstructions.put(emailField, "Format this field as a correct email");

                    FormFiller formFiller = new FormFiller(customerOrdersForm, fieldsInstructions);
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
        });

        HorizontalLayout documentLayout = new HorizontalLayout(fillDocumentButton, pdfDocument);

        debugLayout.add(documentLayout, dataLayout);
        add(debugLayout);

    }
}
