// 공통 로그인 페이지 JavaScript
let selectedStorageType = null;

document.addEventListener('DOMContentLoaded', function() {
    // 페이지 요소들
    const localStorageCard = document.getElementById('localStorageCard');
    const sessionStorageCard = document.getElementById('sessionStorageCard');
    const cookieCard = document.getElementById('cookieCard');
    const loginForm = document.getElementById('loginForm');
    const authForm = document.getElementById('authForm');
    const backButton = document.getElementById('backButton');
    const selectedMethod = document.getElementById('selectedMethod');
    const loginButton = document.getElementById('loginButton');

    // 페이지 로드 시 기존 로그인 상태 확인 (자동 리다이렉트 비활성화)
    // checkExistingLogin();

    // 토큰 상태 표시 업데이트
    updateTokenStatusDisplay();

    // 저장 방식 선택 이벤트
    localStorageCard.addEventListener('click', () => selectStorageType('localStorage'));
    sessionStorageCard.addEventListener('click', () => selectStorageType('sessionStorage'));
    cookieCard.addEventListener('click', () => selectStorageType('cookie'));

    // 뒤로가기 버튼
    backButton.addEventListener('click', showStorageSelection);

    // 로그인 폼 제출
    authForm.addEventListener('submit', handleLogin);

    // 저장 방식 선택 함수
    function selectStorageType(type) {
        selectedStorageType = type;

        // 카드 선택 화면 숨기고 로그인 폼 표시
        hideStorageCards();
        showLoginForm(type);
    }

    // 저장 방식 카드들 숨기기
    function hideStorageCards() {
        const cards = document.querySelectorAll('.storage-card');
        cards.forEach(card => {
            card.style.display = 'none';
        });
        document.querySelector('.grid').style.display = 'none';
        document.querySelector('h1').style.display = 'none';
        document.querySelector('p').style.display = 'none';
    }

    // 로그인 폼 표시
    function showLoginForm(type) {
        const methodNames = {
            localStorage: 'localStorage',
            sessionStorage: 'sessionStorage',
            cookie: 'httpOnly Cookie'
        };

        const colors = {
            localStorage: 'bg-blue-500 hover:bg-blue-600',
            sessionStorage: 'bg-green-500 hover:bg-green-600',
            cookie: 'bg-orange-500 hover:bg-orange-600'
        };

        selectedMethod.textContent = methodNames[type];
        loginButton.className = `w-full py-3 px-4 rounded-md text-white font-medium transition duration-200 ${colors[type]}`;
        loginForm.style.display = 'block';
    }

    // 저장 방식 선택 화면으로 돌아가기
    function showStorageSelection() {
        // 로그인 폼 숨기기
        loginForm.style.display = 'none';

        // 카드들 다시 표시
        const cards = document.querySelectorAll('.storage-card');
        cards.forEach(card => {
            card.style.display = 'block';
        });
        document.querySelector('.grid').style.display = 'grid';
        document.querySelector('h1').style.display = 'block';
        document.querySelector('p').style.display = 'block';

        // 폼 초기화
        authForm.reset();
        selectedStorageType = null;
    }

    // 로그인 처리
    async function handleLogin(event) {
        event.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        if (!email || !password) {
            showMessage('이메일과 비밀번호를 입력해주세요.', 'error');
            return;
        }

        if (!selectedStorageType) {
            showMessage('저장 방식이 선택되지 않았습니다.', 'error');
            return;
        }

        // 로딩 상태
        loginButton.disabled = true;
        loginButton.textContent = '로그인 중...';

        try {
            console.log('🔐 로그인 시작:', selectedStorageType, '방식, 이메일:', email);

            // JWT API 호출
            const response = await axios.post('/jwt/auth/login', {
                email: email,
                password: password
            });

            console.log('🎯 로그인 API 응답:', response.status, response.data);

            if (response.data.success) {
                const { accessToken, refreshToken } = response.data.data;

                console.log('💾 토큰 저장 시작:', selectedStorageType);

                // 선택한 방식으로 토큰 저장
                await saveTokensByType(selectedStorageType, accessToken, refreshToken);

                console.log('✅ 토큰 저장 완료, 페이지 이동 예정:', `/pages/${selectedStorageType}/list`);
                showMessage(`${selectedStorageType} 방식으로 로그인 성공!`, 'success');

                // 해당 방식의 목록 페이지로 이동
                setTimeout(() => {
                    window.location.href = `/pages/${selectedStorageType}/list`;
                }, 1500);

            } else {
                throw new Error(response.data.message || '로그인 실패');
            }

        } catch (error) {
            console.error('로그인 실패:', error);

            let errorMessage = '로그인에 실패했습니다.';
            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            showMessage(errorMessage, 'error');

        } finally {
            // 로딩 상태 해제
            loginButton.disabled = false;
            loginButton.textContent = '로그인';
        }
    }

    // 선택한 방식으로 토큰 저장
    async function saveTokensByType(type, accessToken, refreshToken) {
        switch (type) {
            case 'localStorage':
                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('refreshToken', refreshToken);
                console.log('localStorage에 토큰 저장 완료');
                break;

            case 'sessionStorage':
                sessionStorage.setItem('accessToken', accessToken);
                sessionStorage.setItem('refreshToken', refreshToken);
                console.log('sessionStorage에 토큰 저장 완료');
                break;

            case 'cookie':
                // 서버에 쿠키 설정 요청
                await axios.post('/jwt/auth/set-cookie-tokens', {
                    accessToken: accessToken,
                    refreshToken: refreshToken
                });
                console.log('httpOnly Cookie에 토큰 저장 완료');
                break;

            default:
                throw new Error('지원하지 않는 저장 방식입니다.');
        }
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

    // 기존 로그인 상태 확인
    async function checkExistingLogin() {
        // localStorage 확인 (최우선)
        const localAccessToken = localStorage.getItem('accessToken');
        if (localAccessToken && isTokenValid(localAccessToken)) {
            showMessage('localStorage에 유효한 토큰이 있습니다. 자동으로 이동합니다...', 'success');
            setTimeout(() => {
                window.location.href = '/pages/localStorage/list';
            }, 1500);
            return;
        }

        // sessionStorage 확인 (두 번째)
        const sessionAccessToken = sessionStorage.getItem('accessToken');
        if (sessionAccessToken && isTokenValid(sessionAccessToken)) {
            showMessage('sessionStorage에 유효한 토큰이 있습니다. 자동으로 이동합니다...', 'success');
            setTimeout(() => {
                window.location.href = '/pages/sessionStorage/list';
            }, 1500);
            return;
        }

        // httpOnly Cookie 확인 (세 번째)
        try {
            const response = await axios.get('/jwt/auth/check');
            if (response.data.success && response.data.data.valid) {
                showMessage('httpOnly Cookie에 유효한 토큰이 있습니다. 자동으로 이동합니다...', 'success');
                setTimeout(() => {
                    window.location.href = '/pages/cookie/list';
                }, 1500);
                return;
            }
        } catch (error) {
            // Cookie 확인 실패는 무시 (로그인되지 않음)
        }

        // 모든 방식에서 로그인되지 않음 - 로그인 선택 화면 표시
        console.log('기존 로그인이 없습니다. 새로 로그인해주세요.');
    }

    // 토큰 상태 표시 업데이트
    function updateTokenStatusDisplay() {
        // localStorage 토큰 상태
        updateLocalStorageStatus();

        // sessionStorage 토큰 상태
        updateSessionStorageStatus();

        // Cookie 토큰 상태
        updateCookieStatus();
    }

    // localStorage 토큰 상태 업데이트
    function updateLocalStorageStatus() {
        const token = localStorage.getItem('accessToken');
        const statusElement = document.getElementById('localStorageStatus');
        const infoElement = document.getElementById('localStorageInfo');

        if (token && isTokenValid(token)) {
            const user = getUserFromToken(token);
            statusElement.className = 'px-2 py-1 text-xs rounded-full bg-green-100 text-green-600';
            statusElement.textContent = '로그인됨';
            infoElement.innerHTML = `
                <div>사용자: ${user ? user.email : '알 수 없음'}</div>
                <div>만료: ${user ? new Date(user.exp * 1000).toLocaleString() : '알 수 없음'}</div>
            `;
        } else {
            statusElement.className = 'px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600';
            statusElement.textContent = '로그인 필요';
            infoElement.textContent = '토큰이 없거나 만료되었습니다';
        }
    }

    // sessionStorage 토큰 상태 업데이트
    function updateSessionStorageStatus() {
        const token = sessionStorage.getItem('accessToken');
        const statusElement = document.getElementById('sessionStorageStatus');
        const infoElement = document.getElementById('sessionStorageInfo');

        if (token && isTokenValid(token)) {
            const user = getUserFromToken(token);
            statusElement.className = 'px-2 py-1 text-xs rounded-full bg-green-100 text-green-600';
            statusElement.textContent = '로그인됨';
            infoElement.innerHTML = `
                <div>사용자: ${user ? user.email : '알 수 없음'}</div>
                <div>만료: ${user ? new Date(user.exp * 1000).toLocaleString() : '알 수 없음'}</div>
            `;
        } else {
            statusElement.className = 'px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600';
            statusElement.textContent = '로그인 필요';
            infoElement.textContent = '토큰이 없거나 만료되었습니다';
        }
    }

    // Cookie 토큰 상태 업데이트
    async function updateCookieStatus() {
        const statusElement = document.getElementById('cookieStatus');
        const infoElement = document.getElementById('cookieInfo');

        try {
            const response = await axios.get('/jwt/auth/check');
            if (response.data.success && response.data.data.valid) {
                statusElement.className = 'px-2 py-1 text-xs rounded-full bg-green-100 text-green-600';
                statusElement.textContent = '로그인됨';
                infoElement.innerHTML = `
                    <div>httpOnly Cookie 활성</div>
                    <div>서버에서 확인됨</div>
                `;
            } else {
                statusElement.className = 'px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600';
                statusElement.textContent = '로그인 필요';
                infoElement.textContent = 'Cookie가 없거나 만료되었습니다';
            }
        } catch (error) {
            statusElement.className = 'px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600';
            statusElement.textContent = '로그인 필요';
            infoElement.textContent = 'Cookie가 없거나 만료되었습니다';
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
            return null;
        }
    }


    // JWT 토큰 유효성 간단 확인 (클라이언트용)
    function isTokenValid(token) {
        if (!token) return false;

        try {
            const parts = token.split('.');
            if (parts.length !== 3) return false;

            const payload = JSON.parse(atob(parts[1]));
            const currentTime = Math.floor(Date.now() / 1000);

            return payload.exp > currentTime;
        } catch (error) {
            return false;
        }
    }
});