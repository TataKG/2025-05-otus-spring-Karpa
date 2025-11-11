// common-auth.js - функции аутентификации
class AuthUtils {
    static async checkAuthStatus() {
        try {
            console.log("Checking auth status...");
            const response = await fetch('/api/auth/user', {
                credentials: 'include'
            });

            console.log("Auth response status:", response.status);

            if (response.ok) {
                const result = await response.json();
                console.log("Auth response data:", result);

                if (result.success && result.data && result.data.authenticated) {
                    console.log("User authenticated:", result.data);
                    this.showAuthenticatedUI(result.data);
                } else {
                    console.log("User not authenticated");
                    this.showUnauthenticatedUI();
                }
            } else {
                console.log("Auth endpoint failed, using fallback");
                this.checkAuthFallback();
            }
        } catch (error) {
            console.log('Auth check failed, using fallback:', error);
            this.checkAuthFallback();
        }
    }

    static showAuthenticatedUI(userData) {
        console.log("Showing authenticated UI for:", userData);

        // Скрываем кнопку входа
        const loginLink = document.getElementById('loginLink');
        if (loginLink) loginLink.style.display = 'none';

        // Показываем кнопку выхода и "Мои рецепты" для всех авторизованных
        const logoutLink = document.getElementById('logoutLink');
        const myRecipesLink = document.getElementById('myRecipesLink');
        if (logoutLink) logoutLink.style.display = 'block';
        if (myRecipesLink) myRecipesLink.style.display = 'block';

        // Устанавливаем имя пользователя
        const usernameDisplay = document.getElementById('usernameDisplay');
        if (usernameDisplay && userData.name) {
            usernameDisplay.textContent = userData.name;
        }

        // Проверяем роль ADMIN - исправленная логика
        const adminBadge = document.getElementById('adminBadge');
        const adminPanelLink = document.querySelector('.admin-panel-link');

        // Проверяем разными способами
        const hasAdminRole =
            (userData.roles && userData.roles.includes('ADMIN')) ||
            (userData.authorities && userData.authorities.includes('ADMIN')) ||
            userData.isAdmin === true;

        console.log("User has ADMIN role:", hasAdminRole, "Roles:", userData.roles, "Authorities:", userData.authorities, "isAdmin:", userData.isAdmin);

        if (hasAdminRole) {
            console.log("User has ADMIN role - showing admin elements");
            if (adminBadge) adminBadge.style.display = 'inline';
            if (adminPanelLink) adminPanelLink.style.display = 'block';
        } else {
            console.log("User does not have ADMIN role");
            if (adminBadge) adminBadge.style.display = 'none';
            if (adminPanelLink) adminPanelLink.style.display = 'none';
        }
    }

    static showUnauthenticatedUI() {
        console.log("Showing unauthenticated UI");

        // Показываем кнопку входа, скрываем остальное
        const loginLink = document.getElementById('loginLink');
        const logoutLink = document.getElementById('logoutLink');
        const myRecipesLink = document.getElementById('myRecipesLink');

        if (loginLink) loginLink.style.display = 'block';
        if (logoutLink) logoutLink.style.display = 'none';
        if (myRecipesLink) myRecipesLink.style.display = 'none';

        const adminPanelLink = document.querySelector('.admin-panel-link');
        if (adminPanelLink) adminPanelLink.style.display = 'none';
    }

    // Fallback метод
    static async checkAuthFallback() {
        try {
            const response = await fetch('/api/recipes/my-recipes', {
                credentials: 'include'
            });

            if (response.status === 200) {
                this.showAuthenticatedUI({ name: 'Пользователь' });
                this.checkAdminRights();
            } else {
                this.showUnauthenticatedUI();
            }
        } catch (error) {
            this.showUnauthenticatedUI();
        }
    }

    // Проверка админских прав
    static async checkAdminRights() {
        try {
            const response = await fetch('/api/admin/categories', {
                credentials: 'include'
            });

            if (response.status === 200) {
                const adminBadge = document.getElementById('adminBadge');
                const adminPanelLink = document.querySelector('.admin-panel-link');

                if (adminBadge) adminBadge.style.display = 'inline';
                if (adminPanelLink) adminPanelLink.style.display = 'block';
            }
        } catch (error) {
            // Не админ - ничего не делаем
        }
    }

    static handleApiError(error, defaultMessage = 'Произошла ошибка') {
        console.error('API Error:', error);
        let message = defaultMessage;

        if (error.message.includes('401')) {
            message = 'Требуется авторизация';
        } else if (error.message.includes('403')) {
            message = 'Доступ запрещен. Требуются права администратора.';
        } else if (error.message.includes('404')) {
            message = 'Ресурс не найден';
        } else if (error.message.includes('500')) {
            message = 'Ошибка сервера';
        } else if (error.message.includes('access denied')) {
            message = 'Доступ запрещен';
        } else if (error.message) {
            message = error.message;
        }

        CommonUtils.showToast(message, 'error');
        return message;
    }
}

// Инициализация аутентификации при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    AuthUtils.checkAuthStatus();
});