# Validation Rules - one set of rules for Frontend, API and Database

Applied 2026-09-19. Every rule below is enforced in three places, so bad data is stopped as
early as possible and can never get in by a side door:

| Layer | Where | Job |
|---|---|---|
| 1. Frontend | `src/lib/validation.js` (Zod, copy from section 9) | Instant feedback under each form field. Convenience only, never trusted. |
| 2. API | Jakarta annotations on the request DTOs (`@NotBlank`, `@Size`, `@Pattern`, ...) | The real gate. Returns `400` with a message per field. |
| 3. Database | Column sizes + `NOT NULL` + `CHECK` constraints (`db/constraints.sql`) | Last line of defence: refuses bad rows even from SQL, scripts or future code. |

**Single source of truth (backend):** every number and regex lives in
`com.petadoption.validation.Rules`. The DTO annotations and the entity `@Column(length = ...)`
both read those constants, so the API and the database columns cannot drift apart.
**Executable check:** `ValidationRulesTest` tests every rule at its boundary using the same
examples as this document. If you change a rule, change `Rules.java`, this file,
`db/constraints.sql`, the frontend `validation.js` and that test together.

---

## 1. Conventions

- **Trim first.** The frontend trims text inputs before validating and sending. The API does not
  trim, so `" Bob"` (leading space) fails the name pattern.
- **Optional means null or omitted, never an empty string.** A blank optional input must be sent
  as `null` (or left out). An empty string `""` fails patterns such as phone and postal code.
  The `validation.js` helper `emptyToNull` does this.
- **Required** = present and not blank. `@NotBlank` (text) or `@NotNull` (numbers, dates, enums).
- **Emails are case-insensitive.** The API stores and looks up emails trimmed and lowercase, so
  `Asha@Example.com` and `asha@example.com` are the same account (register, login and forgot-password
  all normalise). The database also refuses any email that is not lowercase. Spaces around an email are a
  validation error (the frontend trims first), while letter case is always accepted. Show the email exactly
  as the API returns it (lowercase).
- Length limits count characters (UTF-16 code units, the same as JavaScript `string.length`).
- Messages are user-safe and identical in the API and in `validation.js`, so the user sees the
  same sentence whichever layer catches the mistake.

## 2. Shared patterns

| Name | Rule | Regex | Valid examples | Invalid examples |
|---|---|---|---|---|
| Person name (first/last) | 1-50 chars, starts with a letter, then letters, spaces, `.` `'` `-` | `^[\p{L}][\p{L} .'-]*$` | `Asha`, `Mary-Jane`, `O'Neil`, `Dr. Rao`, `Zoe` | `1Bob`, `-Bob`, `Bob3`, `Bob@`, ` ` |
| Email | at most 254 chars, standard shape, TLD of 2+ letters | `^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$` | `a@b.co`, `first.last+tag@sub.example.org` | `plain`, `a@b`, `a@b.c`, `@b.com`, `a b@c.com`, `a@b..com` |
| Password (register / reset) | 8-64 chars with at least one lowercase, one uppercase, one digit and one special character | `^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]).{8,64}$` | `Password@123`, `Aa1!aaaa`, `My_Pass-w0rd` | `password@123` (no uppercase), `PASSWORD@123` (no lowercase), `Password@abc` (no digit), `Password1234` (no special), `Pa@1` (too short), `Pass word1` (a space is not a special character) |
| Phone | 7-15 digits, optional leading `+`, no spaces or dashes | `^\+?[0-9]{7,15}$` | `9876543210`, `+919876543210` | `12345`, `12-3456-7890`, `+`, `phone` |
| Postal code | 3-10 chars: letters, digits, space, hyphen; starts and ends with a letter or digit | `^[A-Za-z0-9][A-Za-z0-9 -]{1,8}[A-Za-z0-9]$` | `560001`, `SW1A 1AA`, `12345-6789` | `12`, ` 12345`, `12#45` |

Special characters allowed for passwords: `! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : " \ | , . < > / ? ~ `` ` ``
(a space or a letter with an accent does not count as a special character).
The login form does NOT apply the password policy (only "required, at most 64 characters"),
so accounts created before the rules still sign in.
64 is the maximum because BCrypt only uses the first 72 bytes.

