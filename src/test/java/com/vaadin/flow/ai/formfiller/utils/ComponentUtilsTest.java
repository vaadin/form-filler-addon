package com.vaadin.flow.ai.formfiller.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ComponentUtilsTest {

    @Test
    public void testCreateMappingWorksForLayout() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");
        formLayout.add(textField);
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(formLayout);
        assertEquals(componentsMapping.components().get(0).type(), TextField.class.getSimpleName());
        assertEquals(componentsMapping.components().get(0).id(), "name");
        assertEquals(componentsMapping.components().get(0).component(), textField);
        assertTrue(componentsMapping.componentsJSONMap().containsKey("name"));
        assertTrue(componentsMapping.componentsTypesJSONMap().containsKey("name"));
    }

    @Test
    public void testCreateMappingWorksForSingleComponent() {
        TextField textField = new TextField();
        textField.setId("name");
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(textField);
        assertEquals(componentsMapping.components().get(0).type(), TextField.class.getSimpleName());
        assertEquals(componentsMapping.components().get(0).id(), "name");
        assertEquals(componentsMapping.components().get(0).component(), textField);
        assertTrue(componentsMapping.componentsJSONMap().containsKey("name"));
        assertTrue(componentsMapping.componentsTypesJSONMap().containsKey("name"));
    }

    @Test
    public void testCreateMappingDoesNotWorkIfNoId() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        formLayout.add(textField);
        ComponentUtils.ComponentsMapping componentsMapping = ComponentUtils.createMapping(textField);
        assertTrue(componentsMapping.components().isEmpty());
        assertTrue(componentsMapping.componentsJSONMap().isEmpty());
        assertTrue(componentsMapping.componentsTypesJSONMap().isEmpty());
    }

    @Test
    public void testGetComponentInfoForTextField() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");
        formLayout.add(textField);
        List<ComponentUtils.ComponentInfo> componentInfoList = ComponentUtils.getComponentInfo(formLayout);
        componentInfoList.forEach(componentInfo -> {
            assertEquals("name", componentInfo.id());
            assertEquals("TextField", componentInfo.type());
            assertSame(componentInfo.component(), textField);
        });
    }


    @Test
    public void testGetAllChildren() {
        VerticalLayout formLayout = new VerticalLayout();
        TextField textField = new TextField();
        textField.setId("name");
        formLayout.add(textField);
        Stream<Component> children = ComponentUtils.getAllChildren(formLayout);
        List<Component> childrenList = children.toList();
        // vertical layout and text field:
        assertEquals(2, childrenList.size());
        assertEquals(formLayout, childrenList.get(0));
        assertEquals(textField, childrenList.get(1));
    }


    @Test
    public void testIsSupportedComponent() {
        List<Component> supportedComponents = List.of(
                new TextField(),
                new TextArea(),
                new NumberField(),
                new BigDecimalField(),
                new IntegerField(),
                new EmailField(),
                new PasswordField(),
                new DatePicker(),
                new TimePicker(),
                new DateTimePicker(),
                new ComboBox(),
                new Checkbox(),
                new CheckboxGroup(),
                new RadioButtonGroup(),
                new Grid(),
                new MultiSelectComboBox()
        );

        supportedComponents.forEach(supportedComponent -> assertTrue(ComponentUtils.isSupportedComponent(supportedComponent)));
    }

    // TODO: add further potentially fillComponents test as well.
}
