import {http} from "./httpClient";

export const getRubrics = () => http("api/rubrics");