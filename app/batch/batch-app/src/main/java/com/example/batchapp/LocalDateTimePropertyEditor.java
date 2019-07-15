package com.example.batchapp;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.util.StringUtils;

public class LocalDateTimePropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {

        if (StringUtils.isEmpty(text)) {
            setValue(null);
            return;
        }

        try {
            setValue(LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getAsText() {
        Object date = getValue();
        if (date != null) {
            return ((LocalDateTime) getValue()).format(DateTimeFormatter.ISO_DATE_TIME);
        }
        return "";
    }
}
