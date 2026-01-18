const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const checkBackendHealth = async () => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2000); // 2s timeout
    const response = await fetch(`${API_BASE_URL}/health`, { signal: controller.signal });
    clearTimeout(timeoutId);
    return response.ok;
  } catch (e) {
    return false;
  }
};

export const fetchDynamicTree = async () => {
  const response = await fetch(`${API_BASE_URL}/public/tree`);
  if (!response.ok) throw new Error('API Error');
  return await response.json();
};