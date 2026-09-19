package com.petadoption.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailUtilTest {

    @Test
    void shouldLowercaseAndTrim() {
        assertEquals("asha@example.com",
                EmailUtil.normalize("Asha@Example.COM"));
        assertEquals("asha@example.com",
                EmailUtil.normalize("  asha@example.com \n"));
        assertEquals("a.b+tag@x.org",
                EmailUtil.normalize("A.B+Tag@X.org"));
    }

    @Test
    void shouldLeaveAlreadyNormalEmailUntouched() {
        assertEquals("asha@example.com",
                EmailUtil.normalize("asha@example.com"));
    }

    @Test
    void shouldNotDependOnTheDefaultLocale() {
        // Turkish dotted/dotless i must not change the result.
        java.util.Locale original = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            assertEquals("info@site.com", EmailUtil.normalize("INFO@SITE.COM"));
        } finally {
            java.util.Locale.setDefault(original);
        }
    }

    @Test
    void shouldPassNullThrough() {
        assertNull(EmailUtil.normalize(null));
    }
}
