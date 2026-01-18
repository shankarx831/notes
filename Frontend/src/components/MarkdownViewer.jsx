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
        <div className="space-y-16">
            {intro && (
                <article className="prose-scientific dark:prose-scientific prose-invert max-w-none">
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
                const sectionId = title.toLowerCase().replace(/[^\w\s-]/g, '').replace(/\s+/g, '-');

                return (
                    <Question key={index} number={num} text={title} id={sectionId}>
                        <article className="prose-scientific dark:prose-scientific prose-invert max-w-none">
                            <ReactMarkdown
                                remarkPlugins={[remarkGfm, remarkMath]}
                                rehypePlugins={[[rehypeKatex, {
                                    output: 'html',
                                    throwOnError: false,
                                    strict: false
                                }]]}
                                components={{
                                    div: ({ node, className, children, ...props }) => {
                                        if (className?.includes('math-display')) {
                                            return (
                                                <div className="math-wrapper not-prose w-full flex justify-center py-6">
                                                    <div className="scale-110 md:scale-125" {...props}>
                                                        {children}
                                                    </div>
                                                </div>
                                            );
                                        }
                                        return <div className={className} {...props}>{children}</div>;
                                    },
                                    span: ({ node, className, children, ...props }) => {
                                        if (className?.includes('math-inline')) {
                                            return <span className="math-inline-fix not-prose inline-block px-1" {...props}>{children}</span>;
                                        }
                                        return <span className={className} {...props}>{children}</span>;
                                    },
                                    code({ node, inline, className, children, ...props }) {
                                        const match = /language-(\w+)/.exec(className || '');
                                        return !inline && match ? (
                                            <CodeBlock code={String(children).replace(/\n$/, '')} language={match[1]} />
                                        ) : (
                                            <code className="bg-slate-100 dark:bg-slate-800 text-blue-600 dark:text-blue-400 rounded-lg px-2 py-0.5 font-mono text-sm border border-slate-200 dark:border-slate-700 font-bold" {...props}>
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