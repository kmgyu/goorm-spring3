// Fetch 로그인 기능

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

// 로그인 처리 함수
async function handleLogin(e) {
    e.preventDefault();

    const submitButton = e.target.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;

    // 로딩 상태 표시
    showLoading(submitButton, '로그인 중...');

    const formData = new FormData(e.target);
    const loginData = {
        email: formData.get('email'),
        password: formData.get('password')
    };

    try {
        const response = await apiCall.post('/auth/login', loginData);

        if (response.success) {
            showSuccessMessage(response.message);

            // 게시판 목록으로 이동
            setTimeout(() => {
                window.location.href = '/fetch/posts';
            }, 1000);
        } else {
            showErrorMessage(response.error.message);
        }
    } catch (error) {
        showErrorMessage('로그인 중 오류가 발생했습니다.');
    } finally {
        // 로딩 상태 해제
        hideLoading(submitButton, originalText);
    }
}

// 로그아웃 처리 함수
async function logout() {
    try {
        const response = await apiCall.post('/auth/logout');

        if (response.success) {
            showSuccessMessage(response.message);

            setTimeout(() => {
                window.location.href = '/fetch/auth/login';
            }, 1000);
        } else {
            showErrorMessage(response.error.message);
        }

    } catch (error) {
        console.error('Logout error:', error);
        showErrorMessage('로그아웃 중 오류가 발생했습니다.');
    }
}

// 세션 확인 함수
async function checkSession() {
    try {
        const response = await apiCall.get('/auth/check');

        if (response.success) {
            updateUserInfo(response.data.user);
            return response.data.user;
        } else {
            updateUserInfo(null);
            return null;
        }
    } catch (error) {
        updateUserInfo(null);
        return null;
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