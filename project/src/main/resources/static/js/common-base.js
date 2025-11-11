// common-base.js - базовые утилиты и API клиент
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
        // Проверяем, что Bootstrap доступен
        if (typeof bootstrap === 'undefined') {
            console.warn('Bootstrap not available for toast');
            return;
        }

        const toastElement = type === 'success'
            ? document.getElementById('successToast')
            : document.getElementById('errorToast');

        const toastMessage = type === 'success'
            ? document.getElementById('successToastMessage')
            : document.getElementById('errorToastMessage');

        if (toastMessage && toastElement) {
            toastMessage.textContent = message;
            try {
                const toast = new bootstrap.Toast(toastElement);
                toast.show();
            } catch (error) {
                console.error('Error showing toast:', error);
            }
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
        const errorMessage = error?.message || '';

        if (errorMessage.includes('401')) {
            message = 'Требуется авторизация';
            // Перенаправляем на страницу входа при 401
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else if (errorMessage.includes('403')) {
            message = 'Доступ запрещен';
        } else if (errorMessage.includes('404')) {
            message = 'Ресурс не найден';
        } else if (errorMessage.includes('500')) {
            message = 'Ошибка сервера';
        } else if (errorMessage) {
            message = errorMessage;
        }

        this.showToast(message, 'error');
        return message;
    }

    // Универсальный метод для получения названия категории из разных структур данных
    static getCategoryName(recipe) {
        if (!recipe) return 'Не указана';

        // Пробуем разные пути к данным категории
        if (recipe.categoryName) {
            return recipe.categoryName;
        } else if (recipe.category && recipe.category.name) {
            return recipe.category.name;
        } else if (recipe.category && typeof recipe.category === 'string') {
            return recipe.category;
        } else if (recipe.categoryId) {
            return `Категория ID: ${recipe.categoryId}`;
        }
        return 'Не указана';
    }

    // Универсальный метод для получения имени автора
    static getAuthorName(recipe) {
        if (!recipe) return 'Неизвестен';

        // Пробуем разные пути к данным автора
        if (recipe.authorName) {
            return recipe.authorName;
        } else if (recipe.author && recipe.author.user && recipe.author.user.username) {
            return recipe.author.user.username;
        } else if (recipe.author && recipe.author.username) {
            return recipe.author.username;
        } else if (recipe.author && typeof recipe.author === 'string') {
            return recipe.author;
        }

        return 'Неизвестен'; // Добавлен возврат по умолчанию
    }

    // Универсальный метод для получения количества комментариев
    static getCommentCount(recipe) {
        if (!recipe) return 0;
        return recipe.commentCount || recipe.commentsCount || (Array.isArray(recipe.comments) ? recipe.comments.length : 0);
    }

    // Новый метод: проверка наличия DOM элемента
    static ensureElement(selector) {
        const element = typeof selector === 'string' ? document.querySelector(selector) : selector;
        if (!element) {
            console.warn(`Element not found: ${selector}`);
        }
        return element;
    }
}

// Базовый класс для работы с API
class BaseApiClient {
    constructor(baseUrl = '/api') {
        this.baseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
        this.pendingRequests = new Map();
    }

    async makeRequest(fullUrl, options) {
        try {
            const response = await fetch(fullUrl, options);
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

            // Handle 204 No Content
            if (response.status === 204) {
                return { success: true };
            }

            const data = await response.json();
            console.log(`✅ API Response data:`, data);
            return data;
        } catch (error) {
            console.error(`💥 Fetch error for ${fullUrl}:`, error);
            throw error;
        }
    }

    async request(url, options = {}) {
        const fullUrl = url.startsWith('/') ? `${this.baseUrl}${url}` : `${this.baseUrl}/${url}`;

        // Защита от дублирующихся запросов
        const requestKey = `${options.method || 'GET'}:${fullUrl}`;
        if (this.pendingRequests.has(requestKey)) {
            console.log(`⚠️ Skipping duplicate request: ${requestKey}`);
            return this.pendingRequests.get(requestKey);
        }

        console.log(`🔗 API ${options.method || 'GET'}: ${fullUrl}`);

        const defaultOptions = {
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        try {
            const requestPromise = this.makeRequest(fullUrl, { ...defaultOptions, ...options });
            this.pendingRequests.set(requestKey, requestPromise);

            const response = await requestPromise;
            return response;
        } catch (error) {
            // Преобразуем ошибку для единообразной обработки
            if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
                throw new Error('Ошибка сети. Проверьте подключение к интернету.');
            }
            throw error;
        } finally {
            this.pendingRequests.delete(requestKey);
        }
    }

    async get(url) {
        return this.request(url, { method: 'GET' });
    }

    async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    async patch(url, data) {
        return this.request(url, {
            method: 'PATCH',
            body: JSON.stringify(data)
        });
    }

    async delete(url) {
        return this.request(url, { method: 'DELETE' });
    }

    // Новый метод: отмена всех pending запросов
    cancelAllRequests() {
        this.pendingRequests.clear();
        console.log('🧹 All pending requests cancelled');
    }

    // Новый метод: проверка наличия pending запросов
    hasPendingRequests() {
        return this.pendingRequests.size > 0;
    }
}

// Экспорты для использования в модульной системе
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { CommonUtils, BaseApiClient };
}