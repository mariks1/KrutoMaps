
export async function http(path, options = {}) {
  const token = localStorage.getItem("token") || "";
  const res = await fetch(`${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  });
  if (!res.ok) throw new Error(res.statusText);
  return res.json();
}