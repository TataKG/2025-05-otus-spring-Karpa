// app.js - для главной страницы
class CookbookApp extends BaseApiClient {
    constructor() {
        super('/api');
        this.messages = window.i18nMessages || this.getFallbackMessages();
        this.categories = [];
        this.authors = [];
        this.allRecipes = [];
        this.filteredRecipes = [];
        this.currentFilters = {
            category: '',
            author: '',
            search: ''
        };
        this.init();
    }

    getFallbackMessages() {
        return {
            loading: 'Загрузка...',
            close: 'Закрыть',
            view: 'Просмотр',
            retry: 'Повторить',
            cancel: 'Отмена',
            'recipe-details': 'Детали рецепта',
            'recipes-loading': 'Загрузка рецептов...',
            'no-recipes': 'Рецепты не найдены',
            'error-loading-recipes': 'Ошибка при загрузке рецептов',
            'error-loading-details': 'Ошибка при загрузке деталей рецепта',
            'error-loading-comments': 'Ошибка при загрузке комментариев',
            category: 'Категория',
            author: 'Автор',
            comments: 'Комментарии',
            actions: 'Действия',
            ingredients: 'Ингредиенты',
            description: 'Описание',
            inventory: 'Необходимый инвентарь',
            'comments-title': 'Комментарии к рецепту',
            'no-comments': 'Комментариев пока нет',
            'comment-author': 'Автор',
            'comment-date': 'Дата',
            'comment-content': 'Комментарий',
            'comments-loading': 'Загрузка комментариев...',
            'recipe-details-category': '📂 Категория',
            'recipe-details-author': '👨‍🍳 Автор',
            'recipe-details-ingredients': '🛒 Ингредиенты',
            'recipe-details-description': '📝 Описание',
            'recipe-details-inventory': '🔧 Необходимый инвентарь',
            'recipe-details-comments': '💬 Комментарии'
        };
    }

    async init() {
        await this.loadCategories();
        await this.loadAuthors();
        await this.loadRecipes();
        this.setupEventListeners();
    }

    async loadCategories() {
        try {
            const response = await this.get('/categories');
            if (response.success) {
                this.categories = response.data;
                this.populateCategoryFilter();
            }
        } catch (error) {
            console.error('Error loading categories:', error);
        }
    }

    async loadAuthors() {
        try {
            // Загружаем авторов из рецептов
            const response = await this.get('/recipes');
            if (response.success) {
                // Извлекаем уникальных авторов из рецептов
                const authorsMap = new Map();
                response.data.forEach(recipe => {
                    if (recipe.authorName && !authorsMap.has(recipe.authorName)) {
                        authorsMap.set(recipe.authorName, recipe.authorName);
                    }
                });
                this.authors = Array.from(authorsMap.values()).sort();
                this.populateAuthorFilter();
            }
        } catch (error) {
            console.error('Error loading authors:', error);
        }
    }

    populateCategoryFilter() {
        const categoryFilter = document.getElementById('categoryFilter');
        if (categoryFilter) {
            categoryFilter.innerHTML = '<option value="">Все категории</option>' +
                this.categories.map(cat =>
                    `<option value="${CommonUtils.escapeHtml(cat.name)}">${CommonUtils.escapeHtml(cat.name)}</option>`
                ).join('');
        }
    }

    populateAuthorFilter() {
        const authorFilter = document.getElementById('authorFilter');
        if (authorFilter) {
            authorFilter.innerHTML = '<option value="">Все авторы</option>' +
                this.authors.map(author =>
                    `<option value="${CommonUtils.escapeHtml(author)}">${CommonUtils.escapeHtml(author)}</option>`
                ).join('');
        }
    }

    async loadRecipes() {
        try {
            CommonUtils.showLoadingState('recipesTableBody', this.messages['recipes-loading'], 6);
            const response = await this.get('/recipes');

            if (response.success) {
                this.allRecipes = response.data;
                this.applyFilters();
            } else {
                CommonUtils.showError('recipesTableBody', this.messages['error-loading-recipes'], 6);
            }
        } catch (error) {
            console.error('Error loading recipes:', error);
            CommonUtils.showError('recipesTableBody', this.messages['error-loading-recipes'], 6);
        }
    }

