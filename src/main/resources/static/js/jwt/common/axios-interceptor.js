// Axios 인터셉터 설정 (자동 토큰 갱신)
(function() {
    let isRefreshing = false;
    let failedQueue = [];

    // 대기 중인 요청 처리
    const processQueue = (error, token = null) => {
        failedQueue.forEach(prom => {
            if (error) {
                prom.reject(error);
            } else {
                prom.resolve(token);
            }
        });

        failedQueue = [];
    };

    // 요청 인터셉터: Authorization 헤더 자동 추가
    axios.interceptors.request.use(
        function(config) {
            const tokenManager = window.TokenManager;
            const storageType = tokenManager.getStorageType();

            // Cookie 방식이 아닌 경우에만 Authorization 헤더 추가
            if (storageType !== 'cookie') {
                const accessToken = tokenManager.getAccessToken();
                if (accessToken) {
                    config.headers.Authorization = `Bearer ${accessToken}`;
                }
            }

            return config;
        },
        function(error) {
            return Promise.reject(error);
        }
    );

    // 응답 인터셉터: 401 에러 시 자동 토큰 갱신
    axios.interceptors.response.use(
        function(response) {
            return response;
        },
        async function(error) {
            const originalRequest = error.config;

            // 401 에러이고 아직 재시도하지 않은 경우
            if (error.response?.status === 401 && !originalRequest._retry) {
                const tokenManager = window.TokenManager;
                const storageType = tokenManager.getStorageType();

                // 로그인 페이지나 이미 재시도한 요청은 제외
                if (originalRequest.url?.includes('/jwt/auth/login') ||
                    originalRequest.url?.includes('/jwt/auth/refresh')) {
                    return Promise.reject(error);
                }

                // 이미 토큰 갱신 중인 경우 대기열에 추가
                if (isRefreshing) {
                    return new Promise((resolve, reject) => {
                        failedQueue.push({ resolve, reject });
                    }).then(token => {
                        if (storageType !== 'cookie' && token) {
                            originalRequest.headers.Authorization = `Bearer ${token}`;
                        }
                        return axios(originalRequest);
                    }).catch(err => {
                        return Promise.reject(err);
                    });
                }

                originalRequest._retry = true;
                isRefreshing = true;

                try {
                    // 토큰 갱신 시도
                    const newAccessToken = await refreshToken();

                    if (newAccessToken) {
                        // 대기 중인 요청들 처리
                        processQueue(null, newAccessToken);

                        // 원래 요청 재시도
                        if (storageType !== 'cookie') {
                            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        }
                        return axios(originalRequest);
                    } else {
                        throw new Error('토큰 갱신 실패');
                    }

                } catch (refreshError) {
                    console.error('토큰 갱신 실패:', refreshError);
                    processQueue(refreshError, null);

                    // 토큰 갱신 실패 시 로그아웃 처리
                    await tokenManager.clearTokens();
                    showMessage('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');

                    // 로그인 페이지로 이동 (현재 페이지가 로그인 페이지가 아닌 경우)
                    if (!window.location.pathname.includes('/pages/jwt/auth/login')) {
                        setTimeout(() => {
                            window.location.href = '/pages/jwt/auth/login';
                        }, 2000);
                    }

                    return Promise.reject(refreshError);

                } finally {
                    isRefreshing = false;
                }
            }

            return Promise.reject(error);
        }
    );

    // 토큰 갱신 함수
    async function refreshToken() {
        const tokenManager = window.TokenManager;
        const refreshToken = tokenManager.getRefreshToken();
        const storageType = tokenManager.getStorageType();

        if (!refreshToken && storageType !== 'cookie') {
            throw new Error('Refresh Token이 없습니다.');
        }

        try {
            // 토큰 갱신 상태 표시
            updateTokenStatusToRefreshing();

            const requestData = storageType === 'cookie' ? {} : { refreshToken: refreshToken };

            const response = await axios.post('/jwt/auth/refresh', requestData);

            if (response.data.success) {
                const newAccessToken = response.data.data.accessToken;
                const newRefreshToken = response.data.data.refreshToken;

                // 새 토큰 저장
                await tokenManager.saveTokens(newAccessToken, newRefreshToken);

                // 토큰 상태 업데이트
                updateTokenStatus();

                showMessage('토큰이 자동으로 갱신되었습니다.', 'success');

                return newAccessToken;
            } else {
                throw new Error(response.data.message || '토큰 갱신 실패');
            }

        } catch (error) {
            console.error('토큰 갱신 요청 실패:', error);
            throw error;
        }
    }

    // 토큰 상태를 갱신 중으로 표시
    function updateTokenStatusToRefreshing() {
        const statusElements = document.querySelectorAll('#tokenStatus, #headerTokenStatus');
        statusElements.forEach(element => {
            element.textContent = '갱신중';
            element.className = 'token-status refreshing';
        });
    }

    // 수동 토큰 갱신 함수 (전역으로 노출)
    window.manualRefreshToken = async function() {
        try {
            const newAccessToken = await refreshToken();
            if (newAccessToken) {
                showMessage('토큰이 성공적으로 갱신되었습니다.', 'success');
                return true;
            }
        } catch (error) {
            showMessage('토큰 갱신에 실패했습니다: ' + error.message, 'error');
            return false;
        }
    };

})();