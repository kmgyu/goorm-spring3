// localStorage 공통 함수들

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    updateUserInfo();
    updateTokenStatus();
    setupLogout();
});

// localStorage에서 토큰 가져오기
function getAccessToken() {
    return localStorage.getItem('accessToken');
}

function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}

// localStorage에 토큰 저장하기
function saveAccessToken(token) {
    localStorage.setItem('accessToken', token);
    console.log('localStorage에 Access Token 저장 완료');
}

function saveRefreshToken(token) {
    localStorage.setItem('refreshToken', token);
    console.log('localStorage에 Refresh Token 저장 완료');
}

// localStorage에서 토큰 삭제하기
function removeTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    console.log('localStorage에서 모든 토큰 삭제 완료');
}

// JWT 토큰 유효성 검사
function isTokenValid(token) {
    if (!token) return false;

    try {
        const parts = token.split('.');
        if (parts.length !== 3) return false;

        const payload = JSON.parse(atob(parts[1]));
        const currentTime = Math.floor(Date.now() / 1000);

        return payload.exp > currentTime;
    } catch (error) {
        console.error('토큰 검증 실패:', error);
        return false;
    }
}

// JWT 토큰에서 사용자 정보 추출
function getUserFromToken(token) {
    try {
        const parts = token.split('.');
        const payload = JSON.parse(atob(parts[1]));
        return {
            email: payload.sub,
            exp: payload.exp,
            iat: payload.iat
        };
    } catch (error) {
        console.error('사용자 정보 추출 실패:', error);
        return null;
    }
}

// 사용자 정보 업데이트
function updateUserInfo() {
    console.log('👤 localStorage 사용자 정보 업데이트 시작');
    const accessToken = getAccessToken();
    console.log('🔍 localStorage accessToken 확인:', accessToken ? '있음' : '없음');
    const userInfo = document.getElementById('userInfo');
    const navLinks = document.getElementById('navLinks');
    const logoutBtn = document.getElementById('logoutBtn');

    if (accessToken && isTokenValid(accessToken)) {
        const user = getUserFromToken(accessToken);
        if (user) {
            // 사용자 정보 표시
            document.getElementById('userEmail').textContent = user.email;
            document.getElementById('userName').textContent = user.email.split('@')[0];
            document.getElementById('userInitial').textContent = user.email.charAt(0).toUpperCase();

            // UI 요소 표시
            userInfo.classList.remove('hidden');
            navLinks.classList.remove('hidden');
            logoutBtn.classList.remove('hidden');
        }
    } else {
        // 로그인되지 않은 상태
        redirectToLogin();
    }
}

// 토큰 상태 업데이트
function updateTokenStatus() {
    const accessToken = getAccessToken();
    const tokenStatus = document.getElementById('tokenStatus');

    if (!tokenStatus) return;

    if (accessToken && isTokenValid(accessToken)) {
        const user = getUserFromToken(accessToken);
        const expiry = new Date(user.exp * 1000);

        tokenStatus.textContent = `유효 (만료: ${expiry.toLocaleTimeString()})`;
        tokenStatus.className = 'px-3 py-1 rounded-full text-xs token-status valid';
    } else {
        tokenStatus.textContent = '만료됨 또는 없음';
        tokenStatus.className = 'px-3 py-1 rounded-full text-xs token-status expired';
    }
}

// 로그아웃 기능 설정
function setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

// 로그아웃 처리
function logout() {
    if (confirm('localStorage에서 토큰을 삭제하고 로그아웃하시겠습니까?')) {
        removeTokens();
        showMessage('localStorage에서 토큰이 삭제되었습니다. 로그아웃되었습니다.', 'success');

        setTimeout(() => {
            window.location.href = '/pages/auth/login';
        }, 1500);
    }
}

// 로그인 페이지로 리다이렉트
function redirectToLogin() {
    showMessage('localStorage에 유효한 토큰이 없습니다. 로그인 페이지로 이동합니다...', 'warning');

    // 즉시 리다이렉트 (서버에서 이미 인증 실패로 리다이렉트될 수 있으므로)
    window.location.href = '/pages/auth/login';
}

// 간단한 fetch API 래퍼 함수
async function apiCall(url, options = {}) {
    const accessToken = getAccessToken();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (accessToken) {
        headers.Authorization = `Bearer ${accessToken}`;
        console.log('localStorage에서 토큰 추가:', accessToken.substring(0, 20) + '...');
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    // 401 에러 시 토큰 갱신 시도
    if (response.status === 401) {
        console.log('401 에러 - 토큰 갱신 시도');
        const refreshToken = getRefreshToken();

        if (refreshToken) {
            try {
                const refreshResponse = await fetch('/jwt/auth/refresh', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken })
                });

                const refreshData = await refreshResponse.json();

                if (refreshData.success) {
                    saveAccessToken(refreshData.data.accessToken);
                    if (refreshData.data.refreshToken) {
                        saveRefreshToken(refreshData.data.refreshToken);
                    }

                    updateTokenStatus();
                    showMessage('토큰이 자동으로 갱신되었습니다.', 'success');

                    // 원래 요청 재시도
                    headers.Authorization = `Bearer ${refreshData.data.accessToken}`;
                    return await fetch(url, { ...options, headers });
                }
            } catch (error) {
                console.error('토큰 갱신 실패:', error);
            }
        }

        // 토큰 갱신 실패 시 로그아웃
        removeTokens();
        showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
        window.location.href = '/pages/auth/login';
        return;
    }

    return response;
}

// 메시지 표시 함수
function showMessage(message, type = 'info') {
    const container = document.getElementById('messageContainer');
    if (!container) return;

    const messageDiv = document.createElement('div');
    messageDiv.className = `message p-4 rounded-md mb-4 transition-opacity duration-300 ${
        type === 'success' ? 'bg-green-100 text-green-700 border border-green-200' :
        type === 'error' ? 'bg-red-100 text-red-700 border border-red-200' :
        type === 'warning' ? 'bg-yellow-100 text-yellow-700 border border-yellow-200' :
        'bg-blue-100 text-blue-700 border border-blue-200'
    }`;
    messageDiv.textContent = message;

    container.appendChild(messageDiv);

    // 3초 후 자동 제거
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}

// 주기적으로 토큰 상태 업데이트 (30초마다)
setInterval(updateTokenStatus, 30000);