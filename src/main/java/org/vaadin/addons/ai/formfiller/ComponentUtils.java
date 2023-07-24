package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public record ComponentsMapping(Map<String, Object> componentsJSONMap, Map<String, String> componentsTypesJSONMap) {
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
        ComponentsMapping mapping = new ComponentsMapping(buildHierarchy(componentInfoList), buildTypes(componentInfoList));
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
                        || (componentInfo.component instanceof TextArea)
                        || (componentInfo.component instanceof TextArea)) {
                    inputFieldMap.put(componentInfo.id, "String");
                } else if ((componentInfo.component instanceof DatePicker)) {
                    inputFieldMap.put(componentInfo.id, "Date");
                } else if ((componentInfo.component instanceof DateTimePicker)) {
                    inputFieldMap.put(componentInfo.id, "Date and Time");
                } else if ((componentInfo.component instanceof NumberField)) {
                    inputFieldMap.put(componentInfo.id, "Number");
                }
                else if (componentInfo.component instanceof Grid.Column<?>) {
                    // Nothing to do as columns are managed in the Grid case
                } else if (componentInfo.component instanceof Grid<?>) {
                    Grid inspectedComponent = (Grid) componentInfo.component;
                    for (Field f : inspectedComponent.getBeanType().getDeclaredFields()) {
                        inputFieldMap.put(f.getName(), f.getType().getSimpleName());
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

}
