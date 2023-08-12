package org.vaadin.addons.ai.formfiller.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.addons.ai.formfiller.FormFiller;
import org.vaadin.addons.ai.formfiller.FormFillerResult;
import org.vaadin.addons.ai.formfiller.data.OrderItem;
import org.vaadin.addons.ai.formfiller.utils.ComponentUtils;
import org.vaadin.addons.ai.formfiller.utils.DebugTool;
import org.vaadin.addons.ai.formfiller.utils.ExtraInstructionsTool;

import java.util.ArrayList;
import java.util.HashMap;

@Route("")
public class FormFillerTextDemo extends Div {

    FormLayout formLayout;

    ExtraInstructionsTool extraInstructionsTool = new ExtraInstructionsTool();

    public FormFillerTextDemo() {
        formLayout = new FormLayout();

        TextField nameField = new TextField("Name");
        nameField.setId("name");
        formLayout.add(nameField);

        TextField addressField = new TextField("Address");
        addressField.setId("address");
        formLayout.add(addressField);

        TextField phoneField = new TextField("Phone");
        phoneField.setId("phone");
        formLayout.add(phoneField);

        IntegerField age = new IntegerField("age");
        age.setId("age");
        formLayout.add(age);

        EmailField emailField = new EmailField("Email");
        emailField.setId("email");
        formLayout.add(emailField);

        PasswordField clientId = new PasswordField("Client Id");
        clientId.setId("clientId");
        formLayout.add(clientId);

        DateTimePicker dateCreationField = new DateTimePicker("Creation Date");
        dateCreationField.setId("creationDate");
        formLayout.add(dateCreationField);

        DatePicker dueDateField = new DatePicker("Due Date");
        dueDateField.setId("dueDate");
        formLayout.add(dueDateField);

        ComboBox<String> orderEntity = new ComboBox<>("Order Entity");
        orderEntity.setId("orderEntity");
        orderEntity.setItems("Person", "Company");
        formLayout.add(orderEntity);

        NumberField orderTotal = new NumberField("Order Total");
        orderTotal.setId("orderTotal");
        formLayout.add(orderTotal);

        BigDecimalField orderTaxes = new BigDecimalField("Order Taxes");
        orderTaxes.setId("orderTaxes");
        formLayout.add(orderTaxes);

        TextArea orderDescription = new TextArea("Order Description");
        orderDescription.setId("orderDescription");
        formLayout.add(orderDescription);

        RadioButtonGroup<String> paymentMethod = new RadioButtonGroup<>("Payment Method");
        paymentMethod.setItems("Credit Card", "Cash", "Paypal");
        paymentMethod.setId("paymentMethod");
        formLayout.add(paymentMethod);

        Checkbox isFinnishCustomer = new Checkbox("Is Finnish Customer");
        isFinnishCustomer.setId("isFinnishCustomer");
        formLayout.add(isFinnishCustomer);

        CheckboxGroup<String> typeService = new CheckboxGroup<>("Type of Service");
        typeService.setItems("Software", "Hardware", "Consultancy");
        typeService.setId("typeService");
        formLayout.add(typeService);

        Grid<OrderItem> orderGrid = new Grid<>(OrderItem.class);
        orderGrid.removeAllColumns();
        orderGrid.addColumn(OrderItem::getOrderId).setHeader("Order Id").setKey("orderId").setId("orderId");
        orderGrid.addColumn(OrderItem::getItemName).setHeader("Item Name").setKey("itemName").setId("itemName");
        orderGrid.addColumn(OrderItem::getOrderDate).setHeader("Order Date").setKey("orderDate").setId("orderDate");
        orderGrid.addColumn(OrderItem::getOrderStatus).setHeader("Order Status").setKey("orderStatus").setId("orderStatus");
        orderGrid.addColumn(OrderItem::getOrderTotal).setHeader("Order Cost").setKey("orderCost").setId("orderCost");
        orderGrid.setId("orders");

        formLayout.add(orderGrid);

        formLayout.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new FormLayout.ResponsiveStep("500px", 2));
        // Stretch the username field over 2 columns
        formLayout.setColspan(orderGrid, 2);

        add(formLayout);

        VerticalLayout debugLayout = new VerticalLayout();
        debugLayout.setWidthFull();

        DebugTool debugTool = new DebugTool();

        ComboBox<String> texts = new ComboBox<>("Select a text or just type your own <br>in the debug Input Source field");
        texts.setItems("Text1", "Text2", "Text3");
        texts.setValue("Text1");
        texts.setAllowCustomValue(false);
        debugTool.getDebugInput().setValue(getExampleTexts().get("Text1"));
        texts.addValueChangeListener(e -> {
            debugTool.getDebugInput().setValue(getExampleTexts().get(texts.getValue()));
        });

        Button fillButton = new Button("Fill Form From Input Text");
        fillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillButton.addClickListener(event -> {
            debugTool.getDebugJsonTarget().setValue("");
            debugTool.getDebugTypesTarget().setValue("");
            debugTool.getDebugResponse().setValue("");
            String input = debugTool.getDebugInput().getValue();
            if (input != null && !input.isEmpty()) {
                HashMap<Component, String> fieldsInstructions = new HashMap<>();

                for (Component c : extraInstructionsTool.getExtraInstructions().keySet()) {
                    if (extraInstructionsTool.getExtraInstructions().get(c).getValue() != null && !extraInstructionsTool.getExtraInstructions().get(c).getValue().isEmpty())
                        fieldsInstructions.put(c, extraInstructionsTool.getExtraInstructions().get(c).getValue());
                }
                ArrayList<String> contextInformation = new ArrayList<>();
                for (TextField c : extraInstructionsTool.getContextInstructions()) {
                    if (!c.getValue().isEmpty())
                        contextInformation.add(c.getValue());
                }
                clearForm();
                FormFiller formFiller = new FormFiller(formLayout, fieldsInstructions, contextInformation);
                FormFillerResult result = formFiller.fill(input);
                debugTool.getDebugPrompt().setValue(result.getRequest());
                debugTool.getDebugJsonTarget().setValue(String.format("%s", formFiller.getMapping().componentsJSONMap()));
                debugTool.getDebugTypesTarget().setValue(String.format("%s", formFiller.getMapping().componentsTypesJSONMap()));
                debugTool.getDebugResponse().setValue(result.getResponse());
            }
        });

