import React, { useRef } from 'react';
import { createHashRouter, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Footer from './components/Footer';
import MarkdownViewer from './components/MarkdownViewer';

/* =======================
   Breadcrumbs (UNCHANGED)
   ======================= */
const Breadcrumbs = ({ dept, year, section, subj }) => (
  <nav className="flex items-center text-xs text-gray-500 dark:text-gray-400 mb-6 font-medium space-x-2 pdf-hide select-none">
    <span className="uppercase tracking-wider">{dept}</span>
    <span className="text-gray-300 dark:text-gray-600">/</span>
    <span className="capitalize">{year.replace('year', 'Year ')}</span>
    <span className="text-gray-300 dark:text-gray-600">/</span>
    <span className="capitalize">{section.replace('-', ' ')}</span>
    <span className="text-gray-300 dark:text-gray-600">/</span>
    <span className="text-blue-600 dark:text-blue-400 font-bold uppercase bg-blue-50 dark:bg-blue-900/30 px-2 py-0.5 rounded border border-blue-100 dark:border-blue-900">
      {subj}
    </span>
  </nav>
);

/* =========================
   HYBRID PAGE WRAPPER
   (MD + JSX)
   ========================= */
const PageWrapper = ({ noteData, dept, year, section, subj }) => {
  const contentRef = useRef(null);
  const { type, meta, component: Component, content } = noteData;

  return (
    <div id="pdf-content" ref={contentRef} className="w-full">

      {/* ===== HEADER / HERO ===== */}
      <div className="bg-gradient-to-br from-blue-600 to-indigo-800 p-8 md:p-12 rounded-3xl shadow-xl mb-10 text-white relative overflow-hidden group">
        <div className="relative z-10">
          <Breadcrumbs dept={dept} year={year} section={section} subj={subj} />

          <h1 className="text-3xl md:text-5xl font-black tracking-tight mt-4">
            {meta.title}
          </h1>

          <p className="mt-4 text-blue-100 max-w-xl opacity-90 font-medium">
            SMVEC Academic Resource • Prepared with care by Shankar
          </p>
        </div>

        {/* Decorative blur */}
        <div className="absolute -right-10 -bottom-10 w-64 h-64 bg-white/10 rounded-full blur-3xl group-hover:bg-white/20 transition-all duration-700" />
      </div>

      {/* ===== CONTENT ===== */}
      <div className="min-h-[60vh]">
        {type === 'md' ? (
          <MarkdownViewer content={content} />
        ) : (
          <div className="bg-white dark:bg-gray-800 p-6 md:p-10 rounded-xl shadow-sm">
            <Component />
          </div>
        )}
      </div>

      <Footer />
    </div>
  );
};

/* =========================
   ROUTE GENERATOR
   ========================= */
export function generateRoutes(tree) {
  const routes = [];

  Object.keys(tree).forEach(dept => {
    Object.keys(tree[dept]).forEach(year => {
      Object.keys(tree[dept][year]).forEach(section => {
        Object.keys(tree[dept][year][section]).forEach(subject => {
          tree[dept][year][section][subject].forEach(note => {
            routes.push({
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

  return createHashRouter([
    {
      path: '/',
      element: <Layout tree={tree} />,
      children: [
        { index: true, element: <Home tree={tree} /> },
        ...routes,
        { path: '*', element: <Navigate to="/" /> }
      ]
    }
  ]);
}