    applyFilters() {
        this.filteredRecipes = this.allRecipes.filter(recipe => {
            const matchesCategory = !this.currentFilters.category ||
                recipe.categoryName === this.currentFilters.category;
            const matchesAuthor = !this.currentFilters.author ||
                recipe.authorName === this.currentFilters.author;
            const matchesSearch = !this.currentFilters.search ||
                recipe.title.toLowerCase().includes(this.currentFilters.search.toLowerCase());

            return matchesCategory && matchesAuthor && matchesSearch;
        });

        this.displayRecipes(this.filteredRecipes);
        this.updateRecipesCount();
    }

    updateRecipesCount() {
        const countElement = document.getElementById('recipesCount');
        if (countElement) {
            const total = this.allRecipes.length;
            const filtered = this.filteredRecipes.length;
            const countText = filtered === total ?
                `${total} рецептов` :
                `${filtered} из ${total} рецептов`;
            countElement.textContent = countText;
        }
    }

    displayRecipes(recipes) {
        const tbody = document.getElementById('recipesTableBody');

        if (!recipes || recipes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-4 text-muted">
                        ${this.messages['no-recipes']}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = recipes.map(recipe => `
            <tr>
                <td class="fw-bold text-primary">${CommonUtils.escapeHtml(recipe.title)}</td>
                <td>
                    <span class="badge bg-secondary">${CommonUtils.escapeHtml(recipe.categoryName)}</span>
                </td>
                <td class="text-muted">${CommonUtils.escapeHtml(recipe.authorName)}</td>
                <td>
                    <span class="badge bg-info text-dark comments-badge"
                          style="cursor: pointer;"
                          data-recipe-id="${recipe.id}"
                          data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                          title="${this.messages.view} ${this.messages.comments.toLowerCase()}">
                        💬 ${recipe.commentCount || 0}
                    </span>
                </td>
                <td class="text-muted small">
                    ${CommonUtils.formatDateShort(recipe.createdAt)}
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary view-recipe"
                            data-recipe-id="${recipe.id}">
                        👁️ ${this.messages.view}
                    </button>
                </td>
            </tr>
        `).join('');

        this.addRecipeViewListeners();
        this.addCommentsViewListeners();
    }

    addRecipeViewListeners() {
        document.querySelectorAll('.view-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.view-recipe').dataset.recipeId;
                if (recipeId) {
                    this.loadRecipeDetails(recipeId);
                }
            });
        });
    }

    addCommentsViewListeners() {
        document.querySelectorAll('.comments-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.comments-badge').dataset.recipeId;
                const recipeTitle = e.target.closest('.comments-badge').dataset.recipeTitle;
                this.showCommentsModal(recipeId, recipeTitle);
            });
        });
    }

    async loadRecipeDetails(recipeId) {
        try {
            const response = await this.get(`/recipes/${recipeId}/detailed`);
            if (response.success) {
                this.showRecipeModal(response.data);
            } else {
                CommonUtils.showToast(this.messages['error-loading-details'], 'error');
            }
        } catch (error) {
            console.error('Error loading recipe details:', error);
            CommonUtils.showToast(this.messages['error-loading-details'], 'error');
        }
    }

    async loadRecipeComments(recipeId) {
        try {
            const response = await fetch(`${this.baseUrl}/recipes/${recipeId}/comments`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const result = await response.json();
            if (result.success) {
                return result.data;
            } else {
                throw new Error(result.message || 'Failed to load comments');
            }
        } catch (error) {
            console.error('Error loading recipe comments:', error);
            throw error;
        }
    }

    showRecipeModal(recipe) {
        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        modalTitle.textContent = `📖 ${CommonUtils.escapeHtml(recipe.title)}`;

        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-category']}:</h6>
                        <p><span class="badge bg-primary">${CommonUtils.escapeHtml(recipe.category.name)}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-author']}:</h6>
                        <p class="text-muted">${CommonUtils.escapeHtml(recipe.author.user.username)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-ingredients']}:</h6>
                        <div class="list-group">
                            ${recipe.ingredients.map(ingredient => `
                                <div class="list-group-item list-group-item-action">
                                    ${CommonUtils.escapeHtml(ingredient)}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-description']}:</h6>
                        <p class="text-muted border-start border-3 border-primary ps-3 py-2 bg-light">
                            ${CommonUtils.escapeHtml(recipe.description)}
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-inventory']}:</h6>
                        <div class="d-flex flex-wrap gap-2">
                            ${recipe.inventoryItems.map(item => `
                                <span class="badge bg-warning text-dark">
                                    🍴 ${CommonUtils.escapeHtml(item.name)}
                                </span>
                            `).join('')}
                        </div>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-comments']}:</h6>
                        <p>
                            <span class="badge bg-info text-dark comments-badge"
                                  style="cursor: pointer;"
                                  data-recipe-id="${recipe.id}"
                                  data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                                  title="${this.messages.view} ${this.messages.comments.toLowerCase()}">
                                💬 ${recipe.commentCount || 0} ${CommonUtils.getCommentText(recipe.commentCount || 0)}
                            </span>
                        </p>
                    </div>
                </div>
            </div>
        `;

        const commentsBadge = modalBody.querySelector('.comments-badge');
        if (commentsBadge) {
            commentsBadge.addEventListener('click', () => {
                const recipeId = commentsBadge.dataset.recipeId;
                const recipeTitle = commentsBadge.dataset.recipeTitle;
                this.showCommentsModal(recipeId, recipeTitle);
            });
        }

        const modal = new bootstrap.Modal(document.getElementById('recipeModal'));
        modal.show();
    }

    async showCommentsModal(recipeId, recipeTitle) {
        const modalTitle = document.getElementById('commentsModalTitle');
        const modalBody = document.getElementById('commentsModalBody');

        modalTitle.textContent = `💬 ${this.messages['comments-title']}: ${CommonUtils.escapeHtml(recipeTitle)}`;

        modalBody.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">${this.messages.loading}</span>
                </div>
                <p class="mt-2 text-muted">${this.messages['comments-loading']}</p>
            </div>
        `;

        const modal = new bootstrap.Modal(document.getElementById('commentsModal'));
        modal.show();

        try {
            const comments = await this.loadRecipeComments(recipeId);
            this.displayComments(comments, modalBody);
        } catch (error) {
            modalBody.innerHTML = `
                <div class="alert alert-danger">
                    <p>${this.messages['error-loading-comments']}</p>
                    <button class="btn btn-sm btn-outline-primary" onclick="app.showCommentsModal('${recipeId}', '${CommonUtils.escapeHtml(recipeTitle)}')">
                        ${this.messages.retry}
                    </button>
                </div>
            `;
        }
    }

    displayComments(comments, modalBody) {
        if (!comments || comments.length === 0) {
            modalBody.innerHTML = `
                <div class="text-center py-4">
                    <div class="text-muted mb-2">💬</div>
                    <p class="text-muted">${this.messages['no-comments']}</p>
                </div>
            `;
            return;
        }

        modalBody.innerHTML = `
            <div class="comments-list">
                ${comments.map(comment => `
                    <div class="card mb-3">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <div>
                                    <h6 class="card-title mb-1">
                                        👤 ${CommonUtils.escapeHtml(comment.userName || comment.author || this.messages['comment-author'])}
                                    </h6>
                                    <small class="text-muted">
                                        📅 ${new Date(comment.createdAt).toLocaleString()}
                                    </small>
                                </div>
                            </div>
                            <p class="card-text mt-3">${CommonUtils.escapeHtml(comment.content)}</p>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    setupEventListeners() {
        // Фильтр по категории
        const categoryFilter = document.getElementById('categoryFilter');
        if (categoryFilter) {
            categoryFilter.addEventListener('change', (e) => {
                this.currentFilters.category = e.target.value;
                this.applyFilters();
            });
        }

        // Фильтр по автору
        const authorFilter = document.getElementById('authorFilter');
        if (authorFilter) {
            authorFilter.addEventListener('change', (e) => {
                this.currentFilters.author = e.target.value;
                this.applyFilters();
            });
        }

        // Поиск по названию
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.currentFilters.search = e.target.value;
                    this.applyFilters();
                }, 300);
            });
        }

        // Очистка фильтров
        const clearFilters = document.getElementById('clearFilters');
        if (clearFilters) {
            clearFilters.addEventListener('click', () => {
                this.currentFilters = { category: '', author: '', search: '' };
                if (categoryFilter) categoryFilter.value = '';
                if (authorFilter) authorFilter.value = '';
                if (searchInput) searchInput.value = '';
                this.applyFilters();
            });
        }
    }
}

let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new CookbookApp();
});