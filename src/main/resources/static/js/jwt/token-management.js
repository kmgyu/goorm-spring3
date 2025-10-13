// JWT 토큰 관리 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const tokenManager = window.TokenManager;

    // 페이지 요소들
    const currentStorageType = document.getElementById('currentStorageType');
    const changeStorageBtn = document.getElementById('changeStorageBtn');
    const refreshAllBtn = document.getElementById('refreshAllBtn');

    const clearLocalStorageBtn = document.getElementById('clearLocalStorageBtn');
    const clearSessionStorageBtn = document.getElementById('clearSessionStorageBtn');
    const clearCookieBtn = document.getElementById('clearCookieBtn');
    const clearAllTokensBtn = document.getElementById('clearAllTokensBtn');

    const localStorageInfo = document.getElementById('localStorageInfo');
    const sessionStorageInfo = document.getElementById('sessionStorageInfo');
    const testCookieBtn = document.getElementById('testCookieBtn');

    const browserInfo = document.getElementById('browserInfo');
    const storageSupport = document.getElementById('storageSupport');

    // 이벤트 리스너 등록
    if (refreshAllBtn) {
        refreshAllBtn.addEventListener('click', loadAllTokenInfo);
    }

    if (changeStorageBtn) {
        changeStorageBtn.addEventListener('click', () => {
            window.location.href = '/pages/jwt/auth/login';
        });
    }

    if (clearLocalStorageBtn) {
        clearLocalStorageBtn.addEventListener('click', clearLocalStorageTokens);
    }

    if (clearSessionStorageBtn) {
        clearSessionStorageBtn.addEventListener('click', clearSessionStorageTokens);
    }

    if (clearCookieBtn) {
        clearCookieBtn.addEventListener('click', clearCookieTokens);
    }

    if (clearAllTokensBtn) {
        clearAllTokensBtn.addEventListener('click', clearAllTokens);
    }

    if (testCookieBtn) {
        testCookieBtn.addEventListener('click', testCookieTokens);
    }

    // 초기 로드
    loadAllTokenInfo();
    loadBrowserInfo();
    loadStorageSupport();

    // 모든 토큰 정보 로드
    function loadAllTokenInfo() {
        loadCurrentStorageType();
        loadLocalStorageInfo();
        loadSessionStorageInfo();
        updateTokenStatus();
        updateStorageTypeDisplay();
    }

    // 현재 저장 방식 표시
    function loadCurrentStorageType() {
        const type = tokenManager.getStorageType();
        if (currentStorageType) {
            if (type) {
                currentStorageType.textContent = type;
                currentStorageType.className = `px-4 py-2 rounded-full font-medium ${getStorageTypeClass(type)}`;
            } else {
                currentStorageType.textContent = '설정되지 않음';
                currentStorageType.className = 'px-4 py-2 bg-gray-400 text-white rounded-full font-medium';
            }
        }
    }

    // 저장 방식별 CSS 클래스
    function getStorageTypeClass(type) {
        switch (type) {
            case 'localStorage':
                return 'bg-blue-600 text-white';
            case 'sessionStorage':
                return 'bg-green-600 text-white';
            case 'cookie':
                return 'bg-orange-600 text-white';
            default:
                return 'bg-gray-400 text-white';
        }
    }

    // localStorage 정보 로드
    function loadLocalStorageInfo() {
        if (!localStorageInfo) return;

        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');
        const storageType = localStorage.getItem('storageType');

        let html = '';

        if (!accessToken && !refreshToken && !storageType) {
            html = '<p class="text-gray-500">저장된 토큰이 없습니다.</p>';
        } else {
            html += '<div class="grid grid-cols-1 gap-4">';

            if (accessToken) {
                const tokenInfo = analyzeToken(accessToken);
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-blue-600 mb-2">Access Token</h4>
                        <div class="text-sm space-y-1">
                            <p><strong>상태:</strong> <span class="${tokenInfo.isValid ? 'text-green-600' : 'text-red-600'}">${tokenInfo.isValid ? '유효' : '만료'}</span></p>
                            <p><strong>이메일:</strong> ${tokenInfo.email || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${tokenInfo.expiry || '알 수 없음'}</p>
                            <p><strong>토큰 길이:</strong> ${accessToken.length} 문자</p>
                        </div>
                    </div>
                `;
            }

            if (refreshToken) {
                const tokenInfo = analyzeToken(refreshToken);
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-green-600 mb-2">Refresh Token</h4>
                        <div class="text-sm space-y-1">
                            <p><strong>상태:</strong> <span class="${tokenInfo.isValid ? 'text-green-600' : 'text-red-600'}">${tokenInfo.isValid ? '유효' : '만료'}</span></p>
                            <p><strong>이메일:</strong> ${tokenInfo.email || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${tokenInfo.expiry || '알 수 없음'}</p>
                            <p><strong>토큰 길이:</strong> ${refreshToken.length} 문자</p>
                        </div>
                    </div>
                `;
            }

            if (storageType) {
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-gray-600 mb-2">저장 방식 설정</h4>
                        <p class="text-sm">설정된 방식: <strong>${storageType}</strong></p>
                    </div>
                `;
            }

            html += '</div>';
        }

        localStorageInfo.innerHTML = html;
    }

    // sessionStorage 정보 로드
    function loadSessionStorageInfo() {
        if (!sessionStorageInfo) return;

        const accessToken = sessionStorage.getItem('accessToken');
        const refreshToken = sessionStorage.getItem('refreshToken');
        const storageType = sessionStorage.getItem('storageType');

        let html = '';

        if (!accessToken && !refreshToken && !storageType) {
            html = '<p class="text-gray-500">저장된 토큰이 없습니다.</p>';
        } else {
            html += '<div class="grid grid-cols-1 gap-4">';

            if (accessToken) {
                const tokenInfo = analyzeToken(accessToken);
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-blue-600 mb-2">Access Token</h4>
                        <div class="text-sm space-y-1">
                            <p><strong>상태:</strong> <span class="${tokenInfo.isValid ? 'text-green-600' : 'text-red-600'}">${tokenInfo.isValid ? '유효' : '만료'}</span></p>
                            <p><strong>이메일:</strong> ${tokenInfo.email || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${tokenInfo.expiry || '알 수 없음'}</p>
                            <p><strong>토큰 길이:</strong> ${accessToken.length} 문자</p>
                        </div>
                    </div>
                `;
            }

            if (refreshToken) {
                const tokenInfo = analyzeToken(refreshToken);
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-green-600 mb-2">Refresh Token</h4>
                        <div class="text-sm space-y-1">
                            <p><strong>상태:</strong> <span class="${tokenInfo.isValid ? 'text-green-600' : 'text-red-600'}">${tokenInfo.isValid ? '유효' : '만료'}</span></p>
                            <p><strong>이메일:</strong> ${tokenInfo.email || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${tokenInfo.expiry || '알 수 없음'}</p>
                            <p><strong>토큰 길이:</strong> ${refreshToken.length} 문자</p>
                        </div>
                    </div>
                `;
            }

            if (storageType) {
                html += `
                    <div class="border rounded-lg p-4">
                        <h4 class="font-medium text-gray-600 mb-2">저장 방식 설정</h4>
                        <p class="text-sm">설정된 방식: <strong>${storageType}</strong></p>
                    </div>
                `;
            }

            html += '</div>';
        }

        sessionStorageInfo.innerHTML = html;
    }

    // 토큰 분석
    function analyzeToken(token) {
        try {
            const parts = token.split('.');
            if (parts.length !== 3) {
                return { isValid: false, error: '잘못된 JWT 형식' };
            }

            const payload = JSON.parse(atob(parts[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            const isValid = payload.exp > currentTime;

            return {
                isValid: isValid,
                email: payload.sub,
                expiry: new Date(payload.exp * 1000).toLocaleString('ko-KR'),
                iat: new Date(payload.iat * 1000).toLocaleString('ko-KR')
            };
        } catch (error) {
            return { isValid: false, error: '토큰 분석 실패' };
        }
    }

    // localStorage 토큰 삭제
    function clearLocalStorageTokens() {
        if (confirm('localStorage의 모든 JWT 토큰을 삭제하시겠습니까?')) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('storageType');

            showMessage('localStorage 토큰이 삭제되었습니다.', 'success');
            loadLocalStorageInfo();
            loadCurrentStorageType();
        }
    }

    // sessionStorage 토큰 삭제
    function clearSessionStorageTokens() {
        if (confirm('sessionStorage의 모든 JWT 토큰을 삭제하시겠습니까?')) {
            sessionStorage.removeItem('accessToken');
            sessionStorage.removeItem('refreshToken');
            sessionStorage.removeItem('storageType');

            showMessage('sessionStorage 토큰이 삭제되었습니다.', 'success');
            loadSessionStorageInfo();
            loadCurrentStorageType();
        }
    }

    // Cookie 토큰 삭제
    async function clearCookieTokens() {
        if (confirm('httpOnly Cookie의 모든 JWT 토큰을 삭제하시겠습니까?')) {
            try {
                await axios.post('/jwt/auth/clear-cookie-tokens');
                localStorage.removeItem('storageType');
                sessionStorage.removeItem('storageType');

                showMessage('Cookie 토큰이 삭제되었습니다.', 'success');
                loadCurrentStorageType();
            } catch (error) {
                console.error('Cookie 삭제 실패:', error);
                showMessage('Cookie 삭제에 실패했습니다.', 'error');
            }
        }
    }

    // 모든 토큰 삭제
    async function clearAllTokens() {
        if (confirm('모든 저장소의 JWT 토큰을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.')) {
            try {
                // localStorage 삭제
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('storageType');

                // sessionStorage 삭제
                sessionStorage.removeItem('accessToken');
                sessionStorage.removeItem('refreshToken');
                sessionStorage.removeItem('storageType');

                // Cookie 삭제
                await axios.post('/jwt/auth/clear-cookie-tokens');

                showMessage('모든 토큰이 삭제되었습니다.', 'success');
                loadAllTokenInfo();

            } catch (error) {
                console.error('토큰 삭제 실패:', error);
                showMessage('일부 토큰 삭제에 실패했습니다.', 'error');
            }
        }
    }

    // Cookie 토큰 테스트
    async function testCookieTokens() {
        try {
            showMessage('Cookie 토큰을 테스트하는 중...', 'info');

            const response = await axios.get('/jwt/posts');

            if (response.data.success) {
                showMessage('Cookie 토큰이 유효합니다!', 'success');
            } else {
                showMessage('Cookie 토큰이 유효하지 않습니다.', 'error');
            }
        } catch (error) {
            if (error.response?.status === 401) {
                showMessage('Cookie 토큰이 만료되었거나 유효하지 않습니다.', 'error');
            } else {
                showMessage('Cookie 토큰 테스트에 실패했습니다.', 'error');
            }
        }
    }

    // 브라우저 정보 로드
    function loadBrowserInfo() {
        if (!browserInfo) return;

        const info = {
            userAgent: navigator.userAgent,
            cookieEnabled: navigator.cookieEnabled,
            language: navigator.language,
            platform: navigator.platform
        };

        browserInfo.innerHTML = `
            <p><strong>쿠키 지원:</strong> ${info.cookieEnabled ? '✅' : '❌'}</p>
            <p><strong>언어:</strong> ${info.language}</p>
            <p><strong>플랫폼:</strong> ${info.platform}</p>
        `;
    }

    // 저장소 지원 여부 확인
    function loadStorageSupport() {
        if (!storageSupport) return;

        const support = {
            localStorage: checkStorageSupport('localStorage'),
            sessionStorage: checkStorageSupport('sessionStorage'),
            cookies: navigator.cookieEnabled
        };

        storageSupport.innerHTML = `
            <p><strong>localStorage:</strong> ${support.localStorage ? '✅' : '❌'}</p>
            <p><strong>sessionStorage:</strong> ${support.sessionStorage ? '✅' : '❌'}</p>
            <p><strong>Cookies:</strong> ${support.cookies ? '✅' : '❌'}</p>
        `;
    }

    // 저장소 지원 확인 함수
    function checkStorageSupport(type) {
        try {
            const storage = window[type];
            const testKey = '__test__';
            storage.setItem(testKey, 'test');
            storage.removeItem(testKey);
            return true;
        } catch (e) {
            return false;
        }
    }
});