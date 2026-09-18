package com.petadoption.ai.coordinator;

public enum Route {

    MATCHING,
    CARE,
    ADOPTION,
    UNSURE;

    /**
     * Parses the classifier's raw text. Anything that is not exactly
     * one known label (after trimming/uppercasing) is UNSURE - the
     * coordinator never guesses a destination.
     */
    public static Route parse(String raw) {

        if (raw == null) {
            return UNSURE;
        }

        String cleaned = raw.trim().toUpperCase();

        for (Route route : values()) {
            if (route.name().equals(cleaned)) {
                return route;
            }
        }

        return UNSURE;
    }
}
