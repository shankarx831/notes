# Backend Engineering Guide

This directory contains the Spring Boot 3 + Java 21 backend.

## 🏛 Architecture

We follow a strict **Service-Repository** pattern. Controllers should contain NO business logic.

```text
Controller (DTOs) -> Service (Domain Logic) -> Repository (JPA) -> Database
```

### Key Invariants
1.  **Statelessness**: The server tracks no session state. All auth is via JWT in the `Authorization` header.
2.  **Audit Trail**: NO DELETION or ROLE CHANGE occurs without an `AuditLog` entry.
3.  **Fail-Safe defaults**: Users are created with `status = INACTIVE` and `role = STUDENT` by default, unless overriden by Admin logic.

---

## 🔐 Security Module (`config/`)

### `SecurityConfig.java`
- Disables CSRF (Stateless API).
- Enables CORS for `localhost:5173` (Frontend).
- Configures the `JwtAuthenticationFilter` before the standard `UsernamePasswordAuthenticationFilter`.

### Role Hierarchy
- **ADMIN**: Full access.
- **TEACHER**: `POST /api/notes`, `DELETE /api/notes/{id}/request`
- **STUDENT**: `GET /api/notes/**`

---

## 📝 Entity Lifecycle

### Note Status Machine
A note flows through these states:
1.  **DRAFT**: Private to the uploader.
2.  **PUBLISHED**: Visible to all students.
3.  **ARCHIVED**: Read-only, hidden from default lists.
4.  **DELETE_PENDING**: Marked for deletion by a Teacher; awaiting Admin approval.
5.  **DELETED**: Soft-deleted (or hard deleted based on config).

### Deletion Workflow with Approval
We strictly forbid teachers from hard-deleting content.
1. Teacher calls `DELETE /api/notes/{id}`.
2. Backend checks if `User` is TEACHER.
3. Backend creates a `DeletionRequest` entity.
4. Note status updates to `DELETE_PENDING` (starts vanishing from some views).
5. Admin calls `PUT /api/admin/requests/{id}/approve`.
6. Request is approved, Note is permanently removed (or flagged DELETED).

---

## 🧪 Testing Strategy
We use `JUnit 5` and `Mockito`.
- **Integration Tests**: `AdminDashboardServiceTest` spins up a mock context to verify service-to-repository wiring.
- **Unit Tests**: Focus on business logic (e.g., ensuring a Teacher cannot delete a Note they don't own, depending on policy).

---

## ⚠️ Database Migration
Currently, `application.properties` allows `ddl-auto = update` (or `create` for resets).
In a true production environment (AWS/GCP), this **MUST** be disabled in favor of Flyway/Liquibase migrations.
