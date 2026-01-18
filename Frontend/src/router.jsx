import React, { useRef, useState, useEffect } from 'react';
import { createHashRouter, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Home from './pages/Home';
import Login from './pages/Login';
import TeacherUpload from './pages/TeacherUpload';
import AdminDashboard from './pages/AdminDashboard';
import Footer from './components/Footer';
import MarkdownViewer from './components/MarkdownViewer';
import DownloadPdfButton from './components/DownloadPdfButton';

// --- Constants ---

const Breadcrumbs = ({ dept, year, section, subj }) => (
  <nav className="flex items-center text-[10px] font-black uppercase tracking-widest text-white/50 mb-6 space-x-2 pdf-hide select-none transition-all">
    <span className="cursor-default">{dept}</span>
    <span className="opacity-20">/</span>
    <span className="cursor-default">{year.replace('year', 'Year ')}</span>
    <span className="opacity-20">/</span>
    <span className="cursor-default">{section.replace('-', ' ')}</span>
    <span className="opacity-20">/</span>
    <span className="bg-white/10 px-2 py-0.5 rounded text-white font-black">
      {subj}
    </span>
  </nav>
);

// --- Page Wrapper with DELETE Logic ---

const PageWrapper = ({ noteData, dept, year, section, subj }) => {
  const contentRef = useRef(null);
  const navigate = useNavigate();
  const { type, meta, component: Component, content } = noteData;
  const { user, token } = useAuth(); // Get Auth Context

  // Extract headers for ToC
  const headers = typeof content === 'string' ? content.split(/^##\s+/m).slice(1).map((section, index) => {
    const titleLine = section.split('\n')[0];
    const match = titleLine.match(/^(\d+)\.?\s*(.*)/);
    const title = match ? match[2] : titleLine;
    const id = title.toLowerCase().replace(/[^\w\s-]/g, '').replace(/\s+/g, '-');
    return { title, id, num: match ? match[1] : (index + 1).toString() };
  }) : [];

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteReason, setDeleteReason] = useState('');
  const [readingProgress, setReadingProgress] = useState(0);
  const [showBackToTop, setShowBackToTop] = useState(false);
  const [isFocusMode, setIsFocusMode] = useState(false);
  const [showToC, setShowToC] = useState(true);
  const isDynamic = noteData.id && !isNaN(noteData.id);
  const isSuperAdmin = noteData.uploadedByEmail === 'sankaranarayanan5.ssv@gmail.com';
  const displayUploaderName = isSuperAdmin || !isDynamic ? 'Shankar' : (noteData.uploadedByName || 'Shankar');
  const displayUploaderEmail = isSuperAdmin || !isDynamic ? 'shankar@smvec.ac.in' : (noteData.uploadedByEmail || 'shankar@smvec.ac.in');

  useEffect(() => {
    const handleScroll = () => {
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;
      const scrollTop = window.scrollY;
      const progress = (scrollTop / (documentHeight - windowHeight)) * 100;
      setReadingProgress(progress);
      setShowBackToTop(scrollTop > 400);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleRequestDelete = async () => {
    try {
      await fetch('http://localhost:8080/api/teacher/request-delete', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          noteId: noteData.id,
          email: user.email,
          reason: deleteReason
        })
      });
      alert('Request sent to Admin');
      setIsDeleteModalOpen(false);
      setDeleteReason('');
    } catch (err) {
      alert('Failed to send delete request');
      console.error(err);
    }
  };

  return (
    <div id="pdf-content" ref={contentRef} className="w-full relative">
      {/* --- READING PROGRESS BAR --- */}
      <div className="fixed top-0 left-0 w-full h-1 z-50 pdf-hide bg-gray-200 dark:bg-gray-800">
        <div
          className="h-full bg-gradient-to-r from-white/40 to-white transition-all duration-150"
          style={{ width: `${readingProgress}%` }}
        />
      </div>

      {/* --- ACTION BUTTONS (Only for Teachers/Admin) --- */}
      <div className={`absolute top-4 right-4 flex items-center gap-3 z-[60] pdf-hide transition-opacity ${isFocusMode ? 'opacity-0 pointer-events-none' : 'opacity-100'}`}>
        <button
          onClick={() => setIsFocusMode(!isFocusMode)}
          className="h-10 bg-white/10 backdrop-blur-lg text-white border border-white/20 px-5 rounded-2xl text-[10px] font-black uppercase tracking-[0.1em] hover:bg-white/20 transition-all flex items-center gap-2 hover-lift"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
          Study Focus Mode
        </button>
        {user && noteData.id && (user.role === 'ROLE_TEACHER' || user.role === 'ROLE_ADMIN') && (
          <div className="flex items-center gap-2">
            <button
              onClick={() => navigate('/teacher/upload', { state: { editMode: true, note: noteData } })}
              className="h-10 bg-white/10 backdrop-blur-lg text-white border border-white/20 px-5 rounded-2xl text-[10px] font-black uppercase tracking-[0.1em] hover:bg-white/20 transition-all hover-lift"
            >
              Edit
            </button>
            <button
              onClick={() => setIsDeleteModalOpen(true)}
              className="h-10 bg-rose-500 text-white px-5 rounded-2xl text-[10px] font-black uppercase tracking-[0.1em] hover:bg-rose-600 transition-all shadow-xl shadow-rose-500/30 hover-lift"
            >
              Delete
            </button>
          </div>
        )}
      </div>

      {/* --- STUDY FOCUS OVERLAY --- */}
      {isFocusMode && (
        <div
          className="fixed inset-0 z-[100] bg-white dark:bg-gray-900 overflow-y-auto p-4 md:p-20 focus-mode-scroll"
          onKeyDown={e => e.key === 'Escape' && setIsFocusMode(false)}
        >
          <button
            onClick={() => setIsFocusMode(false)}
            className="fixed top-8 right-8 p-4 bg-gray-100 dark:bg-gray-800 rounded-full hover:scale-110 transition-transform cursor-pointer shadow-lg z-[110]"
            title="Exit Focus Mode (ESC)"
          >
            <svg className="w-6 h-6 text-gray-800 dark:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>

          <div className="max-w-4xl mx-auto py-10">
            <h1 className="text-4xl md:text-6xl font-black text-gray-900 dark:text-white mb-10 tracking-tight leading-tight">
              {meta.title}
            </h1>
            <div className="prose dark:prose-invert max-w-none text-lg">
              <MarkdownViewer content={content} />
            </div>
            <div className="mt-20 pt-10 border-t border-gray-100 dark:border-gray-800 text-center text-gray-400 text-sm italic">
              End of "{meta.title}" - Study hard!
            </div>
          </div>
        </div>
      )}

      {/* --- DELETE MODAL --- */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 pdf-hide">
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-xl w-96">
            <h3 className="font-bold text-lg mb-4 dark:text-white">Request Deletion</h3>
            <textarea
              className="w-full border p-2 rounded mb-4 dark:bg-gray-700 dark:text-white"
              placeholder="Reason for deletion..."
              value={deleteReason}
              onChange={e => setDeleteReason(e.target.value)}
            />
            <div className="flex justify-end gap-2">
              <button onClick={() => setIsDeleteModalOpen(false)} className="px-4 py-2 text-gray-500">Cancel</button>
              <button onClick={handleRequestDelete} className="px-4 py-2 bg-red-600 text-white rounded">Send Request</button>
            </div>
          </div>
        </div>
      )}

      {/* --- PDF ONLY SIMPLE HEADER --- */}
      <div className="pdf-only mb-10 pb-8 border-b-2 border-gray-100">
        <h1 className="text-4xl font-black text-black mb-2">{meta.title}</h1>
        <div className="flex items-center gap-4 text-xs font-bold text-gray-500 uppercase tracking-widest">
          <span>{displayUploaderName}</span>
          <span className="w-1 h-1 rounded-full bg-gray-300"></span>
          <span>{dept}</span>
          <span className="w-1 h-1 rounded-full bg-gray-300"></span>
          <span>{subject}</span>
        </div>
      </div>

      {/* --- HEADER --- */}
      <div className={`bg-gradient-to-br from-blue-700 to-indigo-900 p-10 md:p-16 rounded-[40px] shadow-2xl mb-12 text-white relative overflow-hidden group transition-all duration-700 pdf-hide ${isFocusMode ? 'scale-95 opacity-0' : 'scale-100 opacity-100'}`}>
        <div className="relative z-10">
          <Breadcrumbs dept={dept} year={year} section={section} subj={subj} />

          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div className="space-y-4">
              <h1 className="text-4xl md:text-6xl font-black tracking-tight leading-tight max-w-3xl drop-shadow-sm">
                {meta.title}
              </h1>

              <div className="flex items-center gap-5 pt-4">
                <div className="w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-md flex items-center justify-center font-black text-xl border border-white/20">
                  {displayUploaderName[0]}
                </div>
                <div>
                  <p className="font-extrabold text-lg tracking-tight">{displayUploaderName}</p>
                </div>
              </div>
            </div>

            <div className="flex flex-col items-end gap-5">


              <div className="flex items-center gap-4 opacity-70 text-[10px] font-black uppercase tracking-[0.2em]">
                <span className="flex items-center gap-2 bg-black/30 px-3 py-1.5 rounded-lg border border-white/5">
                  {Math.max(1, Math.ceil((content?.length || 0) / 1000))} min read
                </span>
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(window.location.href);
                    alert("Link copied to clipboard!");
                  }}
                  className="flex items-center gap-2 bg-black/30 hover:bg-white/20 px-3 py-1.5 rounded-lg transition-all border border-white/5"
                >
                  Share
                </button>
                <span className={`px-3 py-1.5 rounded-lg border flex items-center gap-2 ${isDynamic ? 'bg-green-500/10 text-green-300 border-green-500/20' : 'bg-yellow-500/10 text-yellow-300 border-yellow-500/20'}`}>
                  <span className={`w-1.5 h-1.5 rounded-full ${isDynamic ? 'bg-green-400 animate-pulse' : 'bg-yellow-400 animate-pulse'}`}></span>
                  JAR: {isDynamic ? 'ONLINE' : 'OFFLINE'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Decorative blur */}
        <div className="absolute -right-10 -bottom-10 w-96 h-96 bg-white/10 rounded-full blur-[100px] group-hover:bg-white/20 transition-all duration-1000" />
      </div>

      <div className={`flex gap-10 transition-all duration-700 ${isFocusMode ? 'blur-xl opacity-0 scale-95' : 'blur-0 opacity-100 scale-100'}`}>
        {/* --- TABLE OF CONTENTS SIDEBAR --- */}
        {headers.length > 0 && (
          <aside className={`hidden lg:block shrink-0 sticky top-24 self-start pdf-hide transition-all duration-500 ${showToC ? 'w-64 opacity-100' : 'w-0 opacity-0 overflow-hidden'}`}>
            <div className="flex items-center justify-between mb-4 ml-4">
              <h3 className="text-[10px] font-black uppercase tracking-widest text-gray-400">Chapters</h3>
              <button
                onClick={() => setShowToC(false)}
                className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded text-gray-400"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" /></svg>
              </button>
            </div>
            <nav className="space-y-1">
              {headers.map((h) => (
                <a
                  key={h.id}
                  href={`#${h.id}`}
                  className="block px-4 py-2.5 text-xs font-bold text-gray-500 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900/10 rounded-xl transition-all border-l-2 border-transparent hover:border-blue-600 truncate"
                >
                  <span className="opacity-30 mr-2 font-mono">{h.num}</span> {h.title}
                </a>
              ))}
            </nav>
          </aside>
        )}

        {/* TOC SHOW BUTTON */}
        {headers.length > 0 && !showToC && (
          <button
            onClick={() => setShowToC(true)}
            className="fixed left-6 top-1/2 -translate-y-1/2 z-40 p-3 bg-white dark:bg-gray-800 shadow-2xl rounded-full border border-gray-100 dark:border-gray-700 hover:scale-110 transition-all pdf-hide group"
          >
            <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" /></svg>
            <span className="absolute left-14 bg-gray-900 text-white text-[10px] font-black uppercase px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">Show Index</span>
          </button>
        )}

        {/* --- CONTENT AREA --- */}
        <div className="flex-1 min-w-0 min-h-[60vh]">
          {type === 'md' ? (
            <MarkdownViewer content={content} />
          ) : (
            <div className="bg-white dark:bg-gray-800 p-6 md:p-10 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
              <Component />
            </div>
          )}
        </div>
      </div>

      {/* --- BACK TO TOP --- */}
      <button
        onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
        className={`fixed bottom-8 right-8 p-3 bg-blue-600 text-white rounded-full shadow-2xl transition-all duration-300 z-50 ${showBackToTop ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10 pointer-events-none'}`}
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 10l7-7m0 0l7 7m-7-7v18" /></svg>
      </button>

      <Footer />
    </div>
  );
};

