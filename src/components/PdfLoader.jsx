import React, { useEffect, useState } from 'react';

const PdfLoader = ({ status, onClose }) => {
  const [progress, setProgress] = useState(0);
  const [timeLeft, setTimeLeft] = useState(4);

  // Reset or Run Animation based on status
  useEffect(() => {
    if (status === 'generating') {
      setProgress(0);
      setTimeLeft(4);
      
      const timer = setInterval(() => {
        setProgress((old) => (old >= 100 ? 100 : old + 1));
      }, 40); // 40ms * 100 = 4000ms

      const countdown = setInterval(() => {
        setTimeLeft((t) => (t > 0 ? t - 1 : 0));
      }, 1000);

      return () => {
        clearInterval(timer);
        clearInterval(countdown);
      };
    }
  }, [status]);

  if (status === 'idle') return null;

  return (
    <div className="fixed inset-0 z-[100] bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 transition-all duration-300">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-sm w-full p-6 text-center border border-gray-200 dark:border-gray-700 transform scale-100 transition-all duration-500">
        
        {/* --- DYNAMIC HEADER ICON --- */}
        <div className="h-20 flex items-center justify-center mb-2">
          {status === 'generating' ? (
            // LOADING CIRCLE
            <div className="w-16 h-16 relative">
              <svg className="w-full h-full -rotate-90">
                <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="none" className="text-gray-100 dark:text-gray-700" />
                <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="none" className="text-blue-600 transition-all duration-100 ease-linear" strokeDasharray="175" strokeDashoffset={175 - (175 * progress) / 100} strokeLinecap="round" />
              </svg>
              <div className="absolute inset-0 flex items-center justify-center text-xs font-bold text-blue-600">
                {timeLeft}s
              </div>
            </div>
          ) : (
            // SUCCESS CHECKMARK (Animated Pop)
            <div className="w-16 h-16 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center animate-bounce-slight">
              <svg className="w-8 h-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" /></svg>
            </div>
          )}
        </div>

        {/* --- DYNAMIC TEXT --- */}
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-1 transition-all">
          {status === 'generating' ? 'Preparing PDF...' : 'Download Started!'}
        </h3>
        
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6 transition-all">
          {status === 'generating' 
            ? 'Please wait while I format your document.' 
            : 'Check your downloads folder.'}
        </p>

        {/* --- GITHUB PROMO (ALWAYS VISIBLE) --- */}
        <div className="bg-gray-900 text-white p-4 rounded-xl shadow-lg mb-6 transform transition-transform hover:scale-105">
          <p className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-2">Support the Project</p>
          <a 
            href="https://github.com/shankarx831/notes" 
            target="_blank" 
            rel="noreferrer"
            className="flex items-center justify-center gap-2 w-full py-2 bg-white/10 hover:bg-white/20 rounded-lg transition-colors border border-white/10"
          >
            <svg className="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
            <span className="font-bold text-sm">Star on GitHub</span>
          </a>
        </div>

        {/* --- CLOSE BUTTON (Only visible on Success) --- */}
        {status === 'success' && (
          <button 
            onClick={onClose}
            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition-colors shadow-md animate-fade-in"
          >
            Close
          </button>
        )}
        
        {status === 'generating' && (
           <div className="h-12 flex items-center justify-center">
              <span className="text-xs text-gray-400">Please do not close this window</span>
           </div>
        )}

      </div>
    </div>
  );
};

export default PdfLoader;