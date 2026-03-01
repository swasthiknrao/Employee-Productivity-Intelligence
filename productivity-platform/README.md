# Employee Productivity Intelligence Platform

A Java console application that measures, analyzes, and optimizes workforce performance using JDBC (MySQL). It aggregates activity data, tasks, and communications to compute productivity scores and surface insights.

## Requirements

- **Java 11+**
- **MySQL 8** (or compatible server)

## Setup

### 1. Database

Create the database and tables:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

Optional seed data (departments, activity types, sample employees, sessions, tasks):

```bash
mysql -u root -p < src/main/resources/seed.sql
```

### 2. Configuration

Copy the example config and set your MySQL credentials:

```bash
copy src\main\resources\db.properties.example src\main\resources\db.properties
```

Edit `src/main/resources/db.properties`:

```properties
jdbc.url=jdbc:mysql://localhost:3306/productivity_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
jdbc.user=root
jdbc.password=YOUR_PASSWORD
```

Do **not** commit `db.properties` (it is gitignored).

### 3. Build and run

```bash
mvn compile exec:java -q
```

Or run the main class from your IDE:

- Main class: `com.productivity.Main`

To build a runnable JAR:

```bash
mvn package
java -jar target/productivity-platform-1.0.0.jar
```

(Ensure the MySQL driver is on the classpath; with the jar plugin above you may need to add the driver to `lib/` and run with `java -cp "lib/*:target/productivity-platform-1.0.0.jar" com.productivity.Main`.)

## Features

- **Employees & Departments** – List/add/edit departments and employees.
- **Activities & Tasks** – Log work sessions (employee, time range, activity type), CRUD tasks, log communications (email/chat).
- **Dashboard** – Compute productivity scores (task completion, focus ratio, collaboration), team view (top employees, department summary), individual view with trend and work rhythm.
- **Insights** – Rules-based bottleneck and recommendations (low completion, high meeting load, low collaboration).
- **Reports & Export** – Daily/weekly and department reports; CSV export for snapshots and tasks.
- **Setup** – Test connection, optionally run schema from the app.
- **Audit log** – Sensitive actions (view reports, export) are logged for compliance.

## Project structure

- `src/main/java/com/productivity/` – Main, config, model, dao, service, util, console.
- `src/main/resources/` – `db.properties`, `schema.sql`, `seed.sql`.

## License

Use for learning and internal use. Ensure compliance and privacy when handling employee data.
