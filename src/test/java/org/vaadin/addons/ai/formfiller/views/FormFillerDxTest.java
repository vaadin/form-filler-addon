package org.vaadin.addons.ai.formfiller.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import org.junit.Test;
import org.vaadin.addons.ai.formfiller.FormFiller;
import org.vaadin.addons.ai.formfiller.FormFillerResult;
import org.vaadin.addons.ai.formfiller.utils.DebugTool;
import org.vaadin.addons.ai.formfiller.utils.ExtraInstructionsTool;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

@Route("dx")
public class FormFillerDxTest extends Div {

    FormLayout formLayout;

    ExtraInstructionsTool extraInstructionsTool = new ExtraInstructionsTool();

    public FormFillerDxTest() {
        formLayout = new FormLayout();

        TextField IBAN = new TextField("IBAN");
        IBAN.setId("IBAN");
        formLayout.add(IBAN);

        TextField bic = new TextField("BIC");
        bic.setId("BIC");
        formLayout.add(bic);

        TextField payer = new TextField("payer");
        payer.setId("payer");
        formLayout.add(payer);

        DatePicker date = new DatePicker("date");
        date.setId("date");
        formLayout.add(date);

        TextArea provider = new TextArea("Provider");
        provider.setId("provider");
        formLayout.add(provider);

        TextField refNumber = new TextField("reference number");
        refNumber.setId("reference number");
        formLayout.add(refNumber);



        formLayout.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new FormLayout.ResponsiveStep("500px", 2));

        // 1. Implement the fillButton.addClickListener() method, this button will trigger the autofilling of the form:
        // - check the documentation what you need to do exactly

        // 2. Add a simple component and autofill it:
        // - e.g. e-mail field to form and try to fill it with the preset text

        // 3. Add some more complex components and autofill them:
        // - e.g. like a Checkbox, or RadioButton

        // 4. Add the most complex component and autofill them:
        // - Grid


        Grid<BankInfo> grid = new Grid<>(BankInfo.class);
        grid.setId("Bank Info");
        grid.removeAllColumns();
        grid.addColumn(BankInfo::getIBAN).setHeader("IBAN").setKey("IBAN").setId("IBAN");
        grid.addColumn(BankInfo::getPayer).setHeader("payer").setKey("payer").setId("payer");
//        grid.addColumn(BankInfo::getDate).setHeader("Date").setKey("date").setId("date");
        formLayout.add(grid);

        // 5. Fine tune your results:
        // - Try adding extra general/contexts instructions to make the fillings better, try to uppercase some values, try to translate it to some other languages, change some formats, etc.
        // - Try adding extra field instructions (not general just specific to some field e.g. Make uppercase the first letter of the name)

        // 6. Try some fancy:
        // - e.g. add some of your own text or translate the text to different language and try to fill the form in English as well!!

        // 7++. Implement a new LLMService:
        // - Copy+paste an existing service from ChatGPTChatCompletionService (new model) or ChatGPTService (old model),
        // - Try to change the model to something else (OpenAI documentation or we can help to give you more model names),
        // - Try to change the underlying prompt,
        // - Try to change Temperature, or other parameters

        add(formLayout);

        VerticalLayout debugLayout = new VerticalLayout();
        debugLayout.setWidthFull();

        DebugTool debugTool = new DebugTool();
//        debugTool.hideDebugTool();

        ComboBox<String> texts = new ComboBox<>("Select a text or just type your own <br>in the debug Input Source field");
        texts.setItems("Text1", "Text2");
        texts.setValue("Text1");
        texts.setAllowCustomValue(false);

        debugTool.getDebugInput().setValue(getExampleTexts().get("Text1"));
        texts.addValueChangeListener(e -> {
            debugTool.getDebugInput().setValue(getExampleTexts().get(texts.getValue()));
        });

        Button fillButton = new Button("Fill Form From Input Text");
        fillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillButton.addClickListener(event -> {
            // 1. Implement the fillButton.addClickListener() method, this button will trigger the auto filling of the form:
            // - check the documentation what you need to do exactly

            FormFiller formFiller = new FormFiller(formLayout);
            FormFillerResult fill = formFiller.fill(debugTool.getDebugInput().getValue());

            TextArea debugResponse = debugTool.getDebugResponse();
            debugResponse.setValue(fill.getResponse());
        });


        HorizontalLayout imagesLayout = new HorizontalLayout(texts);
        VerticalLayout documentLayout = new VerticalLayout(fillButton, extraInstructionsTool, imagesLayout);
        debugLayout.add(documentLayout, debugTool);
        add(debugLayout);
    }

    public static class BankInfo {
        String IBAN;
        String payer;
        LocalDate date;

        public String getIBAN() {
            return IBAN;
        }

        public void setIBAN(String IBAN) {
            this.IBAN = IBAN;
        }

        public String getPayer() {
            return payer;
        }

        public void setPayer(String payer) {
            this.payer = payer;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BankInfo() {
        }

        public BankInfo(String IBAN, String payer, LocalDate date) {
            this.IBAN = IBAN;
            this.payer = payer;
            this.date = date;
        }
    }
    public HashMap<String, String> getExampleTexts() {
        HashMap<String, String> texts = new HashMap<>();
        texts.put("Text1", "Order sent by the customer Andrew Jackson on '2023-04-05 12:13:00'\n" +
                "Address: Ruukinkatu 2-4, FI-20540 Turku, Finland \n" +
                "Phone Number: 555-1234 \n" +
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
                "Payment Method: Cash\n");
        texts.put("Text2", "Order sent by the customer Andrew Jackson on '2023-04-05 12:13:00'\n" +
                "Address: 1234 Elm Street, Springfield, USA \n" +
                "Phone Number: 555-1234 \n" +
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
                "Payment Method: Credit Card");
        texts.put("Text3", "This is an invoice of an order for the project 'Vaadin AI Form Filler'" +
                " providing some hardware and sent by the customer Andrew Jackson who lives at " +
                "Ruukinkatu 2-4, FI-20540 Turku (Finland) and can be reached at phone number 555-1234 " +
                "and at email 'andrewjackson@gmail.com. Andrew has placed five items: number 1001 " +
                "contains two items of smartphone for a total of $1,000 placed on 2023 January " +
                "the 10th with a status of deliberate; number 1002 includes one item of laptop " +
                "with a total of $1,500 placed on 2023 February the 15th with a status of in transit; " +
                "number 1003 consists of five items of wireless headphones for a total of $500 placed " +
                "on 2023 March the 20th with a status of cancelled; number 1004 is for 'Headphones' " +
                "with a cost of $999 and placed on '2023-01-01' with status In transit. The invoice " +
                "was paid using a Paypal account.");
        return texts;
    }
}
