package com.vaadin.flow.ai.formfiller;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

//TODO: Add more tests for FormFiller
public class FormFillerTest {

    FormFiller formFiller;

    @Before
    public void setUp() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");
        formLayout.add(textField);
        formFiller = Mockito.spy(new FormFiller(formLayout));
    }

    @Test
    public void reportUsageStatisticsWasExecuted() {
        Mockito.verify(formFiller, Mockito.times(1)).reportUsageStatistics();
    }
}
