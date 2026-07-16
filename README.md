# Unified Service Scheduler

The **Unified Service Scheduler** is a Spring Boot application designed to automate the service appointment booking process for automotive dealerships. The system ensures that an appointment is only confirmed when both a qualified technician (matching all requested skill sets) and an available service bay are simultaneously reserved for the cumulative duration of all requested services.

---

## 1. Project Overview

### What it does
Exposes RESTful APIs that validate appointment requests, calculate sequential service durations, search for qualified technicians, check for resource overlaps, and book appointments by assigning a single qualified technician and an available service bay.

### Business Problem Solved
Replaces manual scheduling and avoids scheduling conflicts (such as overlapping technician assignments or double-booked service bays) while enforcing skill matching within individual dealerships.

### Project Scope
- REST APIs for appointment creation, single lookup, and listing.
- Read-only master data APIs for Customers, Vehicles, Dealerships, and Service Types.
- Core availability and resource assignment engines.
- Seed data scripts to support local testing and deployment.
- JUnit 5 automated testing suite.

---

## 2. Technology Stack

- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 4.1.0 (with Spring Data JPA)
- **Build Tool**: Maven 3 (via Maven Wrapper)
- **Database**: Microsoft SQL Server
- **Testing Frameworks**: JUnit 5, Mockito, Spring Boot Test, MockMvc
- **API Documentation**: Springdoc OpenAPI / Swagger (`springdoc-openapi-starter-webmvc-ui`)
- **Important Libraries**: Lombok, MapStruct, Jackson (custom package bindings)

---

## 3. Project Structure

The project code is located in the [src/main/java/com/keyloop/scheduler](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler) package:

```text
com.keyloop.scheduler
├── controller     # REST API Controllers (endpoints, HTTP mapping, request validation)
├── dto            # Request and Response DTO payloads
├── entity         # Domain models and JPA entities
├── enums          # Domain enums (e.g. AppointmentStatus)
├── exception      # Custom business and resource exceptions
├── mapper         # MapStruct mapper interfaces for DTO translations
├── repository     # Spring Data JPA Repository layers
└── service        # Service interfaces and business implementations
```

### Packages & Responsibilities
- **[controller](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler/controller)**: Validates incoming payloads with Jakarta Bean Validation and handles API request routing.
- **[service](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler/service)**: Orchesrates scheduling transactions, validates business rules, determines resource availability, and executes deterministic assignments.
- **[repository](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler/repository)**: Connects to the database and runs custom JPQL fetch queries to avoid performance bottlenecks.
- **[entity](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler/entity)**: Maps Java entities to SQL Server tables using custom physical naming configurations.

---

## 4. Prerequisites & Database Configuration

Before building or running the project, ensure you have:
1. **Java Development Kit (JDK) 21** or higher.
2. **Microsoft SQL Server** running.
3. A database named **`ServiceAppointmentDB`** initialized.

