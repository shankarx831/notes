import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { atomDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { motion } from 'framer-motion';
import { Note } from '@/types';
import { Button } from '@/components/ui/Button';
import 'katex/dist/katex.min.css'; // Ensure CSS is imported

interface NoteViewerProps {
    note: Note;
    onExportPdf?: () => void;
    onEdit?: () => void;
}

export const NoteViewer: React.FC<NoteViewerProps> = ({ note, onExportPdf, onEdit }) => {
    const [readingProgress, setReadingProgress] = useState(0);
    const [fontSize, setFontSize] = useState(16);

    // Scroll Progress Listener
    useEffect(() => {
        const handleScroll = () => {
            const scrollTotal = document.documentElement.scrollHeight - window.innerHeight;
            if (scrollTotal > 0) {
                setReadingProgress((window.scrollY / scrollTotal) * 100);
            }
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    return (
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-20">

            {/* --- Sticky Header --- */}
            <motion.div
                className="sticky top-0 z-50 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-200 dark:border-gray-800 px-4 py-3 flex items-center justify-between"
                initial={{ y: -100 }}
                animate={{ y: 0 }}
            >
                <div className="flex items-center gap-3 overflow-hidden">
                    <Button variant="ghost" size="sm" className="shrink-0 -ml-2">
                        ← Back
                    </Button>
                    <div className="flex flex-col">
                        <h1 className="font-bold text-sm truncate max-w-[200px] dark:text-white">
                            {note.title}
                        </h1>
                        <span className="text-[10px] text-gray-500 uppercase tracking-wider">
                            {note.subject} • {note.updatedAt.split('T')[0]}
                        </span>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    {onExportPdf && (
                        <Button variant="secondary" size="sm" onClick={onExportPdf}>
                            PDF
                        </Button>
                    )}
                    <Button variant="ghost" size="sm" onClick={() => setFontSize(prev => prev === 16 ? 18 : 16)}>
                        {fontSize === 16 ? 'A' : 'A+'}
                    </Button>
                </div>

                {/* Reading Progress Bar */}
                <div className="absolute bottom-0 left-0 h-[2px] bg-blue-600 transition-all duration-150" style={{ width: `${readingProgress}%` }} />
            </motion.div>

            {/* --- Content Area --- */}
            <motion.div
                className="max-w-3xl mx-auto px-5 py-8"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 }}
            >
                <article
                    className="prose prose-blue dark:prose-invert max-w-none break-words"
                    style={{ fontSize: `${fontSize}px` }}
                >
                    <ReactMarkdown
                        remarkPlugins={[remarkMath, remarkGfm]}
                        rehypePlugins={[rehypeKatex]}
                        components={{
                            // Custom Code Block Renderer with Copy
                            code({ node, inline, className, children, ...props }: any) {
                                const match = /language-(\w+)/.exec(className || '');
                                return !inline && match ? (
                                    <div className="relative group rounded-lg overflow-hidden my-6 shadow-xl">
                                        <div className="absolute right-2 top-2 opacity-0 group-hover:opacity-100 transition-opacity z-10">
                                            <Button
                                                size="sm"
                                                variant="secondary"
                                                className="h-7 text-xs bg-white/90 backdrop-blur"
                                                onClick={() => navigator.clipboard.writeText(String(children).replace(/\n$/, ''))}
                                            >
                                                Copy
                                            </Button>
                                        </div>
                                        <SyntaxHighlighter
                                            style={atomDark}
                                            language={match[1]}
                                            PreTag="div"
                                            customStyle={{ margin: 0, borderRadius: 0 }}
                                            {...props}
                                        >
                                            {String(children).replace(/\n$/, '')}
                                        </SyntaxHighlighter>
                                    </div>
                                ) : (
                                    <code className={className} {...props}>
                                        {children}
                                    </code>
                                );
                            },
                            // Animate images
                            img({ src, alt }) {
                                return (
                                    <motion.img
                                        src={src}
                                        alt={alt}
                                        className="rounded-xl shadow-lg mx-auto"
                                        initial={{ scale: 0.95, opacity: 0 }}
                                        whileInView={{ scale: 1, opacity: 1 }}
                                        transition={{ duration: 0.5 }}
                                        viewport={{ once: true }}
                                    />
                                );
                            }
                        }}
                    >
                        {note.content}
                    </ReactMarkdown>
                </article>
            </motion.div>

            {/* --- FAB for Teachers --- */}
            {onEdit && (
                <motion.div
                    className="fixed bottom-6 right-6 z-40"
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    whileHover={{ scale: 1.1 }}
                    whileTap={{ scale: 0.9 }}
                >
                    <button
                        onClick={onEdit}
                        className="w-14 h-14 bg-blue-600 text-white rounded-full shadow-2xl flex items-center justify-center focus:outline-none focus:ring-4 focus:ring-blue-500/30"
                        aria-label="Edit Note"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                        </svg>
                    </button>
                </motion.div>
            )}

        </div>
    );
};
