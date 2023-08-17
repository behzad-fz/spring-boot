package com.bank.validtionRule;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM dd yyyy");

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateValue = p.getValueAsString();
        try {
            return LocalDate.parse(dateValue, FORMATTER);
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + dateValue, e);
        }
    }
}
