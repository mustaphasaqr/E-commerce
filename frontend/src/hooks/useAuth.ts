import { useEffect } from "react";

export const useCheckAuthenticated = () => {
  const user = localStorage.getItem("userDetails");
  return user
    ? { user, isAuthenticated: true }
    : { user, isAuthenticated: false };
};

export const useAuth = () => {
  useEffect(() => {
    // TODO: Initialize user if needed
  }, []);
};
