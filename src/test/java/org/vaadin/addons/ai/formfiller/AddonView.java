package org.vaadin.addons.ai.formfiller;

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

import java.time.LocalDate;
import java.util.HashMap;

@Route("")
public class AddonView extends Div {

    FormLayout customerOrdersForm;

    public AddonView() {
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

        Grid<Order> orderGrid = new Grid<>(Order.class);
        orderGrid.removeAllColumns();
        orderGrid.addColumn(Order::getOrderId).setHeader("Order Id").setKey("orderId").setId("orderId");
        orderGrid.addColumn(Order::getItemName).setHeader("Item Name").setKey("itemName").setId("itemName");
        orderGrid.addColumn(Order::getOrderDate).setHeader("Order Date").setKey("orderDate").setId("orderDate");
        orderGrid.addColumn(Order::getOrderStatus).setHeader("Order Status").setKey("orderStatus").setId("orderStatus");
        orderGrid.addColumn(Order::getOrderTotal).setHeader("Order Cost").setKey("orderCost").setId("orderCost");
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
        Button fillButton = new Button("Fill Form From Input Text");
        fillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillButton.addClickListener(event -> {
            debugJsonTarget.setValue("");
            debugTypesTarget.setValue("");
            debugResponse.setValue("");
            String input = debugInput.getValue();
            if (input != null && !input.isEmpty()) {
                HashMap<Component, String> fieldsInstructions = new HashMap<>();
                fieldsInstructions.put(nameField, "Format this field in Uppercase");
                fieldsInstructions.put(orderGrid.getColumnByKey("orderDate"), "Format this field as a date with format yyyy/MM/dd");
                fieldsInstructions.put(orderGrid.getColumnByKey("orderId"), "Format this field as a string");
                fieldsInstructions.put(orderEntity, "To fill this field select one of these options \"Person\" or \"Company\" according to the entity who is generating the order.");
                fieldsInstructions.put(paymentMethod, "To fill this field select one of these options \"Credit Card\" or \"Cash\" or \"Paypal\" according to the payment method used.");
                fieldsInstructions.put(emailField, "Format this field as a correct email");
                fieldsInstructions.put(typeService, "To fill this field select none, one or more of these options \"Software\", \"Hardware\", \"Consultancy\" according to the items type included in the order.");

                FormFiller formFiller = new FormFiller(customerOrdersForm, fieldsInstructions);
                FormFillerResult result = formFiller.fill(input);
                debugPrompt.setValue(result.getRequest());
                debugJsonTarget.setValue(String.format("%s",formFiller.getMapping().componentsJSONMap()));
                debugTypesTarget.setValue(String.format("%s",formFiller.getMapping().componentsTypesJSONMap()));
                debugResponse.setValue(result.getResponse());
            }
        });

        Upload pdfDocument = new Upload();
        FileBuffer fileBuffer = new FileBuffer();
        pdfDocument.setReceiver(fileBuffer);

        Button fillDocumentButton = new Button("Fill Form From Document");
        fillDocumentButton.setEnabled(false);
        fillDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillDocumentButton.addClickListener(event -> {
//            debugJsonTarget.setValue("");
//            debugTypesTarget.setValue("");
//            debugResponse.setValue("");
//            String input = debugInput.getValue();
//            if (input != null && !input.isEmpty()) {
//                HashMap<Component, String> fieldsInstructions = new HashMap<>();
//                fieldsInstructions.put(nameField, "Format this field in Uppercase");
//                fieldsInstructions.put(orderGrid.getColumnByKey("orderDate"), "Format this field as a date with format yyyy/MM/dd");
//                fieldsInstructions.put(orderGrid.getColumnByKey("orderId"), "Format this field as a string");
//                fieldsInstructions.put(orderEntity, "To field this field select one of these options \"Person\" or \"Company\" according to the entity who is generating the order.");
//                fieldsInstructions.put(emailField, "Format this field as a correct email");
//
//                FormFiller formFiller = new FormFiller(customerOrdersForm, fieldsInstructions);
//                FormFillerResult result = formFiller.fill(input);
//                debugPrompt.setValue(result.getRequest());
//                debugJsonTarget.setValue(String.format("%s",formFiller.getMapping().componentsJSONMap()));
//                debugTypesTarget.setValue(String.format("%s",formFiller.getMapping().componentsTypesJSONMap()));
//                debugResponse.setValue(result.getResponse());
//            }
        });

        pdfDocument.addStartedListener(e ->
                fillDocumentButton.setEnabled(false));

        pdfDocument.addFinishedListener(e -> {
            String documentPath = fileBuffer.getFileData().getFile().getAbsolutePath();
            fillDocumentButton.setEnabled(true);
        });

        HorizontalLayout documentLayout = new HorizontalLayout(fillDocumentButton, pdfDocument);

        debugLayout.add(fillButton, documentLayout, dataLayout);
        add(debugLayout);

    }
}
