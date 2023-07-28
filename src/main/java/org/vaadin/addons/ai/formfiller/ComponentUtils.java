package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Util class to deal with a component and its children
 * (get children, map hierarchy, map types,...)
 *
 * @author Vaadin Ltd.
 */
public class ComponentUtils {
    private static final Logger logger = LoggerFactory.getLogger(ComponentUtils.class);

    /**
     * Record with information of the hierarchy of the components to be filled and
     * the value types of each one of them.
     *
     * @param componentsJSONMap      the components' hierarchy where parent and children are
     *                               described in a map
     * @param componentsTypesJSONMap the components' value types
     */
    public record ComponentsMapping(List<ComponentInfo> components, Map<String, Object> componentsJSONMap, Map<String, String> componentsTypesJSONMap) {
    }

    /**
     * Record with information of a component to be filled.
     *
     * @param id        the component id
     * @param type      the component type
     * @param component the component
     */
    public record ComponentInfo(String id, String type, Component component) {
    }

    /**
     * Creates the mapped structures with the information required to generate the
     * prompt and fill the components after the response. This includes the hierarchy
     * of components and the value types of each component.
     *
     * @param component the component target (can be a component or a container of components)
     * @return the mapped structures with the information
     */
    public static ComponentsMapping createMapping(Component component) {
        List<ComponentInfo> componentInfoList = new ArrayList<>();
        findChildComponents(component, componentInfoList);
        ComponentsMapping mapping = new ComponentsMapping(componentInfoList, buildHierarchy(componentInfoList), buildTypes(componentInfoList));
        return mapping;
    }

    private static void findChildComponents(Component component, List<ComponentInfo> componentInfoList) {
        component.getChildren().forEach(childComponent -> {
            String componentType = childComponent.getClass().getSimpleName();
            String id = childComponent.getId().orElse(null);

            if (id != null) {
                ComponentInfo info = new ComponentInfo(id, componentType, childComponent);
                componentInfoList.add(info);
            }
            findChildComponents(childComponent, componentInfoList);
        });
    }

    private static Map<String, Object> buildHierarchy(List<ComponentInfo> componentInfoList) {
        Map<String, Object> json = new HashMap<>();
        for (ComponentInfo componentInfo : componentInfoList) {
            if (componentInfo.type.equalsIgnoreCase("Column"))
                continue;
            String id = componentInfo.id;
            if (id != null && !id.isEmpty()) {
                if (componentInfo.component instanceof Grid
                        || componentInfo.component instanceof MultiSelectListBox) {
                    HashMap<String, Object> columns = new HashMap<>();
                    if (componentInfo.component instanceof Grid) {
                        Grid<?> grid = (Grid<?>) componentInfo.component;
                        grid.getColumns().forEach(c -> columns.put(c.getId().get(), ""));
                        ArrayList<HashMap<String, Object>> listColumns = new ArrayList<>();
                        listColumns.add(columns);
                        json.put(id, listColumns);
                    } else {
                        json.put(id, new ArrayList<>());
                    }
                } else {
                    json.put(id, "");
                }
            }
        }
        return json;
    }

