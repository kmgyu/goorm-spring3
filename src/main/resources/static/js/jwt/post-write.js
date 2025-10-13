// JWT 글쓰기 페이지 JavaScript
document.addEventListener('DOMContentLoaded', async function() {
    const tokenManager = window.TokenManager;

    // 로그인 상태 확인
    const isAuthenticated = await checkAuthenticationStatus();
    if (!isAuthenticated) {
        return; // 인증 실패 시 여기서 종료
    }

    // 페이지 요소들
    const writeForm = document.getElementById('writeForm');
    const submitBtn = document.getElementById('submitBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const headerLogoutBtn = document.getElementById('headerLogoutBtn');
    const checkTokenBtn = document.getElementById('checkTokenBtn');
    const forceExpireBtn = document.getElementById('forceExpireBtn');
    const titleInput = document.getElementById('title');
    const contentTextarea = document.getElementById('content');

    // 이벤트 리스너 등록
    if (writeForm) {
        writeForm.addEventListener('submit', handleSubmit);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            if (confirm('작성 중인 내용이 있습니다. 정말 나가시겠습니까?')) {
                window.location.href = '/pages/jwt/list';
            }
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    if (headerLogoutBtn) {
        headerLogoutBtn.addEventListener('click', logout);
        headerLogoutBtn.classList.remove('hidden');
    }

    if (checkTokenBtn) {
        checkTokenBtn.addEventListener('click', checkTokenManually);
    }

    if (forceExpireBtn) {
        forceExpireBtn.addEventListener('click', simulateTokenExpiry);
    }

    // 입력 값 변경 시 자동 저장 (선택사항)
    if (titleInput) {
        titleInput.addEventListener('input', autoSave);
    }

    if (contentTextarea) {
        contentTextarea.addEventListener('input', autoSave);
    }

    // 초기 로드
    updateTokenStatus();
    updateStorageTypeDisplay();
    loadDraftContent();

    // 폼 제출 처리
    async function handleSubmit(event) {
        event.preventDefault();

        const title = titleInput?.value?.trim();
        const content = contentTextarea?.value?.trim();

        if (!title) {
            showMessage('제목을 입력해주세요.', 'error');
            titleInput?.focus();
            return;
        }

        if (!content) {
            showMessage('내용을 입력해주세요.', 'error');
            contentTextarea?.focus();
            return;
        }

        // 제출 버튼 비활성화
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '작성 중...';
        }

        try {
            const response = await axios.post('/jwt/posts', {
                title: title,
                content: content
            });

            if (response.data.success) {
                showMessage('게시글이 성공적으로 작성되었습니다!', 'success');

                // 임시 저장 데이터 삭제
                clearDraftContent();

                // 2초 후 목록 페이지로 이동
                setTimeout(() => {
                    window.location.href = '/pages/jwt/list';
                }, 2000);

            } else {
                throw new Error(response.data.message || '게시글 작성 실패');
            }

        } catch (error) {
            console.error('게시글 작성 실패:', error);

            let errorMessage = '게시글 작성에 실패했습니다.';
            if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = error.message;
            }

            showMessage(errorMessage, 'error');

            // 401 에러인 경우 토큰 상태 재확인
            if (error.response?.status === 401) {
                updateTokenStatus();
                showMessage('토큰이 만료되었습니다. 자동으로 갱신을 시도합니다.', 'warning');
            }

        } finally {
            // 제출 버튼 복원
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '게시글 작성';
            }
        }
    }

    // 수동 토큰 확인
    async function checkTokenManually() {
        const currentToken = tokenManager.getAccessToken();
        const storageType = tokenManager.getStorageType();

        if (storageType === 'cookie') {
            showMessage('쿠키 방식에서는 JavaScript로 토큰을 직접 확인할 수 없습니다.', 'info');
            return;
        }

        if (!currentToken) {
            showMessage('토큰이 없습니다. 다시 로그인해주세요.', 'error');
            return;
        }

        const isValid = tokenManager.validateToken(currentToken);
        const userInfo = tokenManager.getUserFromToken(currentToken);
        const expiry = tokenManager.getTokenExpiry(currentToken);

        if (isValid) {
            showMessage(`토큰이 유효합니다. 만료시간: ${expiry.toLocaleString()}`, 'success');
        } else {
            showMessage('토큰이 만료되었습니다. 자동 갱신을 시도합니다.', 'warning');
            await window.manualRefreshToken();
        }

        updateTokenStatus();
    }

    // 토큰 만료 시뮬레이션
    function simulateTokenExpiry() {
        const storageType = tokenManager.getStorageType();

        if (storageType === 'cookie') {
            showMessage('쿠키 방식에서는 토큰 만료 시뮬레이션을 할 수 없습니다.', 'info');
            return;
        }

        // 만료된 토큰으로 교체 (임의의 만료된 JWT)
        const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid';

        if (storageType === 'localStorage') {
            localStorage.setItem('accessToken', expiredToken);
        } else if (storageType === 'sessionStorage') {
            sessionStorage.setItem('accessToken', expiredToken);
        }

        updateTokenStatus();
        showMessage('토큰을 만료 상태로 변경했습니다. 다음 API 호출 시 자동 갱신됩니다.', 'warning');
    }

    // 자동 저장 기능
    function autoSave() {
        const title = titleInput?.value || '';
        const content = contentTextarea?.value || '';

        if (title || content) {
            const draftData = {
                title: title,
                content: content,
                timestamp: new Date().toISOString()
            };

            localStorage.setItem('jwt_post_draft', JSON.stringify(draftData));
        }
    }

    // 임시 저장 내용 로드
    function loadDraftContent() {
        try {
            const draftData = localStorage.getItem('jwt_post_draft');
            if (draftData) {
                const draft = JSON.parse(draftData);
                const draftTime = new Date(draft.timestamp);
                const now = new Date();
                const diffHours = (now - draftTime) / (1000 * 60 * 60);

                // 24시간 이내의 임시 저장 내용만 복원
                if (diffHours < 24) {
                    if (titleInput && draft.title) {
                        titleInput.value = draft.title;
                    }
                    if (contentTextarea && draft.content) {
                        contentTextarea.value = draft.content;
                    }

                    showMessage(`임시 저장된 내용을 불러왔습니다. (${formatTimeDiff(diffHours)} 전)`, 'info');
                } else {
                    // 24시간이 지난 임시 저장 데이터 삭제
                    localStorage.removeItem('jwt_post_draft');
                }
            }
        } catch (error) {
            console.error('임시 저장 내용 로드 실패:', error);
        }
    }

    // 임시 저장 내용 삭제
    function clearDraftContent() {
        localStorage.removeItem('jwt_post_draft');
    }

    // 시간 차이 포맷팅
    function formatTimeDiff(hours) {
        if (hours < 1) {
            return Math.round(hours * 60) + '분';
        } else {
            return Math.round(hours) + '시간';
        }
    }

    // 페이지 언로드 시 임시 저장
    window.addEventListener('beforeunload', function(event) {
        const title = titleInput?.value?.trim() || '';
        const content = contentTextarea?.value?.trim() || '';

        if (title || content) {
            autoSave();
        }
    });

    // 주기적으로 토큰 상태 업데이트 (30초마다)
    setInterval(updateTokenStatus, 30000);

    // 인증 상태 확인 함수
    async function checkAuthenticationStatus() {
        const storageType = tokenManager.getStorageType();

        if (storageType === 'cookie') {
            // Cookie 방식: API 호출로 인증 확인
            try {
                const response = await axios.get('/jwt/auth/me');
                return response.data.success;
            } catch (error) {
                showMessage('로그인이 필요합니다.', 'error');
                setTimeout(() => {
                    window.location.href = '/pages/jwt/auth/login';
                }, 2000);
                return false;
            }
        } else if (storageType && tokenManager.isLoggedIn()) {
            // localStorage/sessionStorage: 토큰 유효성 확인
            return true;
        } else {
            // 토큰이 없거나 유효하지 않음
            showMessage('로그인이 필요합니다.', 'error');
            setTimeout(() => {
                window.location.href = '/pages/jwt/auth/login';
            }, 2000);
            return false;
        }
    }
});