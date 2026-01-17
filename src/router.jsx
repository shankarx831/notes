import React, { useRef } from 'react';
import { createHashRouter, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import DownloadPdfButton from './components/DownloadPdfButton';
import Footer from './components/Footer';

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

const PageWrapper = ({ component: Component, meta, dept, year, section, subj }) => {
  const contentRef = useRef(null);
  
  return (
    <div id="pdf-content" ref={contentRef} className="w-full">
      <div className="bg-white dark:bg-gray-800 p-6 md:p-10 rounded-xl shadow-sm min-h-[60vh] mb-8">
        <Breadcrumbs dept={dept} year={year} section={section} subj={subj} />
        <Component />
        <DownloadPdfButton contentRef={contentRef} department={dept} year={year} subject={subj} title={meta.title} />
      </div>
      <Footer />
    </div>
  );
};

export function generateRoutes(tree) {
  const routes = [];
  Object.keys(tree).forEach(dept => {
    Object.keys(tree[dept]).forEach(year => {
      Object.keys(tree[dept][year]).forEach(section => {
        Object.keys(tree[dept][year][section]).forEach(subject => {
          tree[dept][year][section][subject].forEach(note => {
            routes.push({
              path: `/${dept}/${year}/${section}/${subject}/${note.id}`,
              element: <PageWrapper component={note.component} meta={note.meta} dept={dept} year={year} section={section} subj={subject} />
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