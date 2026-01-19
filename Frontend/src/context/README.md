# Context Providers (`/src/context`)

We use React Context for global state management to avoid prop-drilling in our recursive layouts.

## 📡 `DataProvider.jsx`
The primary state orchestrator for content.
- **Boot Sequence**:
  1. Checks `localStorage` for `app_has_visited` (controls Splash Screen).
  2. Pings Backend.
  3. If Backend UP: Fetches real-time tree from `/api/notes/tree`.
  4. If Backend DOWN: Loads static tree from `utils/notesLoader`.
- **Exposed Value**: `tree`, `loading`, `mode`, `backendAvailable`.

## 🔑 `AuthContext.jsx`
Manages JWT-based user sessions.
- **Logic**: 
  - On mount, validates `localStorage` token.
  - Handles the `login()` and `logout()` side effects (clearing storage, redirecting).
- **Security**: Wraps the `ROLE` based visibility logic used in the Header/Sidebar.

---

### Best Practices for Future Maintainers
- **Avoid Over-usage**: Context should only be used for "truly global" data. Component-specific state should live in `useState` or a dedicated Store (Zustand/Redux) if it becomes too complex.
- **Type Safety**: When adding new fields to `DataProvider`, ensure they are added to the JSDoc/Type definitions to prevent undefined crashes in the Sidebar.
