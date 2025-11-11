// admin-authors.js - исправленная версия с правильной логикой фильтрации
class AdminAuthorsApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.authors = [];
        this.filteredAuthors = [];
        this.currentFilter = 'ALL';
        this.initialized = false;
    }

    async init() {
        if (this.initialized) {
            console.log('⚠️ AdminAuthorsApp already initialized');
            return;
        }

        try {
            await this.loadAuthors();
            this.setupEventListeners();
            this.updateStatistics();
            this.initialized = true;
        } catch (error) {
            console.error('Error initializing AdminAuthorsApp:', error);
        }
    }

    async loadAuthors() {
        try {
            CommonUtils.showLoadingState('authorsTableBody', 'Загрузка авторов...', 7);
            const response = await this.get('/authors');

            if (response.success) {
                this.authors = response.data || [];
                console.log(`📊 Loaded ${this.authors.length} authors`);
                this.applyFilter(this.currentFilter);
                this.updateStatistics();
            } else {
                CommonUtils.showError('authorsTableBody', 'Ошибка при загрузке авторов', 7);
            }
        } catch (error) {
            console.error('Error loading authors:', error);
            CommonUtils.showError('authorsTableBody', 'Ошибка при загрузке авторов', 7);
        }
    }

    applyFilter(filter) {
        this.currentFilter = filter;

        switch (filter) {
            case 'ADMIN':
                // Администраторы: те, у кого есть роль ADMIN
                this.filteredAuthors = this.authors.filter(author =>
                    this.hasRole(author, 'ADMIN')
                );
                break;
            case 'USER':
                // Пользователи: те, у кого есть роль USER (включая администраторов)
                this.filteredAuthors = this.authors.filter(author =>
                    this.hasRole(author, 'USER')
                );
                break;
            default:
                this.filteredAuthors = this.authors;
        }

        this.displayAuthors(this.filteredAuthors);
        this.updateFilterStatus();
    }

    // Проверяет, есть ли у пользователя указанная роль
    hasRole(author, roleName) {
        if (!author) return false;

        const roles = this.getAuthorRoles(author);

        return roles.some(role =>
            role === roleName || role === `ROLE_${roleName}`
        );
    }

    // Получение всех ролей автора
    getAuthorRoles(author) {
        if (!author) return [];

        if (author.roles && Array.isArray(author.roles)) {
            return author.roles;
        }

        if (author.user && author.user.roles && Array.isArray(author.user.roles)) {
            return author.user.roles;
        }

        return ['USER']; // Роль по умолчанию
    }

    // Форматирование ролей для отображения
    formatRoles(roles) {
        if (!roles || roles.length === 0) {
            return '<span class="badge bg-secondary role-badge">USER</span>';
        }

        return roles.map(role => {
            const roleName = role.replace('ROLE_', '');
            const badgeClass = roleName === 'ADMIN' ? 'bg-danger' : 'bg-secondary';
            return `<span class="badge ${badgeClass} me-1 role-badge">${roleName}</span>`;
        }).join('');
    }

    displayAuthors(authors) {
        const tbody = document.getElementById('authorsTableBody');

        if (!authors || authors.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-4 text-muted">
                        Авторы не найдены
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = authors.map(author => {
            const roles = this.getAuthorRoles(author);
            const isAdmin = this.hasRole(author, 'ADMIN');

            return `
            <tr class="author-row" style="cursor: pointer;" onclick="adminAuthors.showAuthorDetails(${author.id})">
                <td class="fw-bold">${author.id}</td>
                <td>
                    <strong>${CommonUtils.escapeHtml(author.user?.username || 'Неизвестно')}</strong>
                    ${isAdmin ? ' <span class="badge bg-danger">ADMIN</span>' : ''}
                </td>
                <td class="text-muted">${CommonUtils.escapeHtml(author.user?.email || '')}</td>
                <td>
                    ${this.formatRoles(roles)}
                </td>
                <td>
                    <small class="text-muted">${CommonUtils.escapeHtml(author.bio || 'Биография не указана')}</small>
                </td>
                <td>
                    <span class="badge bg-info">${author.recipeCount || 0}</span>
                </td>
                <td class="text-muted small">
                    ${CommonUtils.formatDateShort(author.createdAt)}
                </td>
            </tr>
            `;
        }).join('');
    }

    updateStatistics() {
        const totalAuthors = this.authors.length;

        // Подсчет администраторов (те, у кого есть роль ADMIN)
        const adminCount = this.authors.filter(author =>
            this.hasRole(author, 'ADMIN')
        ).length;

        // Подсчет пользователей (те, у кого есть роль USER)
        const userCount = this.authors.filter(author =>
            this.hasRole(author, 'USER')
        ).length;

        // Обновляем отображение
        document.getElementById('total-authors').textContent = totalAuthors;
        document.getElementById('count-all').textContent = totalAuthors;
        document.getElementById('count-admin').textContent = adminCount;
        document.getElementById('count-user').textContent = userCount;

        console.log(`📈 Statistics: Total=${totalAuthors}, Has ADMIN role=${adminCount}, Has USER role=${userCount}`);

        // Логируем детальную информацию для отладки
        console.log('🔍 Detailed role analysis:');
        this.authors.forEach(author => {
            const roles = this.getAuthorRoles(author);
            const hasAdmin = this.hasRole(author, 'ADMIN');
            const hasUser = this.hasRole(author, 'USER');

            console.log(`👤 ${author.user?.username}: roles=${JSON.stringify(roles)}, hasAdmin=${hasAdmin}, hasUser=${hasUser}`);
        });
    }

    updateFilterStatus() {
        const statusElement = document.getElementById('filter-status');
        const count = this.filteredAuthors.length;
        const total = this.authors.length;

        switch (this.currentFilter) {
            case 'ADMIN':
                statusElement.textContent = `Показаны пользователи с ролью ADMIN: ${count} из ${total}`;
                break;
            case 'USER':
                statusElement.textContent = `Показаны пользователи с ролью USER: ${count} из ${total}`;
                break;
            default:
                statusElement.textContent = `Показаны все авторы: ${count}`;
        }
    }

    showAuthorDetails(authorId) {
        const author = this.authors.find(a => a.id === authorId);
        if (!author) {
            console.error('Author not found:', authorId);
            return;
        }

        const roles = this.getAuthorRoles(author);

        // Заполняем модальное окно
        document.getElementById('detail-username').textContent = author.user?.username || 'Неизвестно';
        document.getElementById('detail-email').textContent = author.user?.email || 'Не указан';
        document.getElementById('detail-bio').textContent = author.bio || 'Биография не указана';
        document.getElementById('detail-recipe-count').textContent = author.recipeCount || 0;
        document.getElementById('detail-created-at').textContent = CommonUtils.formatDateShort(author.createdAt);

        // Отображаем роли
        const rolesContainer = document.getElementById('detail-roles');
        rolesContainer.innerHTML = this.formatRoles(roles);

        // Показываем модальное окно
        try {
            const modal = new bootstrap.Modal(document.getElementById('authorDetailsModal'));
            modal.show();
        } catch (error) {
            console.error('Error showing author details modal:', error);
        }
    }

    setupEventListeners() {
        // Обработчики для фильтров
        document.querySelectorAll('.filter-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const role = e.currentTarget.dataset.role;

                // Обновляем активный фильтр
                document.querySelectorAll('.filter-badge').forEach(b => {
                    b.classList.remove('active');
                });
                e.currentTarget.classList.add('active');

                // Применяем фильтр
                this.applyFilter(role);
            });
        });
    }
}

