# CarePoint Hospital Patient Management System

A Java 17 and Spring Boot prototype with three role-specific portals, a responsive browser frontend, and local JSON file storage. No SQL database is used.

## Open in Spring Tool Suite

1. Extract the project ZIP.
2. In STS, choose **File → Import → Existing Maven Projects** and select the extracted project folder.
3. Wait for Maven dependencies to finish downloading.
4. Open `src/main/java/com/hospital/HospitalApplication.java` and choose **Run As → Spring Boot App**.
5. Visit [http://localhost:8080](http://localhost:8080).

Alternatively, run `mvn spring-boot:run` from the project directory. JDK 17+ and Maven 3.6+ are required.

## Demo sign-in accounts

The application creates these accounts the first time it starts, if `data/users.json` does not already contain users:

| Portal | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin123!` |
| Doctor | `doctor` | `Doctor123!` |
| Pharmacy | `pharmacy` | `Pharmacy123!` |

Change demo credentials before sharing or deploying the prototype. Passwords are stored as salted PBKDF2 hashes in the JSON user file; the demo password values above are intentionally public.

## Included workflows

- **Admin portal:** hospital-wide overview (patients, doctors, appointments, today's visits, scheduled/completed visits, prescriptions, and dispensings); create, view, update, and delete patients, doctors, and appointments.
- **Doctor portal:** read patient records; review and update appointment date, reason, and status; write visit reports with assessment and follow-up recommendations; create prescriptions with one or more medicines, strength, dose, duration, quantity, food timing, and selected times of day.
- **Pharmacy portal:** find prescriptions using a patient ID or name, review medicine directions, and record pickup. The prescription ID and patient ID are shown for handoff. Repeated dispensing is rejected.
- **Role-specific login:** admin, doctor, and pharmacy pages have separate sign-in portals. Server-side session checks enforce portal permissions; hiding a screen in the browser is not the security boundary.

## JSON data

The app reads and writes JSON files in `data/` relative to the application working directory. It creates files when records are first saved. Supported files include `patients.json`, `doctors.json`, `appointments.json`, `users.json`, `prescriptions.json`, and `reports.json`. Keep backups of this directory. The downloadable source project intentionally excludes local patient records.

## REST endpoints

All API routes require a signed-in session except `POST /api/auth/login`. Login requests include `username`, `password`, and `role` (`ADMIN`, `DOCTOR`, or `PHARMACY`). `GET /api/auth/me` returns the active portal and `POST /api/auth/logout` ends the session.

| Endpoint | Use |
|---|---|
| `/api/dashboard` | Admin overview and activity metrics |
| `/api/patients` | Patient CRUD (admin); read-only for doctors |
| `/api/doctors` | Doctor CRUD (admin); read-only for doctors |
| `/api/appointments` | Appointment CRUD (admin); read/update for doctors |
| `/api/reports` | Create/list patient visit reports (doctor) |
| `/api/prescriptions` | Create/list prescriptions (doctor) |
| `/api/pharmacy/prescriptions?search={id-or-name}` | Find a patient's prescriptions (pharmacy) |
| `/api/pharmacy/prescriptions/{id}/dispense` | Mark a prescription picked up (pharmacy) |

Patient and doctor CRUD retain the original API request shapes. Appointment date values use an ISO date/time string such as `2026-11-07T10:30`.

## Extending the prototype

Keep HTTP endpoints in `controller`, business rules in `service`, JSON persistence behind `repo`, and serialized records in `model`. The shared `JsonFileStore` provides atomic file replacement for each save. The static portal pages and JavaScript live in `src/main/resources/static`.

## Prototype safety

This is an educational/local prototype, not a production electronic medical record or pharmacy system. It does not provide production-grade identity management, encryption at rest, audit trails, data retention controls, regulatory compliance, or clinical decision support. Use synthetic data only; do not expose this service to the public internet or enter real patient records.
