import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';

const SearchModal = ({ tree, isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();
  const inputRef = React.useRef(null);

  // Focus input when opened
  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => inputRef.current.focus(), 50);
    }
  }, [isOpen]);

  // Flatten the tree into a single searchable list
  const allNotes = useMemo(() => {
    const notes = [];
    if (!tree) return notes;

    Object.keys(tree).forEach(dept => {
      Object.keys(tree[dept]).forEach(year => {
        Object.keys(tree[dept][year]).forEach(sec => {
          Object.keys(tree[dept][year][sec]).forEach(subj => {
            tree[dept][year][sec][subj].forEach(note => {
              notes.push({
                title: note.meta.title,
                path: `/${dept}/${year}/${sec}/${subj}/${note.id}`,
                breadcrumbs: `${dept.toUpperCase()} > ${year} > ${subj.toUpperCase()}`
              });
            });
          });
        });
      });
    });
    return notes;
  }, [tree]);

  // Filter results
  const results = allNotes.filter(n => 
    n.title.toLowerCase().includes(query.toLowerCase()) || 
    n.breadcrumbs.toLowerCase().includes(query.toLowerCase())
  );

  const handleSelect = (path) => {
    navigate(path);
    onClose();
    setQuery('');
  };

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 z-[100] bg-black/50 backdrop-blur-sm flex items-start justify-center pt-20 transition-opacity"
      onClick={onClose}
    >
      <div 
        className="bg-white dark:bg-gray-800 w-full max-w-xl rounded-xl shadow-2xl overflow-hidden border border-gray-200 dark:border-gray-700 mx-4" 
        onClick={e => e.stopPropagation()}
      >
        {/* Search Header */}
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center gap-3">
          <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
          <input 
            ref={inputRef}
            type="text" 
            placeholder="Search notes, subjects..." 
            className="flex-1 bg-transparent outline-none text-lg text-gray-800 dark:text-white placeholder-gray-400"
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === 'Escape' && onClose()}
          />
          <kbd className="hidden sm:inline-block px-2 py-1 text-xs font-semibold text-gray-500 bg-gray-100 dark:bg-gray-700 rounded border border-gray-200 dark:border-gray-600">ESC</kbd>
        </div>

        {/* Results List */}
        <div className="max-h-[60vh] overflow-y-auto">
          {results.length === 0 ? (
            <div className="p-8 text-center text-gray-500 text-sm">
              No notes found for "{query}"
            </div>
          ) : (
            <div className="p-2 space-y-1">
              {results.map((note) => (
                <button
                  key={note.path}
                  onClick={() => handleSelect(note.path)}
                  className="w-full text-left p-3 rounded-lg hover:bg-blue-50 dark:hover:bg-blue-900/20 group transition-colors flex items-center justify-between"
                >
                  <div>
                    <div className="font-semibold text-gray-800 dark:text-gray-200 group-hover:text-blue-600 dark:group-hover:text-blue-400">
                      {note.title}
                    </div>
                    <div className="text-xs text-gray-400 uppercase font-mono mt-0.5">
                      {note.breadcrumbs}
                    </div>
                  </div>
                  <svg className="w-4 h-4 text-gray-300 group-hover:text-blue-400 opacity-0 group-hover:opacity-100 transition-all -translate-x-2 group-hover:translate-x-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
                </button>
              ))}
            </div>
          )}
        </div>
        
        {/* Footer */}
        <div className="px-4 py-2 bg-gray-50 dark:bg-gray-700/50 border-t border-gray-200 dark:border-gray-700 text-xs text-gray-500 flex justify-between">
           <span>{results.length} results</span>
           <span>Use ↑↓ to navigate</span>
        </div>
      </div>
    </div>
  );
};

export default SearchModal;