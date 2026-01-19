import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

/**
 * ThemeToggle Component
 * Swaps between Light, Dark, and scientifically-optimized Reading (Solarized) modes.
 * Uses Framer Motion for spring physics and View Transitions API for radial reveals.
 */
const ThemeToggle = () => {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'light';
  });

  // Apply theme class to <html> for global CSS scoping
  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove('dark', 'sepia');
    if (theme !== 'light') {
      root.classList.add(theme);
    }
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = async (e) => {
    const modes = ['light', 'dark', 'sepia'];
    const currentIndex = modes.indexOf(theme);
    const nextTheme = modes[(currentIndex + 1) % modes.length];

    // Fallback for browsers that don't support document.startViewTransition
    if (!document.startViewTransition) {
      setTheme(nextTheme);
      return;
    }

    const x = e.clientX;
    const y = e.clientY;
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    );

    const transition = document.startViewTransition(() => {
      setTheme(nextTheme);
    });

    await transition.ready;

    // Premium radial expand animation from click point
    document.documentElement.animate(
      {
        clipPath: [
          `circle(0px at ${x}px ${y}px)`,
          `circle(${endRadius}px at ${x}px ${y}px)`,
        ],
      },
      {
        duration: 500,
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
        pseudoElement: '::view-transition-new(root)',
      }
    );
  };

  const getIcon = () => {
    // High-fidelity spring config for organic feel
    const springProps = {
      type: "spring",
      stiffness: 260,
      damping: 20
    };

    switch (theme) {
      case 'dark':
        return (
          <motion.svg
            key="moon"
            initial={{ scale: 0.3, rotate: -90, opacity: 0 }}
            animate={{ scale: 1, rotate: 0, opacity: 1 }}
            exit={{ scale: 0.3, rotate: 90, opacity: 0 }}
            transition={springProps}
            className="w-5 h-5 text-blue-400"
            fill="currentColor"
            viewBox="0 0 24 24"
          >
            <path d="M12 3c.132 0 .263 0 .393.007a9 9 0 0 0 9.6 9.6A9 9 0 1 1 12 3z" />
          </motion.svg>
        );
      case 'sepia':
        return (
          <motion.svg
            key="reading"
            initial={{ scale: 0.3, rotate: -90, opacity: 0 }}
            animate={{ scale: 1, rotate: 0, opacity: 1 }}
            exit={{ scale: 0.3, rotate: 90, opacity: 0 }}
            transition={springProps}
            className="w-5 h-5 text-[#b58900]"
            fill="none"
            stroke="currentColor"
            strokeWidth={2.5}
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </motion.svg>
        );
      default:
        return (
          <motion.svg
            key="sun"
            initial={{ scale: 0.3, rotate: -90, opacity: 0 }}
            animate={{ scale: 1, rotate: 0, opacity: 1 }}
            exit={{ scale: 0.3, rotate: 90, opacity: 0 }}
            transition={springProps}
            className="w-5 h-5 text-amber-500"
            fill="currentColor"
            viewBox="0 0 24 24"
          >
            <path d="M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM2 13h2a1 1 0 1 0 0-2H2a1 1 0 1 0 0 2zm18 0h2a1 1 0 1 0 0-2h-2a1 1 0 1 0 0 2zM11 2v2a1 1 0 1 0 2 0V2a1 1 0 1 0-2 0zm0 18v2a1 1 0 1 0 2 0v-2a1 1 0 1 0-2 0zM5.99 4.58a1 1 0 1 0-1.41 1.41l1.41 1.41a1 1 0 1 0 1.41-1.41L5.99 4.58zm12.02 12.02a1 1 0 1 0-1.41 1.41l1.41 1.41a1 1 0 1 0 1.41-1.41l-1.41-1.41zm-12.02 0l-1.41 1.41a1 1 0 1 0 1.41 1.41l1.41-1.41a1 1 0 1 0-1.41-1.41zM18.01 4.58l1.41-1.41a1 1 0 1 0-1.41-1.41l-1.41 1.41a1 1 0 1 0 1.41 1.41z" />
          </motion.svg>
        );
    }
  };

  return (
    <button
      onClick={toggleTheme}
      className={`
        relative p-2.5 rounded-2xl transition-all duration-300 group
        ${theme === 'dark' ? 'bg-slate-800 border-slate-700 hover:bg-slate-700' :
          theme === 'sepia' ? 'bg-[#eee8d5] border-[#93a1a1] hover:bg-[#e2d5b5]' :
            'bg-slate-100 border-slate-200 hover:bg-slate-200'}
        border shadow-sm active:scale-95 z-50
      `}
      title={`Current: ${theme} mode`}
    >
      <div className="w-5 h-5 flex items-center justify-center overflow-hidden">
        <AnimatePresence mode="wait" initial={false}>
          {getIcon()}
        </AnimatePresence>
      </div>

      <span className="absolute -bottom-10 left-1/2 -translate-x-1/2 px-2 py-1 rounded-lg bg-slate-900 text-white text-[10px] font-bold opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none uppercase tracking-widest whitespace-nowrap">
        {theme === 'sepia' ? 'Reading' : theme} mode
      </span>
    </button>
  );
};

export default ThemeToggle;