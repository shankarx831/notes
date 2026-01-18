import React from 'react';
import Question from '@/components/Question';

export const meta = {
  title: "Modern Layouts: Flexbox vs Grid",
  order: 1
};

export default function WebLayouts() {
  return (
    <div className="space-y-8">
      <div className="bg-gradient-to-r from-teal-500 to-green-600 text-white p-8 rounded-2xl shadow-lg">
        <h1 className="text-4xl font-extrabold mb-2">CSS Layouts</h1>
        <p className="opacity-90">Mastering Flexbox and CSS Grid for responsive design.</p>
      </div>

      <Question number="1" text="Flexbox Architecture">
        <p className="mb-4">Flexbox is a <strong>one-dimensional</strong> layout method for laying out items in rows or columns.</p>
        
        <div className="flex flex-col md:flex-row gap-4 mb-4">
          <div className="flex-1 bg-gray-100 dark:bg-gray-800 p-4 rounded text-center">
            <h5 className="font-bold mb-2">Main Axis</h5>
            <div className="flex gap-2 justify-center border-2 border-dashed border-blue-400 p-2">
              <div className="w-8 h-8 bg-blue-500 rounded text-white flex items-center justify-center">1</div>
              <div className="w-8 h-8 bg-blue-500 rounded text-white flex items-center justify-center">2</div>
              <div className="w-8 h-8 bg-blue-500 rounded text-white flex items-center justify-center">3</div>
            </div>
            <p className="text-xs mt-2 text-gray-500">justify-content: center</p>
          </div>
          
          <div className="flex-1 bg-gray-100 dark:bg-gray-800 p-4 rounded text-center">
             <h5 className="font-bold mb-2">Cross Axis</h5>
             <div className="h-20 flex items-center justify-center border-2 border-dashed border-green-400 p-2">
                <div className="w-8 h-8 bg-green-500 rounded text-white flex items-center justify-center">A</div>
             </div>
             <p className="text-xs mt-2 text-gray-500">align-items: center</p>
          </div>
        </div>
        
        <div className="bg-gray-900 text-gray-100 p-4 rounded font-mono text-sm">
{`.container {
  display: flex;
  justify-content: space-between; /* Horizontal spacing */
  align-items: center;          /* Vertical centering */
}`}
        </div>
      </Question>

      <Question number="2" text="CSS Grid Layout">
         <p>CSS Grid is a <strong>two-dimensional</strong> system, handling both rows and columns simultaneously.</p>
         <div className="grid grid-cols-2 gap-4 my-4 p-4 bg-gray-200 dark:bg-gray-700 rounded">
            <div className="bg-purple-500 text-white p-4 rounded shadow">Header (span 2)</div>
            <div className="bg-purple-400 text-white p-4 rounded shadow">Sidebar</div>
            <div className="bg-purple-400 text-white p-4 rounded shadow">Content</div>
            <div className="bg-purple-500 text-white p-4 rounded shadow col-span-2">Footer (span 2)</div>
         </div>
         <p className="text-sm italic">The above visual is created using Tailwind's Grid classes.</p>
      </Question>
    </div>
  );
}