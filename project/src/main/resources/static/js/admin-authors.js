// admin-authors.js
class AuthorsAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.authors = [];
        this.filteredAuthors = [];
        this.currentFilter = 'ALL';
        this.translations = this.getTranslations();
        this.init();
    }

    getTranslations() {
        return {
            loading: 'Загрузка авторов...',
            noData: 'Авторы не найдены',
            filterAll: 'Все',
            roleAdmin: 'Администраторы',
            roleUser: 'Пользователи',
            total: 'Всего авторов',
            details: 'Детали автора',
            showingAll: 'Показаны все авторы',
            showingAdmins: 'Показаны администраторы',
            showingUsers: 'Показаны пользователи'
        };
    }

    async init() {
        console.log('AuthorsAdminApp initialized');
        await this.loadAuthors();
        this.setupEventListeners();
    }

    async loadAuthors() {
        try {
            console.log('Loading authors...');
            CommonUtils.showLoadingState('authorsTableBody', this.translations.loading, 7);

            const response = await this.get('/authors');
            console.log('Authors loaded:', response);

            if (response.success) {
                this.authors = response.data;
                console.log('Authors count:', this.authors.length);
                this.updateStatistics();
                this.applyFilter(this.currentFilter);
            } else {
                CommonUtils.showError('authorsTableBody', 'Ошибка загрузки авторов', 7);
            }
        } catch (error) {
            console.error('Error loading authors:', error);
            CommonUtils.showError('authorsTableBody', 'Ошибка загрузки авторов', 7);
        }
    }

    updateStatistics() {
        // Обновляем счетчики
        const total = this.authors.length;
        const adminCount = this.authors.filter(a => this.hasRole(a, 'ADMIN')).length;
        const userCount = this.authors.filter(a => this.hasRole(a, 'USER')).length;

        document.getElementById('total-authors').textContent = total;
        document.getElementById('count-all').textContent = total;
        document.getElementById('count-admin').textContent = adminCount;
        document.getElementById('count-user').textContent = userCount;
    }

    // Вспомогательный метод для проверки ролей
    hasRole(author, role) {
        return author.roles && author.roles.includes(role);
    }

    setupEventListeners() {
        // Фильтры по ролям
        document.querySelectorAll('.filter-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const role = e.target.closest('.filter-badge').dataset.role;
                this.setFilter(role);
            });
        });

        // Обработчики для строк таблицы
        document.addEventListener('click', (e) => {
            if (e.target.closest('.author-row')) {
                const authorId = e.target.closest('.author-row').dataset.authorId;
                this.showAuthorDetails(authorId);
            }
        });
    }

    setFilter(role) {
        // Обновляем активный фильтр
        document.querySelectorAll('.filter-badge').forEach(badge => {
            badge.classList.remove('active');
        });
        document.querySelector(`[data-role="${role}"]`).classList.add('active');

        this.currentFilter = role;
        this.applyFilter(role);
    }

    applyFilter(role) {
        if (role === 'ALL') {
            this.filteredAuthors = this.authors;
            document.getElementById('filter-status').textContent = this.translations.showingAll;
        } else {
            this.filteredAuthors = this.authors.filter(author =>
                this.hasRole(author, role)
            );

            if (role === 'ADMIN') {
                document.getElementById('filter-status').textContent = this.translations.showingAdmins;
            } else if (role === 'USER') {
                document.getElementById('filter-status').textContent = this.translations.showingUsers;
            }
        }

        this.displayAuthors();
    }

    displayAuthors() {
        const tbody = document.getElementById('authorsTableBody');

        if (this.filteredAuthors.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        ${this.translations.noData}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.filteredAuthors.map(author => `
            <tr class="author-row" data-author-id="${author.id}" style="cursor: pointer;">
                <td>${author.id}</td>
                <td class="fw-bold">${CommonUtils.escapeHtml(author.user?.username || '')}</td>
                <td>${CommonUtils.escapeHtml(author.user?.email || '')}</td>
                <td>
                    <div class="d-flex flex-wrap gap-1">
                        ${this.renderRoles(author.roles)}
                    </div>
                </td>
                <td>
                    <span class="text-truncate d-inline-block" style="max-width: 200px;"
                          title="${CommonUtils.escapeHtml(author.bio || '')}">
                        ${CommonUtils.escapeHtml(author.bio || '---')}
                    </span>
                </td>
                <td>
                    <span class="badge bg-primary">${author.recipeCount || 0}</span>
                </td>
                <td class="small">${CommonUtils.formatDateShort(author.createdAt)}</td>
            </tr>
        `).join('');
    }

    renderRoles(roles) {
        if (!roles || roles.length === 0) return '<span class="text-muted">---</span>';

        return roles.map(role => {
            const roleClass = role === 'ADMIN' ? 'bg-danger' : 'bg-secondary';
            const roleText = role === 'ADMIN' ? 'ADMIN' : 'USER';
            return `<span class="badge ${roleClass} role-badge">${roleText}</span>`;
        }).join('');
    }

    showAuthorDetails(authorId) {
        const author = this.authors.find(a => a.id == authorId);
        if (!author) return;

        // Заполняем модальное окно данными
        document.getElementById('detail-username').textContent = author.user?.username || '---';
        document.getElementById('detail-email').textContent = author.user?.email || '---';
        document.getElementById('detail-roles').innerHTML = this.renderRoles(author.roles);
        document.getElementById('detail-bio').textContent = author.bio || '---';
        document.getElementById('detail-recipe-count').textContent = author.recipeCount || 0;
        document.getElementById('detail-created-at').textContent =
            CommonUtils.formatDateShort(author.createdAt);

        // Показываем модальное окно
        new bootstrap.Modal(document.getElementById('authorDetailsModal')).show();
    }
}

// Инициализация приложения
let authorsApp;
document.addEventListener('DOMContentLoaded', () => {
    authorsApp = new AuthorsAdminApp();
});