## 3. Fields by endpoint

`R` = required, `-` = optional. "DB" = the database column type/constraint.

### Auth
| Endpoint | Field | Req | Rule | DB |
|---|---|---|---|---|
| POST `/api/auth/register` | `firstName` | R | person name, 1-50 | `users.first_name varchar(50) NOT NULL` |
| | `lastName` | R | person name, 1-50 | `users.last_name varchar(50) NOT NULL` |
| | `email` | R | email, max 254; stored lowercase | `users.email varchar(254) NOT NULL UNIQUE` + CHECK regex + CHECK lowercase |
| | `password` | R | password policy | only the BCrypt hash is stored |
| POST `/api/auth/login` | `email` | R | max 254 | - |
| | `password` | R | max 64 (no policy) | - |
| POST `/api/auth/forgot-password` | `email` | R | email, max 254 | - |
| POST `/api/auth/reset-password` | `token` | R | max 100 | - |
| | `newPassword` | R | password policy | hash only |

### Profile (`PUT /api/users/me`)
| Field | Req | Rule | DB |
|---|---|---|---|
| `firstName`, `lastName` | R | person name, 1-50 | `varchar(50) NOT NULL` |
| `phone` | - | phone pattern | `varchar(16)` + CHECK regex |
| `addressLine1`, `addressLine2` | - | max 100 | `varchar(100)` |
| `city`, `state`, `country` | - | max 60 | `varchar(60)` |
| `postalCode` | - | postal pattern | `varchar(10)` + CHECK regex |

### Pets (`POST`/`PUT /api/pets`)
| Field | Req | Rule | DB |
|---|---|---|---|
| `name` | R | 1-60 chars | `pets.name varchar(60) NOT NULL` |
| `species` | R | one of `Dog`, `Cat`, `Rabbit`, `Bird`, `Other` (case-sensitive) | `varchar(20)` + CHECK in list |
| `breed` | - | max 60 | `varchar(60)` |
| `age` | R | whole number 0-40 (years) | `integer` + CHECK 0..40 |
| `gender` | R | one of `Male`, `Female`, `Unknown` | `varchar(10)` + CHECK in list |
| `description` | - | max 1000 | `varchar(1000)` |
| `energyLevel` | R | `LOW`, `MEDIUM`, `HIGH` | enum text |
| `temperament` | R | `CALM`, `PLAYFUL`, `INDEPENDENT`, `AFFECTIONATE`, `PROTECTIVE` | enum text |
| `sterilized` | R | boolean (`true`/`false`) | `boolean NOT NULL` |
| `specialCareNotes` | - | max 500 | `varchar(500)` |
Image uploads: `image/*` only, max 5 MB (enforced by the API).

### Shelters (`POST`/`PUT /api/shelters`)
| Field | Req | Rule | DB |
|---|---|---|---|
| `name` | R | 2-100 chars | `varchar(100) NOT NULL` + CHECK length >= 2 |
| `email` | R | email, max 254 | `varchar(254)` + CHECK regex |
| `phone` | R | phone pattern | `varchar(16)` + CHECK regex |
| `addressLine1`, `addressLine2` | - | max 100 | `varchar(100)` |
| `city` | R | 1-60 | `varchar(60)` |
| `state` | - | max 60 | `varchar(60)` |
| `postalCode` | - | postal pattern | `varchar(10)` + CHECK regex |
| `country` | R | 1-60 | `varchar(60)` |
| `description` | - | max 1000 | `varchar(1000)` |

### Adoption applications
| Endpoint | Field | Req | Rule | DB |
|---|---|---|---|---|
| POST `/api/adoptions` | `petId` | R | positive integer | FK |
| | `livingSituation` | R | 1-500 | `varchar(500)` (nullable for old rows) |
| | `preferredContact` | R | 1-100 | `varchar(100)` (nullable for old rows) |
| | `applicantNotes` | - | max 1000 | `varchar(1000)` |
| | `priorPetExperience` | - | max 500 | `varchar(500)` |
| | `householdDetails` | - | max 500 | `varchar(500)` |
| PUT `/api/adoptions/{id}/approve` and `/reject` | `notes` (optional body) | - | max 500 | `review_notes` / `rejection_reason` `varchar(500)` |

