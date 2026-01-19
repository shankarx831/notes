# Frontend Utilities (`/src/utils`)

This directory contains pure logic helpers and data loaders that are decoupled from the React component tree.

## 📄 `notesLoader.js`
The backbone of **Static Mode**.
- **Responsibility**: Scans the `src/pages` directory for `.md` files (via Vite's `import.meta.glob`).
- **Logic**: 
  - Parses file paths (e.g., `it/year1/section-a/networks/intro.md`).
  - Extracts metadata (department, year, section, subject).
  - Hydrates each note with a unique ID derived from the filename.
- **Why it exists**: To allow a purely filesystem-based blog/documentation experience without needing a database for basic reading.

## 🖨 `pdfExporter.js`
Handles high-fidelity PDF generation.
- **Tech**: `html2canvas` + `jsPDF`.
- **Logic**:
  - Clones the target DOM element to prevent user interaction artifacts during capture.
  - Applies a high-quality scale (e.g., 2x) for print clarity.
  - Generates an A4-sized PDF on the fly.
- **Constraint**: Purely client-side; performance varies with device CPU/Memory.

## 🌳 `treeUtils.ts` (Typed)
Data transformation layer for the sidebar.
- **Responsibility**: Converts a flat array of `Note` objects (from API or static loader) into a nested `FileSystemNode` tree.
- **Sorting**: Implements deterministic sorting (Alphabetical for folders, Chronological for files).
- **Maintenance**: Treat this as a pure function. Always write unit tests in `treeUtils.test.ts` before modifying recursion logic.
