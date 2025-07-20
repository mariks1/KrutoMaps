import { MAP_BOUNDS } from "@shared/constants/mapBounds";

export function convertCoordsToPixels(lat, lng, mapWidth, mapHeight) {
  const { topLeft, bottomRight } = MAP_BOUNDS;
  const west = topLeft.lng;
  const east = bottomRight.lng;
  const north = topLeft.lat;
  const south = bottomRight.lat;
  const xRatio = (lng - west) / (east - west);
  const yRatio = (north - lat) / (north - south);
  return { x: xRatio * mapWidth, y: yRatio * mapHeight };
}

export default function clusterMarkers(items, mapWidth, mapHeight, threshold = 20) {
  const pixelData = items.map((item) => {
    const { x, y } = convertCoordsToPixels(
      item.latitude ?? item.pointY,
      item.longitude ?? item.pointX,
      mapWidth,
      mapHeight
    );
    return { ...item, px: x, py: y };
  });

  const graph = Array.from({ length: pixelData.length }, () => []);
  for (let i = 0; i < pixelData.length - 1; i++) {
    for (let j = i + 1; j < pixelData.length; j++) {
      const dx = pixelData[i].px - pixelData[j].px;
      const dy = pixelData[i].py - pixelData[j].py;
      const dist = Math.hypot(dx, dy);
      if (dist < threshold) {
        graph[i].push(j);
        graph[j].push(i);
      }
    }
  }

  const visited = new Array(pixelData.length).fill(false);
  const clusters = [];
  function dfs(node, buff) {
    if (visited[node]) return;
    visited[node] = true;
    buff.push(pixelData[node]);
    graph[node].forEach((n) => dfs(n, buff));
  }

  for (let i = 0; i < pixelData.length; i++) {
    if (!visited[i]) {
      const c = [];
      dfs(i, c);
      clusters.push(c);
    }
  }
  return clusters;
}