### Appointments, slots, medical records
| Endpoint | Field | Req | Rule | DB |
|---|---|---|---|---|
| POST `/api/appointments` | `petId`, `slotId` | R | positive integers | FK |
| | `notes` | - | max 500 | `varchar(500)` |
| POST `/api/slots` | `slotDateTime` | R | `yyyy-MM-ddTHH:mm:ss`, must be in the future | `NOT NULL` (future rule: API only) |
| POST `/api/pets/{id}/medical-records` | `recordDate` | R | `yyyy-MM-dd`, not in the future | `date NOT NULL` (not-future rule: API only) |
| | `description` | R | 1-1000 | `varchar(1000) NOT NULL` |
| | `vetName` | - | max 100 | `varchar(100)` |

### Admin
| Endpoint | Field | Req | Rule |
|---|---|---|---|
| PUT `/api/users/{id}/roles` | `roles` | R | non-empty set; each one of `ROLE_SYSTEM_ADMIN`, `ROLE_SHELTER_ADMIN`, `ROLE_SHELTER_STAFF`, `ROLE_ADOPTER` |
| PUT `/api/users/{id}/shelter` | `shelterId` | - | positive integer, or `null` to unassign |

### AI chat (`POST /api/ai/**`)
| Field | Req | Rule |
|---|---|---|
| `message` | R | not blank, at most 1000 characters |

### Query parameters (`GET /api/pets`, `GET /api/shelters`)
| Param | Rule |
|---|---|
| `species` | max 20 chars (text filter, case-insensitive) |
| `minAge`, `maxAge` | whole numbers 0-40; `minAge` must not be greater than `maxAge` (`400`) |
| `search` (pets) / `name` (shelters) | max 100 chars |
| `page` / `size` / `sort` | `size` is capped at 100 by the server; page is 0-indexed |

## 4. Error format for validation failures

`400` with the usual error body plus a new `fieldErrors` object (only on validation errors):

```json
{
  "timestamp": "2026-09-19T12:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Password must be 8-64 characters with an uppercase letter, a lowercase letter, a digit and a special character",
  "fieldErrors": {
    "password": "Password must be 8-64 characters with an uppercase letter, a lowercase letter, a digit and a special character",
    "email": "Email must be a valid address of at most 254 characters"
  }
}
```
- `fieldErrors` keys are the JSON field names of the request body (for a list such as `roles`,
  the key is just `roles`). Show each message under its field; set React Hook Form errors with
  `setError(field, { message })`.
- `message` is one of the field messages (use it for a toast when there is no matching field).
- Query-parameter problems use the same shape with the parameter name as the key.
- Rules that need context (for example "you already have an active application for this pet")
  return `error: "Business Error"` with no `fieldErrors`.

## 5. What the database does NOT enforce (API only)

- Slot date-time is in the future; medical record date is not in the future (they depend on "now",
  which a `CHECK` constraint must not do).
- Password policy (only a hash is stored).
- One active application per adopter per pet, pet/slot availability, shelter ownership: business
  rules checked in the services.
- `NOT NULL` is not applied to `pets.species/age/gender` and
  `adoption_applications.living_situation/preferred_contact`, because rows created before these
  rules existed may be empty. The API requires them for every new or updated record.

## 6. Applying the database constraints

`pet-adoption-system/db/constraints.sql` is idempotent (safe to run many times).

- Existing database: run it once. It also normalises old demo rows (`DOG` becomes `Dog`,
  `MALE` becomes `Male`, fills the empty legacy pet).
- Brand-new database: start the app once (Hibernate creates the tables with the right column sizes),
  then run the script to add the `CHECK` constraints.

```
psql -h localhost -U postgres -d pet_adoption_db -f pet-adoption-system/db/constraints.sql
```

Hibernate's `ddl-auto: update` never alters an existing column or adds a `CHECK`, which is why the
script exists.

## 7. Changing a rule (checklist)

1. `Rules.java` (constant, regex and message).
2. `db/constraints.sql` (column size and/or CHECK), then run it.
3. This document and `FRONTEND_API_REFERENCE.md`.
4. Frontend `src/lib/validation.js`.
5. `ValidationRulesTest` (boundary cases), then run the whole test suite.

