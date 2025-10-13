// Cookie 방식 JWT 인증 처리
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        const cookieBtn = document.getElementById('cookieBtn');
        const loginForm = document.getElementById('loginForm');
        const loginButton = document.getElementById('loginButton');
        const selectedStorage = document.getElementById('selectedStorage');

        if (!cookieBtn) return;

        // Cookie 방식 선택
        cookieBtn.addEventListener('click', function() {
            selectStorageType('cookie');
            window.TokenManager.setStorageType('cookie');

            // UI 업데이트
            updateStorageSelection('cookie');
            updateSelectedStorageText('httpOnly Cookie - XSS 공격 차단');
            enableLoginButton();
        });

        // 로그인 폼 제출 (이미 다른 파일에서 등록되어 있으므로 중복 등록 방지)
        if (loginForm && !loginForm.hasCookieListener) {
            loginForm.addEventListener('submit', handleLogin);
            loginForm.hasCookieListener = true;
        }

        function selectStorageType(type) {
            // 모든 버튼 초기화
            document.querySelectorAll('.storage-btn').forEach(btn => {
                btn.classList.remove('selected');
                btn.classList.remove('border-blue-400', 'border-green-400', 'border-orange-400');
            });

            // 선택된 버튼 하이라이트
            const selectedBtn = document.getElementById(type + 'Btn');
            if (selectedBtn) {
                selectedBtn.classList.add('selected');
                selectedBtn.classList.add('border-orange-400');
            }
        }

        function updateStorageSelection(type) {
            selectStorageType(type);
        }

        function updateSelectedStorageText(text) {
            if (selectedStorage) {
                selectedStorage.textContent = text;
                selectedStorage.classList.remove('text-blue-600', 'text-green-600');
                selectedStorage.classList.add('text-orange-600', 'font-bold');
            }
        }

        function enableLoginButton() {
            if (loginButton) {
                loginButton.disabled = false;
                loginButton.className = 'w-full bg-orange-500 hover:bg-orange-700 text-white py-2 px-4 rounded-md transition duration-200';
                loginButton.textContent = 'httpOnly Cookie로 로그인';
            }
        }

        async function handleLogin(event) {
            // Cookie가 선택되지 않은 경우 이벤트 처리하지 않음
            if (window.TokenManager.getStorageType() !== 'cookie') {
                return;
            }

            event.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            if (!email || !password) {
                showMessage('이메일과 비밀번호를 입력해주세요.', 'error');
                return;
            }

            // 로그인 버튼 비활성화
            loginButton.disabled = true;
            loginButton.textContent = '로그인 중...';

            try {
                const response = await axios.post('/jwt/auth/login', {
                    email: email,
                    password: password
                });

                if (response.data.success) {
                    const { accessToken, refreshToken } = response.data.data;

                    // httpOnly Cookie에 토큰 저장 (서버에 요청)
                    await window.TokenManager.saveTokens(accessToken, refreshToken);

                    showMessage('httpOnly Cookie로 로그인 성공!', 'success');

                    // 토큰 정보 표시
                    displayTokenInfo(accessToken, refreshToken);

                    // 헤더 네비게이션 업데이트
                    updateHeaderNavigation();

                    // 2초 후 게시글 목록으로 이동
                    setTimeout(() => {
                        window.location.href = '/pages/jwt/list';
                    }, 2000);

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
                // 로그인 버튼 복원
                loginButton.disabled = false;
                loginButton.textContent = 'httpOnly Cookie로 로그인';
            }
        }

        function displayTokenInfo(accessToken, refreshToken) {
            const tokenManager = window.TokenManager;

            // Access Token 정보
            const accessTokenInfo = tokenManager.getUserFromToken(accessToken);
            const accessTokenExpiry = tokenManager.getTokenExpiry(accessToken);

            // Refresh Token 정보
            const refreshTokenExpiry = tokenManager.getTokenExpiry(refreshToken);

            const infoMessage = `
                <div class="token-info bg-orange-50 border border-orange-200 rounded-lg p-4 mt-4">
                    <h4 class="font-bold text-orange-800 mb-2">🍪 httpOnly Cookie 저장 완료</h4>
                    <div class="text-sm text-orange-700 space-y-1">
                        <p><strong>사용자:</strong> ${accessTokenInfo.email}</p>
                        <p><strong>Access Token 만료:</strong> ${accessTokenExpiry.toLocaleString()}</p>
                        <p><strong>Refresh Token 만료:</strong> ${refreshTokenExpiry.toLocaleString()}</p>
                        <p class="text-green-600 font-medium">✅ JavaScript에서 접근 불가 (XSS 차단)</p>
                        <p class="text-red-600 font-medium">⚠️ CSRF 공격에 취약할 수 있습니다</p>
                        <p class="text-blue-600 font-medium">ℹ️ 서버에서 자동으로 쿠키를 포함하여 전송</p>
                    </div>
                </div>
            `;

            const messageContainer = document.getElementById('messageContainer');
            if (messageContainer) {
                const infoDiv = document.createElement('div');
                infoDiv.innerHTML = infoMessage;
                messageContainer.appendChild(infoDiv);
            }
        }

        // 페이지 로드 시 이미 Cookie에 토큰이 있는지 확인
        // (httpOnly Cookie는 JavaScript에서 직접 확인할 수 없으므로 서버 검증이 필요)
        const tokenManager = window.TokenManager;
        if (tokenManager.getStorageType() === 'cookie') {
            // Cookie 방식이 설정되어 있으면 로그인 상태로 간주
            selectStorageType('cookie');
            updateSelectedStorageText('httpOnly Cookie - 현재 로그인 상태 (추정)');

            if (loginButton) {
                loginButton.textContent = '쿠키 상태 확인 - 게시글 보기';
                loginButton.onclick = () => window.location.href = '/pages/jwt/list';
            }
        }
    });
})();