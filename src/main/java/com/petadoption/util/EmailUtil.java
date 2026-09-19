package com.petadoption.util;

import java.util.Locale;

/**
 * Emails are stored and looked up in one canonical form (trimmed,
 * lowercase), so "Asha@Example.com" and "asha@example.com" are the same
 * account. Apply it wherever a user TYPES an email (register, login,
 * forgot password); every other lookup uses the already-stored value.
 */
public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalize(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
