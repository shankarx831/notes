import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { atomDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/Button';
import 'katex/dist/katex.min.css';

interface MarkdownRendererProps {
    content: string;
    fontSize?: number;
}

export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({ content, fontSize = 16 }) => {
    return (
        <article
            className="prose prose-blue dark:prose-invert max-w-none break-words"
            style={{ fontSize: `${fontSize}px` }}
        >
            <ReactMarkdown
                remarkPlugins={[remarkMath, remarkGfm]}
                rehypePlugins={[rehypeKatex]}
                components={{
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
                {content}
            </ReactMarkdown>
        </article>
    );
};
