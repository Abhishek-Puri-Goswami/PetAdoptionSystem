package com.petadoption.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateBuilderTest {

    @Test
    void shouldBuildWelcomeEmail() {

        String email =
                EmailTemplateBuilder.welcomeEmail(
                        "Abhishek");

        assertThat(email)
                .contains("Welcome to Pet Adoption System");

        assertThat(email)
                .contains("Abhishek");
    }

    @Test
    void shouldBuildAdoptionApprovedEmail() {

        String email =
                EmailTemplateBuilder.adoptionApproved(
                        "Abhishek");

        assertThat(email)
                .contains("approved");
    }

    @Test
    void shouldBuildAppointmentApprovedEmail() {

        String email =
                EmailTemplateBuilder.appointmentApproved(
                        "Abhishek",
                        "Buddy");

        assertThat(email)
                .contains("Buddy");

        assertThat(email)
                .contains("approved");
    }
}