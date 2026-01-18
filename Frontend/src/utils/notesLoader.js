// ------------------------------
// Frontmatter Parser for Markdown
// ------------------------------
function parseFrontMatter(text) {
  const pattern = /^---\n([\s\S]*?)\n---\n([\s\S]*)$/;
  const match = text.match(pattern);

  if (!match) {
    return {
      meta: { title: 'Untitled', order: 999 },
      content: text
    };
  }

  const metaBlock = match[1];
  const content = match[2];

  const meta = {};
  metaBlock.split('\n').forEach(line => {
    const [key, ...value] = line.split(':');
    if (!key || !value.length) return;

    let val = value.join(':').trim();
    val = val.replace(/^['"](.*)['"]$/, '$1');
    if (!isNaN(val)) val = Number(val);

    meta[key.trim()] = val;
  });

  return { meta, content };
}

// ------------------------------
// Notes Tree Loader
// ------------------------------
export function loadNotesTree() {
  // 1. Load JSX files as modules
  const jsxModules = import.meta.glob('../pages/**/*.jsx', { eager: true });

  // 2. Load Markdown files as RAW strings (Vite best practice)
  const mdModules = import.meta.glob('../pages/**/*.md', {
    query: '?raw',
    import: 'default',
    eager: true
  });

  const tree = {};

  // ------------------------------
  // File Processor
  // ------------------------------
  const processFile = (path, module, type) => {
    // Skip system pages
    if (
      path.includes('/Home.jsx') ||
      path.includes('/NotFound.jsx')
    ) return;

    // Clean path → remove base + extension
    const cleanPath = path
      .replace('../pages/', '')
      .replace(/\.(jsx|md)$/, '');

    const parts = cleanPath.split('/');

    // Enforce strict structure:
    // dept/year/section/subject/filename
    if (parts.length !== 5) return;

    const [dept, year, section, subject, filename] = parts;

    let meta;
    let content;
    let component;

    if (type === 'jsx') {
      meta = module.meta || { title: filename, order: 999 };
      component = module.default;
    } else {
      const parsed = parseFrontMatter(module);
      meta = parsed.meta;
      content = parsed.content;
    }

    // Build tree structure
    if (!tree[dept]) tree[dept] = {};
    if (!tree[dept][year]) tree[dept][year] = {};
    if (!tree[dept][year][section]) tree[dept][year][section] = {};
    if (!tree[dept][year][section][subject])
      tree[dept][year][section][subject] = [];

    tree[dept][year][section][subject].push({
      id: filename,
      type,          // 'jsx' | 'md'
      meta,
      component,     // JSX only
      content         // MD only
    });
  };

  // ------------------------------
  // Process Files
  // ------------------------------
  for (const path in jsxModules) {
    processFile(path, jsxModules[path], 'jsx');
  }

  for (const path in mdModules) {
    processFile(path, mdModules[path], 'md');
  }

  // ------------------------------
  // Sort by meta.order
  // ------------------------------
  for (const d in tree) {
    for (const y in tree[d]) {
      for (const sec in tree[d][y]) {
        for (const subj in tree[d][y][sec]) {
          tree[d][y][sec][subj].sort(
            (a, b) => (a.meta.order ?? 999) - (b.meta.order ?? 999)
          );
        }
      }
    }
  }

  return tree;
}
