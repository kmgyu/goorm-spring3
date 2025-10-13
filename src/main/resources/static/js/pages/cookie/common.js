// httpOnly Cookie 공통 함수들

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    updateUserInfo();
    updateTokenStatus();
    setupLogout();
});

// httpOnly Cookie에서는 JavaScript로 토큰에 직접 접근할 수 없음
function getAccessToken() {
    // httpOnly Cookie는 JavaScript에서 접근 불가
    console.log('httpOnly Cookie는 JavaScript에서 접근할 수 없습니다.');
    return null;
}

function getRefreshToken() {
    // httpOnly Cookie는 JavaScript에서 접근 불가
    console.log('httpOnly Cookie는 JavaScript에서 접근할 수 없습니다.');
    return null;
}

// httpOnly Cookie 토큰 저장 (서버에서 처리)
function saveAccessToken(token) {
    console.log('httpOnly Cookie는 서버에서만 설정할 수 있습니다.');
    // 이 함수는 교육용으로만 존재, 실제로는 서버에서 Set-Cookie 헤더로 처리
}

function saveRefreshToken(token) {
    console.log('httpOnly Cookie는 서버에서만 설정할 수 있습니다.');
    // 이 함수는 교육용으로만 존재, 실제로는 서버에서 Set-Cookie 헤더로 처리
}

// httpOnly Cookie 토큰 삭제 (서버에 요청)
async function removeTokens() {
    try {
        const response = await axios.post('/jwt/auth/logout');
        if (response.data.success) {
            console.log('서버에서 httpOnly Cookie 토큰 삭제 완료');
        }
    } catch (error) {
        console.error('토큰 삭제 실패:', error);
    }
}

// JWT 토큰 유효성 검사 (서버에서 확인)
async function checkTokenValid() {
    try {
        const response = await axios.get('/jwt/auth/check');
        return response.data.success && response.data.data.valid;
    } catch (error) {
        console.error('토큰 검증 실패:', error);
        return false;
    }
}

// 서버에서 사용자 정보 가져오기
async function getUserInfo() {
    try {
        const response = await axios.get('/jwt/auth/me');
        if (response.data.success) {
            return response.data.data;
        }
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
    }
    return null;
}

// 사용자 정보 업데이트
async function updateUserInfo() {
    console.log('👤 httpOnly Cookie 사용자 정보 업데이트 시작');
    const userInfo = document.getElementById('userInfo');
    const navLinks = document.getElementById('navLinks');
    const logoutBtn = document.getElementById('logoutBtn');

    try {
        const user = await getUserInfo();
        if (user) {
            // 사용자 정보 표시
            document.getElementById('userEmail').textContent = user.email;
            document.getElementById('userName').textContent = user.email.split('@')[0];
            document.getElementById('userInitial').textContent = user.email.charAt(0).toUpperCase();

            // UI 요소 표시
            userInfo.classList.remove('hidden');
            navLinks.classList.remove('hidden');
            logoutBtn.classList.remove('hidden');
        } else {
            console.log("warning! not logined");
            // 로그인되지 않은 상태
            // redirectToLogin();
        }
    } catch (error) {
        console.log(error)
        console.log("warning! error detected");
        // 로그인되지 않은 상태
        // redirectToLogin();
    }
}

// 토큰 상태 업데이트
async function updateTokenStatus() {
    const tokenStatus = document.getElementById('tokenStatus');

    if (!tokenStatus) return;

    try {
        const isValid = await checkTokenValid();
        if (isValid) {
            tokenStatus.textContent = '유효 (서버에서 확인됨)';
            tokenStatus.className = 'px-3 py-1 rounded-full text-xs token-status valid';
        } else {
            tokenStatus.textContent = '만료됨 또는 없음';
            tokenStatus.className = 'px-3 py-1 rounded-full text-xs token-status expired';
        }
    } catch (error) {
        tokenStatus.textContent = '확인 실패';
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
async function logout() {
    if (confirm('httpOnly Cookie를 삭제하고 로그아웃하시겠습니까?')) {
        try {
            await removeTokens();
            showMessage('httpOnly Cookie가 삭제되었습니다. 로그아웃되었습니다.', 'success');

            setTimeout(() => {
                window.location.href = '/pages/auth/login';
            }, 1500);
        } catch (error) {
            showMessage('로그아웃 처리 중 오류가 발생했습니다.', 'error');
        }
    }
}

// 로그인 페이지로 리다이렉트
function redirectToLogin() {
    showMessage('유효한 httpOnly Cookie가 없습니다. 로그인 페이지로 이동합니다...', 'warning');

    // 즉시 리다이렉트 (서버에서 이미 인증 실패로 리다이렉트될 수 있으므로)
    window.location.href = '/pages/auth/login';
}

// API 호출 시 쿠키는 자동으로 포함됨 (withCredentials 설정)
axios.defaults.withCredentials = true;

// API 응답에서 401 에러 시 토큰 갱신 시도
axios.interceptors.response.use(
    function (response) {
        return response;
    },
    async function (error) {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            // httpOnly Cookie의 Refresh Token으로 갱신 시도
            try {
                const response = await axios.post('/jwt/auth/refresh-cookie');

                if (response.data.success) {
                    // 토큰 상태 업데이트
                    updateTokenStatus();
                    showMessage('토큰이 자동으로 갱신되었습니다.', 'success');

                    // 원래 요청 재시도
                    return axios(originalRequest);
                }
            } catch (refreshError) {
                console.error('토큰 갱신 실패:', refreshError);
            }

            // 토큰 갱신 실패 시 로그아웃 처리
            showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
            setTimeout(() => {
                window.location.href = '/pages/auth/login';
            }, 2000);
        }

        return Promise.reject(error);
    }
);

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