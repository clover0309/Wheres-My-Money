import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../api/authAPI';

function RegisterPage() {
    const navigate = useNavigate();
    const [id, setId] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [name, setName] = useState('');
    const [emailId, setEmailId] = useState('');
    const [emailDomain, setEmailDomain] = useState('@gmail.com');
    
    const [isIdChecked, setIsIdChecked] = useState(false);
    const [isIdAvailable, setIsIdAvailable] = useState(false);
    const [passwordMatch, setPasswordMatch] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 아이디 중복 확인
    const checkIdDuplicate = async () => {
        if (id.length < 5 || id.length > 10) {
            alert('아이디는 5~10자 사이여야 합니다.');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/auth/check-id?id=${id}`);
            const data = await response.json();
            
            setIsIdChecked(true);
            setIsIdAvailable(data.success); // data.available -> data.success로 변경
            
            if (data.success) {
                alert('사용 가능한 아이디입니다.');
            } else {
                alert('이미 사용 중인 아이디입니다.');
            }
        } catch (error) {
            console.error('아이디 중복 확인 오류:', error);
            alert('아이디 중복 확인 중 오류가 발생했습니다.');
        }
    };

    const handleIdChange = (e) => {
        setId(e.target.value);
        setIsIdChecked(false);
        setIsIdAvailable(false);
    };

    const handlePasswordConfirmChange = (e) => {
        const confirmValue = e.target.value;
        setPasswordConfirm(confirmValue);
        setPasswordMatch(password === confirmValue);
    };

    const handlePasswordChange = (e) => {
        const newPassword = e.target.value;
        setPassword(newPassword);
        if (passwordConfirm) {
            setPasswordMatch(newPassword === passwordConfirm);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!isIdChecked || !isIdAvailable) {
            alert('아이디 중복 확인을 해주세요.');
            return;
        }

        if (!passwordMatch) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        if (password.length < 10 || password.length > 15) {
            alert('비밀번호는 10~15자 사이여야 합니다.');
            return;
        }

        if (!name) {
            alert('이름을 입력해주세요.');
            return;
        }

        if (!emailId) {
            alert('이메일을 입력해주세요.');
            return;
        }

        setIsSubmitting(true);

        try {
            const userData = {
                id: id,
                password: password,
                name: name,
                email: emailId + emailDomain
            };

            const response = await authAPI.register(userData);

            if (response.success) {
                alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
                navigate('/');
            } else {
                alert(response.message || '회원가입에 실패했습니다.');
            }
        } catch (error) {
            console.error('회원가입 오류:', error);
            alert(error.message || '회원가입 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div>
            <h1>회원가입 페이지</h1>
                <form onSubmit={handleSubmit}>
                <input 
                    type="text" 
                    maxLength={10} 
                    minLength={5} 
                    value={id} 
                    onChange={handleIdChange} 
                    placeholder="아이디"
                />
                <button type="button" onClick={checkIdDuplicate}>중복 확인</button>
                {isIdChecked && (
                    <span style={{ color: isIdAvailable ? 'green' : 'red', marginLeft: '10px' }}>
                        {isIdAvailable ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.'}
                    </span>
                )}
                <br />
                <input 
                    type="password" 
                    maxLength={15} 
                    minLength={10} 
                    value={password} 
                    onChange={handlePasswordChange} 
                    placeholder="비밀번호" 
                />
                <br />
                <input 
                    type="password" 
                    value={passwordConfirm}
                    onChange={handlePasswordConfirmChange}
                    placeholder="비밀번호 확인" 
                />
                {passwordConfirm && (
                    <span style={{ color: passwordMatch ? 'green' : 'red', marginLeft: '10px' }}>
                        {passwordMatch ? '일치' : '불일치'}
                    </span>
                )}
                <br />
                <input 
                    type="text" 
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="이름" 
                />
                <br />
                <input 
                    type="text" 
                    value={emailId}
                    onChange={(e) => setEmailId(e.target.value)}
                    placeholder="이메일 ID" 
                /> @
                <select value={emailDomain} onChange={(e) => setEmailDomain(e.target.value)}>
                    <option value="@gmail.com">gmail.com</option>
                    <option value="@naver.com">naver.com</option>
                    <option value="@daum.net">daum.net</option>
                    <option value="@hanmail.net">hanmail.net</option>
                    <option value="@nate.com">nate.com</option>
                </select>
                <br />
                <input type="hidden" value="user" />
                <input type="hidden" value="ACTIVE" />
                
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? '처리중...' : '회원가입'}
                </button>
                </form>
        </div>
    );
}

export default RegisterPage;