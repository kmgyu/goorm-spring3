// JWT 토큰 관리 공통 함수
window.TokenManager = (function() {
    let currentStorageType = null;

    // 저장 방식별 토큰 관리
    const storageHandlers = {
        localStorage: {
            setAccessToken: (token) => localStorage.setItem('accessToken', token),
            getAccessToken: () => localStorage.getItem('accessToken'),
            setRefreshToken: (token) => localStorage.setItem('refreshToken', token),
            getRefreshToken: () => localStorage.getItem('refreshToken'),
            clearTokens: () => {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('storageType');
            }
        },
        sessionStorage: {
            setAccessToken: (token) => sessionStorage.setItem('accessToken', token),
            getAccessToken: () => sessionStorage.getItem('accessToken'),
            setRefreshToken: (token) => sessionStorage.setItem('refreshToken', token),
            getRefreshToken: () => sessionStorage.getItem('refreshToken'),
            clearTokens: () => {
                sessionStorage.removeItem('accessToken');
                sessionStorage.removeItem('refreshToken');
                sessionStorage.removeItem('storageType');
            }
        },
        cookie: {
            setAccessToken: () => {
                // httpOnly Cookie는 JavaScript에서 설정할 수 없음
                // 서버에서 설정하므로 여기서는 아무것도 하지 않음
            },
            getAccessToken: () => {
                // httpOnly Cookie는 JavaScript에서 읽을 수 없음
                // axios 인터셉터에서 서버가 자동으로 쿠키를 포함하므로 null 반환
                return null;
            },
            setRefreshToken: () => {
                // httpOnly Cookie는 JavaScript에서 설정할 수 없음
            },
            getRefreshToken: () => {
                // httpOnly Cookie는 JavaScript에서 읽을 수 없음
                return null;
            },
            clearTokens: async () => {
                // 서버에 쿠키 삭제 요청
                try {
                    await axios.post('/jwt/auth/clear-cookie-tokens');
                    localStorage.removeItem('storageType');
                    sessionStorage.removeItem('storageType');
                } catch (error) {
                    console.error('쿠키 삭제 실패:', error);
                }
            }
        }
    };

    return {
        // 저장 방식 설정
        setStorageType: function(type) {
            currentStorageType = type;
            localStorage.setItem('storageType', type);
            sessionStorage.setItem('storageType', type);
        },

        // 현재 저장 방식 조회
        getStorageType: function() {
            if (currentStorageType) return currentStorageType;

            return localStorage.getItem('storageType') ||
                   sessionStorage.getItem('storageType') ||
                   null;
        },

        // Access Token 저장
        setAccessToken: function(token) {
            const type = this.getStorageType();
            if (type && storageHandlers[type]) {
                storageHandlers[type].setAccessToken(token);
            }
        },

        // Access Token 조회
        getAccessToken: function() {
            const type = this.getStorageType();
            if (type && storageHandlers[type]) {
                return storageHandlers[type].getAccessToken();
            }
            return null;
        },

        // Refresh Token 저장
        setRefreshToken: function(token) {
            const type = this.getStorageType();
            if (type && storageHandlers[type]) {
                storageHandlers[type].setRefreshToken(token);
            }
        },

        // Refresh Token 조회
        getRefreshToken: function() {
            const type = this.getStorageType();
            if (type && storageHandlers[type]) {
                return storageHandlers[type].getRefreshToken();
            }
            return null;
        },

        // 토큰 저장 (로그인 성공 시 사용)
        saveTokens: async function(accessToken, refreshToken) {
            const type = this.getStorageType();

            if (type === 'cookie') {
                // Cookie 방식: 서버에 요청하여 httpOnly Cookie 설정
                try {
                    await axios.post('/jwt/auth/set-cookie-tokens', {
                        accessToken: accessToken,
                        refreshToken: refreshToken
                    });
                } catch (error) {
                    console.error('쿠키 설정 실패:', error);
                    throw error;
                }
            } else {
                // localStorage/sessionStorage 방식
                this.setAccessToken(accessToken);
                this.setRefreshToken(refreshToken);
            }
        },

        // 모든 토큰 삭제
        clearTokens: async function() {
            const type = this.getStorageType();
            if (type && storageHandlers[type]) {
                await storageHandlers[type].clearTokens();
            }
            currentStorageType = null;
        },

        // 토큰 유효성 검사
        validateToken: function(token) {
            if (!token) return false;

            try {
                // JWT 토큰의 기본 구조 검증 (header.payload.signature)
                const parts = token.split('.');
                if (parts.length !== 3) return false;

                // payload 디코딩하여 만료시간 확인
                const payload = JSON.parse(atob(parts[1]));
                const currentTime = Math.floor(Date.now() / 1000);

                return payload.exp > currentTime;
            } catch (error) {
                return false;
            }
        },

        // 토큰 만료 시간 조회
        getTokenExpiry: function(token) {
            try {
                const parts = token.split('.');
                const payload = JSON.parse(atob(parts[1]));
                return new Date(payload.exp * 1000);
            } catch (error) {
                return null;
            }
        },

        // 토큰에서 사용자 정보 추출
        getUserFromToken: function(token) {
            try {
                const parts = token.split('.');
                const payload = JSON.parse(atob(parts[1]));
                return {
                    email: payload.sub,
                    exp: payload.exp,
                    iat: payload.iat
                };
            } catch (error) {
                return null;
            }
        },

        // 로그인 상태 확인
        isLoggedIn: function() {
            const type = this.getStorageType();
            if (!type) return false;

            if (type === 'cookie') {
                // 쿠키 방식은 서버에서 검증해야 하므로 일단 true 반환
                // 실제 검증은 API 호출 시 수행됨
                return true;
            }

            const accessToken = this.getAccessToken();
            return accessToken && this.validateToken(accessToken);
        }
    };
})();

