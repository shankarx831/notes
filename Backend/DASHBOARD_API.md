# Dashboard & Operational Features API Documentation

## Overview

This document describes the MAANG-level dashboard backend APIs for the Student Notes Platform. These APIs power the admin and teacher operational dashboards.

## Design Principles

- **Reliability > Features** - APIs fail gracefully, never silently
- **Explicit State Machines** - No boolean flags, clear status enums
- **Auditable Actions** - Every significant action creates an audit log entry
- **Idempotent APIs** - Safe to retry, handles double-clicks
- **Pagination Everywhere** - All list endpoints are paginated
- **No Entity Leakage** - DTOs only, never expose raw database IDs

## Authentication

All endpoints require JWT authentication via Bearer token:

```http
Authorization: Bearer <jwt_token>
```

## Response Format

All responses follow the standard `ApiResponse` structure:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-19T10:00:00Z",
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

Error responses:

```json
{
  "success": false,
  "data": null,
  "error": {
    "message": "Resource not found",
    "code": "RESOURCE_NOT_FOUND",
    "fieldErrors": null
  },
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-19T10:00:00Z"
}
```

## Correlation ID

All requests are traced via correlation ID:
- Pass `X-Correlation-ID` header to use your own (for distributed tracing)
- If not provided, one is generated automatically
- Response includes `X-Correlation-ID` header

---

# Admin Dashboard API

**Base URL:** `/api/admin`  
**Required Role:** `ROLE_ADMIN`

## System Overview

### GET /api/admin/overview

Returns aggregated dashboard metrics. Designed for graceful degradation - if one metric fails, others still load.

**Response:**
```json
{
  "success": true,
  "data": {
    "totalNotes": 1250,
    "publishedNotes": 1000,
    "draftNotes": 200,
    "deletedNotes": 30,
    "archivedNotes": 15,
    "deletePendingNotes": 5,
    "totalUsers": 50,
    "activeTeachers": 45,
    "disabledTeachers": 5,
    "pendingDeletionRequests": 3,
    "recentActivity": {
      "notesUploadedLast24h": 15,
      "notesUploadedLast7d": 75,
      "notesUploadedLast30d": 250,
      "deletionRequestsLast24h": 1,
      "deletionRequestsLast7d": 5
    },
    "healthStatus": {
      "databaseHealthy": true,
      "cacheHealthy": true,
      "lastCheckTime": "2026-01-19T10:00:00",
      "readOnlyMode": false
    },
    "computedAt": "2026-01-19T10:00:00"
  }
}
```

**Graceful Degradation:** If a metric fails, it returns `-1` instead of failing the entire request.

---

## Deletion Request Management

### GET /api/admin/deletion-requests

