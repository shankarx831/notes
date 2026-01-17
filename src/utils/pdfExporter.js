import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

export async function exportNoteToPdf({ element, fileName }) {
  if (!element) return;

  // 1. Force Light Mode (Crucial for Ink Saving)
  const wasDark = document.documentElement.classList.contains('dark');
  document.documentElement.classList.remove('dark');

  // 2. Save original inline styles
  const originalWidth = element.style.width;
  const originalPadding = element.style.padding;
  const originalMargin = element.style.margin;

  // 3. Set Fixed "Paper" Width for Consistency
  // 800px ensures text size matches A4 proportions perfectly
  element.style.width = '800px'; 
  element.style.padding = '40px'; 
  element.style.margin = '0'; 
  
  // Apply PDF styling class (white background, black text)
  element.classList.add('pdf-mode');
  
  // Wait for React/Browser to repaint colors (prevents black background glitch)
  await new Promise(resolve => setTimeout(resolve, 100));

  try {
    // 4. Capture Canvas
    // Note: The 'document.write' warning in console is normal for html2canvas
    const canvas = await html2canvas(element, {
      scale: 2, // High resolution (Retina)
      useCORS: true,
      windowWidth: 800,
      backgroundColor: '#ffffff',
      logging: false, // Reduce console noise
    });

    // 5. Initialize PDF
    const pdf = new jsPDF('p', 'mm', 'a4');
    const pdfWidth = 210; 
    const pdfHeight = 297; 
    const imgWidth = pdfWidth; 
    
    // Calculate aspect ratio
    const pageHeightInImgProps = (canvas.height * imgWidth) / canvas.width;
    const contentHeight = canvas.height;
    const pageHeight = (pdfHeight * canvas.width) / pdfWidth; 

    let heightLeft = contentHeight;
    let pageNum = 1;

    // Watermark Helper
    const addWatermark = () => {
      pdf.setFontSize(9);
      pdf.setTextColor(150, 150, 150); // Light Grey
      pdf.text("shankar.com", 200, 290, { align: 'right' });
    };

    // Use JPEG 0.75 for good balance of size vs quality
    const imgData = canvas.toDataURL('image/jpeg', 0.75);

    // --- Page 1 ---
    pdf.addImage(imgData, 'JPEG', 0, 0, imgWidth, pageHeightInImgProps);
    addWatermark();
    heightLeft -= pageHeight;

    // --- Subsequent Pages (Continuous Slicing) ---
    while (heightLeft > 0) {
      pageNum++;
      pdf.addPage();
      
      // We shift the image upwards (negative Y) to show the next chunk
      const yOffset = -((pageNum - 1) * pdfHeight);
      
      pdf.addImage(imgData, 'JPEG', 0, yOffset, imgWidth, pageHeightInImgProps);
      addWatermark();
      
      heightLeft -= pageHeight;
    }

    pdf.save(`${fileName}.pdf`);

  } catch (err) {
    console.error("PDF Generation failed", err);
    alert("Could not generate PDF. Please try again.");
  } finally {
    // 6. Restore Original UI
    element.style.width = originalWidth;
    element.style.padding = originalPadding;
    element.style.margin = originalMargin;
    element.classList.remove('pdf-mode');
    
    // Restore Dark Mode if needed
    if (wasDark) {
      document.documentElement.classList.add('dark');
    }
  }
}