## 8. Demo data note

Every demo account uses `Password@123`, which satisfies the password policy.
Pets now use title-case values: species `Dog`/`Cat`, gender `Male`/`Female`/`Unknown`.

## 9. Frontend `src/lib/validation.js` (copy as is)

Plain JavaScript with Zod. Messages match the API word for word.

```js
import { z } from "zod";

// ---- patterns (identical to the backend Rules.java) ----
export const NAME_REGEX = /^[\p{L}][\p{L} .'-]*$/u;
export const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/;
export const PASSWORD_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?~`]).{8,64}$/;
export const PHONE_REGEX = /^\+?[0-9]{7,15}$/;
export const POSTAL_REGEX = /^[A-Za-z0-9][A-Za-z0-9 -]{1,8}[A-Za-z0-9]$/;

export const SPECIES = ["Dog", "Cat", "Rabbit", "Bird", "Other"];
export const GENDERS = ["Male", "Female", "Unknown"];
export const ENERGY_LEVELS = ["LOW", "MEDIUM", "HIGH"];
export const TEMPERAMENTS = ["CALM", "PLAYFUL", "INDEPENDENT", "AFFECTIONATE", "PROTECTIVE"];
export const ROLES = ["ROLE_SYSTEM_ADMIN", "ROLE_SHELTER_ADMIN", "ROLE_SHELTER_STAFF", "ROLE_ADOPTER"];

// ---- messages (identical to the backend) ----
export const MESSAGES = {
  name: "Name must be 1-50 characters: letters, spaces, . ' -",
  email: "Email must be a valid address of at most 254 characters",
  password:
    "Password must be 8-64 characters with an uppercase letter, a lowercase letter, a digit and a special character",
  phone: "Phone must be 7-15 digits, optionally starting with +",
  postal: "Postal code must be 3-10 letters, digits, spaces or hyphens",
  species: "Species must be one of: Dog, Cat, Rabbit, Bird, Other",
  gender: "Gender must be one of: Male, Female, Unknown",
  role: "Role must be ROLE_SYSTEM_ADMIN, ROLE_SHELTER_ADMIN, ROLE_SHELTER_STAFF or ROLE_ADOPTER",
};

// ---- helpers ----
const required = (label) => z.string().trim().min(1, `${label} is required`);
const maxLen = (schema, n, message) => schema.max(n, message);

// Optional text: "" becomes undefined so the API never receives an empty string.
const optionalText = (n, message) =>
  z
    .string()
    .trim()
    .max(n, message)
    .optional()
    .transform((v) => (v ? v : undefined));

const optionalPattern = (regex, message) =>
  z
    .string()
    .trim()
    .optional()
    .transform((v) => (v ? v : undefined))
    .refine((v) => v === undefined || regex.test(v), message);

/** Turns "" into null for any field you build by hand. */
export const emptyToNull = (value) => (value === "" || value === undefined ? null : value);

const personName = (label) =>
  required(label)
    .max(50, MESSAGES.name)
    .regex(NAME_REGEX, MESSAGES.name);

const email = required("Email")
  .max(254, MESSAGES.email)
  .regex(EMAIL_REGEX, MESSAGES.email);

const password = required("Password").regex(PASSWORD_REGEX, MESSAGES.password);

// ---- schemas ----
export const registerSchema = z.object({
  firstName: personName("First name"),
  lastName: personName("Last name"),
  email,
  password,
});

// Login checks presence and length only, never the password policy.
export const loginSchema = z.object({
  email: required("Email").max(254, MESSAGES.email),
  password: required("Password").max(64, "Password must be at most 64 characters"),
});

export const forgotPasswordSchema = z.object({ email });

export const resetPasswordSchema = z.object({
  token: required("Reset token").max(100, "Reset token must be at most 100 characters"),
  newPassword: required("New password").regex(PASSWORD_REGEX, MESSAGES.password),
});

