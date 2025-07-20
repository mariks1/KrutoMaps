import { http } from "./httpClient";

export const postSelection = (payload) =>
  http("api/selection", {
    method: "POST",
    body: JSON.stringify(payload),
  });