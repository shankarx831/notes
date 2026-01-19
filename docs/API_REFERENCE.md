# API Reference

**Base URL**: `http://localhost:8080/api`

This page documents the REST API surface for the StudentNotes backend.
All endpoints expect `Content-Type: application/json`.

---

## 🔑 Authentication

### Login
**POST** `/auth/login`

Returns a JWT token for the session.

**Request:**
```json
{
  "email": "teacher@test.com",
  "password": "password"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUz...",
  "type": "Bearer",
  "name": "Teacher Name",
  "role": "ROLE_TEACHER"
}
```

---

## 📚 Notes

### Get Note Tree (Dynamic)
**GET** `/notes/tree`

Returns the hierarchical structure of all PUBLISHED notes.

**Response:**
```json
{
  "cs": {
    "year1": {
      "networks": [
        { "id": "uuid-1", "title": "Intro to TCP", "author": "Dr. Smith" }
      ]
    }
  }
}
```

### Upload Note (Teacher)
**POST** `/notes`
**Auth**: `ROLE_TEACHER` | `ROLE_ADMIN`

**Request:**
```json
{
  "title": "Advanced Algorithms",
  "content": "# Markdown Content...",
  "department": "cs",
  "year": "3",
  "subject": "algorithms"
}
```

### Request Deletion (Teacher)
**DELETE** `/notes/{id}`
**Auth**: `ROLE_TEACHER`

Creates a `DeletionRequest`. Does not immediately delete the note.

**Response (202 Accepted):**
```json
{
  "message": "Deletion requested. Awaiting Admin approval.",
  "requestId": 15
}
```

---

## 🛡 Admin

### Dashboard Overview
**GET** `/admin/dashboard`
**Auth**: `ROLE_ADMIN`

Returns system health and stats.

**Response:**
```json
{
  "totalNotes": 150,
  "pendingDeletions": 3,
  "activeUsers": 45,
  "recentActivity": [...]
}
```

### Approve Deletion
**PUT** `/admin/requests/{requestId}/approve`
**Auth**: `ROLE_ADMIN`

Finalizes the deletion of a note.

---

## 🚦 Health Check

### Backend Status
**GET** `/health` (or `/api/health`)

Used by the Frontend `DataProvider` to decide between Static and Dynamic mode.

**Response:**
```json
{
  "status": "UP",
  "db": "CONNECTED"
}
```
