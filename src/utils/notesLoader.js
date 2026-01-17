export function loadNotesTree() {
  const modules = import.meta.glob('../pages/**/*.jsx', { eager: true });
  
  const tree = {};

  for (const path in modules) {
    const module = modules[path];
    
    // FIX: Ignore Home.jsx and any non-nested files to prevent console warnings
    if (path.includes('/Home.jsx') || path.includes('/NotFound.jsx')) continue;

    // Normalize path
    const cleanPath = path.replace('../pages/', '').replace(/\.jsx$/, '');
    const parts = cleanPath.split('/');

    // Check for exactly 5 levels
    if (parts.length !== 5) {
      // Only warn if it really looks like a note file (deep structure) but is wrong
      if (parts.length > 2) {
        console.warn(`Skipping ${path}: Expected 5 levels, got ${parts.length}`);
      }
      continue;
    }

    const [dept, year, section, subject, filename] = parts;
    const meta = module.meta || { title: filename, order: 999 };

    // Build tree...
    if (!tree[dept]) tree[dept] = {};
    if (!tree[dept][year]) tree[dept][year] = {};
    if (!tree[dept][year][section]) tree[dept][year][section] = {};
    if (!tree[dept][year][section][subject]) tree[dept][year][section][subject] = [];

    tree[dept][year][section][subject].push({
      id: filename,
      meta: meta,
      component: module.default
    });
  }

  // Sort logic...
  for (const d in tree) {
    for (const y in tree[d]) {
      for (const sec in tree[d][y]) {
        for (const s in tree[d][y][sec]) {
          tree[d][y][sec][s].sort((a, b) => a.meta.order - b.meta.order);
        }
      }
    }
  }

  return tree;
}