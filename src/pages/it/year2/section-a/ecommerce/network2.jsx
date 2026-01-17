import React from 'react';
// Now you can use @ to represent the 'src' folder
import Question from '@/components/Question'; 

export const meta = {
  title: "Network Basics",
  order: 1
};

export default function NetworkBasics() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">IT / Year 2 / Section A / Networks</h1>
      <Question number="1" text="What is a LAN?">
        Local Area Network.
      </Question>
    </div>
  );
}