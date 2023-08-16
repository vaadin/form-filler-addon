# Form Filler Add-on project

The Form Filler Addon is an experimental feature that provides an easy way for Flow users to create forms filled automatically from natural language input sources using GPT technologies.

```java
@Route("test")
public class FormTest extends Div {

    public FormTest() {

        TextField nameField = new TextField("Name");
        nameField.setId("name");

        TextField addressField = new TextField("Address");
        addressField.setId("address");

        FormLayout fl = new FormLayout();
        fl.add(nameField, addressField);

        FormFiller formFiller = new FormFiller(fl);
        formFiller.fill("My name is Bart and I live at 742 Evergreen Terrace, Springfield USA");

        add(fl);
    }
}
```

![Screenshot 2023-08-16 at 20 25 09](https://github.com/vaadin/form-filler-addon/assets/106953874/3b2fad67-a95e-424b-9bc5-8e240c1cd215)


___This is an experimental feature and it may be removed, altered, or limited to commercial subscribers in future releases.___

## Add-on structure
The addon includes the FormFiller addon and some demos to check its capabilities.

- src/main: addon code
- src/test: Demos

## API Review
### Constructors
There are 6 constructors all of them based on the same one just providing default values when a parameter of the base constructor is not provided. 

These parameters are:

__target:__ the target component or group of components (layout) to fill. This is the only mandatory parameter without default value. 

__componentInstructions:__ additional instructions for the AI module related to a specific component/field (i.e.: field format, field explanation, etc...). Use these instructions to provide additional information to the AI module about a specific field when the response of the form filler is not accurate enough. By default this structure is initialized empty.

__contextInstructions:__ additional instructions for the AI module related to the input source or all components/fields (i.e.: target language, vocabulary explanation, current time, etc..). Use these instructions to provide additional information to the AI module about the context of the input source in general. By default this structure is initialized empty.

__llmService:__ the AI module service to use. By default, this service would use OpenAI ChatGPT with chat/completion end point and the "gpt-3.5-turbo-16k-0613" model. There is another built in service using also ChatGPT but with the /completion endpoint and the "text-davinci-003" model. About ChatGPT models, newest models could not be better for the specific task of the Form Filler. So far tests has not clearly identified the best model so don't hesitate to test both services and give us feedback about your results. More models and others LLM providers will be added to the addon in the future. If you want to create your own provider service you just need to extend the interface LLMService and add it as a parameter to the Form Filler. 
 	
### Methods
```java
public FormFillerResult fill(String input)
```

The main method to be called when we want to fill the form fields after setting up the FormFiller object. This method fills the registered fields and returns a structure with information of the process such as the AI module request and response.   

### Example:

For a whole example check the main [demo code](https://github.com/vaadin/form-filler-addon/blob/main/src/test/java/com/vaadin/flow/ai/formfiller/views/FormFillerTextDemo.java) 

The only requirement to make a component/field accessible to the Form Filler is just to set a (meaningful) id to the component. 

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
   orderGrid.setId("orders");

   formLayout.add(orderGrid);
```

Filling the form:

- common use case
  
```java
   FormFiller formFiller = new FormFiller(formLayout);
   FormFillerResult result = formFiller.fill(input);
```

- adding extra instructions use case
  
```java
   FormFiller formFiller = new FormFiller(formLayout, fieldsInstructions, contextInformation);
   FormFillerResult result = formFiller.fill(input);
```

- using a different model use case
  
```java
   FormFiller formFiller = new FormFiller(formLayout, new ChatGPTService());
   FormFillerResult result = formFiller.fill(input);
```


## Best practices & limitations

To make a set of components ready to be filled by the FormFiller the only requirements are:
- Target fields are descendants of the same container (layout) passed to the FormFiller. The best way is to integrate all the fields in a FormLayout but any kind of Layout can be used. Also all the descendants of the passed container are going to be included so several containers can be used inside the main container. 
- Every field has an ID (Component::setId). The ID should be meaningful about the data contained by the field. Grid columns are a special case as they are not input fields, they will be inspected directly from the Bean of the GRID so use meaningful names for the Bean fields.

Anyways Remember that later you can add extra information about any component to help the AI module if the Id is not enough to understand what data you are looking for. Of course you can use a sentence as an Id but for cleaner code we recommend to use Ids in combination with extra instructions but it is up to the developer to choose. For most cases a 2-3 word Id is enough for the AI module to understand the target. i.e.:
```java
        CheckboxGroup<String> typeService = new CheckboxGroup<>("Type of Service");
        typeService.setItems("Software", "Hardware", "Consultancy");
        typeService.setId("typeService");
        formLayout.add(typeService);
        ......
        HashMap<Component,String> fieldInstructions = new HashMap<>();
        fieldInstructions.put(typeService, "This field describes the type of the items of the order");

        FormFiller formFiller = new FormFiller(formLayout, fieldsInstructions);
        FormFillerResult result = formFiller.fill(input);
```

better than 

```java
        CheckboxGroup<String> typeService = new CheckboxGroup<>("Type of Service");
        typeService.setItems("Software", "Hardware", "Consultancy");
        typeService.setId("the type of the items of the order");
        formLayout.add(typeService);
        ......
        FormFiller formFiller = new FormFiller(formLayout);
        FormFillerResult result = formFiller.fill(input);
```

This extra instructions can be used not only for understanding but also for formatting or error fixes i.e.:

```java
        HashMap<Component,String> fieldInstructions = new HashMap<>();
        fieldInstructions.put(nameField, "Format this field in Uppercase");
        fieldInstructions.put(emailField, "Format this field as a correct email");
```

There are some limitations for some fields specialy the ones containing dates the FormFiller has its own standard formatting requirement so be careful manipulating them.  

## Components supported:

- TextField
- EmailField
- PasswordField
- NumberField
- IntegerField
- BigDecimalField
- DatePicker
- TimePicker
- DateTimePicker
- TextArea
- Checkbox
- CheckboxGroup
- RadioButtonGroup
- ComboBox
- MultiSelectComboBox
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

- The size of the text that can be processed is related to the capacity of the used model. The model included as default in the addon has a limit of 16384 tokens (approx 16000 words including request and response).
- So far parametrized components such as checkboxes or radio buttons work with basic types not custom classes.
- Grid must be defined using a Bean type.  

## Demos

The demo has 3 built-in views available. In all demos you have preloaded examples that you can use to test them. Of course, you can always use your own examples of input sources. 

### General Demo Structure

All demos follow the same layout:
 
- FORM: The form to be filled 

![Screenshot 2023-08-06 at 12 56 44](https://github.com/vaadin/form-filler-addon/assets/106953874/8ebe0e31-0db9-49af-b438-28a499d926d1)

- ACTIONS: Button actions to process the input, to load examples or documents and show/hide tools.

The actions are different for the text input and the document input just in the functionality of uploading documents instead of using predefined examples. In the case of the input text you just need to modify the ‘Debug Input Source’ text area. 

![Screenshot 2023-08-06 at 12 56 22](https://github.com/vaadin/form-filler-addon/assets/106953874/7bdaa441-1936-4fd4-b39e-f139b2254b93)

- EXTRA INSTRUCTIONS TOOL: A tool to include extra instructions for the form fields dynamically during runtime. 

The extra instructions tool is just a set of text fields to be able to add more context information to the prompt at runtime. This information can be related to a specific field.
For example in text demo try for name “Format this field in Uppercase” and for context information  “Translate items to Spanish”. 

![Screenshot 2023-08-06 at 12 56 44](https://github.com/vaadin/form-filler-addon/assets/106953874/0bf1f6ec-aead-4a5b-b2c1-57c3c66dfbe7)

- DEBUG TOOL: A tool to visualize all the steps involved in the Form Filler process to enhance debugging of prompts. Here you can detect problems or mistakes of the AI interpretation of the input source and you can give more context using extra instructions.

The Debug Tool includes text areas to visualize each of the important parts of the process:

__Debug Input Source:__ The exact input data that is sent to ChatGPT

__Debug JSON target:__ The target JSON schema required to ChatGPT to describe the data

![Screenshot 2023-08-06 at 12 56 58](https://github.com/vaadin/form-filler-addon/assets/106953874/57883332-dc69-455d-afd9-9712b0b395fa)

__Debug Type target:__ The information about fields (type, context) shared with ChatGPT

__Debug Prompt:__ Final prompt as it is sent to ChatGPT. 

![Screenshot 2023-08-06 at 12 57 06](https://github.com/vaadin/form-filler-addon/assets/106953874/662a3c20-fc08-4f69-8d09-b3e330dd6d74)

__Debug Response:__ The response received from ChatGPT.

![Screenshot 2023-08-06 at 12 57 24](https://github.com/vaadin/form-filler-addon/assets/106953874/e2f72478-a335-4db4-9c66-2c0c7bd26ce6)

### Views

#### Text Input 

- “/” - Main example using text area to get the text input for the form filler. This example has a form using at least one example of all the supported components by the addon. 

#### Image Input

In these examples we use snapshots from 1 page documents to get the text. In both examples you can load your own image to test. 

- “/invoice” - Example using invoice documents as input source. These documents usually are well formatted and contain similar information. 

- “/receipt” - Example using receipt documents as input source. These documents usually are not well formatted and contain different formats and information. 

## Deployment

Starting the test/demo server:
```
mvn jetty:run
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


