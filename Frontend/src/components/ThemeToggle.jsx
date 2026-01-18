import React, { useEffect, useState } from 'react';

const ThemeToggle = () => {
  const [theme, setTheme] = useState(() => {
    if (localStorage.getItem('theme')) {
      return localStorage.getItem('theme');
    }
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'light';
  });

  // Effect to apply the class to HTML
  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    localStorage.setItem('theme', theme);
  }, [theme]);

  // THE MAGIC WAVE FUNCTION
  const toggleTheme = async (e) => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';

    // 1. Fallback for browsers that don't support View Transitions API
    if (!document.startViewTransition) {
      setTheme(newTheme);
      return;
    }

    // 2. Get click coordinates
    const x = e.clientX;
    const y = e.clientY;

    // 3. Calculate distance to the furthest corner (radius needed to cover screen)
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    );

    // 4. Start the transition
    const transition = document.startViewTransition(() => {
      // This is where the actual React state update happens
      setTheme(newTheme);
    });

    // 5. Animate the circle
    await transition.ready;

    // Create a circular clip path that expands from the mouse position
    const clipPath = [
      `circle(0px at ${x}px ${y}px)`,
      `circle(${endRadius}px at ${x}px ${y}px)`,
    ];

    // Animate the root element's new view
    document.documentElement.animate(
      {
        clipPath: clipPath,
      },
      {
        duration: 400,
        easing: 'ease-in-out',
        // Start the pseudo-element animation
        pseudoElement: '::view-transition-new(root)',
      }
    );
  };

  return (
    <button
      onClick={toggleTheme}
      className="p-2 rounded-full transition-colors hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500 dark:text-gray-400 focus:outline-none z-50 relative"
      title="Toggle Dark Mode"
    >
      <div className="relative w-6 h-6 overflow-hidden">
        {/* Sun Icon */}
        <div
          className={`absolute inset-0 transform transition-transform duration-500 ${
            theme === 'dark' ? 'rotate-0 opacity-100' : '-rotate-90 opacity-0'
          }`}
        >
          <svg className="w-6 h-6 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
        </div>

        {/* Moon Icon */}
        <div
          className={`absolute inset-0 transform transition-transform duration-500 ${
            theme === 'dark' ? 'rotate-90 opacity-0' : 'rotate-0 opacity-100'
          }`}
        >
          <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
          </svg>
        </div>
      </div>
    </button>
  );
};

export default ThemeToggle;