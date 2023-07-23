package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
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

    public record ComponentsMapping(Map<String, Object> componentsJSONMap, Map<String, String> componentsTypesJSONMap) {
    }

    public static ComponentsMapping createMapping(Component component) {
        List<FormFillerComponentInfo> componentInfoList = new ArrayList<>();
        findChildComponents(component, componentInfoList);
        ComponentsMapping mapping = new ComponentsMapping(buildJsonFromComponentInfoList(componentInfoList), buildTypesJsonFromComponentInfoList(componentInfoList));

        return mapping;
    }

    private static void findChildComponents(Component component, List<FormFillerComponentInfo> componentInfoList) {
        component.getChildren().forEach(childComponent -> {
            String componentType = childComponent.getClass().getSimpleName();
            String id = childComponent.getId().orElse(null);

            if (id != null) {
                FormFillerComponentInfo info = new FormFillerComponentInfo(id, componentType, childComponent);
                componentInfoList.add(info);

                if (isContainedInCustomForm(childComponent)) {
                    // Handle the custom form case by finding child components inside the custom
                    // form.
                    findChildComponents(childComponent, info.getChildren());
                } else {
                    findChildComponents(childComponent, componentInfoList);
                }
            } else {
                findChildComponents(childComponent, componentInfoList);
            }
        });
    }

    private static Map<String, Object> buildJsonFromComponentInfoList(List<FormFillerComponentInfo> componentInfoList) {
        Map<String, Object> json = new HashMap<>();
        for (FormFillerComponentInfo componentInfo : componentInfoList) {
            if (componentInfo.getType().equalsIgnoreCase("Column"))
                continue;
            String id = componentInfo.getId();
            if (id != null && !id.isEmpty()) {
                if (isContainedInCustomForm(componentInfo.getComponent())) {
                    json.put(id, buildJsonFromComponentInfoList(componentInfo.getChildren()));
                } else if (componentInfo.getComponent() instanceof Grid
                        || componentInfo.getComponent() instanceof MultiSelectListBox) {
                    HashMap<String, Object> columns = new HashMap<>();
                    if (componentInfo.getComponent() instanceof Grid) {
                        Grid<?> grid = (Grid<?>) componentInfo.getComponent();
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

    private static boolean isContainedInCustomForm(Component component) {
        if (component == null) {
            return false;
        }

        String className = component.getClass().getSimpleName();
        if (className.endsWith("Form")) {
            return true;
        }

        List<Component> children = component.getChildren().collect(Collectors.toList());
        if (children.isEmpty()) {
            return false;
        }

        boolean hasFormField = false;
        for (Component child : children) {
            if (child instanceof TextField || child instanceof ComboBox || child instanceof Grid) {
                hasFormField = true;
                break;
            }
        }

        return hasFormField;
    }

    private static Map<String, String> buildTypesJsonFromComponentInfoList(List<FormFillerComponentInfo> componentInfoList) {
        Map<String, String> inputFieldMap = new HashMap<>();
        for (FormFillerComponentInfo componentInfo : componentInfoList) {
            try {
                if (componentInfo.getComponent() instanceof HasValue<?, ?>) {
                    HasValue inspectedComponent = (HasValue) componentInfo.getComponent();
                    inputFieldMap.put(componentInfo.getId(), inspectedComponent.getValue().getClass().getSimpleName());
                } else if (componentInfo.getComponent() instanceof Grid.Column<?>) {
                    // Nothing to do as columns are managed in the Grid case
                } else if (componentInfo.getComponent() instanceof Grid<?>) {
                    Grid inspectedComponent = (Grid) componentInfo.getComponent();
                    for (Field f : inspectedComponent.getBeanType().getDeclaredFields()) {
                        inputFieldMap.put(f.getName(), f.getType().getSimpleName());
                    }
                }
            } catch (Exception e) {
                logger.error("Error while inferring type of component"+ "Component: " + componentInfo.getId() + " - " + componentInfo.getComponent().getClass().getSimpleName());
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
