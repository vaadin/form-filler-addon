package com.vaadin.flow.ai.formfiller.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtraInstructionsTool extends VerticalLayout {

    HashMap<Component, TextField> extraInstructions = new HashMap<>();

    ArrayList<TextField> contextInstructions = new ArrayList<>();

    public ExtraInstructionsTool() {
        super();
        setWidthFull();
    }

    public void setComponents(List<ComponentUtils.ComponentInfo> components) {
        removeAll();

        TextField contextTextField = new TextField("Context Instructions");
        contextTextField.setWidthFull();
        TextField contextTextField2 = new TextField("Context Instructions 2");
        contextTextField2.setWidthFull();
        TextField contextTextField3 = new TextField("Context Instructions 3");
        contextTextField3.setWidthFull();
        TextField contextTextField4 = new TextField("Context Instructions 4");
        contextTextField4.setWidthFull();

        contextInstructions.add(contextTextField);
        contextInstructions.add(contextTextField2);
        contextInstructions.add(contextTextField3);
        contextInstructions.add(contextTextField4);

        add(contextTextField, contextTextField2, contextTextField3, contextTextField4);

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

    public void setContextInstructions(int index, String contextInstruction) {
        contextInstructions.get(index).setValue(contextInstruction);
    }

    public HashMap<Component, TextField> getExtraInstructions() {
        return extraInstructions;
    }

    public ArrayList<TextField> getContextInstructions() {
        return contextInstructions;
    }
}
