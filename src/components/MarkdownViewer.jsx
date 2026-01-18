import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import CodeBlock from './CodeBlock';
import Question from './Question';
// CRITICAL: Import KaTeX CSS from npm package - includes bundled fonts
import 'katex/dist/katex.min.css';

const MarkdownViewer = ({ content }) => {
    const sections = content.split(/^##\s+/m);
    const intro = sections.shift();

    return (
        <div className="space-y-12">
            {intro && (
                <article className="prose dark:prose-invert max-w-none">
                    <ReactMarkdown remarkPlugins={[remarkGfm, remarkMath]} rehypePlugins={[rehypeKatex]}>
                        {intro}
                    </ReactMarkdown>
                </article>
            )}

            {sections.map((section, index) => {
                const lines = section.split('\n');
                const titleLine = lines.shift();
                const body = lines.join('\n');

                const match = titleLine.match(/^(\d+)\.?\s*(.*)/);
                const num = match ? match[1] : (index + 1).toString();
                const title = match ? match[2] : titleLine;

                return (
                    <Question key={index} number={num} text={title}>
                        <article className="prose dark:prose-invert max-w-none">
                            <ReactMarkdown
                                remarkPlugins={[remarkGfm, remarkMath]}
                                // CRITICAL: Options to ensure math renders correctly
                                rehypePlugins={[[rehypeKatex, {
                                    output: 'html',
                                    throwOnError: false,
                                    strict: false
                                }]]}
                                components={{
                                    // FIX: Catch math-display and wrap it to fix size and alignment
                                    div: ({ node, className, children, ...props }) => {
                                        if (className?.includes('math-display')) {
                                            return (
                                                <div className="not-prose w-full flex justify-center my-10 overflow-x-auto py-4">
                                                    <div className="scale-125 md:scale-150 origin-center" {...props}>
                                                        {children}
                                                    </div>
                                                </div>
                                            );
                                        }
                                        return <div className={className} {...props}>{children}</div>;
                                    },
                                    // Fix Inline Math
                                    span: ({ node, className, children, ...props }) => {
                                        if (className?.includes('math-inline')) {
                                            return <span className="not-prose inline-block px-1" {...props}>{children}</span>;
                                        }
                                        return <span className={className} {...props}>{children}</span>;
                                    },
                                    code({ node, inline, className, children, ...props }) {
                                        const match = /language-(\w+)/.exec(className || '');
                                        return !inline && match ? (
                                            <CodeBlock code={String(children).replace(/\n$/, '')} language={match[1]} />
                                        ) : (
                                            <code className="bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded px-1.5 py-0.5 font-mono text-sm border border-blue-100 dark:border-blue-800" {...props}>
                                                {children}
                                            </code>
                                        );
                                    }
                                }}
                            >
                                {body}
                            </ReactMarkdown>
                        </article>
                    </Question>
                );
            })}
        </div>
    );
};

export default MarkdownViewer;