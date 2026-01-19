import React, { useState } from 'react';
import { exportNoteToPdf } from '../utils/pdfExporter';
import { useEthicalDelay } from '../hooks/useEthicalDelay';
import { EthicalDelayModal } from './loaders/EthicalDelayModal';

interface Props {
    contentRef: React.RefObject<HTMLElement>;
    department: string;
    year: string;
    subject: string;
    title: string;
}

const DownloadPdfButton: React.FC<Props> = ({ contentRef, department, year, subject, title }) => {
    const [isExporting, setIsExporting] = useState(false);

    // Use the hook
    const { isDelayed, progress, trigger, skip } = useEthicalDelay({
        id: 'pdf_export_github_promo', // Unique ID for this specific delay type
        durationMs: 3500, // 3.5s delay
        strategy: 'once-ever' // Only show this promo ONCE ever per user
    });

    const runExport = async () => {
        if (!contentRef.current) return;
        setIsExporting(true);

        const safeName = `${department}_${year}_${subject}_${title}`.replace(/[^a-z0-9]/gi, '_');

        try {
            await exportNoteToPdf({
                element: contentRef.current,
                fileName: safeName,
                title: title
            });
        } catch (e) {
            console.error(e);
            alert("Failed to generate PDF.");
        } finally {
            setIsExporting(false);
        }
    };

    const handleClick = () => {
        // Trigger the delay, which calls runExport on completion
        trigger(runExport);
    };

    return (
        <>
            <button
                onClick={handleClick}
                disabled={isExporting || isDelayed}
                className={`flex items-center justify-center gap-2 px-6 py-2 rounded-xl font-bold text-white transition-all shadow-lg
          ${(isExporting || isDelayed) ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 active:scale-95'}
        `}
            >
                {(isExporting || isDelayed) ? (
                    <>
                        <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span className="text-xs">Processing...</span>
                    </>
                ) : (
                    <>
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <span className="text-xs underline decoration-blue-400 decoration-2 underline-offset-4">Download PDF</span>
                    </>
                )}
            </button>

            {/* The Ethical Delay Modal */}
            <EthicalDelayModal
                isOpen={isDelayed}
                progress={progress}
                title="Generating High-Quality PDF"
                description="This project is completely free and open-source. If the notes help you, please consider starring our repository!"
                onSkip={() => skip(runExport)}
                actionComponent={
                    <a
                        href="https://github.com/shankarx831/notes"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center justify-center gap-2 bg-gray-900 text-white py-3 px-6 rounded-xl font-bold hover:bg-black transition-colors"
                    >
                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" /></svg>
                        Star on GitHub
                    </a>
                }
            />
        </>
    );
};

export default DownloadPdfButton;
