import React from 'react';
import { motion } from 'framer-motion';

export const SplashScreen = () => {
    return (
        <div className="fixed inset-0 bg-white dark:bg-gray-900 flex flex-col items-center justify-center z-50">
            <motion.div
                initial={{ scale: 0.8, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ duration: 0.5 }}
                className="flex flex-col items-center"
            >
                {/* Logo Icon */}
                <div className="w-20 h-20 bg-gradient-to-br from-blue-600 to-indigo-800 rounded-3xl shadow-2xl flex items-center justify-center mb-6">
                    <svg className="w-10 h-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                    </svg>
                </div>

                <h1 className="text-3xl font-black text-gray-900 dark:text-white tracking-tight mb-2">StudentNotes</h1>

                {/* Animated Loading Text */}
                <div className="flex items-center gap-1">
                    <span className="text-sm font-medium text-gray-400">Preparing your workspace</span>
                    <motion.span
                        animate={{ opacity: [0, 1, 0] }}
                        transition={{ duration: 1.5, repeat: Infinity, times: [0, 0.5, 1] }}
                    >.</motion.span>
                    <motion.span
                        animate={{ opacity: [0, 1, 0] }}
                        transition={{ duration: 1.5, repeat: Infinity, delay: 0.2, times: [0, 0.5, 1] }}
                    >.</motion.span>
                    <motion.span
                        animate={{ opacity: [0, 1, 0] }}
                        transition={{ duration: 1.5, repeat: Infinity, delay: 0.4, times: [0, 0.5, 1] }}
                    >.</motion.span>
                </div>

                {/* Fake Progress Bar */}
                <div className="w-48 h-1 bg-gray-100 dark:bg-gray-800 rounded-full mt-8 overflow-hidden">
                    <motion.div
                        className="h-full bg-blue-500 rounded-full"
                        initial={{ width: "0%" }}
                        animate={{ width: "100%" }}
                        transition={{ duration: 2.5, ease: "easeInOut" }}
                    />
                </div>
            </motion.div>
        </div>
    );
};
