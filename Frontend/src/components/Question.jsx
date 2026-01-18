import React from 'react';

const Question = ({ number, text, children, id }) => {
  return (
    <div id={id} className="mb-8 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden break-inside-avoid scroll-mt-20">
      <div className="bg-gray-50 dark:bg-gray-700/50 p-4 border-b border-gray-100 dark:border-gray-700 flex gap-3">
        <span className="flex-shrink-0 flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200 font-bold text-sm">
          {number}
        </span>
        <h3 className="text-lg font-semibold text-gray-800 dark:text-gray-100 pt-0.5">
          {text}
        </h3>
      </div>
      <div className="p-5 text-gray-600 dark:text-gray-300 leading-relaxed space-y-4">
        {children}
      </div>
    </div>
  );
};

export default Question;