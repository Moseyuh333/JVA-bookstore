# JVA Bookstore

JVA Bookstore is a full-stack Java EE web application for operating an online bookstore. The stack combines Servlets, JSP/JSTL, JDBC, PostgreSQL, SiteMesh, and JWT authentication. The current codebase is wired for Heroku deployment via the embedded `webapp-runner`.

## Tech Stack

- Java 11, Maven, Servlet API 4.0.1, JSP/JSTL
- PostgreSQL with plain JDBC (no ORM)
- SiteMesh 2.4.2 for layout composition
- Gson for JSON binding, BCrypt for password hashing, JavaMail for email
- JWT authentication handled via `io.jsonwebtoken`
- Frontend: JSP views with Bootstrap 5, vanilla JS, Fetch API

## Repository Layout

| Path | Purpose |
|------|---------|
| `src/main/java/` | Servlets, filters, DAO layer, utility classes |
| `src/main/webapp/` | JSP views, static assets, WEB-INF configs |
| `src/main/resources/` | Database properties, schema + seed SQL, mail templates |
| `books_full_500.csv` | Product catalog import source |
| `pom.xml` | Maven build + dependency management |

## Getting Started

### Prerequisites

- JDK 11+
- Maven 3.9+
- PostgreSQL 13 or cloud-hosted (e.g. Heroku Postgres)
- (Optional) Heroku CLI for deployment

### Configure the database

1. Copy `src/main/resources/db.properties.example` (if present) to `db.properties` or set the `DATABASE_URL` environment variable (format `postgres://user:pass@host:port/db`).
2. Apply the schema:
   ```sh
   psql $DATABASE_URL -f src/main/resources/schema.sql
   ```
3. Seed baseline data (optional but recommended for local testing):
   ```sh
   psql $DATABASE_URL -f src/main/resources/sample_data.sql
   ```
   Use `books_full_500.csv` with `ImportBooksServlet` to load the full product catalog.

### Build & Run locally

```sh
mvn clean package
java -jar target/dependency/webapp-runner.jar target/ROOT.war
```

The app will listen on `http://localhost:8080`. Update `src/main/resources/email.properties` if you need SMTP during local runs (for OTP, password reset, etc.).

### Common credentials

Use the seeded users from `sample_data.sql` or register via `/register`. Passwords are stored hashed (BCrypt).

## Database Notes

- `schema.sql` defines the canonical schema. It aligns with runtime migrations in `DBUtil` (users, OTP, cart, orders, wishlist, ratings, comments, coupons, payment transactions, etc.).
- Use `otp_schema.sql` if you need to reset only OTP-related tables.
- `sample_data.sql` seeds demo users, books, and ancillary tables.
- `TestDB.java` and `CreateTestUser.java` illustrate raw JDBC usage for quick diagnostics.

## Testing

- Unit/integration tests are not yet implemented. Execute `mvn -DskipTests=false test` when suites are added.
- Manual testing guides will ship with each feature milestone. Record key user flows (auth, cart, checkout, wishlist, reviews) with screen captures.

## Deployment

The project ships with a Heroku-friendly layout:

1. `mvn clean package -DskipTests`
2. `heroku create <app-name>`
3. `heroku addons:create heroku-postgresql:hobby-dev`
4. `git push heroku homepage:main`
5. `heroku run psql -f src/main/resources/schema.sql`

`Procfile` and `system.properties` are already present; deployment artifacts live under `target/` after a build.

## Roadmap

An execution roadmap (UI overhaul, catalog browsing, database-backed cart, COD/VNPAY/MoMo checkout, order tracking, wishlist/history/reviews, performance/security hardening) is maintained in [`docs/ROADMAP.md`](docs/ROADMAP.md). Milestones will be updated as development progresses.

## Documentation

- `README.md` (this document) is the authoritative setup and operations guide.
- Historical setup notes (`README_START_HERE.md`, `QUICK_START.md`, `SETUP_COMPLETE.md`, `IMPLEMENTATION_CHECKLIST.md`, `IMPLEMENTATION_STATUS.md`) now include forward pointers back to this README for consistency.
- Danh sách tài liệu đã lưu trữ được tổng hợp trong `docs/ARCHIVE.md`.
- Feature-specific deep dives live in the `docs/` directory (e.g. `EMAIL_TEST_GUIDE.md`).

## Contributing

- Follow the coding style already established (plain JDBC, DAO pattern, DTO-style models).
- Prefer small PRs with accompanying schema updates (if any) and manual verification steps.
- Keep sensitive secrets out of the codebase; rely on environment variables.

## Support

For questions or to report issues, open a GitHub issue on the `homepage` branch or tag the maintainer.