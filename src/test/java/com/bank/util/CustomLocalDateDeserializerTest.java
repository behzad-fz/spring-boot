package com.bank.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomLocalDateDeserializerTest {

    static class Holder {
        @JsonDeserialize(using = CustomLocalDateDeserializer.class)
        LocalDate date;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesLongForm() throws Exception {
        Holder holder = mapper.readValue("{\"date\":\"August 16 2026\"}", Holder.class);
        assertEquals(LocalDate.of(2026, 8, 16), holder.date);
    }

    @Test
    void parsesIsoDate() throws Exception {
        Holder holder = mapper.readValue("{\"date\":\"2026-08-16\"}", Holder.class);
        assertEquals(LocalDate.of(2026, 8, 16), holder.date);
    }

    @Test
    void parsesSlashFormats() throws Exception {
        Holder dmy = mapper.readValue("{\"date\":\"16/08/2026\"}", Holder.class);
        assertEquals(LocalDate.of(2026, 8, 16), dmy.date);

        Holder mdy = mapper.readValue("{\"date\":\"08/16/2026\"}", Holder.class);
        assertEquals(LocalDate.of(2026, 8, 16), mdy.date);
    }

    @Test
    void malformedDateReportsClearError() {
        JsonMappingException ex = assertThrows(JsonMappingException.class,
                () -> mapper.readValue("{\"date\":\"not-a-date\"}", Holder.class));
        assertTrue(ex.getMessage().contains("Accepted formats"),
                "error should list accepted formats, was: " + ex.getMessage());
    }

    @Test
    void missingDateIsRejected() {
        assertThrows(JsonMappingException.class,
                () -> mapper.readValue("{\"date\":\"\"}", Holder.class));
    }
}
