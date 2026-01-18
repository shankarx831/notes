import { useData } from './context/DataProvider';
import { generateRoutes } from './router';
import { RouterProvider } from 'react-router-dom';

function App() {
  const { tree, loading, mode } = useData();

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        Loading...
      </div>
    );
  }

  // Generate routes based on WHOEVER provided the data (DB or Filesystem)
  const router = generateRoutes(tree);

  return (
    <>
       <RouterProvider router={router} />
       {/* Dev Indicator */}
       <div className="fixed bottom-2 right-2 text-xs bg-gray-800 text-white px-2 py-1 rounded opacity-50">
         Mode: {mode.toUpperCase()}
       </div>
    </>
  );
}

export default App;