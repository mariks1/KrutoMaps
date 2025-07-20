import { useState } from "react";
import { Routes, Route } from "react-router-dom";
import FiltersForm     from "@features/filters/components/FiltersForm";
import MapContainer    from "@features/map/components/MapContainer";
import BestPlacesPanel from "@features/best-places/BestPlacesPanel";
import clsx            from "clsx";
import styles          from "@assets/css/style.module.css";

export default function App() {
  const [selectionData, setSelectionData] = useState(null);
  const [showRealty, setShowRealty]       = useState(true);
  const [showPreferred, setShowPreferred] = useState(false);
  const [showAvoided, setShowAvoided]     = useState(false);

  return (
    <main className={styles.main}>
      <div
        className={clsx(
          styles.container,
          "grid lg:grid-cols-[400px_1fr_300px] gap-4"
        )}
      >
        <div className={styles.mapSettings}>
          <FiltersForm onSelection={setSelectionData} />
        </div>

        <MapContainer
        selectionData={selectionData}
        showRealty={showRealty} onToggleRealty={() => setShowRealty(v => !v)}
        showPreferred={showPreferred} onTogglePreferred={() => setShowPreferred(v => !v)}
        showAvoided={showAvoided} onToggleAvoided={() => setShowAvoided(v => !v)}
        />

        <div className={styles.mapDescr}>
          <BestPlacesPanel
            showRealty={showRealty} onToggleRealty={() => setShowRealty(v => !v)}
            showPreferred={showPreferred} onTogglePreferred={() => setShowPreferred(v => !v)}
            showAvoided={showAvoided} onToggleAvoided={() => setShowAvoided(v => !v)}
          />
        </div>
      </div>

      <Routes>
        <Route path="/" element={null} />
      </Routes>
    </main>
  );
}
