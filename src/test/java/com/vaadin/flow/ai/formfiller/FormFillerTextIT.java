package com.vaadin.flow.ai.formfiller;

import com.vaadin.flow.ai.formfiller.data.OrderItem;
import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.component.textfield.testbench.*;
import com.vaadin.testbench.BrowserTest;
import com.vaadin.testbench.BrowserTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class FormFillerTextIT extends BrowserTestBase {

    /**
     * If running on CI, get the host name from environment variable HOSTNAME
     *
     * @return the host name
     */
    private static String getDeploymentHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }
        return "localhost";
    }

    @BeforeEach
    public void open() {
        getDriver().get("http://"+getDeploymentHostname()+":8080/");
    }

    @BrowserTest
    public void formFill_chooseText1_fillsForm() {
        FormData formData = new FormData();
        formData.name = "ANDREW JACKSON";
        formData.address = "Ruukinkatu 2-4, FI-20540 Turku (Finland)";
        formData.phone = "555-1234";
        formData.age = 45;
        formData.email = "andrewjackson@gmail.com";
        formData.clientId = "45XXD6543";
        formData.creationDate = LocalDateTime.of(2023, 4, 5, 12, 13);
        formData.dueDate = LocalDate.of(2023, 5, 15);
        formData.orderEntity = "Person";
        formData.orderTotal = 20000;
        formData.orderTaxes = 40.6;
        formData.orderDescription = "Vaadin AI Form Filler";
        formData.paymentMethod = "Paypal";
        formData.isFinnishCustomer = true;
        formData.typeService.add("Hardware");

        selectTemplate("Template 003");

        fillForm();

        verifyFormValues(formData, getOrders());
    }

    private void verifyFormValues(FormData form, List<OrderItem> orders) {
        waitUntil(driver ->
                !$(TextFieldElement.class).id("name").getValue().isBlank(), 20L);

        Assertions.assertEquals(form.name,
                $(TextFieldElement.class).id("name").getValue());

        Assertions.assertEquals(form.address,
                $(TextFieldElement.class).id("address").getValue());

        Assertions.assertEquals(form.phone,
                $(TextFieldElement.class).id("phone").getValue());

        Assertions.assertEquals(form.age,
                Integer.valueOf($(IntegerFieldElement.class).id("age")
                        .getValue()));

        Assertions.assertEquals(form.email,
                $(EmailFieldElement.class).id("email").getValue());

        Assertions.assertEquals(form.clientId,
                $(PasswordFieldElement.class).id("clientId").getValue());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm:ss");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy");

        Assertions.assertEquals(form.creationDate.format(dateTimeFormatter),
                $(DateTimePickerElement.class).id("creationDate").getDateTime()
                        .format(dateTimeFormatter));

        Assertions.assertEquals(form.dueDate.format(dateFormatter),
                $(DatePickerElement.class).id("dueDate").getDate().format(dateFormatter));

        ComboBoxElement orderEntity = $(ComboBoxElement.class).id("orderEntity");
        Assertions.assertEquals(form.orderEntity, orderEntity.getSelectedText());

        Assertions.assertEquals(form.orderTotal,
                Integer.valueOf($(NumberFieldElement.class).id("orderTotal")
                        .getValue()));

        Assertions.assertEquals(form.orderTaxes,
                Double.parseDouble($(BigDecimalFieldElement.class).id("orderTaxes")
                        .getValue().replace(",", ".")));

        Assertions.assertEquals(form.orderDescription,
                $(TextAreaElement.class).id("orderDescription").getValue());

        Assertions.assertEquals(form.paymentMethod,
                $(RadioButtonGroupElement.class).id("paymentMethod").getSelectedText());

        Assertions.assertEquals(form.isFinnishCustomer,
                $(CheckboxElement.class).id("isFinnishCustomer").isChecked());

        Assertions.assertTrue(
                $(CheckboxGroupElement.class).id("typeService").getSelectedTexts()
                        .containsAll(form.typeService));

        // todo wait for next alpha
//        Assertions.assertEquals(form.typeService,
//                $(MultiSelectComboBoxElement.class).id("typeServiceMs").getSelectedTexts());

        GridElement ordersGrid = $(GridElement.class).id("orders");
        Assertions.assertEquals(4, ordersGrid.getRowCount());

        for (int row = 0; row < orders.size(); row++) {
            Assertions.assertEquals(orders.get(row).getItemName(),
                    ordersGrid.getCell(row, 0).getText());
            Assertions.assertEquals(orders.get(row).getOrderDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    ordersGrid.getCell(row, 1).getText());
            Assertions.assertEquals(orders.get(row).getOrderId(),
                    ordersGrid.getCell(row, 2).getText());
            Assertions.assertEquals(orders.get(row).getOrderStatus(),
                    ordersGrid.getCell(row, 3).getText());
            Assertions.assertEquals(orders.get(row).getOrderTotal(),
                    Double.valueOf(ordersGrid.getCell(row, 4).getText()));
        }
    }

    private List<OrderItem> getOrders() {
        OrderItem order1 = new OrderItem();
        order1.setItemName("Smartphone");
        order1.setOrderDate(LocalDate.of(2023, 1, 10));
        order1.setOrderId("1001");
        order1.setOrderStatus("Deliberate");
        order1.setOrderTotal(1000.0);

        OrderItem order2 = new OrderItem();
        order2.setItemName("Laptop");
        order2.setOrderDate(LocalDate.of(2023, 2, 15));
        order2.setOrderId("1002");
        order2.setOrderStatus("In transit");
        order2.setOrderTotal(1500.0);

        OrderItem order3 = new OrderItem();
        order3.setItemName("Wireless headphones");
        order3.setOrderDate(LocalDate.of(2023, 3, 20));
        order3.setOrderId("1003");
        order3.setOrderStatus("Cancelled");
        order3.setOrderTotal(500.0);

        OrderItem order4 = new OrderItem();
        order4.setItemName("Headphones");
        order4.setOrderDate(LocalDate.of(2023, 1, 1));
        order4.setOrderId("1004");
        order4.setOrderStatus("In transit");
        order4.setOrderTotal(999.0);

        return Arrays.asList(order1, order2, order3, order4);
    }

    private void fillForm() {
        $(ButtonElement.class).id("fill-form-button").click();
    }

    private void selectTemplate(String template) {
        waitForElementPresent(By.id("text-combobox"));
        ComboBoxElement textCombobox = $(ComboBoxElement.class).id("text-combobox");
        textCombobox.openPopup();
        textCombobox.selectByText(template);
    }
}
