import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

function LoginPage() {
    const { login, isLoggedIn } = useAuth();
    const navigate = useNavigate();

    const [id, setId] = useState('');
    const [password, setPassword] = useState('');

    console.log("로그인 페이지 렌더링. 현재 status 상태 : ", isLoggedIn);

    useEffect(
        () => {
            if (isLoggedIn) {
                console.log("useEffect 내부: 로그인 상태 감지됨. true 반환중. UserStockPage로 가자.");
                // 내부 replace는 다시 로그인 페이지에 오지않게 방지함.
                navigate("/UserStockPage", {replace: true} );
            }
        }, [isLoggedIn, navigate]);
  
    const handleLogin = (e) => {
        console.log("로그인 시도중...");
        //폼 제출시 새로고침 방지.
        e.preventDefault(); 

        if(id === '' || password === ''){
            alert("아이디나 비밀번호를 모두 입력해주세요.");
            return;
        }
        
        login();
    };

    return (
        <div>
            <h1>로그인 페이지</h1>
                <form onSubmit={handleLogin}>
                <input type="text" value={id} onChange={(e) => setId(e.target.value)} placeholder="아이디" />
                <br />
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" />
                <br />
                <button type="submit">로그인</button>
                </form>
                <button type="" onClick={() => navigate("/RegisterPage")}>회원가입</button>
        </div>
    );
}

export default LoginPage;