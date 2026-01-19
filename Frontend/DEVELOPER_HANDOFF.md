# ExamNotes Frontend - Developer Handoff

## 🚀 Overview
MAANG-level, mobile-first frontend for ExamNotes. Built with React (TypeScript), Vite, Tailwind CSS, and Framer Motion. 
Designed for 360x800px mobile baseline, scaling up to desktop.

## 🛠 Tech Stack
- **Framework**: React 19 + Vite 6
- **Language**: TypeScript
- **Styling**: Tailwind CSS (Utility-first)
- **Animation**: Framer Motion
- **Markdown**: react-markdown + rehype-katex (Math) + prism (Code)
- **State/Data**: React Router v7 (Data Loading) + Context
- **Testing**: Vitest + React Testing Library

## 📂 Directory Structure
```
src/
├── components/
│   ├── ui/             # Atomic primitives (Button, Input)
│   ├── note-viewer/    # NoteViewer specific components
│   └── ...
├── types/              # TypeScript interfaces (models)
├── pages/              # Route views
├── context/            # Global state (Auth, Theme)
└── test/               # Test setup
```

## 🎨 Design System & Motion Spec
- **Colors**: Uses Tailwind's `blue` (Primary), `red` (Destructive), `gray` (Neutral).
- **Typography**: System font stack by default (`sans-serif`), optimized for reading.
- **Micro-interactions**:
  - `whileTap={{ scale: 0.95 }}` for buttons.
  - `whileHover={{ scale: 1.02 }}` for cards.
  - Transitions: `duration: 0.2` (120-220ms) for UI feedback.
  - Page Transitions: `opacity` fade + slight `y` slide.

## 🧪 Testing
Run tests with Vitest:
```bash
npm test
```
Tests are located alongside components (e.g., `NoteViewer.test.tsx`).

## 📚 Storybook
Stories are defined in `*.stories.tsx` files.
To initialize Storybook locally:
```bash
npx storybook@latest init --type react_vite
```
Then run:
```bash
npm run storybook
```

## 🚢 Deployment
**GitHub Pages (Static Mode)**
1. Ensure `base: '/repo-name/'` is set in `vite.config.ts`.
2. Run `npm run build`.
3. Deploy the `dist/` folder.

**Full Stack Mode**
1. Configure API Base URL in `.env` (`VITE_API_URL=http://localhost:8080`).
2. Build outputs static assets to `dist/`.
3. Serve `dist/` via Nginx or backend static hosting.

## 🏃‍♂️ Running Locally
To start the dev server:
```bash
npm start
# or
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) to view.
2. Implement `FolderTree` component (recursive).
3. Connect `TeacherUpload` form to backend API using `fetch` or `react-query`.
4. Add E2E tests with Playwright.
