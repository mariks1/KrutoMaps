import { useEffect, useState, useRef } from "react";

export default function useResizeObserver() {
  const ref = useRef(null);
  const [size, setSize] = useState({ width: 0, height: 0 });

  useEffect(() => {
    if (!ref.current) return;
    const obs = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect;
      setSize({ width, height });
    });
    obs.observe(ref.current);
    return () => obs.disconnect();
  }, []);

  return [ref, size];
}