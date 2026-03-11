import { Navigate, createBrowserRouter } from "react-router-dom";
import App from "../App";
import { Cart, Catalog, Home, Product } from "../pages";
import LoginPage from "../pages/Login";
import { RegisterPage } from "../auth/pages/RegisterPage";
import { UserProfilePage } from "../pages/UserProfile/UserProfilePage";
import AdminDashboardPage from "../pages/AdminDashboard/AdminDashboardPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      {
        path: "",
        element: <Home />,
      },
      {
        path: "login",
        element: <LoginPage />,
      },
      {
        path: "register",
        element: <RegisterPage />,
      },
      {
        path: "catalog",
        element: <Catalog />,
        children: [
          {
            path: ":id",
            element: <Catalog />,
          },
        ],
      },
      {
        path: "products",
        children: [
          {
            path: ":id",
            element: <Product />,
          },
        ],
      },
      {
        path: "cart",
        element: <Cart />,
      },
      {
        path: "profile",
        element: <UserProfilePage />,
      },
      {
        path: "admin/users",
        element: <AdminDashboardPage />,
      },
      {
        path: "*",
        element: <Navigate to="/" />,
      },
    ],
  },
]);
