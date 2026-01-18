# Student Notes Platform 🎓

A next-generation study platform built with **Spring Boot** and **React**. Designed for seamless information sharing and optimized learning.

## ✨ Key Features

- **📖 Smart Reading Experience**: LaTeX support, Table of Contents navigation, and reading progress tracking.
- **🧘 Study Focus Mode**: Distraction-free environment for deep learning.
- **📊 Interactive Feedback**: Students can like/dislike notes to provide feedback to teachers.
- **🔍 Advanced Search**: Full-text search engine to find topics instantly.
- **🛡️ Hierarchy-Based Organization**: Notes organized by Dept -> Year -> Section -> Subject.
- **🏫 Teacher & Admin Dashboards**: Centralized management for content uploads and user roles.

## 🛠️ Technology Stack

- **Frontend**: React, Vite, TailwindCSS, React-Markdown.
- **Backend**: Spring Boot, Spring Security (JWT), Hibernate/JPA.
- **Database**: SQL (PostgreSQL/MySQL recommended).

## 🚀 Getting Started

1. **Backend**:
   - Navigate to `/Backend`.
   - Update `application.properties` with your DB credentials.
   - Run `./mvnw spring-boot:run`.

2. **Frontend**:
   - Navigate to `/Frontend`.
   - Run `npm install`.
   - Run `npm run dev`.

## 🌐 JAR Online/Offline Mode
The platform intelligently detects backend connectivity. Features requiring database interaction (voting, dashboards) automatically toggle based on the server's availability.

---
*Built with ❤️ for scholars everywhere.*