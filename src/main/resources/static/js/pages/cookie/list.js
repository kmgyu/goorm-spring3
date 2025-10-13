// httpOnly Cookie 게시글 목록 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // 페이지 요소들
    const refreshTokenBtn = document.getElementById('refreshTokenBtn');
    const clearTokensBtn = document.getElementById('clearTokensBtn');
    const postList = document.getElementById('postList');

    // 이벤트 리스너 등록
    if (refreshTokenBtn) {
        refreshTokenBtn.addEventListener('click', refreshTokenManually);
    }

    if (clearTokensBtn) {
        clearTokensBtn.addEventListener('click', clearAllTokens);
    }

    // 초기 로드
    loadPosts();
    displayCookieInfo();

    // 게시글 목록 로드
    async function loadPosts() {
        try {
            showLoading(true);

            console.log('API 호출 시작: GET /jwt/posts');
            const response = await fetch('/jwt/posts', {
                credentials: 'include' // httpOnly Cookie 전송을 위해 필수
            });

            const data = await response.json();
            console.log('API 응답:', response.status, data);

            if (data.success) {
                displayPosts(data.data);
            } else {
                throw new Error(data.message || '게시글 로드 실패');
            }

        } catch (error) {
            console.error('게시글 로드 실패:', error);
            showMessage('게시글을 불러오는데 실패했습니다: ' + error.message, 'error');
            displayEmptyState();
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
                        </div>
                    </div>
                    <div class="ml-4 flex-shrink-0">
                        <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            httpOnly Cookie JWT
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
                <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <span class="text-3xl">🍪</span>
                </div>
                <h3 class="text-lg font-medium text-gray-900 mb-2">게시글이 없습니다</h3>
                <p class="text-gray-500 mb-6">httpOnly Cookie 방식으로 첫 번째 게시글을 작성해보세요.</p>
                <button onclick="window.location.href='/pages/cookie/write'"
                        class="bg-green-500 hover:bg-green-700 text-white px-4 py-2 rounded-md transition duration-200">
                    글쓰기
                </button>
            </div>
        `;
    }

    // 로딩 상태 표시
    function showLoading(show) {
        if (!postList) return;

        if (show) {
            postList.innerHTML = `
                <div class="loading-state text-center py-12">
                    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto mb-4"></div>
                    <p class="text-gray-500">httpOnly Cookie를 확인하고 게시글을 불러오는 중...</p>
                </div>
            `;
        }
    }

    // 수동 토큰 갱신
    async function refreshTokenManually() {
        try {
            refreshTokenBtn.disabled = true;
            refreshTokenBtn.textContent = '갱신 중...';

            const response = await fetch('/jwt/auth/refresh-cookie', {
                method: 'POST',
                credentials: 'include' // httpOnly Cookie 전송을 위해 필수
            });

            const data = await response.json();

            if (data.success) {
                updateTokenStatus();
                displayCookieInfo();
                showMessage('토큰이 성공적으로 갱신되었습니다!', 'success');

                // 게시글 다시 로드
                loadPosts();
            } else {
                throw new Error(data.message || '토큰 갱신 실패');
            }

        } catch (error) {
            console.error('토큰 갱신 실패:', error);
            showMessage('토큰 갱신에 실패했습니다: ' + error.message, 'error');
        } finally {
            refreshTokenBtn.disabled = false;
            refreshTokenBtn.textContent = '토큰 갱신';
        }
    }

    // 모든 토큰 삭제
    async function clearAllTokens() {
        if (confirm('httpOnly Cookie에서 모든 토큰을 삭제하시겠습니까?\\n삭제 후 로그인 페이지로 이동합니다.')) {
            try {
                await removeTokens();
                showMessage('httpOnly Cookie에서 모든 토큰이 삭제되었습니다.', 'success');

                setTimeout(() => {
                    window.location.href = '/pages/auth/login';
                }, 1500);
            } catch (error) {
                showMessage('토큰 삭제에 실패했습니다.', 'error');
            }
        }
    }

    // 쿠키 정보 표시
    async function displayCookieInfo() {
        const accessTokenInfo = document.getElementById('accessTokenInfo');
        const refreshTokenInfo = document.getElementById('refreshTokenInfo');

        // httpOnly Cookie는 JavaScript에서 직접 접근할 수 없으므로 서버에서 정보를 가져와야 함
        try {
            const response = await fetch('/jwt/auth/cookie-info', {
                credentials: 'include' // httpOnly Cookie 전송을 위해 필수
            });

            const data = await response.json();

            if (data.success) {
                const cookieInfo = data.data;

                // Access Token Cookie 정보
                if (accessTokenInfo) {
                    if (cookieInfo.accessToken) {
                        accessTokenInfo.innerHTML = `
                            <p><strong>상태:</strong> <span class="text-green-600">유효 (서버 확인)</span></p>
                            <p><strong>사용자:</strong> ${cookieInfo.accessToken.user || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${cookieInfo.accessToken.expiry || '알 수 없음'}</p>
                            <p><strong>httpOnly:</strong> <span class="text-green-600">Yes</span></p>
                        `;
                    } else {
                        accessTokenInfo.innerHTML = '<p class="text-gray-500">쿠키가 없습니다</p>';
                    }
                }

                // Refresh Token Cookie 정보
                if (refreshTokenInfo) {
                    if (cookieInfo.refreshToken) {
                        refreshTokenInfo.innerHTML = `
                            <p><strong>상태:</strong> <span class="text-green-600">유효 (서버 확인)</span></p>
                            <p><strong>사용자:</strong> ${cookieInfo.refreshToken.user || '알 수 없음'}</p>
                            <p><strong>만료시간:</strong> ${cookieInfo.refreshToken.expiry || '알 수 없음'}</p>
                            <p><strong>httpOnly:</strong> <span class="text-green-600">Yes</span></p>
                        `;
                    } else {
                        refreshTokenInfo.innerHTML = '<p class="text-gray-500">쿠키가 없습니다</p>';
                    }
                }
            }
        } catch (error) {
            console.error('쿠키 정보 조회 실패:', error);

            // 오류 시 기본 정보 표시
            if (accessTokenInfo) {
                accessTokenInfo.innerHTML = `
                    <p class="text-gray-500">JavaScript에서 httpOnly Cookie에 접근할 수 없습니다</p>
                    <p class="text-sm text-gray-400">브라우저 개발자 도구 > Application > Cookies에서 확인 가능</p>
                `;
            }

            if (refreshTokenInfo) {
                refreshTokenInfo.innerHTML = `
                    <p class="text-gray-500">JavaScript에서 httpOnly Cookie에 접근할 수 없습니다</p>
                    <p class="text-sm text-gray-400">브라우저 개발자 도구 > Application > Cookies에서 확인 가능</p>
                `;
            }
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
        return date.toLocaleDateString('ko-KR') + ' ' + date.toLocaleTimeString('ko-KR');
    }
});