import React from "react";
import MarkerCluster from "./MarkerCluster";
import useClusters from "@features/map/hooks/useClusters";
import mapImg from "@assets/images/map.png";
import styles from "@assets/css/style.module.css";
import useResizeObserver from "@shared/hooks/useResizeObserver";

export default function MapContainer({
  selectionData,
  showRealty,
  showPreferred,
  showAvoided,
}) {
  const [ref, mapSize] = useResizeObserver();
  const { realty, preferred, avoided } = useClusters(
    selectionData,
    mapSize,
    [showRealty, showPreferred, showAvoided]
  );

  return (
    <div className={styles.map}>
      <img ref={ref} src={mapImg} alt="map" className={styles.mapImg} />

      <div className={styles.markersContainer}>
        <MarkerCluster clusters={realty}     type="realty" />
        <MarkerCluster clusters={preferred}  type="preferred" color="green" />
        <MarkerCluster clusters={avoided}    type="avoided"  color="red"   />
      </div>
    </div>
  );
}
