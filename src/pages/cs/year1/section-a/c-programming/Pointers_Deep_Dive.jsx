import React from 'react';
import Question from '@/components/Question';
import CodeBlock from '@/components/CodeBlock'; // Import the new component

export const meta = {
  title: "Unit 3: Pointers & Memory",
  order: 1
};

export default function PointersDeepDive() {
  return (
    <div className="space-y-8">
      <div className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white p-8 rounded-2xl shadow-lg">
        <h1 className="text-4xl font-extrabold mb-2">Pointers & Memory Management</h1>
        <p className="opacity-90">A comprehensive guide to understanding direct memory access in C.</p>
      </div>

      <Question number="1" text="What is a Pointer?">
        <p className="mb-4">
          A pointer is a variable that stores the memory address of another variable.
        </p>
        
        {/* USING THE NEW COMPONENT */}
        <CodeBlock language="c" code={`
int main() {
    int var = 20;   // Actual value
    int *ptr;       // Pointer declaration
    ptr = &var;     // Store address of var

    printf("Value: %d", *ptr); // Output: 20
    return 0;
}
        `} />
      </Question>

      <Question number="2" text="Call by Reference">
        <p className="mb-4">Passing the address allows the function to modify the original variable.</p>
        
        <CodeBlock language="c" code={`
void swap(int *x, int *y) {
    int temp = *x;
    *x = *y;
    *y = temp;
}

int main() {
    int a = 10, b = 20;
    swap(&a, &b);
    // a is now 20, b is 10
}
        `} />
      </Question>
    </div>
  );
}