# 🎓 ExamNotes

ExamNotes is a high-performance, professional documentation platform designed to centralize and simplify academic resources. Built with **React**, **Vite**, and **Tailwind CSS**, it completely eliminates the need for manual navigation updates by using a filesystem-driven architecture.

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)

---

## ✨ Key Features

- 🚀 **Magic Folder Navigation:** No more hardcoded links. Create a folder in `src/pages` and the navigation menu, breadcrumbs, and search index update automatically.
- 🌗 **Circular Wipe Transition:** A modern, smooth dark mode toggle that "spreads" light or "sucks" darkness from the point of click (using View Transitions API).
- 📑 **Split-Screen View:** Pin a note to the left and continue browsing other notes on the right—perfect for comparing concepts or checking questions while reading.
- 🖨️ **Pro PDF Generation:** Download notes as high-quality PDFs. Includes "Ink-Saver" mode (forces white background), automatic watermarking (`shankar.com`), and GitHub contribution links.
- 🔍 **Spotlight Search:** Instant global search for any note or subject across all departments using `Cmd/Ctrl + K`.
- 📱 **Fully Responsive:** Desktop-class features (collapsible sidebar, split view) that scale down perfectly for mobile study sessions.

---

## 📂 Project Structure

Navigation is built 100% automatically based on this directory depth:
`Department` → `Year` → `Section` → `Subject` → `Note.jsx`

```text
src/pages/
  ├── cs/                     <-- Department
  │   ├── year1/              <-- Year
  │   │   ├── section-a/      <-- Section
  │   │   │   ├── c-prog/     <-- Subject
  │   │   │   │   ├── Unit1.jsx  <-- Actual Note
  │   │   │   │   └── Unit2.jsx


  ---

## 👋 About the Creator

I'm **Shankar**, a student at **Sri Manakula Vinayagar Engineering College (SMVEC)**. 

I built this platform because I was tired of hunting through WhatsApp groups and scattered drives for exam resources. My goal was to create a centralized, fast, and professional tool to help my fellow SMVECians and friends ace their exams with ease.

It took many late nights and plenty of coffee to build this. If you find it useful, I'd love to hear your feedback!

- **Instagram:** [@_shankar_831](https://instagram.com/_shankar_831/)
- **GitHub:** [shankarx831](https://github.com/shankarx831)
- **College:** SMVEC 🎓

**Disclaimer:** I try my best to keep these notes accurate, but I am a student just like you! Always double-check critical concepts with your official textbooks.

---