// 페이지 로드 시 토큰 상태 표시 업데이트
document.addEventListener('DOMContentLoaded', function() {
    updateTokenStatus();
    updateStorageTypeDisplay();
    updateHeaderNavigation();
});

// 토큰 상태 표시 업데이트
function updateTokenStatus() {
    const tokenManager = window.TokenManager;
    const statusElements = document.querySelectorAll('#tokenStatus, #headerTokenStatus');

    if (tokenManager.isLoggedIn()) {
        const accessToken = tokenManager.getAccessToken();
        const type = tokenManager.getStorageType();

        statusElements.forEach(element => {
            if (type === 'cookie') {
                element.textContent = '유효 (Cookie)';
                element.className = 'token-status valid';
            } else if (accessToken && tokenManager.validateToken(accessToken)) {
                element.textContent = '유효';
                element.className = 'token-status valid';
            } else {
                element.textContent = '만료됨';
                element.className = 'token-status expired';
            }
        });
    } else {
        statusElements.forEach(element => {
            element.textContent = '없음';
            element.className = 'token-status';
        });
    }
}

// 저장 방식 표시 업데이트
function updateStorageTypeDisplay() {
    const tokenManager = window.TokenManager;
    const type = tokenManager.getStorageType();
    const elements = document.querySelectorAll('#storageType, #headerStorageType');

    elements.forEach(element => {
        if (type) {
            element.textContent = type;
        } else {
            element.textContent = '-';
        }
    });
}

