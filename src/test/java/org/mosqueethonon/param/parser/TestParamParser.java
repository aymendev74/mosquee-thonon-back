package org.mosqueethonon.param.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class TestParamParser {

    private final ParamParser underTest = new ParamParser(new BooleanParamValueParser(), new DateParamValueParser());

    @Test
    public void testValeurNulleDonneNull() {
        assertNull(underTest.parseValue(null, Boolean.class));
    }

    @Test
    public void testValeurVideDonneNull() {
        assertNull(underTest.parseValue("", LocalDate.class));
    }

    @Test
    public void testParseUnBooleenObjet() {
        assertEquals(Boolean.TRUE, underTest.parseValue("true", Boolean.class));
        assertEquals(Boolean.FALSE, underTest.parseValue("false", Boolean.class));
    }

    @Test
    public void testParseUnBooleenPrimitif() {
        assertEquals(Boolean.TRUE, underTest.parseValue("true", boolean.class));
    }

    @Test
    public void testParseUneDate() {
        assertEquals(LocalDate.of(2025, 9, 1), underTest.parseValue("01.09.2025", LocalDate.class));
    }

    @Test
    public void testTypeNonGereEstRejete() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> underTest.parseValue("42", Integer.class));

        assertTrue(exception.getMessage().contains("Integer"));
    }
}
