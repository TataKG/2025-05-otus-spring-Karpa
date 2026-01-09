// admin-recipes.js
class RecipesAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.recipes = [];
        this.categories = [];
        this.authors = [];
        this.translations = this.getTranslations();
        this.init();
    }

    getTranslations() {
        return {
            loading: 'Загрузка...',
            errorLoad: 'Ошибка загрузки рецептов',
            errorLoadCategories: 'Ошибка загрузки категорий',
            errorLoadAuthors: 'Ошибка загрузки авторов',
            errorDelete: 'Ошибка удаления рецепта',
            noData: 'Рецепты не найдены',
            deleteConfirm: 'Вы уверены, что хотите удалить рецепт?',
            deleteSuccess: 'Рецепт успешно удален',
            searchPlaceholder: 'Поиск по названию...',
            filterAllCategories: 'Все категории',
            filterAllAuthors: 'Все авторы',
            recipesCount: 'рецептов'
        };
    }

    async init() {
        console.log('RecipesAdminApp initialized');
        await this.loadCategories();
        await this.loadAuthors();
        await this.loadRecipes();
        this.setupEventListeners();
    }

    async loadRecipes(search = '', categoryId = '', authorId = '') {
        try {
            console.log('Loading recipes with filters:', { search, categoryId, authorId });
            CommonUtils.showLoadingState('recipesTableBody', this.translations.loading, 7);

            const params = new URLSearchParams();
            if (search) params.append('search', search);
            if (categoryId) params.append('categoryId', categoryId);
            if (authorId) params.append('authorId', authorId);

            const url = `/recipes?${params.toString()}`;
            const response = await this.get(url);
            console.log('Recipes loaded:', response);

            if (response.success) {
                this.recipes = response.data;
                console.log('Recipes count:', this.recipes.length);
                this.displayRecipes();
                this.updateRecipesCount();
            } else {
                CommonUtils.showError('recipesTableBody', this.translations.errorLoad, 7);
            }
        } catch (error) {
            console.error('Error loading recipes:', error);
            CommonUtils.showError('recipesTableBody', this.translations.errorLoad, 7);
        }
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
            CommonUtils.showToast(this.translations.errorLoadCategories, 'error');
        }
    }

    async loadAuthors() {
        try {
            const response = await this.get('/authors');
            if (response.success) {
                this.authors = response.data;
                this.populateAuthorFilter();
            }
        } catch (error) {
            console.error('Error loading authors:', error);
            CommonUtils.showToast(this.translations.errorLoadAuthors, 'error');
        }
    }

    populateCategoryFilter() {
        const select = document.getElementById('categoryFilter');
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }

        this.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });
    }

    populateAuthorFilter() {
        const select = document.getElementById('authorFilter');
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }

        this.authors.forEach(author => {
            const option = document.createElement('option');
            option.value = author.id;
            option.textContent = author.user ? author.user.username : author.username || 'Неизвестен';
            select.appendChild(option);
        });
    }

    displayRecipes() {
        const tbody = document.getElementById('recipesTableBody');

        if (this.recipes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        ${this.translations.noData}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.recipes.map(recipe => `
            <tr>
                <td>${recipe.id}</td>
                <td class="fw-bold">${CommonUtils.escapeHtml(recipe.title)}</td>
                <td>${CommonUtils.escapeHtml(recipe.category?.name || recipe.categoryName || '—')}</td>
                <td>${CommonUtils.escapeHtml(recipe.author?.user?.username || recipe.authorName || '—')}</td>
                <td>
                    <span class="badge bg-info">${recipe.commentCount || 0}</span>
                </td>
                <td class="small">${CommonUtils.formatDateShort(recipe.createdAt)}</td>
                <td class="table-actions">
                    <button class="btn btn-sm btn-outline-danger delete-recipe"
                            data-recipe-id="${recipe.id}"
                            data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}">
                        🗑️ <span class="d-none d-md-inline">Удалить</span>
                    </button>
                </td>
            </tr>
        `).join('');

        this.addEventListeners();
    }

    addEventListeners() {
        document.querySelectorAll('.delete-recipe').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const deleteBtn = e.target.closest('.delete-recipe');
                if (!deleteBtn) {
                    console.error('Delete button not found');
                    return;
                }

                const recipeId = deleteBtn.dataset.recipeId;
                const recipeTitle = deleteBtn.dataset.recipeTitle;

                if (!recipeId || !recipeTitle) {
                    console.error('Recipe ID or title not found in dataset');
                    return;
                }

                this.openDeleteModal(recipeId, recipeTitle);
            });
        });
    }

    setupEventListeners() {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.applyFilters();
                }, 500);
            });
        }

        const categoryFilter = document.getElementById('categoryFilter');
        const authorFilter = document.getElementById('authorFilter');

        if (categoryFilter) {
            categoryFilter.addEventListener('change', () => {
                this.applyFilters();
            });
        }

        if (authorFilter) {
            authorFilter.addEventListener('change', () => {
                this.applyFilters();
            });
        }

        const clearFilters = document.getElementById('clearFilters');
        if (clearFilters) {
            clearFilters.addEventListener('click', () => {
                this.clearFilters();
            });
        }

        const confirmDeleteBtn = document.getElementById('confirmDeleteRecipeBtn');
        if (confirmDeleteBtn) {
            confirmDeleteBtn.addEventListener('click', () => {
                this.deleteRecipe();
            });
        }
    }

    applyFilters() {
        const search = document.getElementById('searchInput').value.trim();
        const categoryId = document.getElementById('categoryFilter').value;
        const authorId = document.getElementById('authorFilter').value;

        this.loadRecipes(search, categoryId, authorId);
    }

    clearFilters() {
        document.getElementById('searchInput').value = '';
        document.getElementById('categoryFilter').value = '';
        document.getElementById('authorFilter').value = '';
        this.applyFilters();
    }

    updateRecipesCount() {
        const countElement = document.getElementById('recipesCount');
        if (countElement) {
            countElement.textContent = `${this.recipes.length} ${this.translations.recipesCount}`;
        }
    }

    openDeleteModal(recipeId, recipeTitle) {
        console.log('Opening delete modal for recipe:', recipeId, recipeTitle);

        const titleElement = document.getElementById('deleteRecipeTitle');
        const modalElement = document.getElementById('deleteRecipeModal');

        if (!titleElement) {
            console.error('Element deleteRecipeTitle not found in DOM');
            return;
        }

        if (!modalElement) {
            console.error('Element deleteRecipeModal not found in DOM');
            return;
        }

        titleElement.textContent = recipeTitle;
        modalElement.dataset.recipeId = recipeId;

        const modal = new bootstrap.Modal(modalElement);
        modal.show();

        console.log('Delete modal opened successfully');
    }

    async deleteRecipe() {
        const modalElement = document.getElementById('deleteRecipeModal');
        const deleteBtn = document.getElementById('confirmDeleteRecipeBtn');

        if (!modalElement || !deleteBtn) {
            console.error('Required elements not found for deletion');
            CommonUtils.showToast('Ошибка: элементы интерфейса не найдены', 'error');
            return;
        }

        const recipeId = modalElement.dataset.recipeId;

        if (!recipeId) {
            CommonUtils.showToast('Ошибка: ID рецепта не найден', 'error');
            return;
        }

        deleteBtn.disabled = true;
        deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Удаление...';

        try {
            console.log('Deleting recipe ID:', recipeId);
            const response = await this.delete(`/recipes/${recipeId}`);
            console.log('Delete response:', response);

            if (response.success) {
                CommonUtils.showToast(response.message || this.translations.deleteSuccess);

                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) {
                    modal.hide();
                }

                await this.loadRecipes();
            } else {
                CommonUtils.showToast(response.message || this.translations.errorDelete, 'error');
            }
        } catch (error) {
            console.error('Error deleting recipe:', error);
            CommonUtils.showToast(this.translations.errorDelete, 'error');
        } finally {
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = 'Удалить';
        }
    }
}

// Инициализация приложения
let recipesApp;
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, initializing RecipesAdminApp...');
    recipesApp = new RecipesAdminApp();
});