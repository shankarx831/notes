export interface PdfExportOptions {
    element: HTMLElement;
    fileName: string;
    title: string;
}

export function exportNoteToPdf(options: PdfExportOptions): Promise<void>;
