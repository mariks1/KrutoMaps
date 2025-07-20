import {http} from "./httpClient";

export const getPriceRange = () => http("api/price-range");
export const getAreaRange = () => http("api/area-range");