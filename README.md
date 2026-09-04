# gothdb

Modern read-only database explorer for PostgreSQL and MySQL, shipped as a Spring Boot auto-configuration starter.

> **Early demo — not production-ready.** The API and UI you see here run against an in-memory H2 database with sample data (`demo` module). PostgreSQL/MySQL are the stated goal but not yet dialect-tested against real instances. Expect breaking changes.

## What works right now

- Drop `gothdb-spring-boot-starter` into a Spring Boot app with a `DataSource` — it auto-configures itself, no manual wiring.
- Read-only REST API over `DatabaseMetaData`:
  - `GET /gothdb/api/status`
  - `GET /gothdb/api/schemas`
  - `GET /gothdb/api/schemas/{schema}/tables`
  - `GET /gothdb/api/schemas/{schema}/tables/{table}/columns`
  - `GET /gothdb/api/schemas/{schema}/tables/{table}/primary-key`
  - `GET /gothdb/api/schemas/{schema}/tables/{table}/foreign-keys`
  - `GET /gothdb/api/schemas/{schema}/tables/{table}/indexes`
  - `GET /gothdb/api/schemas/{schema}/tables/{table}/rows?page=&size=`
- Unified error handling (400 bad params, 404 unknown schema/table, generic 500 — no JDBC internals leaked).
- A minimalist black-and-white UI (`ui/`): schemas → tables → columns/data, with connection status.

## Quick start (demo)

Build and run the backend (H2 + sample schema, port 8080):

```bash
mvn package
java -jar demo/target/gothdb-demo-0.0.1-SNAPSHOT.jar
```

Open `http://localhost:8080/gothdb/`. The Maven build installs a project-local Node.js, runs `npm ci`,
bundles the UI into the `gothdb-autoconfigure` JAR under `META-INF/gothdb`, and Spring MVC serves it
from the configured `gothdb.path`.

For frontend development, run the Vite dev server; it proxies API calls to the backend:

```bash
cd ui
npm install
npm run dev
```

Open `http://localhost:5173`.

## Tests

Run the unit and auto-configuration tests without external services:

```bash
mvn test
```

Run the PostgreSQL integration tests with Docker!!!!:

```bash
mvn verify -Ppostgresql-integration-tests
```

## Configuration

```yaml
gothdb:
  enabled: true   # default true, auto-disables without a servlet DataSource
  path: /gothdb    # base path for the API and UI
  ui:
    enabled: true  # set false to expose only the REST API
  schemas:
    include: []    # empty means all schemas
    exclude:       # PostgreSQL system schemas are excluded by default
      - information_schema
      - pg_catalog
      - pg_toast
      - pg_temp_*
      - pg_toast_temp_*
  rows:
    count-mode: exact # exact returns totals; none avoids COUNT(*)
    max-page-size: 200
    query-timeout: 5s
```

## Modules

- `core` — framework-agnostic JDBC metadata reading, no Spring dependency.
- `autoconfigure` — Spring Boot auto-configuration, REST controllers, error handling.
- `spring-boot-starter` — the dependency consumers actually add to their project.
- `demo` — runnable sample app (H2, seeded schema) used to develop and manually verify the above.
- `ui` — the Vite + React frontend, built by Maven and packaged into `gothdb-autoconfigure`.
