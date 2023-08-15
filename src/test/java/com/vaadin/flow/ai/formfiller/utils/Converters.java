package com.vaadin.flow.ai.formfiller.utils;

import com.vaadin.flow.function.SerializableFunction;

import java.time.LocalDate;
import java.util.Date;

public class Converters {

    public static SerializableFunction<Date, LocalDate> dateToLocalDate() {
        return date -> date == null ? null : new java.sql.Date(date.getTime()).toLocalDate();
    }
    public static SerializableFunction<LocalDate, Date> localDateToDate() {
        return localDate -> localDate == null ? null : java.sql.Date.valueOf(localDate);
    }
}