// 헤더 네비게이션 업데이트
async function updateHeaderNavigation() {
    const tokenManager = window.TokenManager;
    const isLoggedIn = tokenManager.isLoggedIn();
    const storageType = tokenManager.getStorageType();

    const beforeLoginNav = document.getElementById('beforeLoginNav');
    const afterLoginNav = document.getElementById('afterLoginNav');

    if (isLoggedIn || storageType === 'cookie') {
        // 로그인된 상태 - 사용자 정보 조회
        try {
            let userInfo = null;

            if (storageType === 'cookie') {
                // Cookie 방식: API 호출로 사용자 정보 조회
                const response = await axios.get('/jwt/auth/me');
                if (response.data.success) {
                    userInfo = response.data.data;
                }
            } else {
                // localStorage/sessionStorage: 토큰에서 사용자 정보 추출
                const accessToken = tokenManager.getAccessToken();
                if (accessToken && tokenManager.validateToken(accessToken)) {
                    const tokenInfo = tokenManager.getUserFromToken(accessToken);
                    userInfo = {
                        email: tokenInfo.email,
                        name: tokenInfo.email.split('@')[0] // 이메일에서 이름 추출
                    };
                }
            }

            if (userInfo) {
                updateUserInfo(userInfo, storageType);
                showAfterLoginNav();
            } else {
                showBeforeLoginNav();
            }

        } catch (error) {
            console.error('사용자 정보 조회 실패:', error);
            showBeforeLoginNav();
        }
    } else {
        showBeforeLoginNav();
    }

    function showBeforeLoginNav() {
        if (beforeLoginNav) beforeLoginNav.classList.remove('hidden');
        if (afterLoginNav) afterLoginNav.classList.add('hidden');
    }

    function showAfterLoginNav() {
        if (beforeLoginNav) beforeLoginNav.classList.add('hidden');
        if (afterLoginNav) afterLoginNav.classList.remove('hidden');
    }
}

// 사용자 정보 업데이트
function updateUserInfo(userInfo, storageType) {
    const userInitial = document.getElementById('userInitial');
    const userName = document.getElementById('userName');
    const userEmail = document.getElementById('userEmail');
    const headerStorageTypeAfter = document.getElementById('headerStorageTypeAfter');

    if (userInitial && userInfo.name) {
        userInitial.textContent = userInfo.name.charAt(0).toUpperCase();
    }

    if (userName && userInfo.name) {
        userName.textContent = userInfo.name;
    }

    if (userEmail && userInfo.email) {
        userEmail.textContent = userInfo.email;
    }

    if (headerStorageTypeAfter && storageType) {
        headerStorageTypeAfter.textContent = storageType;
        headerStorageTypeAfter.className = `px-2 py-1 rounded text-xs ${getStorageTypeClass(storageType)}`;
    }
}

// 저장 방식별 CSS 클래스
function getStorageTypeClass(type) {
    switch (type) {
        case 'localStorage':
            return 'bg-blue-100 text-blue-700';
        case 'sessionStorage':
            return 'bg-green-100 text-green-700';
        case 'cookie':
            return 'bg-orange-100 text-orange-700';
        default:
            return 'bg-gray-100 text-gray-700';
    }
}

// 로그아웃 시 헤더 업데이트
async function logoutAndUpdateHeader() {
    await logout();
    updateHeaderNavigation();
}

// 공통 로그아웃 함수
async function logout() {
    try {
        const tokenManager = window.TokenManager;
        await tokenManager.clearTokens();

        showMessage('로그아웃되었습니다.', 'success');

        // 로그인 페이지로 이동
        setTimeout(() => {
            window.location.href = '/pages/jwt/auth/login';
        }, 1000);

    } catch (error) {
        console.error('로그아웃 실패:', error);
        showMessage('로그아웃 처리 중 오류가 발생했습니다.', 'error');
    }
}

// 메시지 표시 함수
function showMessage(message, type = 'info') {
    const container = document.getElementById('messageContainer');
    if (!container) return;

    const messageDiv = document.createElement('div');
    messageDiv.className = `message p-4 rounded-md mb-4 ${
        type === 'success' ? 'bg-green-100 text-green-700 border border-green-200' :
        type === 'error' ? 'bg-red-100 text-red-700 border border-red-200' :
        type === 'warning' ? 'bg-yellow-100 text-yellow-700 border border-yellow-200' :
        'bg-blue-100 text-blue-700 border border-blue-200'
    }`;
    messageDiv.textContent = message;

    container.appendChild(messageDiv);

    // 3초 후 자동 제거
    setTimeout(() => {
        messageDiv.classList.add('fade-out');
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}