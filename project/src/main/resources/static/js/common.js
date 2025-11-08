// common.js - общие функции для всех страниц
class CommonUtils {
    static escapeHtml(unsafe) {
        if (unsafe === null || unsafe === undefined) return '';
        return unsafe
            .toString()
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    static formatDate(dateString) {
        if (!dateString) return 'Не указана';

        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) {
                return 'Неверная дата';
            }
            return date.toLocaleDateString('ru-RU', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (error) {
            console.error('Error formatting date:', error);
            return 'Ошибка даты';
        }
    }

    static formatDateShort(dateString) {
        if (!dateString) return 'Не указана';

        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) {
                return 'Неверная дата';
            }
            return date.toLocaleDateString('ru-RU', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (error) {
            console.error('Error formatting date:', error);
            return 'Ошибка даты';
        }
    }

    static getCommentText(count) {
        if (count === 0) return 'комментариев';
        if (count % 10 === 1 && count % 100 !== 11) {
            return 'комментарий';
        } else if ([2, 3, 4].includes(count % 10) && ![12, 13, 14].includes(count % 100)) {
            return 'комментария';
        } else {
            return 'комментариев';
        }
    }

    static showToast(message, type = 'success') {
        const toastElement = type === 'success'
            ? document.getElementById('successToast')
            : document.getElementById('errorToast');

        const toastMessage = type === 'success'
            ? document.getElementById('successToastMessage')
            : document.getElementById('errorToastMessage');

        if (toastMessage && toastElement) {
            toastMessage.textContent = message;
            const toast = new bootstrap.Toast(toastElement);
            toast.show();
        }
    }

    static showLoadingState(containerId, message = 'Загрузка...', columns = 5) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <tr>
                    <td colspan="${columns}" class="text-center py-4">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Загрузка...</span>
                        </div>
                        <p class="mt-2 text-muted">${message}</p>
                    </td>
                </tr>
            `;
        }
    }

    static showError(containerId, message, columns = 5) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <tr>
                    <td colspan="${columns}" class="text-center py-4">
                        <div class="text-danger mb-2">❌</div>
                        <p class="text-muted">${message}</p>
                        <button class="btn btn-sm btn-outline-primary" onclick="location.reload()">
                            Повторить
                        </button>
                    </td>
                </tr>
            `;
        }
    }

    static handleApiError(error, defaultMessage = 'Произошла ошибка') {
        console.error('API Error:', error);
        let message = defaultMessage;

        if (error.message.includes('401')) {
            message = 'Требуется авторизация';
        } else if (error.message.includes('403')) {
            message = 'Доступ запрещен';
        } else if (error.message.includes('404')) {
            message = 'Ресурс не найден';
        } else if (error.message.includes('500')) {
            message = 'Ошибка сервера';
        } else if (error.message) {
            message = error.message;
        }

        this.showToast(message, 'error');
        return message;
    }
}

// Базовый класс для работы с API
class BaseApiClient {
    constructor(baseUrl = '/api') {
        this.baseUrl = baseUrl;
    }

    async get(url) {
        const fullUrl = `${this.baseUrl}${url}`;
        console.log(`🔗 API GET: ${fullUrl}`);

        try {
            const response = await fetch(fullUrl, {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            console.log(`📨 API Response status: ${response.status} ${response.statusText}`);

            if (!response.ok) {
                let errorMessage = `HTTP error! status: ${response.status}`;
                try {
                    const errorResult = await response.json();
                    errorMessage = errorResult.message || errorMessage;
                } catch (e) {
                    // Ignore JSON parsing error
                }
                throw new Error(errorMessage);
            }

            const data = await response.json();
            console.log(`✅ API Response data:`, data);
            return data;
        } catch (error) {
            console.error(`💥 Fetch error for ${fullUrl}:`, error);
            throw error;
        }
    }

    async post(url, data) {
        const fullUrl = `${this.baseUrl}${url}`;
        console.log(`🔗 API POST: ${fullUrl}`, data);

        try {
            const response = await fetch(fullUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(data)
            });

            console.log(`📨 API Response status: ${response.status} ${response.statusText}`);

            if (!response.ok) {
                let errorMessage = `HTTP error! status: ${response.status}`;
                try {
                    const errorResult = await response.json();
                    errorMessage = errorResult.message || errorMessage;
                } catch (e) {
                    // Ignore JSON parsing error
                }
                throw new Error(errorMessage);
            }

            return response.json();
        } catch (error) {
            console.error(`💥 Fetch error for ${fullUrl}:`, error);
            throw error;
        }
    }

    async put(url, data) {
        const fullUrl = `${this.baseUrl}${url}`;
        console.log(`🔗 API PUT: ${fullUrl}`, data);

        const response = await fetch(fullUrl, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(data)
        });

        console.log(`📨 API Response status: ${response.status} ${response.statusText}`);

        if (!response.ok) {
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorResult = await response.json();
                errorMessage = errorResult.message || errorMessage;
            } catch (e) {
                const errorText = await response.text();
                if (errorText) {
                    errorMessage = errorText;
                }
            }
            throw new Error(errorMessage);
        }

        return response.json();
    }

    async delete(url) {
        const fullUrl = `${this.baseUrl}${url}`;
        console.log(`🔗 API DELETE: ${fullUrl}`);

        try {
            const response = await fetch(fullUrl, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Accept': 'application/json'
                }
            });

            console.log(`📨 API Response status: ${response.status} ${response.statusText}`);

            // 404 при удалении - это нормально (ресурс уже удален)
            if (response.status === 404) {
                console.warn(`Resource not found during DELETE ${url}, but considering as success`);
                return { success: true, message: "Resource deleted or not found" };
            }

            if (!response.ok) {
                let errorMessage = `HTTP error! status: ${response.status}`;
                try {
                    const errorResult = await response.json();
                    errorMessage = errorResult.message || errorMessage;
                } catch (e) {
                    // Ignore JSON parsing error
                }
                throw new Error(errorMessage);
            }

            // Если ответ пустой (204 No Content), возвращаем успех
            if (response.status === 204) {
                return { success: true };
            }

            return response.json();
        } catch (error) {
            console.error(`💥 Fetch error for ${fullUrl}:`, error);
            throw error;
        }
    }
}