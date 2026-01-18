// Frontmatter Parser for Markdown
function parseFrontMatter(text) {
  const pattern = /^---\n([\s\S]*?)\n---\n([\s\S]*)$/;
  const match = text.match(pattern);

  if (!match) {
    return { meta: { title: 'Untitled', order: 999 }, content: text };
  }

  const metaBlock = match[1];
  const content = match[2];
  
  const meta = {};
  metaBlock.split('\n').forEach(line => {
    const [key, ...value] = line.split(':');
    if (key && value) {
      let val = value.join(':').trim().replace(/^['"](.*)['"]$/, '$1');
      if (!isNaN(val)) val = Number(val);
      meta[key.trim()] = val;
    }
  });

  return { meta, content };
}

export function loadNotesTree() {
  // 1. Load JSX files (Modules)
  const jsxModules = import.meta.glob('../pages/**/*.jsx', { eager: true });
  
  // 2. Load MD files (Raw Text)
  const mdModules = import.meta.glob('../pages/**/*.md', { as: 'raw', eager: true });

  const tree = {};

  // Helper to process any file path
  const processFile = (path, module, type) => {
    // Skip Home and System files
    if (path.includes('/Home.jsx') || path.includes('/NotFound.jsx')) return;

    // Clean Path logic
    const cleanPath = path.replace('../pages/', '').replace(/\.(jsx|md)$/, '');
    const parts = cleanPath.split('/');

    // Strict 5-level check
    if (parts.length !== 5) return;

    const [dept, year, section, subject, filename] = parts;
    let meta, content, component;

    if (type === 'jsx') {
      meta = module.meta || { title: filename, order: 999 };
      component = module.default;
    } else {
      const parsed = parseFrontMatter(module);
      meta = parsed.meta;
      content = parsed.content;
    }

    // Build Tree
    if (!tree[dept]) tree[dept] = {};
    if (!tree[dept][year]) tree[dept][year] = {};
    if (!tree[dept][year][section]) tree[dept][year][section] = {};
    if (!tree[dept][year][section][subject]) tree[dept][year][section][subject] = [];

    tree[dept][year][section][subject].push({
      id: filename,
      type: type, // 'jsx' or 'md'
      meta,
      component, // Only for JSX
      content    // Only for MD
    });
  };

  // Process JSX Files
  for (const path in jsxModules) {
    processFile(path, jsxModules[path], 'jsx');
  }

  // Process MD Files
  for (const path in mdModules) {
    processFile(path, mdModules[path], 'md');
  }

  // Sort logic
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