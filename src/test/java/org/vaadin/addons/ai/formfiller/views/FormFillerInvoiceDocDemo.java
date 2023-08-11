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
import org.vaadin.addons.ai.formfiller.data.OrderItem;
import org.vaadin.addons.ai.formfiller.utils.ComponentUtils;
import org.vaadin.addons.ai.formfiller.utils.DebugTool;
import org.vaadin.addons.ai.formfiller.utils.ExtraInstructionsTool;
import org.vaadin.addons.ai.formfiller.utils.OCRUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

@Route("invoice")
public class FormFillerInvoiceDocDemo extends Div {

    FormLayout invoiceForm;

    Image preview = new Image();
    Upload imageFile = new Upload();
    Notification imageNotification = new Notification("Please select a file to upload or a predefined image");
    FileBuffer fileBuffer = new FileBuffer();

    ExtraInstructionsTool extraInstructionsTool = new ExtraInstructionsTool();

    public FormFillerInvoiceDocDemo() {
        imageNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        imageNotification.setDuration(2000);
        imageNotification.setPosition(Notification.Position.MIDDLE);

        invoiceForm = new FormLayout();

        TextField nameField = new TextField("Name");
        nameField.setId("name");
        invoiceForm.add(nameField);

        TextField addressField = new TextField("Address");
        addressField.setId("address");
        invoiceForm.add(addressField);

        TextField phoneField = new TextField("Phone");
        phoneField.setId("phone");
        invoiceForm.add(phoneField);

        TextField emailField = new TextField("Email");
        emailField.setId("email");
        invoiceForm.add(emailField);

        DateTimePicker dateCreationField = new DateTimePicker("Creation Date");
        dateCreationField.setId("creationDate");
        invoiceForm.add(dateCreationField);

        DatePicker dueDateField = new DatePicker("Due Date");
        dueDateField.setId("dueDate");
        invoiceForm.add(dueDateField);

        ComboBox<String> orderEntity = new ComboBox<>("Order Entity");
        orderEntity.setId("orderEntity");
        orderEntity.setItems("Person", "Company");
        invoiceForm.add(orderEntity);

        NumberField orderTotal = new NumberField("Order Total");
        orderTotal.setId("orderTotal");
        invoiceForm.add(orderTotal);

        TextArea orderDescription = new TextArea("Order Description");
        orderDescription.setId("orderDescription");
        invoiceForm.add(orderDescription);

        RadioButtonGroup<String> paymentMethod = new RadioButtonGroup<>("Payment Method");
        paymentMethod.setItems("Credit Card", "Cash", "Paypal");
        paymentMethod.setId("paymentMethod");
        invoiceForm.add(paymentMethod);

        Checkbox isFinnishCustomer = new Checkbox("Is Finnish Customer");
        isFinnishCustomer.setId("isFinnishCustomer");
        invoiceForm.add(isFinnishCustomer);

        CheckboxGroup<String> typeService = new CheckboxGroup<>("Type of Service");
        typeService.setItems("Software", "Hardware", "Consultancy");
        typeService.setId("typeService");
        invoiceForm.add(typeService);

        Grid<OrderItem> orderGrid = new Grid<>(OrderItem.class);
        orderGrid.removeAllColumns();
        orderGrid.addColumn(OrderItem::getOrderId).setHeader("Order Id").setKey("orderId").setId("orderId");
        orderGrid.addColumn(OrderItem::getItemName).setHeader("Item Name").setKey("itemName").setId("itemName");
        orderGrid.addColumn(OrderItem::getOrderDate).setHeader("Order Date").setKey("orderDate").setId("orderDate");
        orderGrid.addColumn(OrderItem::getOrderStatus).setHeader("Order Status").setKey("orderStatus").setId("orderStatus");
        orderGrid.addColumn(OrderItem::getOrderTotal).setHeader("Order Cost").setKey("orderCost").setId("orderCost");
        orderGrid.setId("orders");

        invoiceForm.add(orderGrid);

        invoiceForm.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new FormLayout.ResponsiveStep("500px", 2));
        // Stretch the username field over 2 columns
        invoiceForm.setColspan(orderGrid, 2);

        add(invoiceForm);

        VerticalLayout debugLayout = new VerticalLayout();
        debugLayout.setWidthFull();

        DebugTool debugTool = new DebugTool();

        imageFile.setReceiver(fileBuffer);

        ComboBox<String> images = new ComboBox<>("Select Image");
        images.setItems("Load my invoice...", "Invoice1", "Invoice2", "Invoice3", "Invoice4", "Invoice5", "Invoice_HU_1");
        images.setValue("Load my invoice...");
        images.setAllowCustomValue(false);
        enableFileUpload();
        images.addValueChangeListener(e -> {
            if (e.getValue().equalsIgnoreCase("Load my invoice...")) {
                enableFileUpload();
            } else {
                imageFile.setVisible(false);
                StreamResource imageResource = new StreamResource(e.getValue()+".png",
                        () -> getClass().getResourceAsStream("/invoices/"+e.getValue()+".png"));
                preview.setSrc(imageResource);
            }
        });

        Button fillDocumentButton = new Button("Fill Form From Document");
        fillDocumentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fillDocumentButton.addClickListener(event -> {
            if (images.getValue().equalsIgnoreCase("Load my invoice..."))
            {
                if (fileBuffer.getFileData() == null) {
                    imageNotification.open();
                    return;
                }
            }
            try {
                FileInputStream fileInputStream = null;
                if (images.getValue().equalsIgnoreCase("Load my invoice...")) {
                    fileInputStream = new FileInputStream(fileBuffer.getFileData().getFile().getAbsolutePath());
                } else {
                    fileInputStream = new FileInputStream(getClass().getResource("/invoices/"+images.getValue()+".png").getFile());
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
                    ArrayList<String> contextInformation = new ArrayList<>();
                    for (TextField c : extraInstructionsTool.getContextInstructions()) {
                        if (!c.getValue().isEmpty())
                            contextInformation.add(c.getValue());
                    }

                    FormFiller formFiller = new FormFiller(invoiceForm, fieldsInstructions, contextInformation);
                    FormFillerResult result = formFiller.fill(input);
                    debugTool.getDebugPrompt().setValue(result.getRequest() + '\n');
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

        extraInstructionsTool.setComponents(ComponentUtils.getComponentInfo(invoiceForm));
        extraInstructionsTool.setVisible(false);
        extraInstructionsTool.setContextInstructions(0,  "Translate all values from the user input to English");
        Button extraInstructionsButton = new Button("Show/Hide extra instructions");
        extraInstructionsButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        extraInstructionsButton.addClickListener(e -> {
            extraInstructionsTool.setVisible(!extraInstructionsTool.isVisible());
        });

        HorizontalLayout imagesLayout = new HorizontalLayout(images, imageFile, preview);
        VerticalLayout documentLayout = new VerticalLayout(fillDocumentButton,  extraInstructionsButton, extraInstructionsTool, imagesLayout);
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
        invoiceForm.getChildren().forEach(component -> {
            if (component instanceof HasValue<?, ?>) {
                ((HasValue) component).clear();
            } else if (component instanceof Grid) {
                ((Grid) component).setItems(new ArrayList<>());
            }
        });
    }
}
