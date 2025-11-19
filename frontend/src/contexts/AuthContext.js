import { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  // 새로고침 시 로컬스토리지에서 로그인 상태 복원
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      setUser(savedUser);
      setIsLoggedIn(true);
    }
  }, []);

  const login = (userData) => {
    setIsLoggedIn(true);
    setUser(userData);
    // 로컬스토리지에 사용자 정보 저장
    localStorage.setItem('user', userData);
  };

  const logout = () => {
    setIsLoggedIn(false);
    setUser(null);
    // 로컬스토리지에서 사용자 정보 제거
    localStorage.removeItem('user');
  };

    return (
      <AuthContext.Provider value={{ isLoggedIn, user, login, logout }}>
        {children}
      </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}