import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

export const authAPI = {
    // 회원가입
    register: async (userData) => {
        try {
            const response = await api.post('/register', userData);
            return response.data;
        } catch (error) {
            throw error.response?.data || { success: false, message: '서버 오류가 발생했습니다.' };
        }
    },

    // 로그인
    login: async (credentials) => {
        try {
            const response = await api.post('/login', credentials);
            return response.data;
        } catch (error) {
            throw error.response?.data || { success: false, message: '서버 오류가 발생했습니다.' };
        }
    },

    // 로그아웃
    logout: async () => {
        try {
            const response = await api.post('/logout');
            return response.data;
        } catch (error) {
            throw error.response?.data || { success: false, message: '서버 오류가 발생했습니다.' };
        } finally {
            console.log("로그아웃 API 호출 완료.");
        }
    },
};

export default api;
