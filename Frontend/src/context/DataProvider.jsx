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
          setTree(dynamicData);
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