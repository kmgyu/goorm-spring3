// Fetch 게시판 기능

document.addEventListener('DOMContentLoaded', function() {
    const path = window.location.pathname;

    if (path.includes('/posts') && !path.includes('/new') && !path.includes('/edit') && !path.match(/\/posts\/\d+$/)) {
        // 목록 페이지
        loadPosts();
        checkUserSession();
    } else if (path.includes('/new')) {
        // 새 글 작성 페이지
        checkUserSession();
        setupFormEvents();
    } else if (path.includes('/edit')) {
        // 수정 페이지
        checkUserSession();
        setupFormEvents();
        loadPostForEdit();
    } else if (path.match(/\/posts\/\d+$/)) {
        // 상세보기 페이지
        checkUserSession();
        loadPostDetail();
    }
});

// 폼 페이지 이벤트 설정
function setupFormEvents() {
    const postForm = document.getElementById('postForm');

    if (postForm) {
        postForm.addEventListener('submit', handleFormSubmit);
    }
}

// 게시글 목록 불러오기
async function loadPosts() {
    try {
        const response = await apiCall.get('/posts');

        if (response.success) {
            renderPosts(response.data || []);
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('게시글 로드 오류:', error);
        showErrorMessage('게시글을 불러오는 중 오류가 발생했습니다.');
    }
}

// 게시글 목록 렌더링
function renderPosts(posts) {
    const postList = document.getElementById('postList');

    if (!postList) return;

    if (posts.length === 0) {
        postList.innerHTML = `
            <tr>
                <td colspan="5" class="px-6 py-4 text-center text-gray-500">
                    등록된 게시글이 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    const postsHTML = posts.map((post, index) => `
        <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${posts.length - index}</td>
            <td class="px-6 py-4 whitespace-nowrap">
                <a href="/fetch/posts/${post.seq}" class="text-sm font-medium text-blue-600 hover:text-blue-800">
                    ${escapeHtml(post.title)}
                </a>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${escapeHtml(post.author?.username || post.author?.name || 'Unknown')}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${formatDate(post.createdAt)}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${post.viewCount || 0}</td>
        </tr>
    `).join('');

    postList.innerHTML = postsHTML;
}

// 수정용 게시글 데이터 로드
async function loadPostForEdit() {
    const seq = getSeqFromUrl();
    if (!seq) return;

    try {
        const response = await apiCall.get(`/posts/${seq}`);

        if (response.success) {
            const post = response.data;

            // 폼에 데이터 채우기
            document.getElementById('title').value = post.title;
            document.getElementById('content').value = post.content;
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('게시글 로드 오류:', error);
        showErrorMessage('게시글 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 폼 제출 처리
async function handleFormSubmit(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const postData = {
        title: formData.get('title'),
        content: formData.get('content')
    };

    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    const isEdit = window.location.pathname.includes('/edit');
    const seq = isEdit ? getSeqFromUrl() : null;

    try {
        showLoading(submitBtn, isEdit ? '수정 중...' : '작성 중...');

        let response;
        if (isEdit) {
            response = await apiCall.put(`/posts/${seq}`, postData);
        } else {
            response = await apiCall.post('/posts', postData);
        }

        if (response.success) {
            showSuccessMessage(response.message);

            // 수정인 경우 상세보기로, 작성인 경우 목록으로
            setTimeout(() => {
                if (isEdit) {
                    window.location.href = `/fetch/posts/${seq}`;
                } else {
                    window.location.href = '/fetch/posts';
                }
            }, 1000);
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('게시글 처리 오류:', error);
        showErrorMessage(isEdit ? '게시글 수정 중 오류가 발생했습니다.' : '게시글 작성 중 오류가 발생했습니다.');
    } finally {
        hideLoading(submitBtn, originalText);
    }
}

// 게시글 상세보기 로드
async function loadPostDetail() {
    const seq = getSeqFromUrl();
    if (!seq) return;

    try {
        const response = await apiCall.get(`/posts/${seq}`);

        if (response.success) {
            renderPostDetail(response.data);
            setupDetailEvents(response.data);
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('게시글 상세 로드 오류:', error);
        showErrorMessage('게시글을 불러오는 중 오류가 발생했습니다.');
    }
}

// 게시글 상세 렌더링
function renderPostDetail(post) {
    const container = document.getElementById('postDetail');
    if (!container) return;

    container.innerHTML = `
        <div class="bg-white shadow rounded-lg p-6">
            <div class="border-b pb-4 mb-4">
                <h1 class="text-2xl font-bold text-gray-900 mb-2">${escapeHtml(post.title)}</h1>
                <div class="flex items-center text-sm text-gray-500 space-x-4">
                    <span>작성자: ${escapeHtml(post.author?.username || 'Unknown')}</span>
                    <span>작성일: ${formatDate(post.createdAt)}</span>
                    <span>조회수: ${post.viewCount || 0}</span>
                </div>
            </div>
            <div class="prose max-w-none">
                <div class="whitespace-pre-wrap text-gray-800">${escapeHtml(post.content)}</div>
            </div>
        </div>
    `;
}

// 상세보기 이벤트 설정
function setupDetailEvents(post) {
    // 작성자 권한 체크 및 버튼 표시
    checkAuthorAndShowButtons(post);

    // 삭제 버튼 이벤트
    const deleteBtn = document.getElementById('deleteBtn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', () => deletePost(post.seq));
    }

    // 수정 버튼 링크 설정
    const editBtn = document.getElementById('editBtn');
    if (editBtn) {
        editBtn.href = `/fetch/posts/${post.seq}/edit`;
    }
}

// 작성자 확인 후 버튼 표시
async function checkAuthorAndShowButtons(post) {
    try {
        const response = await apiCall.get('/auth/check');

        if (response.success && response.data.user) {
            const currentUser = response.data.user;
            const authorButtons = document.getElementById('authorButtons');

            // 작성자이거나 관리자인 경우 버튼 표시
            if (authorButtons && (currentUser.id === post.author?.id || currentUser.role === 'ADMIN')) {
                authorButtons.classList.remove('hidden');
            }
        }
    } catch (error) {
        console.log('권한 체크 중 오류:', error);
    }
}

// URL에서 seq 추출
function getSeqFromUrl() {
    const matches = window.location.pathname.match(/\/posts\/(\d+)/);
    return matches ? parseInt(matches[1]) : null;
}

// 게시글 삭제
async function deletePost(seq) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        const response = await apiCall.delete(`/posts/${seq}`);

        if (response.success) {
            showSuccessMessage(response.message);

            setTimeout(() => {
                window.location.href = '/fetch/posts';
            }, 1000);
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('게시글 삭제 오류:', error);
        showErrorMessage('게시글 삭제 중 오류가 발생했습니다.');
    }
}

// 사용자 세션 확인
async function checkUserSession() {
    try {
        const response = await apiCall.get('/auth/check');

        if (response.success) {
            updateUserInfo(response.data.user);
        } else {
            // 인증되지 않은 경우 로그인 페이지로 리다이렉트
            window.location.href = '/fetch/auth/login';
        }

    } catch (error) {
        // 오류 발생 시에도 로그인 페이지로 리다이렉트
        window.location.href = '/fetch/auth/login';
    }
}

// 사용자 정보 UI 업데이트
function updateUserInfo(user) {
    const userGreeting = document.getElementById('userGreeting');
    const anonymousMenu = document.getElementById('anonymousMenu');
    const authenticatedMenu = document.getElementById('authenticatedMenu');

    if (userGreeting && user) {
        userGreeting.textContent = `${user.username}님`;
    }

    if (anonymousMenu && authenticatedMenu) {
        if (user) {
            anonymousMenu.classList.add('hidden');
            authenticatedMenu.classList.remove('hidden');
        } else {
            anonymousMenu.classList.remove('hidden');
            authenticatedMenu.classList.add('hidden');
        }
    }
}

// 유틸리티 함수들
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}