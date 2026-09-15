package com.petadoption.notification;

public final class EmailTemplateBuilder {

    private EmailTemplateBuilder() {
    }

    public static String welcomeEmail(
            String firstName) {

        return """
                Hello %s,

                Welcome to Pet Adoption System.

                Your account has been created successfully.

                Regards,
                Pet Adoption Team
                """
                .formatted(firstName);
    }

    public static String adoptionApproved(
            String firstName) {

        return """
                Hello %s,

                Congratulations!

                Your adoption application
                has been approved.

                Regards,
                Pet Adoption Team
                """
                .formatted(firstName);
    }

    public static String adoptionRejected(
            String firstName) {

        return """
                Hello %s,

                We are sorry.

                Your adoption application
                was not approved.

                Regards,
                Pet Adoption Team
                """
                .formatted(firstName);
    }

    public static String appointmentApproved(
            String firstName,
            String petName) {

        return """
            Hello %s,

            Your appointment request has been approved.

            Pet:
            %s

            We look forward to meeting you.

            Regards,
            Pet Adoption Team
            """
                .formatted(firstName, petName);
    }

    public static String appointmentRejected(
            String firstName,
            String petName) {

        return """
            Hello %s,

            Your appointment request has been rejected.

            Pet:
            %s

            Please contact the shelter
            if you need additional details.

            Regards,
            Pet Adoption Team
            """
                .formatted(firstName, petName);
    }

    public static String appointmentCompleted(
            String firstName,
            String petName) {

        return """
            Hello %s,

            Thank you for attending your appointment.

            Pet:
            %s

            We appreciate your interest in pet adoption.

            Regards,
            Pet Adoption Team
            """
                .formatted(firstName, petName);
    }
}