import { useState, useCallback, useEffect } from 'react';

type DelayStrategy = 'always' | 'once-ever' | 'once-session';

interface UseEthicalDelayOptions {
    id: string; // Unique ID for localStorage
    durationMs?: number; // Default 2000ms
    strategy?: DelayStrategy;
}

export const useEthicalDelay = ({
    id,
    durationMs = 2500,
    strategy = 'once-ever',
}: UseEthicalDelayOptions) => {
    const [isDelayed, setIsDelayed] = useState(false);
    const [progress, setProgress] = useState(0);

    // Check if we should bypass delay
    const shouldBypass = useCallback(() => {
        if (!navigator.onLine) return true; // Offline users bypass
        if (strategy === 'always') return false;

        const storageKey = `ethical_delay_${id}_completed`;
        const stored = localStorage.getItem(storageKey);

        if (strategy === 'once-ever' && stored) return true;
        if (strategy === 'once-session' && sessionStorage.getItem(storageKey)) return true;

        return false;
    }, [id, strategy]);

    const recordCompletion = useCallback(() => {
        const storageKey = `ethical_delay_${id}_completed`;
        if (strategy === 'once-ever') localStorage.setItem(storageKey, 'true');
        if (strategy === 'once-session') sessionStorage.setItem(storageKey, 'true');
    }, [id, strategy]);

    const trigger = useCallback((onComplete: () => void) => {
        if (shouldBypass()) {
            onComplete();
            return;
        }

        setIsDelayed(true);
        setProgress(0);

        const startTime = Date.now();
        const interval = setInterval(() => {
            const elapsed = Date.now() - startTime;
            const pct = Math.min((elapsed / durationMs) * 100, 100);
            setProgress(pct);

            if (elapsed >= durationMs) {
                clearInterval(interval);
                recordCompletion();
                // We ensure 100% is shown briefly before completing?
                // Actually, normally we wait for user to click "Continue" or auto-complete?
                // Prompt says "User must always be able to cancel" and "Skip / Continue button (always present)".
                // It implies the modal stays open?
                // If it's a "Loader", it usually auto-completes.
                // If it's a "GitHub CTA", it might wait.
                // Let's assume auto-complete for the DELAY logic, but the UI can intercept.
                // For this hook, it just drives the progress.
                // We will expose 'finish' method to be called by UI?
                // Or auto-finish?

                // Let's make it auto-finish the DELAY state.
                // The VISUAL modal will check `isDelayed` state.
                setIsDelayed(false);
                onComplete();
            }
        }, 50);

        return () => clearInterval(interval);
    }, [durationMs, shouldBypass, recordCompletion]);

    const skip = useCallback((onComplete: () => void) => {
        setIsDelayed(false);
        recordCompletion();
        onComplete();
    }, [recordCompletion]);

    return { isDelayed, progress, trigger, skip };
};
