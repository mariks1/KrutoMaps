import { useMemo } from "react";
import clusterMarkers from "@shared/utils/clusterMarkers";
import { convertCoordsToPixels } from "@shared/utils/clusterMarkers";

export default function useClusters(selectionData, mapSize, visible) {
  return useMemo(() => {
    if (!selectionData || !mapSize.width) return { realty: [], preferred: [], avoided: [] };

    const [showRealty, showPreferred, showAvoided] = visible;
    const { realtyEntityList = [], preferredPlaces = [], avoidedPlaces = [] } = selectionData;

    const realty = showRealty
      ? clusterMarkers(realtyEntityList, mapSize.width, mapSize.height)
      : [];
    const preferred = showPreferred
      ? preferredPlaces.map((p) => {
          const { x, y } = convertCoordsToPixels(
            p.latitude,
            p.longitude,
            mapSize.width,
            mapSize.height
          );
          return [{ ...p, px: x, py: y }];
        })
      : [];

    const avoided = showAvoided
      ? avoidedPlaces.map((p) => {
          const { x, y } = convertCoordsToPixels(
            p.latitude,
            p.longitude,
            mapSize.width,
            mapSize.height
          );
          return [{ ...p, px: x, py: y }];
        })
      : [];

    return { realty, preferred, avoided };
  }, [selectionData, mapSize, visible]);
}