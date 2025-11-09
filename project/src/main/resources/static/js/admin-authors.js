class AdminAuthorsApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.authors = [];
        this.filteredAuthors = [];
        this.currentFilter = 'ALL';
        this.init();
    }

    async init() {
        await this.loadAuthors();
        this.setupEventListeners();
        this.updateStatistics();
    }

    async loadAuthors() {
        try {
            CommonUtils.showLoadingState('authorsTableBody', 'Загрузка авторов...', 7);
            const response = await this.get('/authors');

            if (response.success) {
                this.authors = response.data;
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
                this.filteredAuthors = this.authors.filter(author =>
                    author.roles && author.roles.includes('ROLE_ADMIN')
                );
                break;
            case 'USER':
                this.filteredAuthors = this.authors.filter(author =>
                    !author.roles || !author.roles.includes('ROLE_ADMIN')
                );
                break;
            default:
                this.filteredAuthors = this.authors;
        }

        this.displayAuthors(this.filteredAuthors);
        this.updateFilterStatus();
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

        tbody.innerHTML = authors.map(author => `
            <tr class="author-row" style="cursor: pointer;" onclick="adminAuthors.showAuthorDetails(${author.id})">
                <td class="fw-bold">${author.id}</td>
                <td>
                    <strong>${CommonUtils.escapeHtml(author.user.username)}</strong>
                </td>
                <td class="text-muted">${CommonUtils.escapeHtml(author.user.email || '')}</td>
                <td>
                    ${author.roles ? author.roles.map(role => `
                        <span class="badge ${role === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-secondary'} me-1 role-badge">
                            ${role.replace('ROLE_', '')}
                        </span>
                    `).join('') : '<span class="badge bg-secondary role-badge">USER</span>'}
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
        `).join('');
    }

    updateStatistics() {
        // Общее количество
        document.getElementById('total-authors').textContent = this.authors.length;
        document.getElementById('count-all').textContent = this.authors.length;

        // Количество администраторов
        const adminCount = this.authors.filter(author =>
            author.roles && author.roles.includes('ROLE_ADMIN')
        ).length;
        document.getElementById('count-admin').textContent = adminCount;

        // Количество обычных пользователей
        const userCount = this.authors.length - adminCount;
        document.getElementById('count-user').textContent = userCount;
    }

    updateFilterStatus() {
        const statusElement = document.getElementById('filter-status');
        const count = this.filteredAuthors.length;
        const total = this.authors.length;

        switch (this.currentFilter) {
            case 'ADMIN':
                statusElement.textContent = `Показаны администраторы: ${count} из ${total}`;
                break;
            case 'USER':
                statusElement.textContent = `Показаны пользователи: ${count} из ${total}`;
                break;
            default:
                statusElement.textContent = `Показаны все авторы: ${count}`;
        }
    }

    showAuthorDetails(authorId) {
        const author = this.authors.find(a => a.id === authorId);
        if (!author) return;

        // Заполняем модальное окно
        document.getElementById('detail-username').textContent = author.user.username;
        document.getElementById('detail-email').textContent = author.user.email || 'Не указан';
        document.getElementById('detail-bio').textContent = author.bio || 'Биография не указана';
        document.getElementById('detail-recipe-count').textContent = author.recipeCount || 0;
        document.getElementById('detail-created-at').textContent = CommonUtils.formatDateLong(author.createdAt);

        // Отображаем роли
        const rolesContainer = document.getElementById('detail-roles');
        rolesContainer.innerHTML = author.roles ?
            author.roles.map(role => `
                <span class="badge ${role === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-secondary'} me-1">
                    ${role.replace('ROLE_', '')}
                </span>
            `).join('') :
            '<span class="badge bg-secondary">USER</span>';

        // Показываем модальное окно
        const modal = new bootstrap.Modal(document.getElementById('authorDetailsModal'));
        modal.show();
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

// Инициализация при загрузке страницы
let adminAuthors;
document.addEventListener('DOMContentLoaded', () => {
    adminAuthors = new AdminAuthorsApp();
});