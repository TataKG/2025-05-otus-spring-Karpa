// my-recipes.js - для страницы "Мои рецепты"
class MyRecipesApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        console.log("MyRecipesApp initialized");
        this.messages = window.i18nMessages || this.getFallbackMessages();
        this.currentFilter = 'all';
        this.currentUserRecipes = [];
        this.init();
    }

    getFallbackMessages() {
        return {
            loading: 'Загрузка...',
            close: 'Закрыть',
            view: 'Просмотр',
            edit: 'Редактировать',
            delete: 'Удалить',
            cancel: 'Отмена',
            retry: 'Повторить',
            'recipe-details': 'Детали рецепта',
            'recipes-loading': 'Загрузка ваших рецептов...',
            'no-recipes': 'У вас пока нет рецептов',
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
            'status-published': 'Опубликован',
            'status-draft': 'Черновик',
            'delete-confirm-title': 'Подтверждение удаления',
            'delete-confirm-message': 'Вы уверены, что хотите удалить этот рецепт? Это действие нельзя отменить.',
            'delete-success': 'Рецепт успешно удален',
            'delete-error': 'Ошибка при удалении рецепта',
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
            'recipe-details-comments': '💬 Комментарии',
            'recipe-details-status': '📊 Статус',
            'recipe-details-created-date': '📅 Дата создания'
        };
    }

    async init() {
        console.log("MyRecipesApp init started");
        await this.loadMyRecipes();
        this.setupEventListeners();
    }

    async loadMyRecipes() {
        try {
            console.log("Starting to load my recipes...");
            CommonUtils.showLoadingState('recipesTableBody', this.messages['recipes-loading'], 6);

            const response = await this.get('/my-recipes');
            console.log("My recipes response:", response);

            if (response.success) {
                console.log("Recipes loaded successfully:", response.data);
                this.currentUserRecipes = response.data;
                this.updateStatistics(response.data);
                this.displayRecipes(response.data);
            } else {
                console.error("API returned error:", response);
                this.showError(this.messages['error-loading-recipes'] + ': ' + (response.message || 'Unknown error'));
            }
        } catch (error) {
            console.error('Error loading my recipes:', error);
            const errorMessage = CommonUtils.handleApiError(error, this.messages['error-loading-recipes']);
            this.showError(errorMessage);
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

        // Обновляем активную кнопку фильтра
        document.querySelectorAll('.btn-group .btn').forEach(btn => btn.classList.remove('active'));
        document.getElementById(`filter${filterType.charAt(0).toUpperCase() + filterType.slice(1)}`).classList.add('active');

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
                        📝 ${this.messages['no-recipes']}
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
                <td>
                    ${recipe.published ?
                        `<span class="badge bg-success">${this.messages['status-published']}</span>` :
                        `<span class="badge bg-warning text-dark">${this.messages['status-draft']}</span>`
                    }
                </td>
                <td class="text-muted">
                    <small>${CommonUtils.formatDate(recipe.createdAt)}</small>
                </td>
                <td>
                    <span class="badge bg-info text-dark comments-badge"
                          style="cursor: pointer;"
                          data-recipe-id="${recipe.id}"
                          data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                          title="${this.messages.view} ${this.messages.comments.toLowerCase()}">
                        💬 ${recipe.commentCount || 0}
                    </span>
                </td>
                <td class="action-buttons">
                    <div class="btn-group btn-group-sm" role="group">
                        <button class="btn btn-outline-primary view-recipe" data-recipe-id="${recipe.id}">
                            👁️ ${this.messages.view}
                        </button>
                        <button class="btn btn-outline-warning edit-recipe" data-recipe-id="${recipe.id}">
                            ✏️ ${this.messages.edit}
                        </button>
                        ${!recipe.published ? `
                            <button class="btn btn-outline-success publish-recipe" data-recipe-id="${recipe.id}">
                                📢 ${this.messages.publish || 'Опубликовать'}
                            </button>
                        ` : ''}
                        <button class="btn btn-outline-danger delete-recipe" data-recipe-id="${recipe.id}" data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}">
                            🗑️ ${this.messages.delete}
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        this.addRecipeActionListeners();
        this.addCommentsViewListeners();
    }

    addRecipeActionListeners() {
        document.querySelectorAll('.view-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.view-recipe').dataset.recipeId;
                console.log("View recipe:", recipeId);
                this.loadRecipeDetails(recipeId);
            });
        });

        document.querySelectorAll('.edit-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.edit-recipe').dataset.recipeId;
                console.log("Edit recipe:", recipeId);
                this.editRecipe(recipeId);
            });
        });

        // ОБНОВЛЕННЫЙ ОБРАБОТЧИК ДЛЯ КНОПКИ ПУБЛИКАЦИИ
        document.querySelectorAll('.publish-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.publish-recipe').dataset.recipeId;
                console.log("Publish recipe:", recipeId);
                this.publishRecipeSimple(recipeId); // Используем упрощенный метод
            });
        });

        document.querySelectorAll('.delete-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.delete-recipe').dataset.recipeId;
                const recipeTitle = e.target.closest('.delete-recipe').dataset.recipeTitle;
                console.log("Delete recipe:", recipeId, recipeTitle);
                this.showDeleteConfirmation(recipeId, recipeTitle);
            });
        });
    }

    addCommentsViewListeners() {
        document.querySelectorAll('.comments-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.comments-badge').dataset.recipeId;
                const recipeTitle = e.target.closest('.comments-badge').dataset.recipeTitle;
                console.log("View comments for recipe:", recipeId, recipeTitle);
                this.showCommentsModal(recipeId, recipeTitle);
            });
        });
    }

    async loadRecipeDetails(recipeId) {
        try {
            console.log("Loading detailed recipe:", recipeId);
            const response = await this.get(`/${recipeId}/detailed`);

            if (response.success) {
                this.showRecipeModal(response.data);
            } else {
                this.showError(this.messages['error-loading-details']);
            }
        } catch (error) {
            console.error('Error loading recipe details:', error);
            const errorMessage = CommonUtils.handleApiError(error, this.messages['error-loading-details']);
            this.showError(errorMessage);
        }
    }

    async loadRecipeComments(recipeId) {
        try {
            console.log("Loading comments for recipe:", recipeId);
            const response = await this.get(`/${recipeId}/comments`);

            if (response.success) {
                return response.data;
            } else {
                throw new Error(response.message || 'Failed to load comments');
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
                        <p><span class="badge bg-primary">${CommonUtils.escapeHtml(recipe.category?.name || 'Не указана')}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-author']}:</h6>
                        <p class="text-muted">${CommonUtils.escapeHtml(recipe.author?.user?.username || 'Неизвестен')}</p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-ingredients']}:</h6>
                        <div class="list-group">
                            ${(recipe.ingredients || []).map(ingredient => `
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
                            ${(recipe.inventoryItems || []).map(item => `
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

        // Добавляем обработчик для комментариев в модальном окне
        const commentsBadge = modalBody.querySelector('.comments-badge');
        if (commentsBadge) {
            commentsBadge.addEventListener('click', () => {
                const recipeId = commentsBadge.dataset.recipeId;
                const recipeTitle = commentsBadge.dataset.recipeTitle;
                this.showCommentsModal(recipeId, recipeTitle);

                // Закрываем текущее модальное окно
                const recipeModal = bootstrap.Modal.getInstance(document.getElementById('recipeModal'));
                recipeModal.hide();
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
            console.error('Error loading comments:', error);
            modalBody.innerHTML = `
                <div class="alert alert-danger">
                    <p>${this.messages['error-loading-comments']}</p>
                    <button class="btn btn-sm btn-outline-primary" onclick="myRecipesApp.showCommentsModal('${recipeId}', '${CommonUtils.escapeHtml(recipeTitle)}')">
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

    editRecipe(recipeId) {
        window.location.href = `/recipe/edit/${recipeId}`;
    }

    showDeleteConfirmation(recipeId, recipeTitle) {
        document.getElementById('recipeToDeleteTitle').textContent = recipeTitle;

        const confirmBtn = document.getElementById('confirmDeleteBtn');
        // Удаляем старые обработчики и добавляем новый
        confirmBtn.replaceWith(confirmBtn.cloneNode(true));
        const newConfirmBtn = document.getElementById('confirmDeleteBtn');

        newConfirmBtn.onclick = () => {
            this.deleteRecipe(recipeId);
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
            modal.hide();
        };

        const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        modal.show();
    }

    async deleteRecipe(recipeId) {
        try {
            console.log("Deleting recipe:", recipeId);
            const response = await this.delete(`/${recipeId}`);

            if (response.success) {
                CommonUtils.showToast(this.messages['delete-success'], 'success');

                // Обновляем локальный массив и перерисовываем таблицу
                this.currentUserRecipes = this.currentUserRecipes.filter(recipe => recipe.id != recipeId);
                this.updateStatistics(this.currentUserRecipes);
                this.displayRecipes(this.currentUserRecipes);

            } else {
                CommonUtils.showToast(this.messages['delete-error'], 'error');
            }
        } catch (error) {
            console.error('Error deleting recipe:', error);

            // Обрабатываем 404 ошибку как успешное удаление
            if (error.message.includes('404')) {
                CommonUtils.showToast(this.messages['delete-success'], 'success');
                // Обновляем локальный список
                this.currentUserRecipes = this.currentUserRecipes.filter(recipe => recipe.id != recipeId);
                this.updateStatistics(this.currentUserRecipes);
                this.displayRecipes(this.currentUserRecipes);
            } else {
                const errorMessage = CommonUtils.handleApiError(error, this.messages['delete-error']);
                CommonUtils.showToast(errorMessage, 'error');
            }
        }
    }

    async publishRecipeSimple(recipeId) {
        try {
            console.log("Publishing recipe (simple):", recipeId);

            // Показываем индикатор загрузки
            const publishButton = document.querySelector(`.publish-recipe[data-recipe-id="${recipeId}"]`);
            if (publishButton) {
                const originalText = publishButton.innerHTML;
                publishButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Публикация...';
                publishButton.disabled = true;
            }

            // Используем существующий endpoint PATCH /api/recipes/{id}/publish
            const response = await this.patch(`/${recipeId}/publish`, {});

            if (response.success) {
                CommonUtils.showToast(this.messages['publish-success'] || 'Рецепт успешно опубликован!', 'success');

                // Обновляем локальные данные
                const recipeIndex = this.currentUserRecipes.findIndex(r => r.id == recipeId);
                if (recipeIndex !== -1) {
                    this.currentUserRecipes[recipeIndex].published = true;
                }

                this.updateStatistics(this.currentUserRecipes);
                this.displayRecipes(this.currentUserRecipes);

            } else {
                throw new Error(response.message || 'Ошибка при публикации рецепта');
            }

        } catch (error) {
            console.error('Error publishing recipe:', error);

            // Восстанавливаем кнопку
            const publishButton = document.querySelector(`.publish-recipe[data-recipe-id="${recipeId}"]`);
            if (publishButton) {
                publishButton.innerHTML = '📢 Опубликовать';
                publishButton.disabled = false;
            }

            const errorMessage = CommonUtils.handleApiError(error, this.messages['publish-error'] || 'Ошибка при публикации рецепта');
            CommonUtils.showToast(errorMessage, 'error');
        }
    }

    setupEventListeners() {
        console.log("Event listeners setup");
    }

    showError(message) {
        CommonUtils.showError('recipesTableBody', message, 6);
        CommonUtils.showToast(message, 'error');
    }
}

// Инициализация приложения
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM Content Loaded - initializing MyRecipesApp");
    try {
        window.myRecipesApp = new MyRecipesApp();
        console.log("MyRecipesApp initialized successfully");
    } catch (error) {
        console.error("Error initializing MyRecipesApp:", error);
    }
});