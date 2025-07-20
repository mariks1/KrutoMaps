import clsx from "clsx";
import styles from "@assets/css/style.module.css";

export default function MarkerCluster({ clusters, color="", type="realty" }) {
  return clusters.map((cluster, idx) => {
    if (!cluster.length) return null;

    const avgX = cluster.reduce((s, i) => s + i.px, 0) / cluster.length;
    const avgY = cluster.reduce((s, i) => s + i.py, 0) / cluster.length;

    const base =
      type === "realty"
        ? styles.marker
        : clsx(styles.placeMarker, {
            [styles.placeMarkerGreen]: color === "green",
            [styles.placeMarkerRed]:   color === "red",
          });

    const extra = type === "realty" && cluster.length > 1 && styles.groupMarker;

    return (
      <div
        key={idx}
        className={clsx(base, extra)}
        style={{ left: avgX, top: avgY }}
      >
        {type === "realty" && cluster.length > 1 && (
          <div className={styles.markerCount}>{cluster.length}</div>
        )}

        <div className={styles.tooltip}>
          {cluster.map((item, i) => (
            <div key={i} className={styles.tooltipItem}>
              {type === "realty" ? (
                <>
                  <strong>{item.entityName}</strong><br />
                  Адрес: {item.address}<br />
                  Площадь: {item.totalArea} м²<br />
                  Цена: {item.leasePrice} ₽
                </>
              ) : (
                <>
                  <strong>{item.name}</strong><br/>
                  Адрес: {item.address}<br/>
                  Рубрики: {item.rubrics?.join(", ")}
                </>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  });
}
