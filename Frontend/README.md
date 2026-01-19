# Frontend Engineering Guide

This directory contains the React + TypeScript frontend for StudentNotes.

## 🧠 Core Concepts

### 1. Hybrid Data Mode (`DataProvider.jsx`)
The application does not assume the backend exists. On boot, `DataProvider` attempts to ping `/api/health`.
- **Success**: Sets `mode = 'dynamic'`. Enables Auth Context, Uploads, and Admin logic.
- **Failure**: Sets `mode = 'static'`. Falls back to `notesLoader.js`, which parses local Markdown files in `src/pages`.

### 2. Recursive Routing
We do not hardcode routes for notes.
- `treeUtils.ts` transforms the flat/nested file structure into a normalized `FileSystemNode[]` tree.
- `router.jsx` recursively iterates this tree to generate `<Route>` definitions for every leaf node.
- This allows the sidebar and router to stay in sync automatically.

### 3. Ethical UX Pattern (`useEthicalDelay.ts`)
We deliberately introduce friction in high-value actions (like PDF Export) to present ethical CTAs (e.g., "Star us on GitHub").
- **Constraint**: This delay MUST NOT annoy returning users.
- **Implementation**: We check `localStorage` for a completion flag. If found, the delay is skipped entirely (0ms).

---

## 🧱 Key Components

### `Layout.jsx`
The layout orchestrates the "Scientific Design" aesthetic.
- **Glassmorphism**: Uses `backdrop-blur-xl` on sticky headers.
- **Reading Progress**: A scroll-linked progress bar at the top (0px height -> 3px height).
- **Split View**: Allows "pinning" a note to the left (`Reference Mode`) while browsing others on the right.

### `FolderTree.tsx`
A recursive component that renders the sidebar navigation.
- **Performance**: Uses `React.memo` (implicitly via structure) and keeps local state for expansion.
- **Animation**: Uses `framer-motion` for staggered child reveals (`staggerChildren: 0.05`).

### `NoteViewer.tsx` (via `MarkdownRenderer`)
The core reading experience.
- **Sanitization**: Uses `DOMPurify` (via React Markdown safe defaults) to prevent XSS.
- **Enhancements**: Hydrates standard Markdown with:
  - `KaTeX` for math.
  - `SyntaxHighlighter` for code blocks (with Copy button).
  - `Framer Motion` for image entrance animations.

---

## 🎨 Theme System
We use **Tailwind CSS** with a `class` based dark mode.
- `ThemeToggle.jsx` persists preference to `localStorage`.
- We avoid "system default" flickering by checking preference in `index.html` blocking script (before React audits).

## 🛠 Commands

```bash
# Run dev server
npm run dev

# Run unit tests
npm test

# Build for production
npm run build
```
