import React from 'react';
import { Link, NavLink } from 'react-router-dom';
import Collapsible from './Collapsible';

// --- Professional Icons (Matching Home.jsx) ---

const DeptIcon = () => (
  <svg className="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
  </svg>
);

const YearIcon = () => (
  <svg className="w-3.5 h-3.5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
  </svg>
);

const FolderIcon = () => (
  <svg className="w-3.5 h-3.5 text-yellow-500" fill="currentColor" viewBox="0 0 24 24">
    <path d="M2 6a2 2 0 012-2h5l2 2h9a2 2 0 012 2v1H2V6z" />
    <path d="M2 8.5V18a2 2 0 002 2h16a2 2 0 002-2V8.5H2z" />
  </svg>
);

const SubjectIcon = () => (
  <svg className="w-3.5 h-3.5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
  </svg>
);

const FileIcon = () => (
  <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
  </svg>
);

const Sidebar = ({ tree, onCloseMobile }) => {
  return (
    <div className="pb-24 pt-4">
      {/* Brand Logo Link */}
      <div className="px-4 mb-6 border-b border-gray-100 dark:border-gray-700 pb-4">
        <Link 
          to="/" 
          onClick={onCloseMobile} 
          className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition-colors tracking-tight"
        >
          Student<span className="text-gray-800 dark:text-white">Notes</span>
        </Link>
      </div>

      <div className="px-2 space-y-1">
        {/* LEVEL 1: DEPARTMENT */}
        {Object.keys(tree).map((dept) => (
          <Collapsible 
            key={dept} 
            label={`${dept.toUpperCase()} Dept`} 
            icon={<DeptIcon />}
            defaultOpen={true}
            level={0}
          >
            {/* LEVEL 2: YEAR */}
            {Object.keys(tree[dept]).map((year) => (
              <Collapsible 
                key={year} 
                label={year.replace('year', 'Year ')} 
                icon={<YearIcon />}
                level={1}
              >
                {/* LEVEL 3: SECTION */}
                {Object.keys(tree[dept][year]).map((section) => (
                  <Collapsible 
                    key={section} 
                    label={`Sec ${section.replace('section-', '').toUpperCase()}`}
                    icon={<FolderIcon />}
                    level={2}
                  >
                    {/* LEVEL 4: SUBJECT */}
                    {Object.keys(tree[dept][year][section]).map((subject) => (
                      <Collapsible 
                        key={subject} 
                        label={subject.toUpperCase()}
                        icon={<SubjectIcon />}
                        level={3}
                      >
                         {/* LEVEL 5: FILES */}
                         <div className="pl-10 pr-2 py-1 space-y-1">
                            {tree[dept][year][section][subject].map((note) => (
                              <NavLink
                                key={note.id}
                                to={`/${dept}/${year}/${section}/${subject}/${note.id}`}
                                onClick={onCloseMobile}
                                className={({ isActive }) =>
                                  `flex items-center gap-2 text-xs py-2 px-3 rounded-md border-l-2 transition-all group ${
                                    isActive
                                      ? 'bg-blue-50 border-blue-500 text-blue-700 dark:bg-blue-900/30 dark:text-blue-200 font-medium'
                                      : 'border-transparent text-gray-500 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white'
                                  }`
                                }
                              >
                                <span className="shrink-0 opacity-50 group-hover:opacity-100">
                                  <FileIcon />
                                </span>
                                <span className="truncate">{note.meta.title}</span>
                              </NavLink>
                            ))}
                         </div>
                      </Collapsible>
                    ))}
                  </Collapsible>
                ))}
              </Collapsible>
            ))}
          </Collapsible>
        ))}
      </div>
    </div>
  );
};

export default Sidebar;