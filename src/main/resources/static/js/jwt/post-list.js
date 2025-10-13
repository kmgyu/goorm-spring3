// JWT 게시글 목록 페이지 JavaScript
document.addEventListener('DOMContentLoaded', async function() {
    const tokenManager = window.TokenManager;

    // 로그인 상태 확인
    const isAuthenticated = await checkAuthenticationStatus();
    if (!isAuthenticated) {
        return; // 인증 실패 시 여기서 종료
    }

    // 페이지 요소들
    const writeBtn = document.getElementById('writeBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const headerLogoutBtn = document.getElementById('headerLogoutBtn');
    const refreshTokenBtn = document.getElementById('refreshTokenBtn');
    const postList = document.getElementById('postList');

    // 이벤트 리스너 등록
    if (writeBtn) {
        writeBtn.addEventListener('click', () => {
            window.location.href = '/pages/jwt/write';
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    if (headerLogoutBtn) {
        headerLogoutBtn.addEventListener('click', logout);
        headerLogoutBtn.classList.remove('hidden');
    }

    if (refreshTokenBtn) {
        refreshTokenBtn.addEventListener('click', async () => {
            const success = await window.manualRefreshToken();
            if (success) {
                loadPosts(); // 토큰 갱신 후 게시글 다시 로드
            }
        });
    }

    // 초기 로드
    updateTokenStatus();
    updateStorageTypeDisplay();
    loadPosts();

    // 게시글 목록 로드
    async function loadPosts() {
        try {
            showLoading(true);

            const response = await axios.get('/jwt/posts');

            if (response.data.success) {
                displayPosts(response.data.data);
            } else {
                throw new Error(response.data.message || '게시글 로드 실패');
            }

        } catch (error) {
            console.error('게시글 로드 실패:', error);

            if (error.response?.status === 401) {
                showMessage('인증이 만료되었습니다. 다시 로그인해주세요.', 'error');
                setTimeout(() => {
                    window.location.href = '/pages/jwt/auth/login';
                }, 2000);
            } else {
                showMessage('게시글을 불러오는데 실패했습니다.', 'error');
                displayEmptyState();
            }
        } finally {
            showLoading(false);
        }
    }

    // 게시글 목록 표시
    function displayPosts(posts) {
        if (!postList) return;

        if (!posts || posts.length === 0) {
            displayEmptyState();
            return;
        }

        const postsHtml = posts.map(post => `
            <div class="post-item p-6 hover:bg-gray-50 transition-colors duration-200">
                <div class="flex justify-between items-start">
                    <div class="flex-1">
                        <h3 class="text-lg font-semibold text-gray-900 mb-2">
                            ${escapeHtml(post.title)}
                        </h3>
                        <p class="text-gray-600 mb-4 line-clamp-3">
                            ${escapeHtml(post.content)}
                        </p>
                        <div class="flex items-center text-sm text-gray-500 space-x-4">
                            <span class="flex items-center">
                                <svg class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
                                </svg>
                                ${escapeHtml(post.author?.name || post.author?.email || '익명')}
                            </span>
                            <span class="flex items-center">
                                <svg class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                    <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"></path>
                                </svg>
                                ${formatDate(post.createdAt)}
                            </span>
                            <span class="flex items-center">
                                <svg class="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"></path>
                                    <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"></path>
                                </svg>
                                ${post.views || 0}
                            </span>
                        </div>
                    </div>
                    <div class="ml-4 flex-shrink-0">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            JWT
                        </span>
                    </div>
                </div>
            </div>
        `).join('');

        postList.innerHTML = postsHtml;
    }

    // 빈 상태 표시
    function displayEmptyState() {
        if (!postList) return;

        postList.innerHTML = `
            <div class="empty-state text-center py-12">
                <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <h3 class="mt-2 text-sm font-medium text-gray-900">게시글이 없습니다</h3>
                <p class="mt-1 text-sm text-gray-500">첫 번째 게시글을 작성해보세요.</p>
                <div class="mt-6">
                    <button onclick="window.location.href='/pages/jwt/write'" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">
                        <svg class="-ml-1 mr-2 h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
                        </svg>
                        글쓰기
                    </button>
                </div>
            </div>
        `;
    }

    // 로딩 상태 표시
    function showLoading(show) {
        if (!postList) return;

        if (show) {
            postList.innerHTML = `
                <div class="loading-state text-center py-12">
                    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                    <p class="mt-4 text-sm text-gray-500">게시글을 불러오는 중...</p>
                </div>
            `;
        }
    }

    // HTML 이스케이프
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 날짜 포맷팅
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 1) {
            return '오늘';
        } else if (diffDays === 2) {
            return '어제';
        } else if (diffDays <= 7) {
            return `${diffDays - 1}일 전`;
        } else {
            return date.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        }
    }

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