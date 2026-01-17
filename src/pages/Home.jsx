import React from 'react';
import { Link } from 'react-router-dom';
import Collapsible from '../components/Collapsible';

// --- Icon Components for cleaner code ---

const DeptIcon = () => (
  <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
  </svg>
);

const YearIcon = () => (
  <svg className="w-4 h-4 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
  </svg>
);

const FolderIcon = () => (
  <svg className="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 24 24">
    <path d="M2 6a2 2 0 012-2h5l2 2h9a2 2 0 012 2v1H2V6z" />
    <path d="M2 8.5V18a2 2 0 002 2h16a2 2 0 002-2V8.5H2z" />
  </svg>
);

const SubjectIcon = () => (
  <svg className="w-4 h-4 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
  </svg>
);

const FileIcon = () => (
  <svg className="w-4 h-4 text-gray-400 group-hover:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
  </svg>
);

const Home = ({ tree }) => {
  return (
    <div className="p-4 md:p-10 max-w-6xl mx-auto">
      <div className="text-center mb-10">
        <h1 className="text-4xl font-extrabold text-gray-800 dark:text-white mb-2 tracking-tight">
          File Explorer
        </h1>
        <p className="text-gray-500">Browse departments, subjects, and notes</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {Object.keys(tree).map((dept) => (
          <div key={dept} className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
            
            {/* Window Header (Mac-style) */}
            <div className="bg-gray-50 dark:bg-gray-700/50 p-4 border-b border-gray-100 dark:border-gray-600 flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-red-400"></div>
              <div className="w-3 h-3 rounded-full bg-yellow-400"></div>
              <div className="w-3 h-3 rounded-full bg-green-400"></div>
              <span className="ml-2 font-mono text-xs text-gray-400 uppercase tracking-wider">{dept} / root</span>
            </div>
            
            <div className="p-4">
              <h2 className="text-lg font-bold mb-4 uppercase text-gray-800 dark:text-gray-100 flex items-center gap-2">
                <DeptIcon />
                {dept} Department
              </h2>
              
              {/* LEVEL 1: YEAR */}
              {Object.keys(tree[dept]).map((year) => (
                <Collapsible 
                  key={year} 
                  label={year.replace('year', 'Year ')} 
                  icon={<YearIcon />} 
                  level={0}
                >
                  
                  {/* LEVEL 2: SECTION */}
                  {Object.keys(tree[dept][year]).map((section) => (
                    <Collapsible 
                      key={section} 
                      label={`Section ${section.replace('section-', '').toUpperCase()}`} 
                      icon={<FolderIcon />} 
                      level={1}
                    >
                       
                       {/* LEVEL 3: SUBJECT */}
                       <div className="pl-3 pt-1">
                          {Object.keys(tree[dept][year][section]).map((subject) => (
                             <Collapsible 
                                key={subject} 
                                label={subject.toUpperCase()} 
                                icon={<SubjectIcon />} 
                                level={2}
                             >
                                {/* LEVEL 4: NOTES FILES */}
                                <div className="pl-6 border-l border-gray-200 dark:border-gray-700 ml-2.5 mt-1 mb-2 space-y-0.5">
                                    {tree[dept][year][section][subject].map((note) => (
                                      <Link
                                        key={note.id}
                                        to={`/${dept}/${year}/${section}/${subject}/${note.id}`}
                                        className="flex items-center gap-3 text-sm py-2 px-3 rounded hover:bg-gray-50 dark:hover:bg-gray-700/50 text-gray-600 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors group"
                                      >
                                        <FileIcon />
                                        <span className="truncate font-medium">{note.meta.title}</span>
                                      </Link>
                                    ))}
                                </div>
                             </Collapsible>
                          ))}
                       </div>

                    </Collapsible>
                  ))}
                </Collapsible>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Home;