Lists deletion requests with filtering and pagination.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | enum | Filter by: `PENDING`, `APPROVED`, `REJECTED` |
| `teacherId` | long | Filter by teacher (use internal ID) |
| `fromDate` | ISO datetime | Start date filter |
| `toDate` | ISO datetime | End date filter |
| `page` | int | Page number (0-indexed, default: 0) |
| `size` | int | Page size (default: 20, max: 100) |
| `sortBy` | string | Sort field (default: `requestedAt`) |
| `sortDir` | string | Sort direction: `asc` or `desc` (default: `desc`) |

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "publicId": "550e8400-e29b-41d4-a716-446655440000",
        "note": {
          "publicId": "uuid",
          "title": "Network Fundamentals",
          "department": "it",
          "year": "year2",
          "section": "section-a",
          "subject": "networks",
          "status": "DELETE_PENDING",
          "createdAt": "2026-01-15T08:00:00"
        },
        "requestedBy": {
          "publicId": "uuid",
          "name": "John Teacher",
          "email": "john@example.com"
        },
        "reason": "Content is outdated and replaced by newer materials",
        "status": "PENDING",
        "requestedAt": "2026-01-18T14:30:00",
        "resolution": null
      }
    ],
    "pageable": { ... },
    "totalElements": 5
  },
  "pagination": { ... }
}
```

### POST /api/admin/deletion-requests/{publicId}/approve

Approves a deletion request. **Idempotent** - safe to call multiple times.

**Path Parameters:**
- `publicId` - Deletion request's public UUID

**Response:**
```json
{
  "success": true,
  "data": {
    "publicId": "uuid",
    "status": "APPROVED",
    "resolution": {
      "resolvedBy": {
        "publicId": "uuid",
        "name": "Admin User",
        "email": "admin@example.com"
      },
      "resolvedAt": "2026-01-19T10:00:00",
      "rejectionReason": null
    }
  }
}
```

**Effects:**
- Sets request status to `APPROVED`
- Soft-deletes the note (status → `DELETED`)
- Creates audit log entry

### POST /api/admin/deletion-requests/{publicId}/reject

Rejects a deletion request. Requires rejection reason.

**Request Body:**
```json
{
  "reason": "Content is still relevant for the curriculum"
}
```

**Effects:**
- Sets request status to `REJECTED`
- Restores note status to `PUBLISHED`
- Stores rejection reason
- Creates audit log entry

---

## User & Permission Management

### GET /api/admin/teachers

Lists all teacher accounts with pagination.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | enum | Filter by: `ACTIVE`, `DISABLED`, `SUSPENDED` |
| `search` | string | Search by name or email |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |

### GET /api/admin/teachers/{publicId}

Gets a single teacher's details with statistics.

### POST /api/admin/teachers

Creates a new teacher account.

**Request Body:**
```json
{
  "email": "newteacher@example.com",
  "password": "SecurePass123!",
  "name": "New Teacher",
  "phoneNumber": "+91-9876543210",
  "assignedDepartments": ["it", "cs"]
}
```

### PATCH /api/admin/teachers/{publicId}/permissions

Updates a teacher's folder-level permissions.

**Request Body:**
```json
{
  "assignedDepartments": ["it", "cs", "ece"],
  "folderPermissions": [
    {
      "folderPath": "it/year2",
      "canRead": true,
      "canWrite": true,
      "canDelete": false,
      "canManage": false,
      "expiresAt": "2026-06-30T23:59:59"
    }
  ]
}
```

### PATCH /api/admin/users/{publicId}/disable

Disables a user account (no hard deletes).

**Request Body:**
```json
{
  "reason": "Employee resignation effective 2026-01-31"
}
```

### PATCH /api/admin/users/{publicId}/enable

Re-enables a previously disabled user account.

---

## Audit Logs

### GET /api/admin/audit-logs

Lists audit logs with filtering. Read-only, append-only.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `actorId` | long | Filter by actor's user ID |
| `action` | enum | Filter by action type |
| `targetType` | string | Filter by target type: `Note`, `User`, `DeletionRequest` |
| `fromDate` | ISO datetime | Start date filter |
| `toDate` | ISO datetime | End date filter |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 50, max: 100) |

**Action Types:**
- `NOTE_CREATED`, `NOTE_UPDATED`, `NOTE_PUBLISHED`, `NOTE_DELETED`
- `DELETION_REQUESTED`, `DELETION_APPROVED`, `DELETION_REJECTED`
- `USER_CREATED`, `USER_DISABLED`, `USER_ENABLED`, `USER_PERMISSIONS_UPDATED`

---

# Teacher Dashboard API

**Base URL:** `/api/teacher`  
**Required Role:** `ROLE_TEACHER`

## Dashboard

### GET /api/teacher/dashboard

Returns teacher's dashboard overview with notes and deletion requests.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |

**Response:**
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalNotes": 25,
      "draftNotes": 5,
      "publishedNotes": 18,
      "deletePendingNotes": 2,
      "deletedNotes": 0,
      "pendingDeletionRequests": 2
    },
    "notes": [ ... ],
    "notesByFolder": {
      "it/year2/section-a/networks": [ ... ],
      "it/year2/section-b/databases": [ ... ]
    },
    "deletionRequests": [ ... ]
  }
}
```

---

## Notes Management

### GET /api/teacher/notes

Lists teacher's own notes (teachers see ONLY their notes).

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | enum | Filter by: `DRAFT`, `PUBLISHED`, `DELETE_PENDING`, `DELETED` |
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |
| `sortBy` | string | Sort field (default: `updatedAt`) |
| `sortDir` | string | Sort direction (default: `desc`) |

### GET /api/teacher/notes/{publicId}

Gets a single note's details (owner only).

### POST /api/teacher/notes

Creates a new note (creates version 1).

**Request Body:**
```json
{
  "title": "Introduction to Networking",
  "department": "it",
  "year": "year2",
  "section": "section-a",
  "subject": "networks",
  "content": "# Introduction\n\nThis chapter covers...",
  "changeSummary": "Initial version",
  "publishImmediately": false
}
```

**Notes:**
- New upload = new version (version 1)
- Old versions remain immutable
- File size enforced (max 10MB)
- Folder permission enforced

