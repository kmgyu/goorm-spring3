// UI 공통 함수들

// 메시지 표시 함수
function showMessage(type, message) {
    const container = document.getElementById('messageContainer');
    if (!container) return;

    // 기존 메시지 제거
    container.innerHTML = '';

    const messageDiv = document.createElement('div');

    if (type === 'success') {
        messageDiv.className = 'bg-green-50 border border-green-200 rounded-lg p-4 flex items-center space-x-3 message';
        messageDiv.innerHTML = `
            <svg class="w-5 h-5 text-green-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span class="text-green-800 font-medium">${message}</span>
        `;
    } else if (type === 'error') {
        messageDiv.className = 'bg-red-50 border border-red-200 rounded-lg p-4 flex items-center space-x-3 message';
        messageDiv.innerHTML = `
            <svg class="w-5 h-5 text-red-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
            <span class="text-red-800 font-medium">${message}</span>
        `;
    }

    container.appendChild(messageDiv);

    // 3초 후 자동 숨김
    setTimeout(() => {
        messageDiv.classList.add('fade-out');
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}

// 성공 메시지 표시
function showSuccessMessage(message) {
    showMessage('success', message);
}

// 에러 메시지 표시
function showErrorMessage(message) {
    showMessage('error', message);
}

// 폼 필드 에러 표시
function highlightFieldErrors(fieldErrors) {
    // 기존 에러 표시 제거
    document.querySelectorAll('.field-error').forEach(el => el.remove());
    document.querySelectorAll('.border-red-500').forEach(el => {
        el.classList.remove('border-red-500');
        el.classList.add('border-gray-300');
    });

    // 새 에러 표시
    for (const [field, message] of Object.entries(fieldErrors)) {
        const element = document.querySelector(`[name="${field}"]`);
        if (element) {
            // 필드 테두리 변경
            element.classList.remove('border-gray-300');
            element.classList.add('border-red-500');

            // 에러 메시지 추가
            const errorDiv = document.createElement('div');
            errorDiv.className = 'field-error text-red-600 text-sm mt-1';
            errorDiv.textContent = message;
            element.parentNode.appendChild(errorDiv);
        }
    }
}

// 로딩 상태 표시
function showLoading(element, text = '처리 중...') {
    if (element) {
        element.disabled = true;
        element.textContent = text;
        element.classList.add('opacity-50', 'cursor-not-allowed');
    }
}

// 로딩 상태 해제
function hideLoading(element, originalText) {
    if (element) {
        element.disabled = false;
        element.textContent = originalText;
        element.classList.remove('opacity-50', 'cursor-not-allowed');
    }
}

// 날짜 포맷 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 사용자 정보 업데이트
function updateUserInfo(user) {
    const userGreeting = document.getElementById('userGreeting');
    const anonymousMenu = document.getElementById('anonymousMenu');
    const authenticatedMenu = document.getElementById('authenticatedMenu');

    if (user) {
        userGreeting.textContent = `${user.username}님`;
        anonymousMenu.classList.add('hidden');
        authenticatedMenu.classList.remove('hidden');
    } else {
        anonymousMenu.classList.remove('hidden');
        authenticatedMenu.classList.add('hidden');
    }
}