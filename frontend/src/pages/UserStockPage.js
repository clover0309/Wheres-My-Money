import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { authAPI } from "../api/authAPI";

function UserStockPage() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();

    const handleLogout = async () => {
        try {
            const response = await authAPI.logout();
            
            if (response.success) {
                logout();
                alert('로그아웃되었습니다.');
                navigate("/");
            } else {
                alert(response.message || '로그아웃에 실패했습니다.');
            }
        } catch (error) {
            console.error('로그아웃 오류:', error);
            // 에러가 발생해도 로컬 상태는 초기화
            logout();
            navigate("/");
        }
    };

    return (
        <div>
            <h1>로그인 성공</h1>
            {user && <p>환영합니다, {user}님!</p>}
            <button onClick={handleLogout}>로그아웃</button>
        </div>
    );
}

export default UserStockPage;