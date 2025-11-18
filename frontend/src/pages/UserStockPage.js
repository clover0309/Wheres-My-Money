import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

function UserStockPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();

    const handleLogout = () => {
        // 여기서 토큰 제거 로직 추가 필요.
        logout();
        navigate("/LoginPages");
    };

    return (
        <div>
            <h1>로그인 성공</h1>
            <button onClick={handleLogout}>로그아웃</button>
        </div>
    );
}

export default UserStockPage;