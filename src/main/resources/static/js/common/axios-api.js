// Axios API 공통 모듈

// Axios 인스턴스 생성
const axiosAPI = axios.create({
    baseURL: '/api/responseentity',
    timeout: 10000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// 요청 인터셉터
axiosAPI.interceptors.request.use(
    config => {
        // 로딩 표시
        showLoading();

        // 언어 설정 추가
        const language = localStorage.getItem('language') || 'ko';
        config.headers['Accept-Language'] = language;

        // 요청 로깅 (개발 환경)
        if (console && console.log) {
            console.log(`[Axios Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
        }

        return config;
    },
    error => {
        hideLoading();
        console.error('[Axios Request Error]', error);
        return Promise.reject(error);
    }
);

// 응답 인터셉터
axiosAPI.interceptors.response.use(
    response => {
        hideLoading();

        // 응답 로깅 (개발 환경)
        if (console && console.log) {
            console.log(`[Axios Response] ${response.status}`, response.data);
        }

        return response;
    },
    error => {
        hideLoading();

        // 에러 로깅
        console.error('[Axios Response Error]', error);

        // 공통 에러 처리
        handleAxiosError(error);

        return Promise.reject(error);
    }
);

// 에러 처리 함수
function handleAxiosError(error) {
    let errorMessage = '요청 처리 중 오류가 발생했습니다.';

    if (error.response) {
        // 서버가 응답을 반환했지만 오류 상태코드
        const status = error.response.status;
        const data = error.response.data;

        switch (status) {
            case 400:
                errorMessage = data?.message || '잘못된 요청입니다.';
                break;
            case 401:
                errorMessage = '로그인이 필요합니다.';
                // 로그인 페이지로 리다이렉트
                setTimeout(() => {
                    window.location.href = '/axios/auth/login';
                }, 1500);
                break;
            case 403:
                errorMessage = '접근 권한이 없습니다.';
                break;
            case 404:
                errorMessage = '요청한 리소스를 찾을 수 없습니다.';
                break;
            case 422:
                // 검증 오류 - 필드별 오류 메시지 표시
                if (data?.fieldErrors) {
                    showValidationErrors(data.fieldErrors);
                    return;
                }
                errorMessage = data?.message || '입력값을 확인해주세요.';
                break;
            case 500:
                errorMessage = '서버 내부 오류가 발생했습니다.';
                break;
            default:
                errorMessage = data?.message || `서버 오류 (${status})`;
        }
    } else if (error.request) {
        // 요청이 전송되었지만 응답을 받지 못함
        errorMessage = '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.';
    } else {
        // 요청 설정 중 오류 발생
        errorMessage = '요청 처리 중 오류가 발생했습니다.';
    }

    showErrorMessage(errorMessage);
}

// 검증 오류 표시 함수
function showValidationErrors(fieldErrors) {
    // 기존 에러 메시지 제거
    document.querySelectorAll('.field-error').forEach(el => el.remove());

    // 필드별 에러 메시지 표시
    Object.keys(fieldErrors).forEach(fieldName => {
        const field = document.querySelector(`[name="${fieldName}"]`);
        if (field) {
            field.classList.add('is-invalid');

            const errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback field-error';
            errorDiv.textContent = fieldErrors[fieldName];

            field.parentNode.appendChild(errorDiv);
        }
    });
}

// 로딩 표시/숨김 함수
function showLoading() {
    const loadingElement = document.getElementById('loadingIndicator');
    if (loadingElement) {
        loadingElement.style.display = 'block';
    }
}

function hideLoading() {
    const loadingElement = document.getElementById('loadingIndicator');
    if (loadingElement) {
        loadingElement.style.display = 'none';
    }
}

// API 호출 래퍼 함수들
const apiCall = {
    // GET 요청
    get: async (url, config = {}) => {
        try {
            const response = await axiosAPI.get(url, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // POST 요청
    post: async (url, data = {}, config = {}) => {
        try {
            const response = await axiosAPI.post(url, data, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // PUT 요청
    put: async (url, data = {}, config = {}) => {
        try {
            const response = await axiosAPI.put(url, data, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    // DELETE 요청
    delete: async (url, config = {}) => {
        try {
            const response = await axiosAPI.delete(url, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    }
};

// 동시 요청 처리
const apiCallAll = {
    // 여러 요청을 동시에 실행
    all: async (requests) => {
        try {
            const responses = await axios.all(requests);
            return responses.map(response => response.data);
        } catch (error) {
            throw error;
        }
    },

    // 여러 요청 중 하나라도 성공하면 반환
    race: async (requests) => {
        try {
            const response = await Promise.race(requests);
            return response.data;
        } catch (error) {
            throw error;
        }
    }
};

// 요청 취소 기능
const cancelTokenSource = {
    create: () => axios.CancelToken.source(),
    isCancel: (error) => axios.isCancel(error)
};

// 파일 업로드 함수 (진행률 표시 포함)
async function uploadFile(url, file, onProgress = null) {
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await axiosAPI.post(url, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (progressEvent) => {
                if (onProgress && progressEvent.total) {
                    const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    onProgress(progress);
                }
            }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
}

// 페이지 초기화 시 세션 확인
document.addEventListener('DOMContentLoaded', async function() {
    // 로그아웃 버튼 이벤트 연결
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // 로그인 페이지가 아닌 경우에만 세션 확인
    if (!window.location.pathname.includes('/login')) {
        await checkSession();
    }
});

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
        // 401 에러는 인터셉터에서 처리되므로 여기서는 조용히 처리
        if (error.response?.status !== 401) {
            console.error('Session check failed:', error);
        }
        updateUserInfo(null);
        return null;
    }
}

// 로그아웃 함수
async function logout() {
    try {
        const response = await apiCall.post('/auth/logout');

        if (response.success) {
            showSuccessMessage(response.message || '로그아웃되었습니다.');
            localStorage.removeItem('user');
            updateUserInfo(null);

            setTimeout(() => {
                window.location.href = '/axios/auth/login';
            }, 1000);
        } else {
            showErrorMessage(response.error?.message || '로그아웃 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('Logout error:', error);
        showErrorMessage('로그아웃 중 오류가 발생했습니다.');
    }
}