package com.petadoption.ai.coordinator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteTest {

    @Test
    void shouldParseExactLabelsCaseAndWhitespaceInsensitive() {
        assertEquals(Route.MATCHING, Route.parse("MATCHING"));
        assertEquals(Route.CARE, Route.parse("  care\n"));
        assertEquals(Route.ADOPTION, Route.parse("Adoption"));
    }

    @Test
    void shouldFallBackToUnsureForNullOrUnknownText() {
        assertEquals(Route.UNSURE, Route.parse(null));
        assertEquals(Route.UNSURE, Route.parse(""));
        assertEquals(Route.UNSURE, Route.parse("MATCHING or CARE"));
        assertEquals(Route.UNSURE, Route.parse("DELETE_EVERYTHING"));
    }
}
