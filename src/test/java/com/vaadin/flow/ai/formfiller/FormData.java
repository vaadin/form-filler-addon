package com.vaadin.flow.ai.formfiller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FormData {
    String name = "";
    String address = "";
    String phone = "";
    Integer age = null;
    String email = "";
    String clientId = "";
    LocalDateTime creationDate = null;
    LocalDate dueDate = null;
    String orderEntity = "";
    Integer orderTotal = null;
    Double orderTaxes = null;
    String orderDescription = "";
    String paymentMethod = "";
    Boolean isFinnishCustomer = false;
    List<String> typeService = new ArrayList<>();
}
