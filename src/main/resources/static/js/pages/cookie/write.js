// httpOnly Cookie 글쓰기 페이지 JavaScript
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
        testTokenBtn.addEventListener('click', checkTokenStatus);
    }

    // httpOnly Cookie 특성 안내
    showCookieFeatureInfo();

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
            const response = await fetch('/jwt/posts', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include', // httpOnly Cookie 전송을 위해 필수
                body: JSON.stringify({
                    title: title,
                    content: content
                })
            });

            const data = await response.json();
            console.log('API 응답:', response.status, data);

            if (data.success) {
                showMessage('게시글이 성공적으로 작성되었습니다!', 'success');

                // 폼 초기화
                titleInput.value = '';
                contentTextarea.value = '';

                // 목록 페이지로 이동
                setTimeout(() => {
                    window.location.href = '/pages/cookie/list';
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

    // 토큰 상태 확인
    async function checkTokenStatus() {
        try {
            testTokenBtn.disabled = true;
            testTokenBtn.textContent = '확인 중...';

            const response = await fetch('/jwt/auth/check', {
                credentials: 'include' // httpOnly Cookie 전송을 위해 필수
            });

            const data = await response.json();

            if (data.success && data.data.valid) {
                showMessage('httpOnly Cookie 토큰이 유효합니다!', 'success');
                updateTokenStatus();
            } else {
                showMessage('httpOnly Cookie 토큰이 만료되었거나 없습니다.', 'warning');
            }

        } catch (error) {
            console.error('토큰 상태 확인 실패:', error);
            showMessage('토큰 상태 확인에 실패했습니다: ' + error.message, 'error');
        } finally {
            testTokenBtn.disabled = false;
            testTokenBtn.textContent = '토큰 상태 확인';
        }
    }

    // httpOnly Cookie 특성 안내 표시
    function showCookieFeatureInfo() {
        // 페이지 로드 시 httpOnly Cookie의 특성을 안내
        setTimeout(() => {
            showMessage('httpOnly Cookie는 JavaScript에서 접근할 수 없어 XSS 공격으로부터 안전합니다.', 'info');
        }, 1000);

        // 개발자 도구 체험 안내
        setTimeout(() => {
            if (console && console.log) {
                console.log('=== httpOnly Cookie 체험 ===');
                console.log('1. document.cookie를 입력해보세요 - JWT 토큰이 보이지 않습니다');
                console.log('2. Application 탭 > Cookies에서만 확인 가능합니다');
                console.log('3. 이것이 XSS 공격을 방어하는 핵심 메커니즘입니다');
                console.log('==========================');
            }
        }, 2000);
    }

    // localStorage/sessionStorage와 달리 자동 저장 기능 없음을 안내
    function showNoAutoSaveInfo() {
        const titleLength = titleInput.value.length;
        const contentLength = contentTextarea.value.length;

        if (titleLength > 50 || contentLength > 100) {
            showMessage('httpOnly Cookie는 자동 저장 기능이 없습니다. 작성 중인 내용을 잃지 않도록 주의하세요.', 'warning');
        }
    }

    // 입력 이벤트에서 자동 저장 불가 안내
    titleInput.addEventListener('input', showNoAutoSaveInfo);
    contentTextarea.addEventListener('input', showNoAutoSaveInfo);

    // 쿠키 접근 시도 데모 (교육용)
    function demonstrateCookieAccess() {
        try {
            console.log('=== httpOnly Cookie 접근 시도 ===');
            console.log('document.cookie 결과:', document.cookie);
            console.log('JWT 토큰이 보이나요? httpOnly 쿠키는 JavaScript에서 접근할 수 없습니다.');
            console.log('Application 탭 > Cookies에서 확인해보세요.');
            console.log('================================');
        } catch (error) {
            console.error('쿠키 접근 오류:', error);
        }
    }

    // 페이지 로드 후 쿠키 접근 데모 실행
    setTimeout(demonstrateCookieAccess, 3000);
});