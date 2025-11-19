import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authAPI } from "../api/authAPI";
import { useAuth } from "../contexts/AuthContext";

function LoginPage() {
    const { login, isLoggedIn } = useAuth();
    const navigate = useNavigate();

    const [id, setId] = useState('');
    const [password, setPassword] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    console.log("로그인 페이지 렌더링. 현재 status 상태 : ", isLoggedIn);

    useEffect(
        () => {
            if (isLoggedIn) {
                console.log("useEffect 내부: 로그인 상태 감지됨. true 반환중. UserStockPage로 가자.");
                // 내부 replace는 다시 로그인 페이지에 오지않게 방지함.
                navigate("/UserStockPage", {replace: true} );
            }
        }, [isLoggedIn, navigate]);
  
    const handleLogin = async (e) => {
        e.preventDefault();

        if(id === '' || password === ''){
            alert("아이디나 비밀번호를 모두 입력해주세요.");
            return;
        }

        setIsSubmitting(true);

        try {
            const credentials = {
                id: id,
                password: password
            };

            const response = await authAPI.login(credentials);

            if (response.success) {
                alert('로그인 성공!');
                login(response.data); // 사용자 정보 전달
            } else {
                alert(response.message || '로그인에 실패했습니다.');
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            alert(error.message || '로그인 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div>
            <h1>로그인 페이지</h1>
                <form onSubmit={handleLogin}>
                <input type="text" value={id} onChange={(e) => setId(e.target.value)} placeholder="아이디" />
                <br />
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" />
                <br />
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? '로그인 중...' : '로그인'}
                </button>
                </form>
                <button type="button" onClick={() => navigate("/RegisterPage")}>회원가입</button>
        </div>
    );
}

export default LoginPage;