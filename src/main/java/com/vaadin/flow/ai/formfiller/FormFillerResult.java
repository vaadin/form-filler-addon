package com.vaadin.flow.ai.formfiller;

/**
 * FormFiller result after a {@link FormFiller#fill} call.
 * Provides information about the request for the AI module
 * and the response returned from the same AI modue.
 *
 * @author Vaadin Ltd.
 */
public class FormFillerResult {

    /**
     * Prompt request to the AI module
     */
    String request;


    /**
     * Prompt response from to the AI module
     */
    String response;

    /**
     *
     * @param request Prompt request to the AI module
     * @param response Prompt response from to the AI module
     */
    public FormFillerResult(String request, String response) {
        this.request = request;
        this.response = response;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
