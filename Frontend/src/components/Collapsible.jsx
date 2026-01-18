import React, { useState } from 'react';

const Collapsible = ({ label, icon, children, defaultOpen = false, level = 0 }) => {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <div className="select-none">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`
          w-full flex items-center justify-between p-2 rounded-md transition-colors
          hover:bg-gray-100 dark:hover:bg-gray-700 text-left
          ${level === 0 ? 'font-bold text-gray-800 dark:text-gray-100' : ''}
          ${level === 1 ? 'font-semibold text-gray-700 dark:text-gray-200 text-sm' : ''}
          ${level >= 2 ? 'text-sm text-gray-600 dark:text-gray-300' : ''}
        `}
        style={{ paddingLeft: `${level * 12 + 8}px` }}
      >
        <div className="flex items-center gap-2 truncate">
          {icon && <span className="opacity-70">{icon}</span>}
          <span>{label}</span>
        </div>
        <svg 
          className={`w-4 h-4 transform transition-transform duration-200 ${isOpen ? 'rotate-90' : ''}`} 
          fill="none" stroke="currentColor" viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
        </svg>
      </button>

      <div className={`overflow-hidden transition-all duration-300 ${isOpen ? 'max-h-[2000px] opacity-100' : 'max-h-0 opacity-0'}`}>
        {children}
      </div>
    </div>
  );
};

export default Collapsible;