        extraInstructionsTool.setComponents(ComponentUtils.getComponentInfo(formLayout));
        extraInstructionsTool.setVisible(false);
        extraInstructionsTool.setExtraInstructions(nameField, "Format this field in Uppercase");
        extraInstructionsTool.setExtraInstructions(emailField, "Format this field as a correct email");
        extraInstructionsTool.setContextInstructions(0,"Translate item names to Spanish");

        Button extraInstructionsButton = new Button("Show/Hide extra instructions");
        extraInstructionsButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        extraInstructionsButton.addClickListener(e -> {
            extraInstructionsTool.setVisible(!extraInstructionsTool.isVisible());
        });

        HorizontalLayout imagesLayout = new HorizontalLayout(texts);
        VerticalLayout documentLayout = new VerticalLayout(fillButton, extraInstructionsButton, extraInstructionsTool, imagesLayout);
        debugLayout.add(documentLayout, debugTool);
        add(debugLayout);

    }

    private void clearForm() {
        formLayout.getChildren().forEach(component -> {
            if (component instanceof HasValue<?, ?>) {
                ((HasValue) component).clear();
            } else if (component instanceof Grid) {
                ((Grid) component).setItems(new ArrayList<>());
            }
        });
    }

    public HashMap<String, String> getExampleTexts() {
        HashMap<String, String> texts = new HashMap<>();
        texts.put("Text1", "Order sent by the customer Andrew Jackson on '2023-04-05 12:13:00'\n" +
                "Address: Ruukinkatu 2-4, FI-20540 Turku, Finland \n" +
                "Phone Number: 555-1234 \n" +
                "Age: 43 \n" +
                "Client ID: 45XXD6543 \n" +
                "Email: 'andrewjackson@gmail.com \n" +
                "Due Date: 2023-05-05\n" +
                "\n" +
                "This order contains the products for the project 'Form filler AI Addon' that is part of the new development of Vaadin AI kit. \n" +
                "\n" +
                "Items list:\n" +
                "Item Number     Items   Type    Cost    Date    Status\n" +
                "1001    2 Smartphones   Hardware    $1000   '2023-01-10' Delivered\n" +
                "1002    1 Laptop    Hardware    $1500   '2023-02-15'    In Transit\n" +
                "1003    5 Wireless Headphones   Hardware    $500    '2023-03-20'    Cancelled\n" +
                "1004    1 Headphones    Hardware    $999    '2023-01-01'    In Transit\n" +
                "1005    1 Windows License    Software    $1500    '2023-02-01'    Delivered\n" +
                "\n" +
                "Taxes: 25,6€ \n" +
                "Total: 15000€ \n" +
                "Payment Method: Cash\n");
        texts.put("Text2", "Order sent by the customer Andrew Jackson on '2023-04-05 12:13:00'\n" +
                "Address: 1234 Elm Street, Springfield, USA \n" +
                "Phone Number: 555-1234 \n" +
                "Age: 37 \n" +
                "Client ID: 45XXD6543 \n" +
                "Email: 'andrewjackson#gmail.com \n" +
                "Due Date: 2023-05-05\n" +
                "\n" +
                "This order contains the products for the project 'Form filler AI Addon' that is part of the new development of Vaadin AI kit. \n" +
                "\n" +
                "Items list:\n" +
                "Item Number     Items   Type    Cost    Date    Status\n" +
                "1001    2 Smartphones   Hardware    $1000   '2023-01-10' Delivered\n" +
                "1002    1 Laptop    Hardware    $1500   '2023-02-15'    In Transit\n" +
                "1003    5 Wireless Headphones   Hardware    $500    '2023-03-20'    Cancelled\n" +
                "1004    1 Headphones    Hardware    $999    '2023-01-01'    In Transit\n" +
                "\n" +
                "Taxes: 35,6€ \n" +
                "Total: 10000€ \n" +
                "Payment Method: Credit Card");
        texts.put("Text3", "This is an invoice of an order for the project 'Vaadin AI Form Filler'" +
                " providing some hardware and sent by the customer Andrew Jackson with client id 45XXD6543, who lives at " +
                "Ruukinkatu 2-4, FI-20540 Turku (Finland), he is 45 years old and can be reached at phone number 555-1234 " +
                "and at email 'andrewjackson@gmail.com. Andrew has placed five items: number 1001 " +
                "contains two items of smartphone for a total of $1,000 placed on 2023 January " +
                "the 10th with a status of deliberate; number 1002 includes one item of laptop " +
                "with a total of $1,500 placed on 2023 February the 15th with a status of in transit; " +
                "number 1003 consists of five items of wireless headphones for a total of $500 placed " +
                "on 2023 March the 20th with a status of cancelled; number 1004 is for 'Headphones' " +
                "with a cost of $999 and placed on '2023-01-01' with status In transit. The invoice " +
                "was paid using a Paypal account. The taxes included in the invoice are 40,6€ and Total is 20000€");
        return texts;
    }
}