// Защищенная инициализация с проверкой зависимостей
let adminAuthors = null;

function initializeAuthorsApp() {
    if (adminAuthors && adminAuthors.initialized) {
        console.log('⚠️ Authors app already initialized');
        return;
    }

    // Проверяем зависимости
    if (typeof BaseApiClient === 'undefined') {
        console.error('❌ BaseApiClient not available');
        setTimeout(initializeAuthorsApp, 100);
        return;
    }

    if (typeof CommonUtils === 'undefined') {
        console.error('❌ CommonUtils not available');
        setTimeout(initializeAuthorsApp, 100);
        return;
    }

    if (typeof bootstrap === 'undefined') {
        console.error('❌ Bootstrap not available');
        setTimeout(initializeAuthorsApp, 100);
        return;
    }

    try {
        adminAuthors = new AdminAuthorsApp();
        adminAuthors.init().catch(error => {
            console.error('💥 Failed to initialize authors app:', error);
        });

        // Глобальный доступ для обработчиков onclick
        window.adminAuthors = adminAuthors;
    } catch (error) {
        console.error('💥 Error creating authors app:', error);
    }
}

// Инициализация при загрузке DOM
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        console.log('📄 DOM Content Loaded - initializing authors app');
        setTimeout(initializeAuthorsApp, 100);
    });
} else {
    console.log('📄 DOM already loaded - initializing authors app');
    setTimeout(initializeAuthorsApp, 100);
}