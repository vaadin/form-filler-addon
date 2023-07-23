package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

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

        DatePicker dateCreationField = new DatePicker("Creation Date");
        dateCreationField.setValue(LocalDate.now());
        dateCreationField.setId("creationDate");
        customerOrdersForm.add(dateCreationField);

        ComboBox<String> orderEntity = new ComboBox<>("Order Entity");
        orderEntity.setItems("Person", "Company");
        customerOrdersForm.add(orderEntity);

        NumberField orderTotal = new NumberField("Order Total");
        orderTotal.setId("orderTotal");
        customerOrdersForm.add(orderTotal);

        TextArea orderDescription = new TextArea("Order Description");
        orderDescription.setId("orderDescription");
        customerOrdersForm.add(orderDescription);

        Grid<Order> orderGrid = new Grid<>(Order.class);
        orderGrid.removeAllColumns();
        orderGrid.addColumn(Order::getOrderId).setHeader("Order Id").setKey("orderId").setId("orderId");
        orderGrid.addColumn(Order::getItemName).setHeader("Item Name").setKey("itemName").setId("itemName");
        orderGrid.addColumn(Order::getOrderDate).setHeader("Order Date").setKey("orderDate").setId("orderDate");
        orderGrid.addColumn(Order::getOrderStatus).setHeader("Order Status").setKey("orderStatus").setId("orderStatus");
        orderGrid.addColumn(Order::getOrderTotal).setHeader("Order Total").setKey("orderTotal").setId("orderTotal");
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
        Button fillButton = new Button("Fill Form");
        fillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillButton.addClickListener(event -> {
            debugJsonTarget.setValue("");
            debugTypesTarget.setValue("");
            debugResponse.setValue("");
            String input = debugInput.getValue();
            if (input != null && !input.isEmpty()) {
                FormFiller formFiller = new FormFiller(customerOrdersForm);
                FormFillerResult result = formFiller.fill(input);
                debugJsonTarget.setValue(String.format("%s",formFiller.getMapping().componentsJSONMap()));
                debugTypesTarget.setValue(String.format("%s",formFiller.getMapping().componentsTypesJSONMap()));
                debugResponse.setValue(result.getResponse());
            }
        });

        debugLayout.add(fillButton,dataLayout);
        add(debugLayout);

    }
}
