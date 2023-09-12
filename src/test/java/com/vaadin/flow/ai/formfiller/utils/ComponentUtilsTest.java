package com.vaadin.flow.ai.formfiller.utils;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import org.junit.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentUtilsTest {
    @Test
    public void testComboBoxFillItemInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("food");
        comboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", comboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        response.put("food", "pizza");

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals("pizza", comboBox.getValue());
    }

    @Test
    public void testComboBoxFillItemInItemListCustomValueAllowed() {
        //create ComponentInfo
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("food");
        comboBox.setItems("pizza", "pasta", "salad");
        comboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", comboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        response.put("food", "pizza");

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals("pizza", comboBox.getValue());
    }

    @Test
    public void testComboBoxFillItemNotInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("food");
        comboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", comboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        response.put("food", "hamburger");

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertNull(comboBox.getValue());
    }

    @Test
    public void testComboBoxFillItemNotInItemListCustomValueAllowed() {
        //create ComponentInfo
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("food");
        comboBox.setItems("pizza", "pasta", "salad");
        comboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", comboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        response.put("food", "hamburger");

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals("hamburger", comboBox.getValue());
    }

    @Test
    public void testMSBFillItemInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of("pizza"), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemInItemListCustomValueAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");
        multiSelectComboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of("pizza"), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemsInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza", "pasta")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of("pizza", "pasta"), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemInItemsListCustomValueAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");
        multiSelectComboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza", "pasta")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of("pizza", "pasta"), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemNotInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("hamburger")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of(), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemNotInItemListCustomValueAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");
        multiSelectComboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("hamburger")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of("hamburger"), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemsNotInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("hamburger", "goulash")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertEquals( List.of(), multiSelectComboBox.getValue().stream().toList());
    }

    @Test
    public void testMSBFillItemsNotInItemListCustomValueAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");
        multiSelectComboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("hamburger", "goulash")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertTrue(multiSelectComboBox.getValue().stream().toList().contains("hamburger"));
        assertTrue(multiSelectComboBox.getValue().stream().toList().contains("goulash"));
    }

    @Test
    public void testMSBFillItemsSomeIsInItemListCustomValueNotAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza", "goulash")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertTrue(multiSelectComboBox.getValue().stream().toList().contains("pizza"));
    }

    @Test
    public void testMSBFillItemsSomeIsInItemListCustomValueAllowed() {
        //create ComponentInfo
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setId("food");
        multiSelectComboBox.setItems("pizza", "pasta", "salad");
        multiSelectComboBox.setAllowCustomValue(true);

        ComponentUtils.ComponentInfo componentInfo =
                new ComponentUtils.ComponentInfo("food", "ComboBox", multiSelectComboBox);

        //create AI response
        Map<String, Object> response = new HashMap<>();
        // create a list from pizza and pasta
        response.put("food", new ArrayList<>(List.of("pizza", "goulash")));

        //fill component
        ComponentUtils.fillComponents(List.of(componentInfo), response);
        assertTrue(multiSelectComboBox.getValue().stream().toList().contains("pizza"));
        assertTrue(multiSelectComboBox.getValue().stream().toList().contains("goulash"));
    }
}
