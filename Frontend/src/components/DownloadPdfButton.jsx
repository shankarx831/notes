import React, { useState } from 'react';
import { exportNoteToPdf } from '../utils/pdfExporter';

const DownloadPdfButton = ({ contentRef, department, year, subject, title }) => {
  const [isLoading, setIsLoading] = useState(false);

  const handleDownload = async () => {
    if (!contentRef.current) return;
    setIsLoading(true);

    // Sanitize filename
    const safeName = `${department}_${year}_${subject}_${title}`.replace(/[^a-z0-9]/gi, '_');

    try {
      // Small timeout to allow UI to update (show loading spinner) before thread blocks
      setTimeout(async () => {
        await exportNoteToPdf({
          element: contentRef.current,
          fileName: safeName,
          title: title
        });
        setIsLoading(false);
      }, 100);
    } catch (e) {
      alert("Failed to generate PDF. See console.");
      setIsLoading(false);
    }
  };

  return (
    <button
      onClick={handleDownload}
      disabled={isLoading}
      className={`flex items-center justify-center gap-2 px-6 py-2 rounded-xl font-bold text-white transition-all shadow-lg
        ${isLoading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 active:scale-95'}
      `}
    >
      {isLoading ? (
        <>
          <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span className="text-xs">Generating...</span>
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
  );
};

export default DownloadPdfButton;