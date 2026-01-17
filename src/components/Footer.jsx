import React from 'react';

const Footer = () => {
  return (
    <footer className="mt-20 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900/30 pdf-footer">
      <div className="max-w-6xl mx-auto px-6 py-12">
        
        <div className="flex flex-col md:flex-row justify-between gap-12">
          
          {/* --- Left: Personal Note --- */}
          <div className="flex-1 space-y-6">
            <div>
              <h3 className="font-bold text-xl text-gray-900 dark:text-white flex items-center gap-2 mb-3">
                <span>👋</span> <span>Hey, I'm Shankar</span>
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-400 leading-relaxed max-w-lg">
                I built this platform to save us from the chaos of hunting for scattered PDFs before exams. 
                It took some late nights and a lot of coffee to put together, so I genuinely hope it makes your study sessions a little less stressful!
              </p>
              
              {/* Appreciation Note */}
              <p className="mt-4 text-sm font-medium text-pink-600 dark:text-pink-400 bg-pink-50 dark:bg-pink-900/20 p-3 rounded-lg border border-pink-100 dark:border-pink-800 inline-block">
                ❤️ If you found these notes useful, a small appreciation or sharing this with friends means a lot to me!
              </p>
            </div>

            {/* Disclaimer */}
            <div className="pl-4 border-l-4 border-yellow-400 dark:border-yellow-600 py-1">
              <span className="block text-xs font-bold text-gray-900 dark:text-gray-200 uppercase tracking-wide mb-1">
                Disclaimer
              </span>
              <p className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed max-w-md">
                Keep in mind that I am a student just like you, not a professor. While I try my best to be accurate, these notes might contain errors. Please use this as a quick reference and always double-check critical concepts with your official textbooks.
              </p>
            </div>
          </div>

          {/* --- Right: Links --- */}
          <div className="flex flex-col gap-4 shrink-0 md:w-72">
            <h4 className="font-semibold text-gray-900 dark:text-white text-sm uppercase tracking-wider">
              Feedback & Issues
            </h4>
            
            {/* Instagram */}
            <a 
              href="https://instagram.com/_shankar_831/" 
              target="_blank" 
              rel="noreferrer"
              className="flex items-center gap-3 px-4 py-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl hover:border-pink-500 hover:shadow-sm transition-all group"
            >
              <div className="p-2 bg-pink-50 dark:bg-pink-900/20 rounded-full group-hover:bg-pink-100 dark:group-hover:bg-pink-900/40 transition-colors">
                <svg className="w-5 h-5 text-pink-500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/></svg>
              </div>
              <div className="flex flex-col text-left leading-tight">
                <span className="text-xs text-gray-500 font-medium">Found a bug? DM me</span>
                <span className="text-sm font-bold text-gray-800 dark:text-gray-100">@_shankar_831</span>
              </div>
            </a>

            {/* GitHub Button */}
            <a 
              href="https://github.com/shankarx831/notes" 
              target="_blank" 
              rel="noreferrer"
              className="flex items-center gap-3 px-4 py-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl hover:border-gray-400 hover:shadow-sm transition-all group"
            >
              <div className="p-2 bg-gray-100 dark:bg-gray-700 rounded-full">
                <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
              </div>
              <div className="flex flex-col text-left leading-tight">
                <span className="text-xs text-gray-500 font-medium">Open Source</span>
                <span className="text-sm font-bold text-gray-800 dark:text-gray-100">View on GitHub</span>
                
                {/* --- PDF ONLY URL (Visible only in PDF) --- */}
                <span className="pdf-only text-[10px] text-gray-500 mt-1 font-mono">
                  https://github.com/shankarx831/notes
                </span>
              </div>
            </a>
          </div>
        </div>

      </div>
    </footer>
  );
};

export default Footer;