    /**
     * Get all the components expected types to ask the LLM model.
     * It is important to notice that the type description should be
     * understandable by the LLM, we are not talking about any specific
     * coding language type or class. This type helps the LLM to format
     * the value inside the response JSON.
     *
     * TODO: Research about custom class values like for ComboBox. Is it
     * better to ask always for String or is it better to ask for the specific
     * custom class if it has a meaningful name?
     *
     * @param componentInfoList a list of components
     * @return the list of expected types.
     */
    private static Map<String, String> buildTypes(List<ComponentInfo> componentInfoList) {
        Map<String, String> inputFieldMap = new HashMap<>();
        for (ComponentInfo componentInfo : componentInfoList) {
            try {
                if ((componentInfo.component instanceof TextField)
                        || (componentInfo.component instanceof TextArea)) {
                    inputFieldMap.put(componentInfo.id, "a String");
                } else if ((componentInfo.component instanceof NumberField)) {
                    inputFieldMap.put(componentInfo.id, "a Number");
                } else if ((componentInfo.component instanceof DatePicker)) {
                    inputFieldMap.put(componentInfo.id, "a date using format 'yyyy-MM-dd'");
                } else if ((componentInfo.component instanceof TimePicker)) {
                    inputFieldMap.put(componentInfo.id, "a time using format 'HH:mm:ss'");
                } else if ((componentInfo.component instanceof DateTimePicker)) {
                    inputFieldMap.put(componentInfo.id, "a date and time using format 'yyyy-MM-ddTHH:mm:ss'");
                } else if ((componentInfo.component instanceof ComboBox<?>)) {
                    StringJoiner joiner = new StringJoiner("\" OR \"");
                    ((ComboBox<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a String from one of these options \"" + joiner.toString()+"\"");
                } else if (componentInfo.component instanceof MultiSelectComboBox) {
                    inputFieldMap.put(componentInfo.id, "a String");
                } else if ((componentInfo.component instanceof Checkbox)) {
                    inputFieldMap.put(componentInfo.id, "a Boolean");
                } else if ((componentInfo.component instanceof CheckboxGroup<?>)) {
                    StringJoiner joiner = new StringJoiner("\", \"");
                    ((CheckboxGroup<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a Set of Strings selecting none, one or more of these options  \"" + joiner.toString()+"\"");
                } else if ((componentInfo.component instanceof RadioButtonGroup<?>)) {
                    StringJoiner joiner = new StringJoiner("\" OR \"");
                    ((RadioButtonGroup<String>) componentInfo.component).getListDataView().getItems().forEach(joiner::add);
                    inputFieldMap.put(componentInfo.id, "a String from one of these options \"" + joiner.toString()+"\"");
                } else if (componentInfo.component instanceof Grid.Column<?>) {
                    // Nothing to do as columns are managed in the Grid case
                } else if (componentInfo.component instanceof Grid<?>) {
                    Grid inspectedComponent = (Grid) componentInfo.component;
                    for (Field f : inspectedComponent.getBeanType().getDeclaredFields()) {
                        if (f.getType().getSimpleName().equalsIgnoreCase("Date") || f.getType().getSimpleName().equalsIgnoreCase("LocalDate"))
                        inputFieldMap.put(f.getName(), "a date using format 'yyyy-MM-dd'");
                        else if (f.getType().getSimpleName().equalsIgnoreCase("Time") || f.getType().getSimpleName().equalsIgnoreCase("LocalTime"))
                            inputFieldMap.put(f.getName(), "a time using format 'HH:mm:ss'");
                        else if (f.getType().getSimpleName().equalsIgnoreCase("DateTime") || f.getType().getSimpleName().equalsIgnoreCase("LocalDateTime"))
                            inputFieldMap.put(f.getName(), "a date and time using format 'yyyy-MM-ddTHH:mm:ss'");
                        else if (f.getType().getSimpleName().equalsIgnoreCase("Boolean"))
                            inputFieldMap.put(f.getName(), "a Boolean");
                        else if (f.getType().getSimpleName().equalsIgnoreCase("Integer") || f.getType().getSimpleName().equalsIgnoreCase("Long")
                                || f.getType().getSimpleName().equalsIgnoreCase("Double")|| f.getType().getSimpleName().equalsIgnoreCase("Float"))
                            inputFieldMap.put(f.getName(), "a Number");
                        else
                            inputFieldMap.put(f.getName(), "a String");
                    }
                }
            } catch (Exception e) {
                logger.error("Error while inferring type of component" + "Component: " + componentInfo.id + " - " + componentInfo.component.getClass().getSimpleName());
            }
        }
        return inputFieldMap;
    }

    /**
     * Get all children from a parent component
     *
     * @param component target
     * @return Stream of all component children
     */
    public static Stream<Component> getAllChildren(Component component) {
        return Stream.concat(
                Stream.of(component),
                component.getChildren().flatMap(ComponentUtils::getAllChildren));
    }


    /**
     * Fills the component(s) of the target component using the map
     * with components and values.
     *
     * @param mapComponentValues Transformed AI module response to a map with components
     *                           and values.
     */
    public static void fillComponents(List<ComponentInfo> components, Map<String, Object> mapComponentValues) {

        for (ComponentInfo componentInfo : components) {
            if (componentInfo.component.getId().orElse(null) == null) {
                logger.warn("Component has no id so it will be skipped: {}", componentInfo.component);
                continue;
            }
            String id = componentInfo.component.getId().orElse(null);
            try {
                if (id != null) {
                    Object responseValue = mapComponentValues.get(id);
                    if (responseValue == null) {
                        logger.warn("No response value found for id: {}", id);
                        continue;
                    }

                    if (componentInfo.component instanceof Grid) {
                        try {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) responseValue;
                            Grid<?> grid = (Grid<?>) componentInfo.component;
                            Class<?> beanType = grid.getBeanType();
                            fillGridWithWildcards(grid, items, beanType);
                        } catch (Exception e) {
                            logger.error("Error while updating grid with wildcards", e.getMessage());
                        }
                    } else if (componentInfo.component instanceof TextField) {
                        TextField textField = (TextField) componentInfo.component;
                        textField.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof TextArea) {
                        TextArea textArea = (TextArea) componentInfo.component;
                        textArea.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof NumberField) {
                        NumberField numberField = (NumberField) componentInfo.component;
                        numberField.setValue(Double.valueOf(responseValue.toString()));
                    } else if (componentInfo.component instanceof DatePicker) {
                        DatePicker datePicker = (DatePicker) componentInfo.component;
                        datePicker.setValue(LocalDate.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof TimePicker) {
                        TimePicker timePicker = (TimePicker) componentInfo.component;
                        timePicker.setValue(LocalTime.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof DateTimePicker) {
                        DateTimePicker datetimePicker = (DateTimePicker) componentInfo.component;
                        datetimePicker.setValue(LocalDateTime.parse(responseValue.toString()));
                    } else if (componentInfo.component instanceof ComboBox) {
                        ComboBox comboBox = (ComboBox) componentInfo.component;
                        comboBox.setValue(responseValue);
                    } else if (componentInfo.component instanceof MultiSelectComboBox) {
                        MultiSelectComboBox multiSelectComboBox = (MultiSelectComboBox) componentInfo.component;
                        multiSelectComboBox.setValue(responseValue);
                    } else if (componentInfo.component instanceof Checkbox) {
                        Checkbox checkbox = (Checkbox) componentInfo.component;
                        checkbox.setValue((Boolean) responseValue);
                    }  else if (componentInfo.component instanceof CheckboxGroup<?>) {
                        CheckboxGroup<String> checkboxgroup = (CheckboxGroup<String>) componentInfo.component;
                        try {
                            ArrayList<String> list = (ArrayList<String>) responseValue;
                            Set<String> set = new HashSet<String>(list);
                            checkboxgroup.setValue(set);
                        } catch (Exception e) {
                            logger.error("Error while updating checkboxgroup with id: {}", id, e);
                        }
                    } else if (componentInfo.component instanceof RadioButtonGroup<?>) {
                        RadioButtonGroup radioButtonGroup = (RadioButtonGroup) componentInfo.component;
                        radioButtonGroup.setValue(responseValue.toString());
                    } else if (componentInfo.component instanceof Grid.Column<?>) {
                        // Nothing to do as it is managed in Grid
                    }

                }
            } catch (Exception e) {
                logger.error("Error while updating component with id: {} Cause: {}", id, e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void fillGridWithWildcards(Grid<T> grid, List<Map<String, Object>> items, Class<?> beanType) {
        fillGrid((Grid<T>) grid, items, (Class<T>) beanType);
    }

    private static <T> void fillGrid(Grid<T> grid, List<Map<String, Object>> items, Class<T> itemClass) {
        if (items == null) {
            logger.warn("Items list is null. Skipping the update for the grid.");
            return;
        }

        List<T> gridItems = items.stream().map(itemMap -> {
            T item;
            try {
                item = itemClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create a new instance of the item class", e);
            }

            for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                String propName = entry.getKey();
                Object propValue = entry.getValue();
                Grid.Column<T> column = grid.getColumnByKey(propName);
                if (column != null && column.getEditorComponent() != null) {
                    column.getEditorComponent().getElement().setProperty("value", propValue.toString());
                }

                try {

                    Field field = itemClass.getDeclaredField(propName);
                    field.setAccessible(true);
                    if (field.getType().equals(LocalDate.class))
                        field.set(item, LocalDate.parse(propValue.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    else if (field.getType().equals(LocalTime.class))
                        field.set(item, LocalTime.parse(propValue.toString(), DateTimeFormatter.ofPattern("HH:mm:ss")));
                    else if (field.getType().equals(LocalDateTime.class))
                        field.set(item, LocalDateTime.parse(propValue.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                    else if (field.getType().equals(Double.class))
                        field.set(item, Double.valueOf(propValue.toString()));
                    else if (field.getType().equals(Integer.class))
                        field.set(item, Integer.valueOf(propValue.toString()));
                    else if (field.getType().equals(Long.class))
                        field.set(item, Long.valueOf(propValue.toString()));
                    else if (field.getType().equals(Float.class))
                        field.set(item, Float.valueOf(propValue.toString()));
                    else if (field.getType().equals(Boolean.class))
                        field.set(item, Boolean.valueOf(propValue.toString()));
                    else
                    field.set(item, propValue);

                } catch (Exception e) {
                    logger.error("Failed to set field value for '{}': {}", propName, e.getMessage());
                }
            }
            return item;
        }).collect(Collectors.toList());

        grid.setItems(gridItems);
    }
}
