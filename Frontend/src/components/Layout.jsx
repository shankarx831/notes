import React, { useState, useEffect } from 'react';
import { Outlet, useLocation, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Sidebar from './Sidebar';
import { exportNoteToPdf } from '../utils/pdfExporter';
import ThemeToggle from './ThemeToggle';
import SearchModal from './SearchModal';
import PdfLoader from './PdfLoader';
import MarkdownViewer from './MarkdownViewer';
import { useAuth } from '../context/AuthContext';
import { useData } from '../context/DataProvider';

const Layout = ({ tree }) => {
  const location = useLocation();
  const isHomePage = location.pathname === '/';

  const { user, logout } = useAuth();
  const { mode } = useData();

  const [sidebarOpen, setSidebarOpen] = useState(!isHomePage);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [pinnedContent, setPinnedContent] = useState(null);
  const [isSplitView, setIsSplitView] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
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
    // Dynamic paths are /dept/year/sec/subj/id (5 parts)
    if (parts.length !== 5) return null;
    const [dept, year, sec, subj, id] = parts;
    try {
      const note = tree[dept][year][sec][subj].find(n => n.id === id);
      return note ? { ...note, dept, year, sec, subj } : null;
    } catch {
      return null;
    }
  };

  const handlePinPage = () => {
    const current = findCurrentComponent();
    if (current) {
      setPinnedContent(current);
      setIsSplitView(true);
      setSidebarOpen(false);
    }
  };

  const handlePdfDownload = async () => {
    const element = document.getElementById('pdf-content');
    if (!element) return;

    setPdfStatus('generating');
    const filename = location.pathname.split('/').filter(Boolean).join('_') || 'exam_note';

    setTimeout(async () => {
      try {
        await exportNoteToPdf({ element, fileName: filename });
        setPdfStatus('success');
      } catch {
        alert('PDF Generation failed');
        setPdfStatus('idle');
      }
    }, 4000);
  };

  const isNotePage = location.pathname.split('/').filter(Boolean).length === 5;

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white overflow-hidden">

      <PdfLoader status={pdfStatus} onClose={() => setPdfStatus('idle')} />

      {mobileMenuOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 md:hidden" onClick={() => setMobileMenuOpen(false)} />
      )}

      <aside className={`
        fixed md:static inset-y-0 left-0 z-50 bg-white dark:bg-gray-800 border-r
        transition-all duration-300 overflow-y-auto
        ${mobileMenuOpen ? 'translate-x-0 w-72' : '-translate-x-full md:translate-x-0'}
        ${sidebarOpen && !isHomePage ? 'md:w-72' : 'md:w-0 md:opacity-0'}
      `}>
        <Sidebar tree={tree} onCloseMobile={() => setMobileMenuOpen(false)} />
      </aside>

      <div className="flex-1 flex flex-col min-w-0">

        {/* ================= HEADER (SCIENTIFIC DESIGN) ================= */}
        <header className="h-16 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border-b dark:border-slate-800 flex items-center justify-between px-6 relative z-50">

          {/* READING PROGRESS BAR */}
          <div className="absolute top-0 left-0 h-[3px] bg-gradient-to-r from-blue-600 via-indigo-500 to-purple-600 transition-all duration-300 z-[60]" style={{ width: `${window.scrollY > 0 ? (window.scrollY / (document.documentElement.scrollHeight - window.innerHeight)) * 100 : 0}%` }} />

          {/* LEFT: Logo & Sidebar Toggle */}
          <div className="flex items-center gap-4">
            {!isHomePage && (
              <button
                onClick={() => setMobileMenuOpen(true)}
                className="md:hidden p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200 transition-all"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M4 6h16M4 12h16m-7 6h7" /></svg>
              </button>
            )}
            {isHomePage && (
              <div className="flex items-center gap-2">
                <span className="text-xl font-black tracking-tighter text-slate-800 dark:text-white">StudentNotes</span>
              </div>
            )}
          </div>

          {/* RIGHT: Tools & Auth */}
          <div className="flex items-center gap-4">

            {/* SEARCH CMD+K */}
            {/* Mobile Search Button */}
            <button
              onClick={() => setIsSearchOpen(true)}
              className="sm:hidden p-2 text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
            </button>

            {/* Desktop Search Button */}
            <button
              onClick={() => setIsSearchOpen(true)}
              className="hidden sm:flex items-center gap-3 px-4 py-2 text-xs font-bold text-slate-400 bg-slate-100 dark:bg-slate-800/50 rounded-xl border border-slate-200/50 dark:border-slate-700/50 hover:bg-slate-200 dark:hover:bg-slate-800 transition-all"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
              Search <span className="opacity-40 text-[10px]">⌘K</span>
            </button>

            {/* AUTH */}
            {user ? (
              <div className="flex items-center gap-3">
                <div className="hidden lg:flex flex-col items-end">
                  <span className="text-[10px] font-black uppercase tracking-widest text-blue-600 dark:text-blue-400 leading-none">Hello</span>
                  <span className="text-sm font-bold text-slate-700 dark:text-slate-200">{user.name}</span>
                </div>

                <div className="flex items-center gap-1.5 p-1 bg-slate-100 dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700">
                  <Link
                    to="/teacher/upload"
                    className="px-4 py-2 text-[10px] font-black uppercase tracking-widest bg-white dark:bg-slate-700 text-slate-600 dark:text-slate-300 rounded-xl hover:bg-slate-50 transition-all shadow-sm"
                  >
                    Upload
                  </Link>

                  {user.role === 'ROLE_ADMIN' && (
                    <Link
                      to="/admin"
                      className="px-4 py-2 text-[10px] font-black uppercase tracking-widest bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-500/20"
                    >
                      Admin
                    </Link>
                  )}
                </div>

                <button
                  onClick={logout}
                  className="p-2 text-slate-400 hover:text-red-500 transition-colors"
                  title="Logout"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
                </button>
              </div>
            ) : mode === 'dynamic' && (
              <Link
                to="/login"
                className="px-6 py-2.5 text-xs font-black uppercase tracking-widest text-white bg-blue-600 rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-500/20"
              >
                Sign In
              </Link>
            )}

            <div className="h-8 w-px bg-slate-200 dark:bg-slate-700 hidden sm:block mx-1" />
            <ThemeToggle />

            {/* ACTION TOOLS */}
            <div className="flex items-center gap-1.5 bg-slate-100 dark:bg-slate-800 p-1 rounded-2xl border border-slate-200 dark:border-slate-700">
              {isNotePage && (
                <button
                  onClick={handlePdfDownload}
                  disabled={pdfStatus !== 'idle'}
                  className={`p-2 rounded-xl transition-all ${pdfStatus !== 'idle' ? 'animate-pulse bg-purple-100 text-purple-600' : 'hover:bg-white dark:hover:bg-slate-700 text-slate-500 hover:text-purple-600'}`}
                  title="Export to PDF"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                </button>
              )}

              {isNotePage && (
                <button
                  onClick={() => isSplitView ? setIsSplitView(false) : handlePinPage()}
                  className={`p-2 rounded-xl transition-all ${isSplitView ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/20' : 'hover:bg-white dark:hover:bg-slate-700 text-slate-500 hover:text-blue-600'}`}
                  title={isSplitView ? "Close Split View" : "Split View (Reference)"}
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7" /></svg>
                </button>
              )}
            </div>
          </div>
        </header>

        {/* ================= MAIN ================= */}
        <main className="flex-1 overflow-hidden flex bg-slate-50 dark:bg-gray-900 relative">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className={`overflow-y-auto w-full h-full flex flex-col`}
            >
              <div className="flex-1 flex flex-col">
                {isSplitView && pinnedContent ? (
                  <div className="flex h-full">
                    <div className="w-1/2 border-r dark:border-slate-800 overflow-y-auto p-6 bg-slate-50 dark:bg-slate-900/50">
                      <div className="mb-4 flex items-center justify-between">
                        <span className="text-[10px] font-black uppercase tracking-widest text-blue-600">Reference Mode</span>
                        <span className="text-xs font-bold text-slate-500 truncate max-w-[200px]">{pinnedContent.meta?.title}</span>
                      </div>
                      {pinnedContent.type === 'md' ? (
                        <MarkdownViewer content={pinnedContent.content} />
                      ) : (
                        <div className="p-4 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700">
                          <p className="text-sm text-slate-500 text-center py-8">Interactive content not supported in split view</p>
                        </div>
                      )}
                    </div>
                    <div className="w-1/2 overflow-y-auto p-6">
                      <Outlet />
                    </div>
                  </div>
                ) : (
                  <div className="p-6">
                    <Outlet />
                  </div>
                )}
              </div>
            </motion.div>
          </AnimatePresence>
        </main>

        <SearchModal tree={tree} isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} />
      </div>
    </div>
  );
};

export default Layout;