import { http } from "./httpClient";

export async function loginApi(username, password) {
  return http("/auth/sign-in", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export const signUpApi = (username, password) =>
  http("/auth/sign-up", {
    method: "POST",
    body: JSON.stringify({
      username,
      password,
      role: "USER",
    }),
  });