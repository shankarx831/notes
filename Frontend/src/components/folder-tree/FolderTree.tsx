import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Link } from 'react-router-dom';
import { FileSystemNode } from './types';
import { clsx } from 'clsx';

// Icons
const ChevronRight = () => (
    <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
    </svg>
);

const FolderIcon = ({ expanded }: { expanded: boolean }) => (
    <svg
        className={clsx("w-5 h-5 transition-colors", expanded ? "text-blue-500" : "text-yellow-500")}
        fill="currentColor"
        viewBox="0 0 24 24"
    >
        <path d={expanded
            ? "M2 6a2 2 0 012-2h5l2 2h9a2 2 0 012 2v1H2V6z M2 8.5V18a2 2 0 002 2h16a2 2 0 002-2V8.5H2z"
            : "M2 6a2 2 0 012-2h5l2 2h9a2 2 0 012 2v1H2V6z M2 8.5V18a2 2 0 002 2h16a2 2 0 002-2V8.5H2z"}
        />
    </svg>
);

const FileIcon = () => (
    <svg className="w-5 h-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
    </svg>
);

interface NodeProps {
    node: FileSystemNode;
    level: number;
}

export const TreeNode: React.FC<NodeProps> = ({ node, level }) => {
    const [isOpen, setIsOpen] = useState(false);
    const isFolder = node.type === 'folder';
    const hasChildren = node.children && node.children.length > 0;

    const toggleOpen = () => {
        if (isFolder) setIsOpen(!isOpen);
    };

    // Generate link if it's a file
    // Note path structure: /dept/year/section/subject/id
    // Incoming node.path from transform might be messy, let's rely on node.fileData
    const getLink = () => {
        if (!node.fileData) return '#';
        // Reconstruct path from note metadata or params
        // Assuming node.fileData contains everything needed
        const n = node.fileData;
        // Note: Using `any` cast if properties missing in strict Note type, but they should be there
        return `/${n.department}/${n.year}/${n.section || 'section-default'}/${n.subject}/${n.id}`;
    };

    return (
        <div className="select-none">
            <motion.div
                onClick={toggleOpen}
                className={clsx(
                    "flex items-center gap-3 py-3 px-4 my-1 rounded-xl cursor-pointer transition-colors active:scale-[0.98] touch-manipulation",
                    level === 0 ? "bg-white dark:bg-gray-800 shadow-sm border border-gray-100 dark:border-gray-700" : "hover:bg-gray-100 dark:hover:bg-gray-800/50",
                    !isFolder && "ml-4 border-l-2 border-gray-100 dark:border-gray-800 hover:border-blue-500"
                )}
                style={{ marginLeft: level > 0 ? `${level * 12}px` : 0 }}
                whileTap={{ scale: 0.98 }}
            >
                {/* Expansion Icon */}
                <div className={clsx("transition-transform duration-200", isOpen && "rotate-90")}>
                    {isFolder && hasChildren && <ChevronRight />}
                </div>

                {/* Type Icon */}
                <div>
                    {isFolder ? <FolderIcon expanded={isOpen} /> : <FileIcon />}
                </div>

                {/* Label */}
                {isFolder ? (
                    <span className="font-semibold text-gray-700 dark:text-gray-200 capitalize flex-1 text-sm md:text-base">
                        {node.name.replace(/-/g, ' ')}
                    </span>
                ) : (
                    <Link
                        to={getLink()}
                        className="flex-1 min-w-0"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="font-medium text-gray-900 dark:text-gray-100 text-sm truncate">
                            {node.name}
                        </div>
                        <div className="text-[10px] text-gray-400 uppercase tracking-wider mt-0.5">
                            {node.fileData?.updatedAt?.split('T')[0] || 'Recently'}
                        </div>
                    </Link>
                )}
            </motion.div>

            {/* Children Recursion */}
            <AnimatePresence>
                {isOpen && hasChildren && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.2, ease: "easeInOut" }}
                        className="overflow-hidden"
                    >
                        {node.children!.map((child) => (
                            <TreeNode key={child.id} node={child} level={level + 1} />
                        ))}
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};
