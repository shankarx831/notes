import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { TreeNode } from '@/components/folder-tree/FolderTree';
import { transformToTree } from '@/components/folder-tree/treeUtils';
import Footer from '@/components/Footer'; // Assuming Footer is compatible or I need to check
// Footer.jsx is JS, so imports work fine.

interface HomeProps {
    tree: Record<string, any>;
}

const FileIcon = () => (
    <svg className="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
    </svg>
);

const Home: React.FC<HomeProps> = ({ tree }) => {
    // Memoize the transformed tree 
    const rootNodes = useMemo(() => transformToTree(tree), [tree]);

    // Extract Flattened Notes for "Recent Uploads"
    // We can reuse the logic, or maybe the transform function could return both.
    // For now, let's keep it simple and just re-traverse or reuse the old flat logic.
    // Since we already have recursive nodes, we can flatten them easily?
    // Recursion to flatten:
    const getAllFiles = (nodes: any[]): any[] => {
        let files: any[] = [];
        nodes.forEach(node => {
            if (node.type === 'file' && node.fileData) {
                files.push(node.fileData);
            }
            if (node.children) {
                files = [...files, ...getAllFiles(node.children)];
            }
        });
        return files;
    };

    const recentNotes = useMemo(() => {
        const all = getAllFiles(rootNodes);
        return all.sort((a, b) => {
            const dateA = new Date(a.updatedAt || 0).getTime();
            const dateB = new Date(b.updatedAt || 0).getTime();
            return dateB - dateA;
        }).slice(0, 6);
    }, [rootNodes]);


    return (
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900 pb-20">

            {/* --- Hero Section --- */}
            <div className="bg-gradient-to-br from-blue-600 to-indigo-800 text-white px-6 pt-12 pb-24 rounded-b-[40px] shadow-2xl mb-[-40px] relative overflow-hidden">
                <div className="relative z-10 max-w-4xl mx-auto text-center">
                    <h1 className="text-3xl md:text-5xl font-black mb-2 tracking-tight">ExamNotes</h1>
                    <p className="opacity-80 text-sm md:text-base font-medium">Your pocket library for last-minute revision.</p>
                </div>

                {/* Decorative Circles */}
                <div className="absolute top-[-50px] left-[-50px] w-48 h-48 bg-white/10 rounded-full blur-3xl"></div>
                <div className="absolute bottom-[-20px] right-[-20px] w-64 h-64 bg-blue-400/20 rounded-full blur-3xl"></div>
            </div>

            <div className="max-w-4xl mx-auto px-4 relative z-20">

                {/* --- Recent Uploads Cards --- */}
                {recentNotes.length > 0 && (
                    <div className="mb-8">
                        <div className="flex items-center gap-3 mb-4 px-2">
                            <div className="h-1 w-6 bg-blue-500 rounded-full"></div>
                            <h3 className="text-xs font-bold uppercase tracking-widest text-gray-500 dark:text-gray-400">Recent Updates</h3>
                        </div>

                        <div className="grid grid-cols-2 lg:grid-cols-3 gap-3">
                            {recentNotes.map((note) => (
                                <Link
                                    key={note.id}
                                    to={`/${note.department}/${note.year}/${note.section || 'section-default'}/${note.subject}/${note.id}`}
                                    className="bg-white dark:bg-gray-800 p-4 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-lg hover:-translate-y-1 transition-all flex flex-col justify-between h-32 active:scale-95 touch-manipulation"
                                >
                                    <div className="flex justify-between items-start">
                                        <div className="p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg text-blue-600">
                                            <FileIcon />
                                        </div>
                                        <span className="text-[10px] uppercase font-black text-gray-400 bg-gray-100 dark:bg-gray-700 px-1.5 py-0.5 rounded">
                                            {note.department}
                                        </span>
                                    </div>
                                    <div>
                                        <h4 className="font-bold text-xs md:text-sm text-gray-800 dark:text-white line-clamp-2 leading-snug">
                                            {note.title}
                                        </h4>
                                        <p className="text-[10px] text-gray-400 mt-1 truncate">{note.subject}</p>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    </div>
                )}

                {/* --- Main Folder Tree --- */}
                <div className="bg-white dark:bg-gray-800 rounded-3xl shadow-xl shadow-gray-200/50 dark:shadow-none border border-gray-100 dark:border-gray-700 p-6 md:p-8">
                    <h2 className="text-lg font-bold text-gray-800 dark:text-white mb-6 flex items-center gap-2">
                        <svg className="w-5 h-5 text-yellow-500" fill="currentColor" viewBox="0 0 24 24"><path d="M2 6a2 2 0 012-2h5l2 2h9a2 2 0 012 2v1H2V6z M2 8.5V18a2 2 0 002 2h16a2 2 0 002-2V8.5H2z" /></svg>
                        Browse Library
                    </h2>

                    <div className="-ml-2">
                        {rootNodes.map(node => (
                            <TreeNode key={node.id} node={node} level={0} />
                        ))}
                    </div>
                </div>

            </div>

            <div className="mt-12">
                <Footer />
            </div>
        </div>
    );
};

export default Home;
