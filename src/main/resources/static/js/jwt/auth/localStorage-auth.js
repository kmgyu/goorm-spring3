// localStorage 방식 JWT 인증 처리
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        const localStorageBtn = document.getElementById('localStorageBtn');
        const loginForm = document.getElementById('loginForm');
        const loginButton = document.getElementById('loginButton');
        const selectedStorage = document.getElementById('selectedStorage');

        if (!localStorageBtn) return;

        // localStorage 방식 선택
        localStorageBtn.addEventListener('click', function() {
            selectStorageType('localStorage');
            window.TokenManager.setStorageType('localStorage');

            // UI 업데이트
            updateStorageSelection('localStorage');
            updateSelectedStorageText('localStorage - 브라우저 종료해도 유지');
            enableLoginButton();
        });

        // 로그인 폼 제출
        if (loginForm) {
            loginForm.addEventListener('submit', handleLogin);
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
                selectedBtn.classList.add('border-blue-400');
            }
        }

        function updateStorageSelection(type) {
            selectStorageType(type);
        }

        function updateSelectedStorageText(text) {
            if (selectedStorage) {
                selectedStorage.textContent = text;
                selectedStorage.classList.remove('text-blue-600');
                selectedStorage.classList.add('text-blue-600', 'font-bold');
            }
        }

        function enableLoginButton() {
            if (loginButton) {
                loginButton.disabled = false;
                loginButton.className = 'w-full bg-blue-500 hover:bg-blue-700 text-white py-2 px-4 rounded-md transition duration-200';
                loginButton.textContent = 'localStorage로 로그인';
            }
        }

        async function handleLogin(event) {
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

                    // localStorage에 토큰 저장
                    await window.TokenManager.saveTokens(accessToken, refreshToken);

                    showMessage('localStorage로 로그인 성공!', 'success');

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
                loginButton.textContent = 'localStorage로 로그인';
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
                <div class="token-info bg-blue-50 border border-blue-200 rounded-lg p-4 mt-4">
                    <h4 class="font-bold text-blue-800 mb-2">🔐 localStorage 저장 완료</h4>
                    <div class="text-sm text-blue-700 space-y-1">
                        <p><strong>사용자:</strong> ${accessTokenInfo.email}</p>
                        <p><strong>Access Token 만료:</strong> ${accessTokenExpiry.toLocaleString()}</p>
                        <p><strong>Refresh Token 만료:</strong> ${refreshTokenExpiry.toLocaleString()}</p>
                        <p class="text-green-600 font-medium">✅ 브라우저를 닫아도 토큰이 유지됩니다</p>
                        <p class="text-red-600 font-medium">⚠️ XSS 공격에 취약할 수 있습니다</p>
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

        // 페이지 로드 시 이미 localStorage에 토큰이 있는지 확인
        const tokenManager = window.TokenManager;
        if (tokenManager.getStorageType() === 'localStorage' && tokenManager.isLoggedIn()) {
            // 이미 로그인된 상태
            selectStorageType('localStorage');
            updateSelectedStorageText('localStorage - 현재 로그인 상태');

            if (loginButton) {
                loginButton.textContent = '이미 로그인됨 - 게시글 보기';
                loginButton.onclick = () => window.location.href = '/pages/jwt/list';
            }
        }
    });
})();