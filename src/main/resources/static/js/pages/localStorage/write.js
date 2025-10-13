// localStorage 글쓰기 페이지 JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // 페이지 요소들
    const writeForm = document.getElementById('writeForm');
    const titleInput = document.getElementById('title');
    const contentTextarea = document.getElementById('content');
    const submitBtn = document.getElementById('submitBtn');
    const testTokenBtn = document.getElementById('testTokenBtn');

    // 이벤트 리스너 등록
    if (writeForm) {
        writeForm.addEventListener('submit', handleSubmit);
    }

    if (testTokenBtn) {
        testTokenBtn.addEventListener('click', testTokenExpiry);
    }

    // 자동 저장 기능 (localStorage 특징 체험)
    setupAutoSave();

    // 자동 저장된 내용 복원
    restoreAutoSavedContent();

    // 폼 제출 처리
    async function handleSubmit(event) {
        event.preventDefault();

        const title = titleInput.value.trim();
        const content = contentTextarea.value.trim();

        if (!title || !content) {
            showMessage('제목과 내용을 모두 입력해주세요.', 'error');
            return;
        }

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = '작성 중...';

            console.log('API 호출 시작: POST /jwt/posts');
            const response = await apiCall('/jwt/posts', {
                method: 'POST',
                body: JSON.stringify({
                    title: title,
                    content: content
                })
            });

            if (!response) return; // 토큰 갱신 실패로 리다이렉트된 경우

            const data = await response.json();
            console.log('API 응답:', response.status, data);

            if (data.success) {
                showMessage('게시글이 성공적으로 작성되었습니다!', 'success');

                // 자동 저장된 내용 삭제
                clearAutoSavedContent();

                // 목록 페이지로 이동
                setTimeout(() => {
                    window.location.href = '/pages/localStorage/list';
                }, 1500);
            } else {
                throw new Error(data.message || '게시글 작성 실패');
            }

        } catch (error) {
            console.error('게시글 작성 실패:', error);
            showMessage('게시글 작성에 실패했습니다: ' + error.message, 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '게시글 작성';
        }
    }

    // 토큰 만료 테스트
    async function testTokenExpiry() {
        const accessToken = getAccessToken();
        if (!accessToken) {
            showMessage('localStorage에 토큰이 없습니다.', 'error');
            return;
        }

        try {
            testTokenBtn.disabled = true;
            testTokenBtn.textContent = '테스트 중...';

            // 현재 토큰을 만료된 것으로 변경
            const expiredToken = createExpiredToken(accessToken);
            saveAccessToken(expiredToken);

            showMessage('토큰을 인위적으로 만료시켰습니다. 다시 게시글을 작성해보세요!', 'warning');
            updateTokenStatus();

        } catch (error) {
            console.error('토큰 만료 테스트 실패:', error);
            showMessage('토큰 만료 테스트에 실패했습니다.', 'error');
        } finally {
            testTokenBtn.disabled = false;
            testTokenBtn.textContent = '토큰 만료 테스트';
        }
    }

    // 만료된 토큰 생성 (교육용)
    function createExpiredToken(originalToken) {
        try {
            const parts = originalToken.split('.');
            const payload = JSON.parse(atob(parts[1]));

            // 만료 시간을 현재 시간보다 이전으로 설정
            payload.exp = Math.floor(Date.now() / 1000) - 3600; // 1시간 전으로 설정

            // 새로운 payload를 base64로 인코딩
            const newPayload = btoa(JSON.stringify(payload));

            return parts[0] + '.' + newPayload + '.' + parts[2];
        } catch (error) {
            console.error('만료된 토큰 생성 실패:', error);
            return originalToken;
        }
    }

    // 자동 저장 기능 설정
    function setupAutoSave() {
        let autoSaveTimer;

        function autoSave() {
            const title = titleInput.value.trim();
            const content = contentTextarea.value.trim();

            if (title || content) {
                localStorage.setItem('localStorage_draft_title', title);
                localStorage.setItem('localStorage_draft_content', content);
                console.log('localStorage에 임시 저장 완료');
            }
        }

        // 입력할 때마다 자동 저장 (디바운스 적용)
        function handleInput() {
            clearTimeout(autoSaveTimer);
            autoSaveTimer = setTimeout(autoSave, 2000); // 2초 후 저장
        }

        titleInput.addEventListener('input', handleInput);
        contentTextarea.addEventListener('input', handleInput);
    }

    // 자동 저장된 내용 복원
    function restoreAutoSavedContent() {
        const savedTitle = localStorage.getItem('localStorage_draft_title');
        const savedContent = localStorage.getItem('localStorage_draft_content');

        if (savedTitle) {
            titleInput.value = savedTitle;
        }

        if (savedContent) {
            contentTextarea.value = savedContent;
        }

        if (savedTitle || savedContent) {
            showMessage('localStorage에서 임시 저장된 내용을 복원했습니다.', 'info');
        }
    }

    // 자동 저장된 내용 삭제
    function clearAutoSavedContent() {
        localStorage.removeItem('localStorage_draft_title');
        localStorage.removeItem('localStorage_draft_content');
        console.log('localStorage에서 임시 저장 내용 삭제 완료');
    }
});