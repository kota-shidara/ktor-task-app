import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "../contexts/AuthContext"

const PublicOnlyRoute: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return !isAuthenticated ? <Outlet /> : <Navigate to="/dashboard" replace />;
};

export default PublicOnlyRoute;