// --- ROUTE GENERATOR FUNCTION (This is what App.jsx needs!) ---
export function generateRoutes(tree) {
  const dynamicRoutes = [];

  // Traverse the tree to generate routes for every note
  Object.keys(tree).forEach(dept => {
    Object.keys(tree[dept]).forEach(year => {
      Object.keys(tree[dept][year]).forEach(section => {
        Object.keys(tree[dept][year][section]).forEach(subject => {
          tree[dept][year][section][subject].forEach(note => {
            dynamicRoutes.push({
              path: `/${dept}/${year}/${section}/${subject}/${note.id}`,
              element: (
                <PageWrapper
                  noteData={note}
                  dept={dept}
                  year={year}
                  section={section}
                  subj={subject}
                />
              )
            });
          });
        });
      });
    });
  });

  // Return the Router Configuration
  return createHashRouter([
    {
      path: '/',
      element: <Layout tree={tree} />,
      children: [
        { index: true, element: <Home tree={tree} /> },

        // Manual Routes
        { path: 'login', element: <Login /> },
        { path: 'teacher/upload', element: <TeacherUpload /> },
        { path: 'admin', element: <AdminDashboard /> },

        // Dynamic Routes (from DB or Filesystem)
        ...dynamicRoutes,

        // Catch all
        { path: '*', element: <Navigate to="/" /> }
      ]
    }
  ]);
}