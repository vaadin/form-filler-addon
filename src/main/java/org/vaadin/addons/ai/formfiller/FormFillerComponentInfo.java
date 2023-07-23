package org.vaadin.addons.ai.formfiller;

import com.vaadin.flow.component.Component;

import java.util.ArrayList;
import java.util.List;

public class FormFillerComponentInfo {
    private final String id;
    private final String type;
    private final Component component;

    public FormFillerComponentInfo(String id, String type, Component component) {
        this.id = id;
        this.type = type;
        this.component = component;
    }

    private final List<FormFillerComponentInfo> children = new ArrayList<>();

    public void addChild(FormFillerComponentInfo child) {
        children.add(child);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Component getComponent() {
        return component;
    }

    public List<FormFillerComponentInfo> getChildren() {
        return children;
    }
}
