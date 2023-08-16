package com.vaadin.flow.ai.formfiller.utils;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class DebugTool extends FormLayout {
    TextArea debugInput = new TextArea("Debug Input Source");
    TextArea debugJsonTarget = new TextArea("Debug JSON target");
    TextArea debugTypesTarget = new TextArea("Debug Type target");
    TextArea debugPrompt = new TextArea("Debug Prompt");
    TextArea debugResponse = new TextArea("Debug Response");

    public DebugTool() {
        super();
        setWidthFull();

        debugInput.setWidthFull();
        debugInput.setHeight("600px");

        debugJsonTarget.setWidthFull();
        debugJsonTarget.setHeight("600px");

        debugTypesTarget.setWidthFull();
        debugTypesTarget.setHeight("600px");

        debugPrompt.setWidthFull();
        debugPrompt.setHeight("600px");

        debugResponse.setWidthFull();
        debugResponse.setHeight("600px");

        add(debugInput, debugJsonTarget, debugTypesTarget, debugPrompt, debugResponse);

    }

    public TextArea getDebugInput() {
        return debugInput;
    }

    public TextArea getDebugJsonTarget() {
        return debugJsonTarget;
    }

    public TextArea getDebugTypesTarget() {
        return debugTypesTarget;
    }

    public TextArea getDebugPrompt() {
        return debugPrompt;
    }

    public TextArea getDebugResponse() {
        return debugResponse;
    }
}
