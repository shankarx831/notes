# 🎓 StudentNotes

**StudentNotes** is a professional-grade documentation platform engineered to centralize and simplify study materials for students at **SMVEC**. It features a custom-built **Hybrid Note-Builder Engine** that transforms simple text into a high-fidelity, interactive learning environment.

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Markdown](https://img.shields.io/badge/Markdown-000000?style=for-the-badge&logo=markdown&logoColor=white)
![KaTeX](https://img.shields.io/badge/KaTeX-3185FC?style=for-the-badge&logo=katex&logoColor=white)

---

## ✨ Key Features

- 🧠 **Hybrid Note-Builder Engine:** Write in **Markdown (.md)** for maximum efficiency or **JSX (.jsx)** for complex interactivity. The engine automatically parses Markdown H2 headers into elegant, shadow-styled "Question Cards."
- 📐 **Advanced LaTeX Support:** Integration with **KaTeX** specifically tuned for Engineering. Render complex integrals, matrices, and fractions with textbook precision and high-priority CSS resets.
- 🚀 **Filesystem-Driven Navigation:** No manual link management. The UI and routing are automatically generated based on folder depth: `Department` → `Year` → `Section` → `Subject` → `Note`.
- 🌗 **Circle Wipe Dark Mode:** A modern UI transition that "spreads" light or "sucks" darkness from the exact point of user interaction (using the View Transitions API).
- 📑 **Productivity Split-Screen:** Pin a reference note to the left and continue browsing on the right—perfect for cross-referencing topics or lab manuals.
- 🖨️ **Pro PDF Generation:** Client-side generation of high-quality, ink-saving documents. Features include automatic "Light Mode" forcing, small-footprint JPEG compression, and per-page watermarking (`shankar.com`).
- 🔍 **Spotlight Search:** Global indexing of all notes. Find any subject or unit instantly with the `Cmd/Ctrl + K` shortcut.

---

## 📂 Folder Architecture

Structure your files and the engine builds the site for you:

```text
src/pages/
  ├── it/                     <-- Department
  │   ├── year2/              <-- Year
  │   │   ├── section-a/      <-- Section
  │   │   │   ├── networks/   <-- Subject
  │   │   │   │   ├── Unit1.md      <-- Parsed into Question Cards
  │   │   │   │   └── Lab_Work.jsx  <-- Full React Flexibility
```

---

## 🛠️ Tech Stack

- **Core:** React 18, Vite, React Router 6 (Hash Routing)
- **Styling:** Tailwind CSS v3 + Typography Plugin
- **Parsing:** `react-markdown` + `remark-math` + `rehype-katex`
- **PDF System:** `html2canvas` + `jsPDF`
- **Code Syntax:** `react-syntax-highlighter` (vscDarkPlus Theme)

---

## 🚀 Getting Started

### Prerequisites
- Node.js (v18+ recommended)

### Installation & Deployment
1. **Clone & Install:**
   ```bash
   git clone https://github.com/shankarx831/notes.git
   cd notes
   npm install
   ```
2. **Development:**
   ```bash
   npm run dev
   ```
3. **Deploy to GitHub Pages:**
   ```bash
   npm run deploy
   ```

---

## 👋 About the Creator

I'm **Shankar**, a student at **Sri Manakula Vinayagar Engineering College (SMVEC)**.

I built **StudentNotes** because I was tired of the "WhatsApp PDF Chaos"—searching through infinite chats for buried exam resources. I wanted a platform that feels like high-end developer documentation but is built for the specific needs of my fellow SMVECians.

This project is open-source and built for the community. If it helps you prepare for your internals or semesters, feel free to give it a star!

- **Instagram:** [@_shankar_831](https://instagram.com/_shankar_831/)
- **GitHub:** [shankarx831](https://github.com/shankarx831)
- **College:** SMVEC 🎓

**Disclaimer:** I am a student, not a professor. While the engine is professional, the content is human-written and may contain errors. Please use these notes as a reference and cross-check with official textbooks.

---

⭐ **Star this repository if you found it useful!**