### Custom Connection Setup
The database credentials in [application.properties](file:///e:/unified-service-scheduler/src/main/resources/application.properties) are parameterized. You can run with the default credentials (`username=sa`, `password=Nguyen672004@`, `localhost:1433`) or configure your custom connection using one of these methods:

*   **Method A: Environment Variables**
    Set these environment variables before running:
    ```bash
    export DB_URL="jdbc:sqlserver://your-host:1433;databaseName=ServiceAppointmentDB;encrypt=true;trustServerCertificate=true"
    export DB_USERNAME="your_username"
    export DB_PASSWORD="your_password"
    ```
*   **Method B: Command Line Arguments**
    Pass overrides directly when starting the application:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.password=your_password"
    ```

---

## 5. Build Instructions

To build the project and package it into a JAR file, run the following command from the project root directory:

**Windows**:
```powershell
.\mvnw.cmd clean package
```

**Linux / macOS**:
```bash
./mvnw clean package
```

---

## 6. Running the Application

### 1. Database Schema
Create the database schema by executing the definitions located in [schema.sql](file:///e:/unified-service-scheduler/src/main/resources/schema.sql). This is required since DDL auto-generation is disabled:
```properties
spring.jpa.hibernate.ddl-auto=none
```

### 2. Seed Data
After creating the schema, insert the reference and master data records by executing the queries located in [seed-data.sql](file:///e:/unified-service-scheduler/src/main/resources/seed-data.sql).

### 3. Startup Command
Start the application using the Maven wrapper:

**Windows**:
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**:
```bash
./mvnw spring-boot:run
```
The application will run on port `8080` by default.

---

## 7. Running the Tests

To run the complete automated testing suite (unit and integration tests):

**Windows**:
```powershell
.\mvnw.cmd clean test
```

**Linux / macOS**:
```bash
./mvnw clean test
```

### Test Scope
- **Unit Tests**:
  - `AvailabilityServiceImplTest`: Verifies customer/vehicle existence, ownership, duration summation, qualification matching, and resource overlap check logic.
  - `AssignmentServiceImplTest`: Asserts deterministic resource assignments.
  - `AppointmentServiceImplTest`: Tests success paths and validation fail paths during appointment bookings.
- **Integration Tests**:
  - `AppointmentControllerIntegrationTest`: Spins up the mock MVC container to test JSON serializations, model mappings, validation responses, and transaction persistence.

---

## 8. API Documentation

When the application is running, you can access the interactive API documentation and Swagger UI at:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 9. Design Decisions

### 1. Layered Architecture
Decouples layers via the Controller-Service-Repository pattern. Restricts controller actions to validation, service actions to transaction boundaries (`@Transactional`), and repository actions to persistence.

### 2. Physical Naming Strategy
Configured `PhysicalNamingStrategyStandardImpl` to map entities directly to PascalCase tables (e.g. `Customer`, `Vehicle`) and camelCase columns (e.g. `fullName`, `estimatedDuration`) to match the client's SQL schema exactly.

### 3. Auditing Scoped to Appointments
Moved JPA Auditing (`createdAt`, `updatedAt` with `@CreatedDate`, `@LastModifiedDate` and `@EntityListeners(AuditingEntityListener.class)`) exclusively into the `Appointment` entity, as other master data reference tables are read-only and do not include these fields.

### 4. Fetch Joins (Performance)
Overrode the standard listing methods in [AppointmentRepository.java](file:///e:/unified-service-scheduler/src/main/java/com/keyloop/scheduler/repository/AppointmentRepository.java) using `LEFT JOIN FETCH` to eagerly load lazy associations, eliminating N+1 select queries during DTO mapping.

---

## 10. AI Collaboration Narrative

### High-Level AI Strategy
AI acted as an interactive pair-programming assistant during development. The human developer retained full architectural control and mapped out dependencies, while the AI was leveraged for:
- **Drafting boilerplate**: Outlining JAX-RS/Jakarta validator schemas.
- **Troubleshooting physical mapping conversion gotchas**: Resolving the SQLServer naming strategies.
- **Reviewing edge cases**: Spotting potential N+1 mapping issues and identifying the cross-dealership technician leak.

### Verification Process
All code generated or suggested by AI was strictly verified before integration:
- Checked syntax alignment against Java 21 compiler specifications.
- Verified physical query generations by reviewing Hibernate logs (`spring.jpa.show-sql=true`).
- Run the full test suite (`clean test`) to ensure changes preserved 100% test coverage.

### Quality Assurance
- Code changes were reviewed line-by-line using git diffs.
- Validation checks were verified by sending invalid requests to the mock container and asserting 400 Bad Request responses.

---

## 11. Known Limitations

- **Hardcoded Credentials**: Connection properties contain plaintext database passwords.
- **Customer Vehicle Lookup Verification**: The GET customer vehicle lookup endpoint returns an empty array with 200 OK if the customer ID does not exist, rather than returning a 404 Not Found.
- **Technician Shift Timing**: Technicians are assumed to work full-day shifts; availability is determined solely by checking overlapping appointments rather than checking shift hours.

---

## 12. Future Improvements

- **Secure Configuration**: Integrate Spring Cloud Config or external environment variables to secure database credentials.
- **Observability Stack**: Add Spring Boot Actuator and Micrometer libraries to export application performance metrics to Prometheus and Grafana.
- **Database Index Optimization**: Proactively index search columns (`startTime`, `endTime`, `status`) to keep query lookups fast as database records scale.

---

## 13. Reviewer Checklist

1. **Verify Prerequisites**: Confirm JDK 21 and SQL Server are available, and a `ServiceAppointmentDB` database is present.
2. **Review Configuration**: Inspect [application.properties](file:///e:/unified-service-scheduler/src/main/resources/application.properties) to ensure correct credentials.
3. **Execute SQL Seeds**: Apply database schema script [schema.sql](file:///e:/unified-service-scheduler/src/main/resources/schema.sql) followed by references in [seed-data.sql](file:///e:/unified-service-scheduler/src/main/resources/seed-data.sql).
4. **Compile & Run Tests**: Execute `.\mvnw.cmd clean test` to check build stability and test coverage.
5. **Start Application**: Run `.\mvnw.cmd spring-boot:run`.
6. **Access Documentation**: Visit `http://localhost:8080/swagger-ui.html` to review endpoints.
