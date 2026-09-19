package com.petadoption.validation;

/**
 * Single source of truth for every validation limit and pattern.
 *
 * The request DTOs (@Size / @Pattern) and the entity columns
 * (@Column length) both use these constants, so the API and the database
 * cannot drift apart. The same rules are written out for the frontend in
 * docs/VALIDATION_RULES.md: change a value here, change it there.
 */
public final class Rules {

    private Rules() {
    }

    // ---- people ----
    public static final int NAME_MAX = 50;
    public static final String NAME_REGEX = "^[\\p{L}][\\p{L} .'-]*$";
    public static final String NAME_MESSAGE =
            "Name must be 1-50 characters: letters, spaces, . ' -";

    public static final int EMAIL_MAX = 254;
    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";
    public static final String EMAIL_MESSAGE =
            "Email must be a valid address of at most 254 characters";

    public static final int PASSWORD_MAX = 64;
    // 8-64 chars with at least one lowercase, one uppercase, one digit and
    // one special character from:  ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : " \ | , . < > / ? ~ `
    // (64 max because BCrypt only uses the first 72 bytes.)
    public static final String PASSWORD_SPECIAL_CHARS =
            "!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`";
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])"
                    + "(?=.*[" + PASSWORD_SPECIAL_CHARS + "]).{8,64}$";
    public static final String PASSWORD_MESSAGE =
            "Password must be 8-64 characters with an uppercase letter, "
                    + "a lowercase letter, a digit and a special character";

    public static final int PHONE_MAX = 16;
    public static final String PHONE_REGEX = "^\\+?[0-9]{7,15}$";
    public static final String PHONE_MESSAGE =
            "Phone must be 7-15 digits, optionally starting with +";

    // ---- address ----
    public static final int ADDRESS_LINE_MAX = 100;
    public static final int PLACE_MAX = 60;
    public static final int POSTAL_MAX = 10;
    public static final String POSTAL_REGEX =
            "^[A-Za-z0-9][A-Za-z0-9 -]{1,8}[A-Za-z0-9]$";
    public static final String POSTAL_MESSAGE =
            "Postal code must be 3-10 letters, digits, spaces or hyphens";

    // ---- shelter ----
    public static final int SHELTER_NAME_MAX = 100;

    // ---- pet ----
    public static final int PET_NAME_MAX = 60;
    public static final int PET_BREED_MAX = 60;
    public static final int PET_AGE_MAX = 40;
    public static final int SPECIES_MAX = 20;
    public static final String SPECIES_REGEX = "^(Dog|Cat|Rabbit|Bird|Other)$";
    public static final String SPECIES_MESSAGE =
            "Species must be one of: Dog, Cat, Rabbit, Bird, Other";
    public static final int GENDER_MAX = 10;
    public static final String GENDER_REGEX = "^(Male|Female|Unknown)$";
    public static final String GENDER_MESSAGE =
            "Gender must be one of: Male, Female, Unknown";

    // ---- free text ----
    public static final int DESCRIPTION_MAX = 1000;
    public static final int NOTES_MAX = 500;
    public static final int APPLICANT_NOTES_MAX = 1000;
    public static final int PREFERRED_CONTACT_MAX = 100;
    public static final int VET_NAME_MAX = 100;
    public static final int TOKEN_MAX = 100;
    public static final int CHAT_MESSAGE_MAX = 1000;

    // ---- roles ----
    public static final String ROLE_REGEX =
            "^ROLE_(SYSTEM_ADMIN|SHELTER_ADMIN|SHELTER_STAFF|ADOPTER)$";
    public static final String ROLE_MESSAGE =
            "Role must be ROLE_SYSTEM_ADMIN, ROLE_SHELTER_ADMIN, ROLE_SHELTER_STAFF or ROLE_ADOPTER";

    // ---- search query params ----
    public static final int SEARCH_MAX = 100;
}