### PUT /api/teacher/notes/{publicId}

Updates a note (creates new version).

**Request Body:**
```json
{
  "title": "Introduction to Networking - Updated",
  "content": "# Introduction\n\nUpdated content...",
  "changeSummary": "Fixed typos and added new sections"
}
```

### POST /api/teacher/notes/{publicId}/publish

Publishes a draft note.

### GET /api/teacher/notes/{publicId}/versions

Gets version history for a note.

---

## Deletion Requests

### POST /api/teacher/notes/{publicId}/request-delete

Requests deletion of a note. **No direct delete allowed.**

**Request Body:**
```json
{
  "reason": "Content is outdated and has been replaced"
}
```

**Rules:**
- One active request per note
- Duplicate requests are rejected
- Status visible to teacher
- Admin must approve/reject

### GET /api/teacher/deletion-requests

Lists teacher's deletion requests with status.

---

# Note & Deletion Status State Machines

## Note Status

```
                  ┌──────────────┐
                  │    DRAFT     │
                  └──────┬───────┘
                         │ publish
                         ▼
              ┌──────────────────────┐
              │     PUBLISHED        │
              └──────────┬───────────┘
                         │ request delete
                         ▼
              ┌──────────────────────┐
              │   DELETE_PENDING     │
              └─────┬────────┬───────┘
        approve     │        │ reject
                    ▼        ▼
            ┌───────────┐   ┌───────────┐
            │  DELETED  │   │ PUBLISHED │
            └───────────┘   └───────────┘
                 │
                 ▼
            ┌───────────┐
            │ ARCHIVED  │  (terminal state)
            └───────────┘
```

## Deletion Request Status

```
            ┌───────────┐
            │  PENDING  │
            └─────┬─────┘
                  │
      ┌───────────┴───────────┐
      │                       │
      ▼                       ▼
┌───────────┐          ┌───────────┐
│ APPROVED  │          │ REJECTED  │
└───────────┘          └───────────┘
     │                       │
     └───────────┬───────────┘
                 │
        (Immutable after resolution)
```

---

# Rate Limiting

Write endpoints are rate-limited:
- Default: 30 requests/minute per user
- Write operations: 10 requests/minute per user

When rate limited, response includes:
```http
HTTP/1.1 429 Too Many Requests
Retry-After: 45

{
  "success": false,
  "error": {
    "message": "Rate limit exceeded. Please retry after 45 seconds",
    "code": "RATE_LIMIT_EXCEEDED"
  }
}
```

---

# Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `RESOURCE_NOT_FOUND` | 404 | Requested resource does not exist |
| `DUPLICATE_DELETION_REQUEST` | 409 | A pending deletion request already exists for this note |
| `ALREADY_RESOLVED` | 409 | Deletion request has already been approved/rejected |
| `INVALID_STATE_TRANSITION` | 409 | Note cannot transition from current status to target status |
| `EMAIL_ALREADY_EXISTS` | 409 | Email is already registered |
| `CANNOT_DISABLE_SELF` | 409 | Cannot disable your own account |
| `ACCESS_DENIED` | 403 | Insufficient permissions |
| `NO_FOLDER_PERMISSION` | 403 | No access to specified folder |
| `NOT_RESOURCE_OWNER` | 403 | Can only access resources you own |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `VALIDATION_FAILED` | 400 | Request validation failed (check fieldErrors) |
| `CONCURRENT_MODIFICATION` | 409 | Resource was modified by another request |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

# Examples

## Approve Deletion Request

```bash
curl -X POST \
  http://localhost:8080/api/admin/deletion-requests/550e8400-e29b-41d4-a716-446655440000/approve \
  -H 'Authorization: Bearer <admin_jwt_token>' \
  -H 'X-Correlation-ID: my-trace-id-123'
```

## Create Note (Teacher)

```bash
curl -X POST \
  http://localhost:8080/api/teacher/notes \
  -H 'Authorization: Bearer <teacher_jwt_token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Database Normalization",
    "department": "it",
    "year": "year2",
    "section": "section-a",
    "subject": "databases",
    "content": "# Normal Forms\n\n## 1NF\n...",
    "changeSummary": "Initial version covering 1NF to 3NF",
    "publishImmediately": true
  }'
```

## Request Note Deletion

```bash
curl -X POST \
  http://localhost:8080/api/teacher/notes/550e8400-uuid/request-delete \
  -H 'Authorization: Bearer <teacher_jwt_token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "reason": "Content has been superseded by updated curriculum materials"
  }'
```
