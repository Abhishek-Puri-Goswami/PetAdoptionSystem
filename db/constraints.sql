-- ============================================================================
-- Database constraints matching the API validation rules (docs/VALIDATION_RULES.md)
--
-- WHY THIS FILE EXISTS
--   The entities declare @Column(length/nullable) from validation/Rules.java, so a
--   BRAND NEW database gets the right column sizes from Hibernate (ddl-auto: update).
--   But Hibernate never ALTERs an existing column and never creates CHECK
--   constraints. This script does both, and is safe to run many times.
--
-- WHEN TO RUN
--   * once on an existing database, and
--   * once after the very first start-up of a fresh database (to add the CHECKs).
--   psql -h localhost -U postgres -d pet_adoption_db -f db/constraints.sql
--
-- WHAT IS NOT HERE (enforced by the API only, because they depend on "today"
--   or on other tables): slot must be in the future, medical record date must not
--   be in the future, one active application per pet, password policy (only the
--   hash is stored), email uniqueness is already a UNIQUE column.
--
-- NOT NULL is applied only where every existing and future row must have a value.
--   pets.species/age/gender, applications.living_situation/preferred_contact stay
--   nullable in the DATABASE because rows created before the rules existed may be
--   empty; the API still requires them for every new/updated record.
-- ============================================================================

BEGIN;

-- ---- 1. Backfill old demo data so it satisfies the new pick-lists ----------
UPDATE pets SET species = initcap(species) WHERE species IS NOT NULL AND species <> initcap(species);
UPDATE pets SET gender  = initcap(gender)  WHERE gender  IS NOT NULL AND gender  <> initcap(gender);
UPDATE pets SET species = 'Dog'     WHERE id = 4 AND species IS NULL;   -- legacy demo pet with empty fields
UPDATE pets SET age     = 2         WHERE id = 4 AND age     IS NULL;
UPDATE pets SET gender  = 'Unknown' WHERE id = 4 AND gender  IS NULL;

-- ---- 2. Column sizes (must equal validation/Rules.java) --------------------
ALTER TABLE users   ALTER COLUMN first_name    TYPE varchar(50),
                    ALTER COLUMN last_name     TYPE varchar(50),
                    ALTER COLUMN email         TYPE varchar(254),
                    ALTER COLUMN phone         TYPE varchar(16),
                    ALTER COLUMN address_line1 TYPE varchar(100),
                    ALTER COLUMN address_line2 TYPE varchar(100),
                    ALTER COLUMN city          TYPE varchar(60),
                    ALTER COLUMN state         TYPE varchar(60),
                    ALTER COLUMN postal_code   TYPE varchar(10),
                    ALTER COLUMN country       TYPE varchar(60);

ALTER TABLE pets    ALTER COLUMN name               TYPE varchar(60),
                    ALTER COLUMN species            TYPE varchar(20),
                    ALTER COLUMN breed              TYPE varchar(60),
                    ALTER COLUMN gender             TYPE varchar(10),
                    ALTER COLUMN description        TYPE varchar(1000),
                    ALTER COLUMN special_care_notes TYPE varchar(500);

ALTER TABLE shelters ALTER COLUMN name           TYPE varchar(100),
                     ALTER COLUMN email          TYPE varchar(254),
                     ALTER COLUMN phone          TYPE varchar(16),
                     ALTER COLUMN address_line1  TYPE varchar(100),
                     ALTER COLUMN address_line2  TYPE varchar(100),
                     ALTER COLUMN city           TYPE varchar(60),
                     ALTER COLUMN state          TYPE varchar(60),
                     ALTER COLUMN postal_code    TYPE varchar(10),
                     ALTER COLUMN country        TYPE varchar(60),
                     ALTER COLUMN description    TYPE varchar(1000);

ALTER TABLE adoption_applications
    ALTER COLUMN applicant_notes      TYPE varchar(1000),
    ALTER COLUMN living_situation     TYPE varchar(500),
    ALTER COLUMN prior_pet_experience TYPE varchar(500),
    ALTER COLUMN household_details    TYPE varchar(500),
    ALTER COLUMN preferred_contact    TYPE varchar(100),
    ALTER COLUMN review_notes         TYPE varchar(500),
    ALTER COLUMN rejection_reason     TYPE varchar(500);

ALTER TABLE appointments ALTER COLUMN notes TYPE varchar(500);

ALTER TABLE medical_records ALTER COLUMN description TYPE varchar(1000),
                            ALTER COLUMN vet_name    TYPE varchar(100);

-- ---- 3. NOT NULL where every row must have a value -------------------------
ALTER TABLE medical_records ALTER COLUMN record_date SET NOT NULL;
ALTER TABLE medical_records ALTER COLUMN description SET NOT NULL;
ALTER TABLE availability_slots ALTER COLUMN slot_date_time SET NOT NULL;

-- ---- 4. CHECK constraints (NULL passes a CHECK, so optional columns are fine) ----
-- users
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_email;
ALTER TABLE users ADD  CONSTRAINT chk_users_email
    CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$');
-- emails are stored trimmed + lowercase (EmailUtil.normalize), so A@x.com and a@x.com are one account
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_email_lowercase;
ALTER TABLE users ADD  CONSTRAINT chk_users_email_lowercase CHECK (email = lower(email));
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_phone;
ALTER TABLE users ADD  CONSTRAINT chk_users_phone
    CHECK (phone IS NULL OR phone ~ '^\+?[0-9]{7,15}$');
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_postal;
ALTER TABLE users ADD  CONSTRAINT chk_users_postal
    CHECK (postal_code IS NULL OR postal_code ~ '^[A-Za-z0-9][A-Za-z0-9 -]{1,8}[A-Za-z0-9]$');

-- shelters
ALTER TABLE shelters DROP CONSTRAINT IF EXISTS chk_shelters_name;
ALTER TABLE shelters ADD  CONSTRAINT chk_shelters_name CHECK (char_length(name) >= 2);
ALTER TABLE shelters DROP CONSTRAINT IF EXISTS chk_shelters_email;
ALTER TABLE shelters ADD  CONSTRAINT chk_shelters_email
    CHECK (email IS NULL OR email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$');
ALTER TABLE shelters DROP CONSTRAINT IF EXISTS chk_shelters_phone;
ALTER TABLE shelters ADD  CONSTRAINT chk_shelters_phone
    CHECK (phone IS NULL OR phone ~ '^\+?[0-9]{7,15}$');
ALTER TABLE shelters DROP CONSTRAINT IF EXISTS chk_shelters_postal;
ALTER TABLE shelters ADD  CONSTRAINT chk_shelters_postal
    CHECK (postal_code IS NULL OR postal_code ~ '^[A-Za-z0-9][A-Za-z0-9 -]{1,8}[A-Za-z0-9]$');

-- pets
ALTER TABLE pets DROP CONSTRAINT IF EXISTS chk_pets_species;
ALTER TABLE pets ADD  CONSTRAINT chk_pets_species
    CHECK (species IS NULL OR species IN ('Dog','Cat','Rabbit','Bird','Other'));
ALTER TABLE pets DROP CONSTRAINT IF EXISTS chk_pets_gender;
ALTER TABLE pets ADD  CONSTRAINT chk_pets_gender
    CHECK (gender IS NULL OR gender IN ('Male','Female','Unknown'));
ALTER TABLE pets DROP CONSTRAINT IF EXISTS chk_pets_age;
ALTER TABLE pets ADD  CONSTRAINT chk_pets_age
    CHECK (age IS NULL OR age BETWEEN 0 AND 40);

COMMIT;
