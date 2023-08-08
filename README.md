# FormFiller Add-on project

The Form Filler Addon is an experimental feature that provides an easy way for Flow users to create forms filled automatically from natural language input sources using GPT technologies. 

## Add-on structure
The addon includes the FormFiller addon and some demos to check its capabilities. 

## API Review
### Constructors
There are 5 constructors all of them based on the same one just providing default values when a parameter of the base constructor is not provided. 

These parameters are:

__target:__ the target component or group of components (layout) to fill. This is the only mandatory parameter without default value. 

__componentInstructions:__ additional instructions for the AI module (i.e.: field format, field explanation, etc...). Use these instructions to provide additional information to the AI module about a specific field when the response of the form filler is not accurate enough. By default this structure is initialized empty.

__contextInstructions:__ additional instructions for the AI module (i.e.: target language, vocabulary explanation, etc..). Use these instructions to provide additional information to the AI module about the context of the input source in general. By default this structure is initialized empty.

__llmService:__ the AI module service to use. By default, this service would use OpenAI ChatGPT.
 	
### Methods
```java
public FormFillerResult fill(String input)
```

The main method to be called when we want to fill the form fields after setting up the FormFiller object. This method fills the registered fields and returns a structure with information of the process such as the AI module request and response.   

### Example:

Creating the form:

```java
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

   TextField emailField = new TextField("Email");
   emailField.setId("email");
   formLayout.add(emailField);

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
```

Filling the form:

```java
   FormFiller formFiller = new FormFiller(formLayout, fieldsInstructions, contextInformation);
   FormFillerResult result = formFiller.fill(input);
```


## Best practices & limitations

To make a set of components ready to be filled by the FormFiller the only requirements are:
- Target fields are descendants of the same container (layout) passed to the FormFiller. The best way is to integrate all the fields in a FormLayout but any kind of Layout can be used. Also all the descendants of the passed container are going to be included so several containers can be used inside the main container. 
- Every field has an ID (Component::setId). The ID should be meaningful about the data contained by the field. Grid columns are a special case as they are not input fields but the procedure is pretty much the same, you just need to set an ID and a Key for each column (Be sure to use the same for both). 

## Components supported:

- TextField
- NumberField
- DatePicker
- TimePicker
- DateTimePicker
- TextArea
- Checkbox
- CheckboxGroup
- RadioButtonGroup
- ComboBox
- Grid

## Types supported for Grid Columns:

- Date
- LocalDate
- Time
- LocalTime
- DateTime
- LocalDateTime
- Boolean
- Integer
- Long
- Double
- Float
- String

## Limitations

- The size of the text that can be processed is related to the capacity of ChatGPT that for the model included as default in the addon is 16384 tokens (approx 16000 words including request and response).
- So far parametrized components such as checkboxes or radio buttons work with basic types not custom classes.
- Grid must be defined using a Bean type.  

## Demos

The demo has 3 built-in views available. In all demos you have preloaded examples that you can use to test them. Of course, you can always use your own examples of input sources. 

### General Demo Structure

All demos follow the same layout:
 
- FORM: The form to be filled 

![Screenshot 2023-08-06 at 13 02 24](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/0abb204f-6312-41c5-ba9b-a3053fb0e738)

- ACTIONS: Button actions to process the input, to load examples or documents and show/hide tools.

The actions are different for the text input and the document input just in the functionality of uploading documents instead of using predefined examples. In the case of the input text you just need to modify the ‘Debug Input Source’ text area. 

![Screenshot 2023-08-06 at 12 56 22](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/185d13af-55f2-432e-8630-1a80bfed39cc)

- EXTRA INSTRUCTIONS TOOL: A tool to include extra instructions for the form fields dynamically during runtime. 

The extra instructions tool is just a set of text fields to be able to add more context information to the prompt at runtime. This information can be related to a specific field.
For example in text demo try for name “Format this field in Uppercase” and for context information  “Translate items to Spanish”. 

![Screenshot 2023-08-06 at 12 56 44](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/795b3b3d-11f2-44d3-8c22-78c47b5ee185)

- DEBUG TOOL: A tool to visualize all the steps involved in the Form Filler process to enhance debugging of prompts. Here you can detect problems or mistakes of the AI interpretation of the input source and you can give more context using extra instructions.

The Debug Tool includes text areas to visualize each of the important parts of the process:

__Debug Input Source:__ The exact input data that is sent to ChatGPT

__Debug JSON target:__ The target JSON schema required to ChatGPT to describe the data

![Screenshot 2023-08-06 at 12 56 58](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/0bae57ea-2b83-43c2-b08f-076fb8e59708)

__Debug Type target:__ The information about fields (type, context) shared with ChatGPT

__Debug Prompt:__ Final prompt as it is sent to ChatGPT. 

![Screenshot 2023-08-06 at 12 57 06](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/d126fd69-08d5-45d1-b0e4-a539bcdc4617)

__Debug Response:__ The response received from ChatGPT.

![Screenshot 2023-08-06 at 12 57 24](https://github.com/mgarciavaadin/form-filler-addon/assets/106953874/26ee59ad-7d86-4b04-9142-caa773152165)

### Views

#### Text Input 

- “/” - Main example using text area to get the text input for the form filler. This example has a form using at least one example of all the supported components by the addon. 

#### Image Input

In these examples we use snapshots from 1 page documents to get the text. In both examples you can load your own image to test. 

- “/invoice” - Example using invoices  document as input source. These documents usually are well formatted and contain similar information. 

- “/receipt” - Example using receipt  document as input source. These documents usually are not well formatted and contain different formats and information. 

## Deployment

Starting the test/demo server:
```
mvn jetty:run -Pdevelopment
```

This deploys demos at http://localhost:8080, http://localhost:8080/receipt and http://localhost:8080/invoice
 
### Integration test

To run Integration Tests, execute `mvn verify -Pit,production`.

Tests run by default in `headless` mode, to avoid browser windows to be opened for every test.
This behaviour is always disabled when running the tests in debug mode in the IDE
or when running maven with the `-Dmaven.failsafe.debug` sytem property.
On normal execution, headless mode can be deactivated using the `-Dtest.headless=false` system property.

## References

- [Auto Form Filler](https://vaadin.com/directory/component/auto-form-filler) add-on created by Armando Perea
- [ChatGPT Java API](https://github.com/TheoKanning/openai-java) created by Theo Kanning


