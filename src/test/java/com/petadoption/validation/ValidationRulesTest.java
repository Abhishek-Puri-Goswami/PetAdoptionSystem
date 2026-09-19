package com.petadoption.validation;

import com.petadoption.ai.dto.ChatRequestDto;
import com.petadoption.dto.request.*;
import com.petadoption.enums.EnergyLevel;
import com.petadoption.enums.Temperament;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executable version of docs/VALIDATION_RULES.md: every rule is checked at its
 * boundary. If a rule changes, this test and the document change together.
 */
class ValidationRulesTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> invalidFields(Object dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private void assertValid(Object dto) {
        Set<ConstraintViolation<Object>> violations =
                validator.validate(dto);
        assertTrue(violations.isEmpty(), "unexpected: " + violations);
    }

    private void assertInvalid(String field, Object dto) {
        assertTrue(invalidFields(dto).contains(field),
                field + " should be invalid, got " + invalidFields(dto));
    }

    private static String repeat(int n) {
        return "a".repeat(n);
    }

    // ------------------------------------------------------------- names
    @Test
    void personNames() {
        for (String ok : new String[]{"Asha", "Mary-Jane", "O'Neil",
                "Dr. Rao", "Zoë", "李", repeat(50)}) {
            assertValid(register(ok, "Lee", "a@b.co", "Passw0rd!"));
        }
        for (String bad : new String[]{"", " ", "1Bob", "-Bob", "Bob3",
                "Bob@", repeat(51)}) {
            assertInvalid("firstName",
                    register(bad, "Lee", "a@b.co", "Passw0rd!"));
        }
    }

    // ------------------------------------------------------------ emails
    @Test
    void emails() {
        for (String ok : new String[]{"a@b.co", "first.last+tag@sub.example.org",
                "user_1@example-site.com"}) {
            assertValid(register("Asha", "Lee", ok, "Passw0rd!"));
        }
        for (String bad : new String[]{"", "plain", "a@b", "a@b.c", "@b.com",
                "a b@c.com", "a@@b.com", "a@b..com", repeat(250) + "@b.com"}) {
            assertInvalid("email",
                    register("Asha", "Lee", bad, "Passw0rd!"));
        }
    }

    // --------------------------------------------------------- passwords
    @Test
    void passwords() {
        // valid: has upper, lower, digit and special; 8-64 characters
        for (String ok : new String[]{"Password@123", "Aa1!aaaa",
                "Zz9#Zz9#", "My_Pass-w0rd", "Aa1!" + repeat(60),
                "Ab1[cdef", "Ab1\\cdef", "Ab1`cdef"}) {
            assertValid(register("Asha", "Lee", "a@b.co", ok));
        }
        for (String bad : new String[]{
                "",                        // empty
                "Aa1!aaa",                 // 7 characters
                "Aa1!" + repeat(61),       // 65 characters
                "alllowercase1!",          // no uppercase
                "ALLUPPERCASE1!",          // no lowercase
                "NoDigitsHere!!",          // no digit
                "NoSpecial1234",           // no special character
                "Pass word1",              // space is not a special character
                "12345678"}) {             // digits only
            assertInvalid("password",
                    register("Asha", "Lee", "a@b.co", bad));
        }
        assertInvalid("newPassword",
                new ResetPasswordRequestDto("tok", "nodigits"));
        assertInvalid("newPassword",
                new ResetPasswordRequestDto("tok", "NoSpecial123"));
        assertValid(new ResetPasswordRequestDto("tok", "Passw0rd!"));
    }

    @Test
    void loginOnlyChecksPresenceAndLength() {
        assertValid(new LoginRequestDto("anything", "old-weak-pw"));
        assertInvalid("password", new LoginRequestDto("a@b.co", ""));
        assertInvalid("password",
                new LoginRequestDto("a@b.co", repeat(65)));
        assertInvalid("email", new LoginRequestDto("", "x"));
    }

    // ------------------------------------------------------------ phones
    @Test
    void phones() {
        for (String ok : new String[]{"9876543210", "+919876543210",
                "1234567", "123456789012345"}) {
            assertValid(profile(ok, "560001"));
        }
        for (String bad : new String[]{"", "12345", "12-3456-7890",
                "+", "phone", "1234567890123456", "++9198765432"}) {
            assertInvalid("phone", profile(bad, "560001"));
        }
        assertValid(profile(null, null)); // optional
    }

    // ----------------------------------------------------- postal codes
    @Test
    void postalCodes() {
        for (String ok : new String[]{"560001", "SW1A 1AA", "00000",
                "12345-6789", "abc"}) {
            assertValid(profile("9876543210", ok));
        }
        for (String bad : new String[]{"", "12", " 12345", "12345 ",
                "1234567890a", "12#45"}) {
            assertInvalid("postalCode", profile("9876543210", bad));
        }
    }

    // -------------------------------------------------------------- pets
    @Test
    void pets() {
        assertValid(pet("Buddy", "Dog", 0, "Male"));
        assertValid(pet(repeat(60), "Other", 40, "Unknown"));

        assertInvalid("name", pet("", "Dog", 2, "Male"));
        assertInvalid("name", pet(repeat(61), "Dog", 2, "Male"));
        assertInvalid("species", pet("B", "DOG", 2, "Male"));
        assertInvalid("species", pet("B", "Lizard", 2, "Male"));
        assertInvalid("species", pet("B", "", 2, "Male"));
        assertInvalid("age", pet("B", "Dog", -1, "Male"));
        assertInvalid("age", pet("B", "Dog", 41, "Male"));
        assertInvalid("age", pet("B", "Dog", null, "Male"));
        assertInvalid("gender", pet("B", "Dog", 2, "MALE"));
        assertInvalid("gender", pet("B", "Dog", 2, "Boy"));
        assertInvalid("gender", pet("B", "Dog", 2, null));

        assertInvalid("energyLevel", new PetRequestDto("B", "Dog", null,
                2, "Male", null, null, Temperament.CALM, false, null));
        assertInvalid("temperament", new PetRequestDto("B", "Dog", null,
                2, "Male", null, EnergyLevel.LOW, null, false, null));
        assertInvalid("description", new PetRequestDto("B", "Dog", null,
                2, "Male", repeat(1001), EnergyLevel.LOW,
                Temperament.CALM, false, null));
        assertInvalid("specialCareNotes", new PetRequestDto("B", "Dog",
                null, 2, "Male", null, EnergyLevel.LOW,
                Temperament.CALM, false, repeat(501)));
        assertInvalid("breed", new PetRequestDto("B", "Dog", repeat(61),
                2, "Male", null, EnergyLevel.LOW, Temperament.CALM,
                false, null));
    }

    // ---------------------------------------------------------- shelters
    @Test
    void shelters() {
        assertValid(shelter("Happy Paws", "s@x.org", "1234567890",
                "Pune", "India"));
        assertInvalid("name", shelter("A", "s@x.org", "1234567890",
                "Pune", "India"));
        assertInvalid("name", shelter(repeat(101), "s@x.org",
                "1234567890", "Pune", "India"));
        assertInvalid("email", shelter("Happy", "nope", "1234567890",
                "Pune", "India"));
        assertInvalid("email", shelter("Happy", null, "1234567890",
                "Pune", "India"));
        assertInvalid("phone", shelter("Happy", "s@x.org", "12",
                "Pune", "India"));
        assertInvalid("phone", shelter("Happy", "s@x.org", null,
                "Pune", "India"));
        assertInvalid("city", shelter("Happy", "s@x.org", "1234567890",
                "", "India"));
        assertInvalid("country", shelter("Happy", "s@x.org",
                "1234567890", "Pune", ""));
    }

    // ------------------------------------------------------- applications
    @Test
    void adoptionApplications() {
        assertValid(new AdoptionRequestDto(1L, null, "Flat with balcony",
                null, null, "phone"));
        assertInvalid("petId", new AdoptionRequestDto(null, null, "x",
                null, null, "phone"));
        assertInvalid("petId", new AdoptionRequestDto(0L, null, "x",
                null, null, "phone"));
        assertInvalid("livingSituation", new AdoptionRequestDto(1L, null,
                "", null, null, "phone"));
        assertInvalid("livingSituation", new AdoptionRequestDto(1L, null,
                repeat(501), null, null, "phone"));
        assertInvalid("preferredContact", new AdoptionRequestDto(1L,
                null, "x", null, null, " "));
        assertInvalid("applicantNotes", new AdoptionRequestDto(1L,
                repeat(1001), "x", null, null, "phone"));
        assertInvalid("priorPetExperience", new AdoptionRequestDto(1L,
                null, "x", repeat(501), null, "phone"));
        assertInvalid("householdDetails", new AdoptionRequestDto(1L,
                null, "x", null, repeat(501), "phone"));
        assertValid(new AdoptionDecisionRequestDto(null));
        assertValid(new AdoptionDecisionRequestDto(repeat(500)));
        assertInvalid("notes", new AdoptionDecisionRequestDto(repeat(501)));
    }

    // ----------------------------------------- appointments / slots / misc
    @Test
    void appointmentsSlotsAndRecords() {
        assertValid(new AppointmentRequestDto(1L, 2L, null));
        assertInvalid("petId", new AppointmentRequestDto(null, 2L, null));
        assertInvalid("slotId", new AppointmentRequestDto(1L, -3L, null));
        assertInvalid("notes",
                new AppointmentRequestDto(1L, 2L, repeat(501)));

        assertValid(new AvailabilitySlotRequestDto(
                LocalDateTime.now().plusDays(1)));
        assertInvalid("slotDateTime", new AvailabilitySlotRequestDto(
                LocalDateTime.now().minusMinutes(1)));
        assertInvalid("slotDateTime", new AvailabilitySlotRequestDto(null));

        assertValid(new MedicalRecordRequestDto(LocalDate.now(),
                "Vaccination", null));
        assertInvalid("recordDate", new MedicalRecordRequestDto(
                LocalDate.now().plusDays(1), "x", null));
        assertInvalid("recordDate",
                new MedicalRecordRequestDto(null, "x", null));
        assertInvalid("description",
                new MedicalRecordRequestDto(LocalDate.now(), "", null));
        assertInvalid("description", new MedicalRecordRequestDto(
                LocalDate.now(), repeat(1001), null));
        assertInvalid("vetName", new MedicalRecordRequestDto(
                LocalDate.now(), "x", repeat(101)));
    }

    @Test
    void rolesShelterAssignmentAndChat() {
        assertValid(new UpdateUserRoleRequestDto(
                Set.of("ROLE_ADOPTER", "ROLE_SHELTER_ADMIN")));
        assertInvalid("roles", new UpdateUserRoleRequestDto(Set.of()));
        assertTrue(validator.validate(
                new UpdateUserRoleRequestDto(Set.of("ROLE_ROOT")))
                .size() > 0);
        assertTrue(validator.validate(
                new UpdateUserRoleRequestDto(Set.of("ADMIN")))
                .size() > 0);

        assertValid(new AssignShelterRequestDto(null));
        assertValid(new AssignShelterRequestDto(3L));
        assertInvalid("shelterId", new AssignShelterRequestDto(0L));

        assertValid(new ChatRequestDto(repeat(1000)));
        assertInvalid("message", new ChatRequestDto(repeat(1001)));
        assertInvalid("message", new ChatRequestDto(" "));
    }

    // ----------------------------------------------------------- helpers
    private RegisterRequestDto register(
            String first, String last, String email, String password) {
        return new RegisterRequestDto(first, last, email, password);
    }

    private UpdateProfileRequestDto profile(String phone, String postal) {
        return new UpdateProfileRequestDto("Asha", "Lee", phone,
                null, null, null, null, postal, null);
    }

    private PetRequestDto pet(
            String name, String species, Integer age, String gender) {
        return new PetRequestDto(name, species, null, age, gender, null,
                EnergyLevel.LOW, Temperament.CALM, false, null);
    }

    private ShelterRequestDto shelter(String name, String email,
                                      String phone, String city,
                                      String country) {
        return new ShelterRequestDto(name, email, phone, null, null,
                city, null, null, country, null);
    }
}
