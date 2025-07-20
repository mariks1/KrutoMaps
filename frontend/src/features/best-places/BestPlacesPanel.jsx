import React from "react";
import MapControls from "@features/map/components/MapControls";
import styles from "@assets/css/style.module.css";

export default function BestPlacesPanel({
  showRealty,
  onToggleRealty,
  showPreferred,
  onTogglePreferred,
  showAvoided,
  onToggleAvoided,
}) {
  const places = [
    "ул. Эскобара",
    "ул. Чпок Стрит",
    "пер. Трубный",
    "пр. Мэтью Саливан",
  ];

  return (
    <aside className={styles.mapDescr}>
      <h1 className={styles.heading1}>KrutoMaps</h1>
      <p className={styles.mapDescrSubtitle}>
        Аналитика крутости места для вашего бизнеса
      </p>

      <div className={styles.bestPlaces}>
        <h2 className={styles.heading2}>Крутейшие места</h2>
        {places.map((p) => (
          <button key={p} className={styles.placeBtn}>
            <span>{p}</span>
          </button>
        ))}
      </div>

        <MapControls
          showRealty={showRealty}
          onToggleRealty={onToggleRealty}
          showPreferred={showPreferred}
          onTogglePreferred={onTogglePreferred}
          showAvoided={showAvoided}
          onToggleAvoided={onToggleAvoided}
        />
    </aside>
  );
}
