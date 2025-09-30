// Fetch API 공통 함수들

// API 기본 설정
const API_BASE = '/api/responseentity';

// Fetch API 공통 설정
const fetchAPI = {
    // GET 요청
    get: async (url) => {
        const language = localStorage.getItem('language') || 'ko';
        const response = await fetch(`${API_BASE}${url}`, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
                'Accept-Language': language
            }
        });
        return await response.json();
    },

    // POST 요청
    post: async (url, data) => {
        const language = localStorage.getItem('language') || 'ko';
        const response = await fetch(`${API_BASE}${url}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Accept-Language': language
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    },

    // PUT 요청
    put: async (url, data) => {
        const language = localStorage.getItem('language') || 'ko';
        const response = await fetch(`${API_BASE}${url}`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Accept-Language': language
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    },

    // DELETE 요청
    delete: async (url) => {
        const language = localStorage.getItem('language') || 'ko';
        const response = await fetch(`${API_BASE}${url}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
                'Accept-Language': language
            }
        });
        return await response.json();
    }
};

// API 호출 별칭 (호환성을 위해)
const apiCall = fetchAPI;

// 세션 확인 함수 (심플 버전)
async function checkSession() {
    try {
        const response = await fetchAPI.get('/auth/check');

        if (response.success) {
            updateUserInfo(response.data.user);
            return response.data.user;
        } else {
            updateUserInfo(null);
            return null;
        }
    } catch (error) {
        updateUserInfo(null);
        return null;
    }
}

// 로그아웃 함수 (심플 버전)
async function logout() {
    try {
        const response = await fetchAPI.post('/auth/logout');

        if (response.success) {
            showSuccessMessage(response.message);
            localStorage.removeItem('user');
            updateUserInfo(null);
            window.location.href = '/fetch/auth/login';
        } else {
            showErrorMessage(response.error.message);
        }
    } catch (error) {
        showErrorMessage('로그아웃 중 오류가 발생했습니다.');
    }
}

// 페이지 초기화 시 세션 확인
document.addEventListener('DOMContentLoaded', async function() {
    // 로그아웃 버튼 이벤트 연결
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // 로그인 페이지가 아닌 경우에만 세션 확인
    if (!window.location.pathname.includes('/login')) {
        await checkSession();
    }
});