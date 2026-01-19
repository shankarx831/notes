import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface EthicalDelayModalProps {
    isOpen: boolean;
    progress: number;
    title: string;
    description: string;
    onSkip: () => void;
    actionComponent?: React.ReactNode;
}

export const EthicalDelayModal: React.FC<EthicalDelayModalProps> = ({
    isOpen,
    progress,
    title,
    description,
    onSkip,
    actionComponent
}) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    {/* Backdrop */}
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end md:items-center justify-center p-4"
                    >
                        {/* Modal Content */}
                        <motion.div
                            initial={{ y: "100%", opacity: 0 }}
                            animate={{ y: 0, opacity: 1 }}
                            exit={{ y: "100%", opacity: 0 }}
                            transition={{ type: "spring", damping: 25, stiffness: 300 }}
                            className="bg-white dark:bg-gray-900 w-full max-w-sm rounded-3xl p-6 shadow-2xl relative overflow-hidden"
                        >
                            {/* Progress Bar Top */}
                            <div className="absolute top-0 left-0 h-1 bg-gray-100 dark:bg-gray-800 w-full">
                                <motion.div
                                    className="h-full bg-blue-500"
                                    initial={{ width: 0 }}
                                    animate={{ width: `${progress}%` }}
                                    transition={{ ease: "linear", duration: 0.1 }}
                                />
                            </div>

                            <div className="pt-4 text-center space-y-4">
                                {/* Icon or Graphic */}
                                <div className="w-16 h-16 bg-blue-50 dark:bg-blue-900/20 rounded-full flex items-center justify-center mx-auto mb-2 text-3xl">
                                    ⏳
                                </div>

                                <div>
                                    <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">{title}</h3>
                                    <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed px-2">
                                        {description}
                                    </p>
                                </div>

                                {/* Optional Action (GitHub Star) */}
                                {actionComponent && (
                                    <div className="py-2">
                                        {actionComponent}
                                    </div>
                                )}

                                {/* Skip / Status */}
                                <div className="pt-2">
                                    <button
                                        onClick={onSkip}
                                        className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 text-xs font-bold uppercase tracking-widest transition-colors py-2 px-4"
                                    >
                                        {progress < 100 ? "Skip Wait" : "Continue"}
                                    </button>
                                </div>
                            </div>
                        </motion.div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
};
