import React from 'react';
import Question from '@/components/Question';

export const meta = {
  title: "Graph Traversal: BFS & DFS",
  order: 5
};

export default function GraphAlgorithms() {
  return (
    <div className="space-y-8">
      <div className="bg-gradient-to-r from-orange-500 to-red-600 text-white p-8 rounded-2xl shadow-lg">
        <h1 className="text-4xl font-extrabold mb-2">Graph Algorithms</h1>
        <p className="opacity-90">Breadth-First Search vs Depth-First Search.</p>
      </div>

      <Question number="1" text="Breadth-First Search (BFS)">
        <div className="flex gap-4 items-start">
           <div className="flex-1">
              <p className="mb-2">BFS explores neighbors layer by layer. It uses a <strong>Queue</strong> data structure (FIFO).</p>
              <ul className="list-disc pl-5 text-sm space-y-1 mb-4">
                 <li>Good for finding shortest path in unweighted graphs.</li>
                 <li>Moves like a wave spreading out.</li>
              </ul>
              <div className="bg-gray-900 text-green-400 p-4 rounded font-mono text-xs">
{`Queue q;
q.push(startNode);
visited[startNode] = true;

while(!q.empty()) {
  int v = q.front(); q.pop();
  print(v);
  
  for(int u : adj[v]) {
    if(!visited[u]) {
       visited[u] = true;
       q.push(u);
    }
  }
}`}
              </div>
           </div>
        </div>
      </Question>

      <Question number="2" text="Depth-First Search (DFS)">
         <p className="mb-2">DFS goes as deep as possible before backtracking. It uses a <strong>Stack</strong> or <strong>Recursion</strong>.</p>
         <div className="p-4 bg-yellow-50 dark:bg-yellow-900/30 border-l-4 border-yellow-500 text-sm">
            <strong>Key Difference:</strong> If you are looking for a target node that is likely far away from the source, DFS might reach it faster than BFS. However, DFS gets stuck in infinite loops if cycles are not handled.
         </div>
      </Question>
    </div>
  );
}