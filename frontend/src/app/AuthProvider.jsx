import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginApi, signUpApi } from "@services/auth";
import { AuthCtx } from "./AuthContext";

export default function AuthProvider({ children }) {
  const [token, setToken] = useState(
    () => localStorage.getItem("token") || ""
  );
  const navigate = useNavigate();

  const signIn = async (name, password) => {
    const { token } = await loginApi(name, password);
    setToken(token);
    localStorage.setItem("token", token);
    navigate("/");
  };

   const signUp = async (name, password) => {
    const { token } = await signUpApi(name, password);
    setToken(token);
    localStorage.setItem("token", token);
    navigate("/");
   };

  const signOut = () => {
    setToken("");
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <AuthCtx.Provider value={{ token, signIn, signUp, signOut, isAuth: !!token }}>
      {children}
    </AuthCtx.Provider>
  );
}
