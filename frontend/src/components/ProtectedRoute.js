import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

function ProviderdRoute({ children }) {
  const { isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    // 로그인이 진행되지 않았을 때, 로그인 페이지로 자동 리다이렉트.
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProviderdRoute;