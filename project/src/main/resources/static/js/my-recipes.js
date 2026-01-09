// my-recipes.js - только обработка сообщений с бэкенда
class MyRecipesApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        this.currentFilter = 'all';
        this.currentUserRecipes = [];
        this.init();
    }

    async init() {
        await this.loadMyRecipes();
        this.setupEventListeners();
    }

    async loadMyRecipes() {
        try {
            CommonUtils.showLoadingState('recipesTableBody', 'Загрузка ваших рецептов...', 6);

            const response = await this.get('/my-recipes');

            if (response.success) {
                this.currentUserRecipes = response.data;
                this.updateStatistics(response.data);
                this.displayRecipes(response.data);
            } else {
                this.showError(response.message);
            }
        } catch (error) {
            this.showError(error.message);
        }
    }

    updateStatistics(recipes) {
        const totalRecipes = recipes.length;
        const publishedRecipes = recipes.filter(recipe => recipe.published).length;
        const draftRecipes = totalRecipes - publishedRecipes;
        const totalComments = recipes.reduce((sum, recipe) => sum + (recipe.commentCount || 0), 0);

        document.getElementById('totalRecipesCount').textContent = totalRecipes;
        document.getElementById('publishedRecipesCount').textContent = publishedRecipes;
        document.getElementById('draftRecipesCount').textContent = draftRecipes;
        document.getElementById('totalCommentsCount').textContent = totalComments;
    }

    filterRecipes(filterType) {
        this.currentFilter = filterType;

        document.querySelectorAll('.btn-group .btn').forEach(btn => btn.classList.remove('active'));
        const filterButton = document.getElementById(`filter${filterType.charAt(0).toUpperCase() + filterType.slice(1)}`);
        if (filterButton) {
            filterButton.classList.add('active');
        }

        let filteredRecipes = this.currentUserRecipes;

        switch(filterType) {
            case 'published':
                filteredRecipes = this.currentUserRecipes.filter(recipe => recipe.published);
                break;
            case 'drafts':
                filteredRecipes = this.currentUserRecipes.filter(recipe => !recipe.published);
                break;
            case 'all':
            default:
                filteredRecipes = this.currentUserRecipes;
        }

        this.displayRecipes(filteredRecipes);
    }

    displayRecipes(recipes) {
        const tbody = document.getElementById('recipesTableBody');

        if (!recipes || recipes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-4 text-muted">
                        📝 У вас пока нет рецептов
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = recipes.map(recipe => {
            const categoryName = recipe.categoryName || 'Не указана';
            const commentCount = recipe.commentCount || 0;

            return `
                <tr>
                    <td class="fw-bold text-primary">${CommonUtils.escapeHtml(recipe.title)}</td>
                    <td>
                        <span class="badge bg-secondary">${CommonUtils.escapeHtml(categoryName)}</span>
                    </td>
                    <td>
                        ${recipe.published ?
                            `<span class="badge bg-success">Опубликован</span>` :
                            `<span class="badge bg-warning text-dark">Черновик</span>`
                        }
                    </td>
                    <td class="text-muted">
                        <small>${CommonUtils.formatDateShort(recipe.createdAt)}</small>
                    </td>
                    <td>
                        <span class="badge bg-info text-dark comments-badge"
                              style="cursor: pointer;"
                              data-recipe-id="${recipe.id}"
                              data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                              title="Просмотр комментариев">
                            💬 ${commentCount}
                        </span>
                    </td>
                    <td class="action-buttons">
                        <div class="btn-group btn-group-sm" role="group">
                            <button class="btn btn-outline-primary view-recipe"
                                    data-recipe-id="${recipe.id}"
                                    title="Просмотр">
                                👁️ Просмотр
                            </button>
                            <button class="btn btn-outline-warning edit-recipe"
                                    data-recipe-id="${recipe.id}"
                                    title="Редактировать">
                                ✏️ Редактировать
                            </button>
                            ${!recipe.published ? `
                                <button class="btn btn-outline-success publish-recipe"
                                        data-recipe-id="${recipe.id}"
                                        title="Опубликовать">
                                    📢 Опубликовать
                                </button>
                            ` : ''}
                            <button class="btn btn-outline-danger delete-recipe"
                                    data-recipe-id="${recipe.id}"
                                    data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                                    title="Удалить">
                                🗑️ Удалить
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        this.addRecipeActionListeners();
        this.addCommentsViewListeners();
    }

    addRecipeActionListeners() {
        document.querySelectorAll('.view-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.view-recipe').dataset.recipeId;
                this.loadRecipeDetails(recipeId);
            });
        });

        document.querySelectorAll('.edit-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.edit-recipe').dataset.recipeId;
                this.editRecipe(recipeId);
            });
        });

        document.querySelectorAll('.publish-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.publish-recipe').dataset.recipeId;
                this.publishRecipe(recipeId);
            });
        });

        document.querySelectorAll('.delete-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.delete-recipe').dataset.recipeId;
                const recipeTitle = e.target.closest('.delete-recipe').dataset.recipeTitle;
                this.showDeleteConfirmation(recipeId, recipeTitle);
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
            const response = await this.get(`/${recipeId}/detailed`);

            if (response.success) {
                this.showRecipeModal(response.data);
            } else {
                CommonUtils.showToast(response.message, 'error');
            }
        } catch (error) {
            CommonUtils.showToast(error.message, 'error');
        }
    }

    showRecipeModal(recipe) {
        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        modalTitle.textContent = `📖 ${CommonUtils.escapeHtml(recipe.title)}`;

        const categoryName = recipe.category ? recipe.category.name : 'Не указана';
        const authorName = recipe.author ? this.getAuthorDisplayName(recipe.author) : 'Неизвестен';
        const ingredients = recipe.ingredients || [];
        const description = recipe.description || 'Описание отсутствует';
        const inventoryItems = recipe.inventoryItems || [];
        const commentCount = recipe.commentCount || 0;

        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>📂 Категория:</h6>
                        <p><span class="badge bg-primary">${CommonUtils.escapeHtml(categoryName)}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>👨‍🍳 Автор:</h6>
                        <p class="text-muted">${CommonUtils.escapeHtml(authorName)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>🛒 Ингредиенты:</h6>
                        <div class="list-group">
                            ${this.getIngredientsList(ingredients)}
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>📝 Описание:</h6>
                        <p class="text-muted border-start border-3 border-primary ps-3 py-2 bg-light">
                            ${CommonUtils.escapeHtml(description)}
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>🔧 Необходимый инвентарь:</h6>
                        <div class="d-flex flex-wrap gap-2">
                            ${this.getInventoryItemsList(inventoryItems)}
                        </div>
                    </div>

                    <div class="mb-3">
                        <h6>💬 Комментарии:</h6>
                        <p>
                            <span class="badge bg-info text-dark comments-badge"
                                  style="cursor: pointer;"
                                  data-recipe-id="${recipe.id}"
                                  data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                                  title="Просмотр комментариев">
                                💬 ${commentCount}
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

                const recipeModal = bootstrap.Modal.getInstance(document.getElementById('recipeModal'));
                if (recipeModal) {
                    recipeModal.hide();
                }
            });
        }

        const modal = new bootstrap.Modal(document.getElementById('recipeModal'));
        modal.show();
    }

    getAuthorDisplayName(author) {
        if (!author) return 'Неизвестен';

        if (author.username) {
            return author.username;
        } else if (author.name) {
            return author.name;
        } else if (author.user && author.user.username) {
            return author.user.username;
        }
        return 'Неизвестен';
    }

    getIngredientsList(ingredients) {
        if (!ingredients || ingredients.length === 0) {
            return '<div class="list-group-item text-muted">Ингредиенты не указаны</div>';
        }

        return ingredients.map(ingredient => `
            <div class="list-group-item list-group-item-action">
                ${CommonUtils.escapeHtml(ingredient)}
            </div>
        `).join('');
    }

    getInventoryItemsList(inventoryItems) {
        if (!inventoryItems || inventoryItems.length === 0) {
            return '<span class="text-muted">Инвентарь не указан</span>';
        }

        return inventoryItems.map(item => {
            const itemName = item.name || item;
            return `<span class="badge bg-warning text-dark">🍴 ${CommonUtils.escapeHtml(itemName)}</span>`;
        }).join('');
    }

    async showCommentsModal(recipeId, recipeTitle) {
        const modalTitle = document.getElementById('commentsModalTitle');
        const modalBody = document.getElementById('commentsModalBody');

        modalTitle.textContent = `💬 Комментарии: ${CommonUtils.escapeHtml(recipeTitle)}`;

        modalBody.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Загрузка...</span>
                </div>
                <p class="mt-2 text-muted">Загрузка комментариев...</p>
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
                    <p>Ошибка при загрузке комментариев</p>
                    <button class="btn btn-sm btn-outline-primary" onclick="myRecipesApp.showCommentsModal('${recipeId}', '${CommonUtils.escapeHtml(recipeTitle)}')">
                        Повторить
                    </button>
                </div>
            `;
        }
    }

    async loadRecipeComments(recipeId) {
        const response = await fetch(`/api/recipes/${recipeId}/comments`);
        const result = await response.json();
        return result.success ? result.data || [] : [];
    }

    displayComments(comments, modalBody) {
        if (!comments || comments.length === 0) {
            modalBody.innerHTML = `
                <div class="text-center py-4">
                    <div class="text-muted mb-2">💬</div>
                    <p class="text-muted">Комментариев пока нет</p>
                </div>
            `;
            return;
        }

        modalBody.innerHTML = comments.map(comment => `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <h6 class="card-title mb-1">
                                👤 ${CommonUtils.escapeHtml(comment.userName || comment.author || comment.user?.username || 'Автор')}
                            </h6>
                            <small class="text-muted">
                                📅 ${new Date(comment.createdAt).toLocaleString()}
                            </small>
                        </div>
                    </div>
                    <p class="card-text mt-3">${CommonUtils.escapeHtml(comment.content)}</p>
                </div>
            </div>
        `).join('');
    }

    editRecipe(recipeId) {
        window.location.href = `/recipe/edit/${recipeId}`;
    }

    showDeleteConfirmation(recipeId, recipeTitle) {
        document.getElementById('recipeToDeleteTitle').textContent = recipeTitle;

        const confirmBtn = document.getElementById('confirmDeleteBtn');
        confirmBtn.replaceWith(confirmBtn.cloneNode(true));
        const newConfirmBtn = document.getElementById('confirmDeleteBtn');

        newConfirmBtn.onclick = () => {
            this.deleteRecipe(recipeId);
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
            if (modal) {
                modal.hide();
            }
        };

        const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        modal.show();
    }

    async deleteRecipe(recipeId) {
        try {
            const response = await this.delete(`/${recipeId}`);

            if (response.success) {
                CommonUtils.showToast(response.message, 'success');
                this.currentUserRecipes = this.currentUserRecipes.filter(recipe => recipe.id != recipeId);
                this.updateStatistics(this.currentUserRecipes);
                this.displayRecipes(this.currentUserRecipes);
            } else {
                CommonUtils.showToast(response.message, 'error');
            }
        } catch (error) {
            CommonUtils.showToast(error.message, 'error');
        }
    }

    async publishRecipe(recipeId) {
        try {
            const publishButton = document.querySelector(`.publish-recipe[data-recipe-id="${recipeId}"]`);
            if (publishButton) {
                const originalText = publishButton.innerHTML;
                publishButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Публикация...';
                publishButton.disabled = true;
            }

            const response = await this.patch(`/${recipeId}/publish`, {});

            if (response.success) {
                CommonUtils.showToast(response.message, 'success');
                const recipeIndex = this.currentUserRecipes.findIndex(r => r.id == recipeId);
                if (recipeIndex !== -1) {
                    this.currentUserRecipes[recipeIndex].published = true;
                }
                this.updateStatistics(this.currentUserRecipes);
                this.displayRecipes(this.currentUserRecipes);
            } else {
                throw new Error(response.message);
            }

        } catch (error) {
            const publishButton = document.querySelector(`.publish-recipe[data-recipe-id="${recipeId}"]`);
            if (publishButton) {
                publishButton.innerHTML = '📢 Опубликовать';
                publishButton.disabled = false;
            }
            CommonUtils.showToast(error.message, 'error');
        }
    }

    setupEventListeners() {
        const filterAll = document.getElementById('filterAll');
        const filterPublished = document.getElementById('filterPublished');
        const filterDrafts = document.getElementById('filterDrafts');

        if (filterAll) {
            filterAll.addEventListener('click', () => this.filterRecipes('all'));
        }
        if (filterPublished) {
            filterPublished.addEventListener('click', () => this.filterRecipes('published'));
        }
        if (filterDrafts) {
            filterDrafts.addEventListener('click', () => this.filterRecipes('drafts'));
        }
    }

    showError(message) {
        CommonUtils.showError('recipesTableBody', message, 6);
        CommonUtils.showToast(message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.myRecipesApp = new MyRecipesApp();
});