export const profileSchema = z.object({
  firstName: personName("First name"),
  lastName: personName("Last name"),
  phone: optionalPattern(PHONE_REGEX, MESSAGES.phone),
  addressLine1: optionalText(100, "Address line 1 must be at most 100 characters"),
  addressLine2: optionalText(100, "Address line 2 must be at most 100 characters"),
  city: optionalText(60, "City must be at most 60 characters"),
  state: optionalText(60, "State must be at most 60 characters"),
  postalCode: optionalPattern(POSTAL_REGEX, MESSAGES.postal),
  country: optionalText(60, "Country must be at most 60 characters"),
});

export const petSchema = z.object({
  name: required("Pet name").max(60, "Pet name must be at most 60 characters"),
  species: z.enum(SPECIES, { message: MESSAGES.species }),
  breed: optionalText(60, "Breed must be at most 60 characters"),
  age: z.coerce
    .number({ message: "Age is required" })
    .int("Age must be between 0 and 40")
    .min(0, "Age must be between 0 and 40")
    .max(40, "Age must be between 0 and 40"),
  gender: z.enum(GENDERS, { message: MESSAGES.gender }),
  description: optionalText(1000, "Description must be at most 1000 characters"),
  energyLevel: z.enum(ENERGY_LEVELS, { message: "Energy level is required" }),
  temperament: z.enum(TEMPERAMENTS, { message: "Temperament is required" }),
  sterilized: z.boolean(),
  specialCareNotes: optionalText(500, "Special care notes must be at most 500 characters"),
});

export const shelterSchema = z.object({
  name: required("Shelter name").min(2, "Shelter name must be 2-100 characters").max(100, "Shelter name must be 2-100 characters"),
  email,
  phone: required("Shelter phone").regex(PHONE_REGEX, MESSAGES.phone),
  addressLine1: optionalText(100, "Address line 1 must be at most 100 characters"),
  addressLine2: optionalText(100, "Address line 2 must be at most 100 characters"),
  city: required("City").max(60, "City must be at most 60 characters"),
  state: optionalText(60, "State must be at most 60 characters"),
  postalCode: optionalPattern(POSTAL_REGEX, MESSAGES.postal),
  country: required("Country").max(60, "Country must be at most 60 characters"),
  description: optionalText(1000, "Description must be at most 1000 characters"),
});

export const applicationSchema = z.object({
  petId: z.number().int().positive("Pet id must be positive"),
  livingSituation: required("Living situation").max(500, "Living situation must be at most 500 characters"),
  preferredContact: required("Preferred contact").max(100, "Preferred contact must be at most 100 characters"),
  applicantNotes: optionalText(1000, "Applicant notes must be at most 1000 characters"),
  priorPetExperience: optionalText(500, "Prior pet experience must be at most 500 characters"),
  householdDetails: optionalText(500, "Household details must be at most 500 characters"),
});

export const decisionSchema = z.object({
  notes: optionalText(500, "Notes must be at most 500 characters"),
});

export const appointmentSchema = z.object({
  petId: z.number().int().positive("Pet id must be positive"),
  slotId: z.number().int().positive("Slot id must be positive"),
  notes: optionalText(500, "Notes must be at most 500 characters"),
});

// value looks like "2026-09-20T14:30" from <input type="datetime-local">
export const slotSchema = z.object({
  slotDateTime: z
    .string()
    .min(1, "Slot date and time is required")
    .refine((v) => new Date(v).getTime() > Date.now(), "Slot date and time must be in the future"),
});

// value looks like "2026-09-20" from <input type="date">
export const medicalRecordSchema = z.object({
  recordDate: z
    .string()
    .min(1, "Record date is required")
    .refine((v) => v <= new Date().toISOString().slice(0, 10), "Record date cannot be in the future"),
  description: required("Description").max(1000, "Description must be at most 1000 characters"),
  vetName: optionalText(100, "Vet name must be at most 100 characters"),
});

export const rolesSchema = z.object({
  roles: z.array(z.enum(ROLES, { message: MESSAGES.role })).min(1, "At least one role is required"),
});

export const assignShelterSchema = z.object({
  shelterId: z.number().int().positive("Shelter id must be positive").nullable(),
});

export const chatSchema = z.object({
  message: required("Message").max(1000, "Message must be at most 1000 characters"),
});
```

Use them with React Hook Form: `useForm({ resolver: zodResolver(petSchema) })`. When the API replies with
`fieldErrors`, map them with `setError(field, { message })`.
