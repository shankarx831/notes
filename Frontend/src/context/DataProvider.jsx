import React, { createContext, useContext, useEffect, useState } from 'react';
import { loadNotesTree as loadStaticTree } from '../utils/notesLoader';
import { checkBackendHealth, fetchDynamicTree } from '../utils/api';

const DataContext = createContext();

export const DataProvider = ({ children }) => {
  const [tree, setTree] = useState({});
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState('detecting');
  const [backendAvailable, setBackendAvailable] = useState(false);

  useEffect(() => {
    const initData = async () => {
      try {
        console.log("📡 Pinging Backend...");
        const isBackendUp = await checkBackendHealth();

        if (isBackendUp) {
          console.log("✅ Backend ONLINE. Switching to Dynamic Mode.");
          const dynamicData = await fetchDynamicTree();
          const staticData = loadStaticTree();

          // Merge static and dynamic trees
          const mergedTree = mergeTrees(staticData, dynamicData);

          setTree(mergedTree);
          setMode('dynamic');
          setBackendAvailable(true);
        } else {
          throw new Error("Backend unreachable");
        }
      } catch (err) {
        console.warn("⚠️ Backend OFFLINE. Fallback to Static Filesystem.");
        const staticData = loadStaticTree();
        setTree(staticData);
        setMode('static');
        setBackendAvailable(false);
      } finally {
        const isFirstVisit = !localStorage.getItem('app_has_visited');
        if (isFirstVisit) {
          // Intentional delay for first-time onboarding feel
          console.log("🌱 First visit detected. Initializing workspace...");
          setTimeout(() => {
            setLoading(false);
            localStorage.setItem('app_has_visited', 'true');
          }, 2500);
        } else {
          setLoading(false);
        }
      }
    };

    initData();
  }, []);

  return (
    <DataContext.Provider value={{ tree, mode, loading, backendAvailable }}>
      {children}
    </DataContext.Provider>
  );
};

export const useData = () => useContext(DataContext);

// Helper to merge static and dynamic trees
const mergeTrees = (staticTree, dynamicTree) => {
  const merged = { ...staticTree };

  Object.keys(dynamicTree).forEach(dept => {
    if (!merged[dept]) {
      merged[dept] = dynamicTree[dept];
      return;
    }
    // Shallow copy next level
    merged[dept] = { ...merged[dept] };

    Object.keys(dynamicTree[dept]).forEach(year => {
      if (!merged[dept][year]) {
        merged[dept][year] = dynamicTree[dept][year];
        return;
      }
      merged[dept][year] = { ...merged[dept][year] };

      Object.keys(dynamicTree[dept][year]).forEach(sec => {
        if (!merged[dept][year][sec]) {
          merged[dept][year][sec] = dynamicTree[dept][year][sec];
          return;
        }
        merged[dept][year][sec] = { ...merged[dept][year][sec] };

        Object.keys(dynamicTree[dept][year][sec]).forEach(subj => {
          if (!merged[dept][year][sec][subj]) {
            merged[dept][year][sec][subj] = dynamicTree[dept][year][sec][subj];
            return;
          }

          // Merge arrays
          merged[dept][year][sec][subj] = [
            ...merged[dept][year][sec][subj],
            ...dynamicTree[dept][year][sec][subj]
          ];

          // Sort by order
          merged[dept][year][sec][subj].sort(
            (a, b) => (a.meta?.order ?? 999) - (b.meta?.order ?? 999)
          );
        });
      });
    });
  });

  return merged;
};