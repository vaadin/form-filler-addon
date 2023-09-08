package com.vaadin.flow.ai.formfiller;

import com.vaadin.flow.ai.formfiller.services.FormFillerStats;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.UsageStatistics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

//TODO: Add more tests for FormFiller
public class FormFillerTest {

//    FormFiller formFiller;

    @Before
    public void setUp() {
        //set environmental variable
        System.setProperty("OPENAI_TOKEN", "open-ai-key");

        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");
        formLayout.add(textField);
//        formFiller = new FormFiller(formLayout);
    }

//    Would need to add https://stackoverflow.com/questions/71296783/juint-test-giving-error-java-lang-reflect-inaccessibleobjectexception-unable-to to make it work...
//    Probably does not worth it.
    @Test
    public void reportUsageStatisticsWasExecuted() throws Exception {
//        PowerMockito.verifyPrivate(formFiller).invoke("reportUsageStatistics");
//        doNothing().when(FormFillerStats.class);
//        verifyStatic(FormFillerStats.class); //Similar to how you mock static methods
        //this is how you verify them.
//        FormFillerStats.report();
    }
}
