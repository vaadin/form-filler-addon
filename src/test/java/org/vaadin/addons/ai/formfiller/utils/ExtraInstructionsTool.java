package org.vaadin.addons.ai.formfiller.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.HashMap;
import java.util.List;

public class ExtraInstructionsTool extends VerticalLayout {

    HashMap<Component, TextField> extraInstructions = new HashMap<>();

    public ExtraInstructionsTool() {
        super();
        setWidthFull();
    }

    public void setComponents(List<ComponentUtils.ComponentInfo> components) {
        removeAll();

        for (ComponentUtils.ComponentInfo component : components) {
            if (component.component().getId() == null) {
                continue;
            }
            TextField textField = new TextField("" + component.id());
            textField.setWidthFull();
            extraInstructions.put(component.component(), textField);
            add(textField);
        }
    }

    public void setExtraInstructions(Component component, String extraInstruction) {
        if (extraInstructions.containsKey(component)) {
            extraInstructions.get(component).setValue(extraInstruction);
        }
    }

    public HashMap<Component, TextField> getExtraInstructions() {
        return extraInstructions;
    }
}
