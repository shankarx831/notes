import React, { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import { exportNoteToPdf } from '../utils/pdfExporter';
import ThemeToggle from './ThemeToggle';
import SearchModal from './SearchModal';
import PdfLoader from './PdfLoader'; 

const Layout = ({ tree }) => {
  const location = useLocation();
  const isHomePage = location.pathname === '/';

  const [sidebarOpen, setSidebarOpen] = useState(!isHomePage);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [pinnedContent, setPinnedContent] = useState(null);
  const [isSplitView, setIsSplitView] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  
  // --- PDF STATUS STATE ---
  // 'idle' | 'generating' | 'success'
  const [pdfStatus, setPdfStatus] = useState('idle');

  useEffect(() => {
    const handleKey = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchOpen(prev => !prev);
      }
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, []);

  useEffect(() => {
    if (isHomePage) setSidebarOpen(false);
    else setSidebarOpen(true);
    setMobileMenuOpen(false);
  }, [location.pathname, isHomePage]);

  const findCurrentComponent = () => {
    const parts = location.pathname.split('/').filter(Boolean);
    if (parts.length !== 5) return null;
    const [dept, year, sec, subj, id] = parts;
    try {
      const note = tree[dept][year][sec][subj].find(n => n.id === id);
      return note ? { ...note, dept, year, sec, subj } : null;
    } catch (e) { return null; }
  };

  const handlePinPage = () => {
    const current = findCurrentComponent();
    if (current) {
      setPinnedContent(current);
      setIsSplitView(true);
      setSidebarOpen(false); 
    }
  };

  // --- NEW DOWNLOAD LOGIC ---
  const handlePdfDownload = async () => {
    const element = document.getElementById('pdf-content');
    if (!element) return;

    // 1. Start Loading
    setPdfStatus('generating');

    const filename = location.pathname.split('/').filter(Boolean).join('_') || 'exam_note';

    // 2. Wait 4 Seconds
    setTimeout(async () => {
        try {
            // 3. Generate
            await exportNoteToPdf({ element, fileName: filename });
            // 4. Show Success
            setPdfStatus('success');
        } catch (error) {
            alert("PDF Generation failed");
            setPdfStatus('idle'); // Close on error
        }
    }, 4000); 
  };

  const isNotePage = location.pathname.split('/').filter(Boolean).length === 5;

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white overflow-hidden transition-all">
      
      {/* RENDER THE LOADER WITH STATUS */}
      <PdfLoader status={pdfStatus} onClose={() => setPdfStatus('idle')} />

      {mobileMenuOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 md:hidden" onClick={() => setMobileMenuOpen(false)} />
      )}

      <aside className={`
          fixed md:static inset-y-0 left-0 z-50 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700
          transition-all duration-300 ease-in-out overflow-y-auto
          ${mobileMenuOpen ? 'translate-x-0 w-72' : '-translate-x-full md:translate-x-0'}
          ${sidebarOpen && !isHomePage ? 'md:w-72' : 'md:w-0 md:opacity-0 md:overflow-hidden'}
        `}
      >
        <Sidebar tree={tree} onCloseMobile={() => setMobileMenuOpen(false)} />
      </aside>

      <div className="flex-1 flex flex-col h-full min-w-0">
        
        <header className="h-14 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between px-4 shrink-0 z-30">
          <div className="flex items-center gap-3">
            {!isHomePage && (
              <button onClick={() => setMobileMenuOpen(true)} className="md:hidden p-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700">
                 <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
              </button>
            )}
            {!isHomePage && (
              <button onClick={() => setSidebarOpen(!sidebarOpen)} className="hidden md:flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-gray-600 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded hover:text-blue-600 transition-colors">
                {sidebarOpen ? 'Close Menu' : 'Menu'}
              </button>
            )}
            {isHomePage && <span className="text-xl font-bold text-blue-600">StudentsNotes</span>}
          </div>

          <div className="flex items-center gap-3">
            <button onClick={() => setIsSearchOpen(true)} className="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 rounded hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
              <span className="hidden sm:inline">Search</span>
              <span className="hidden sm:inline text-xs border border-gray-300 dark:border-gray-500 rounded px-1 ml-1">⌘K</span>
            </button>

            <div className="h-6 w-px bg-gray-300 dark:bg-gray-600 hidden sm:block"></div>
            <ThemeToggle />
            
            {isNotePage && !isSplitView && (
                <button 
                  onClick={handlePdfDownload} 
                  // Disable button while anything is happening
                  disabled={pdfStatus !== 'idle'} 
                  className="flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-purple-600 bg-purple-100 dark:bg-purple-900/30 dark:text-purple-300 rounded hover:bg-purple-200 dark:hover:bg-purple-900/50 transition-colors"
                >
                    PDF
                </button>
            )}

            {isSplitView ? (
               <button onClick={() => setIsSplitView(false)} className="flex items-center gap-2 px-3 py-1.5 text-sm font-bold text-red-600 bg-red-100 rounded hover:bg-red-200">Exit Split</button>
            ) : (
               isNotePage && (
                 <button onClick={handlePinPage} className="flex items-center gap-2 px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-100 rounded hover:bg-blue-200">Split</button>
               )
            )}
          </div>
        </header>

        <main className="flex-1 overflow-hidden relative flex">
          {isSplitView && pinnedContent && (
             <div className="w-1/2 border-r border-gray-300 dark:border-gray-600 h-full overflow-y-auto bg-gray-100 dark:bg-gray-800/50 p-4 md:p-8">
                <div className="mb-4 pb-2 border-b border-gray-300 dark:border-gray-600 flex justify-between items-center sticky top-0 bg-gray-100 dark:bg-gray-900 z-10">
                   <span className="text-xs font-bold uppercase text-gray-500">Pinned: {pinnedContent.meta.title}</span>
                </div>
                <pinnedContent.component />
             </div>
          )}
          <div className={`h-full overflow-y-auto p-4 md:p-8 transition-all flex flex-col ${isSplitView ? 'w-1/2' : 'w-full'}`}>
             <div className="flex-1 max-w-5xl mx-auto w-full">
               <Outlet />
             </div>
          </div>
        </main>

        <SearchModal tree={tree} isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />
      </div>
    </div>
  );
};

export default Layout;