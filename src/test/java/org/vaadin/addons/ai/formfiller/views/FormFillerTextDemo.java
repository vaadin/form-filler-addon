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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.addons.ai.formfiller.FormFiller;
import org.vaadin.addons.ai.formfiller.FormFillerResult;
import org.vaadin.addons.ai.formfiller.data.OrderItem;
import org.vaadin.addons.ai.formfiller.utils.DebugTool;

import java.util.HashMap;

@Route("")
public class FormFillerTextDemo extends Div {

    FormLayout customerOrdersForm;

    public FormFillerTextDemo() {
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

        DebugTool dataLayout = new DebugTool();

        Button fillButton = new Button("Fill Form From Input Text");
        fillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillButton.addClickListener(event -> {
            dataLayout.getDebugJsonTarget().setValue("");
            dataLayout.getDebugTypesTarget().setValue("");
            dataLayout.getDebugResponse().setValue("");
            String input = dataLayout.getDebugInput().getValue();
            if (input != null && !input.isEmpty()) {
                HashMap<Component, String> fieldsInstructions = new HashMap<>();
                fieldsInstructions.put(nameField, "Format this field in Uppercase");
                fieldsInstructions.put(orderGrid.getColumnByKey("orderDate"), "Format this field as a date with format yyyy/MM/dd");
                fieldsInstructions.put(orderGrid.getColumnByKey("orderId"), "Format this field as a string");
//                fieldsInstructions.put(orderEntity, "To fill this field select one of these options \"Person\" or \"Company\" according to the entity who is generating the order");
//                fieldsInstructions.put(paymentMethod, "To fill this field select one of these options \"Credit Card\" or \"Cash\" or \"Paypal\" according to the payment method used");
                fieldsInstructions.put(emailField, "Format this field as a correct email");
//                fieldsInstructions.put(typeService, "To fill this field select none, one or more of these options \"Software\", \"Hardware\", \"Consultancy\" according to the items type included in the order");

                FormFiller formFiller = new FormFiller(customerOrdersForm, fieldsInstructions);
                FormFillerResult result = formFiller.fill(input);
                dataLayout.getDebugPrompt().setValue(result.getRequest());
                dataLayout.getDebugJsonTarget().setValue(String.format("%s",formFiller.getMapping().componentsJSONMap()));
                dataLayout.getDebugTypesTarget().setValue(String.format("%s",formFiller.getMapping().componentsTypesJSONMap()));
                dataLayout.getDebugResponse().setValue(result.getResponse());
            }
        });

        debugLayout.add(fillButton, dataLayout);
        add(debugLayout);

    }
}
