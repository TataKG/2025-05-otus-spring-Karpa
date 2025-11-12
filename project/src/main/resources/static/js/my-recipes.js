// my-recipes.js - для страницы "Мои рецепты"
class MyRecipesApp extends BaseApiClient {
    handleLanguageChange() {
        // При смене языка просто перезагружаем данные
        this.loadMyRecipes();
    }

    constructor() {
        super('/api/recipes');
        console.log("MyRecipesApp initialized");
        this.messages = window.i18nMessages || this.getFallbackMessages();
        this.currentFilter = 'all';
        this.currentUserRecipes = [];

        window.addEventListener('languageChange', () => {
            this.handleLanguageChange();
        });

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
            publish: 'Опубликовать',
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
            'recipe-details-created-date': '📅 Дата создания',
            'publish-success': 'Рецепт успешно опубликован',
            'publish-error': 'Ошибка при публикации рецепта'
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
        const totalComments = recipes.reduce((sum, recipe) => sum + CommonUtils.getCommentCount(recipe), 0);

        document.getElementById('totalRecipesCount').textContent = totalRecipes;
        document.getElementById('publishedRecipesCount').textContent = publishedRecipes;
        document.getElementById('draftRecipesCount').textContent = draftRecipes;
        document.getElementById('totalCommentsCount').textContent = totalComments;
    }

    filterRecipes(filterType) {
        this.currentFilter = filterType;

        // Обновляем активную кнопку фильтра
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
                        📝 ${this.messages['no-recipes']}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = recipes.map(recipe => {
            const categoryName = CommonUtils.getCategoryName(recipe);
            const commentCount = CommonUtils.getCommentCount(recipe);

            return `
                <tr>
                    <td class="fw-bold text-primary">${CommonUtils.escapeHtml(recipe.title)}</td>
                    <td>
                        <span class="badge bg-secondary">${CommonUtils.escapeHtml(categoryName)}</span>
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
                            💬 ${commentCount}
                        </span>
                    </td>
                    <td class="action-buttons">
                        <div class="btn-group btn-group-sm" role="group">
                            <button class="btn btn-outline-primary view-recipe"
                                    data-recipe-id="${recipe.id}"
                                    title="${this.messages.view}">
                                👁️ ${this.messages.view}
                            </button>
                            <button class="btn btn-outline-warning edit-recipe"
                                    data-recipe-id="${recipe.id}"
                                    title="${this.messages.edit}">
                                ✏️ ${this.messages.edit}
                            </button>
                            ${!recipe.published ? `
                                <button class="btn btn-outline-success publish-recipe"
                                        data-recipe-id="${recipe.id}"
                                        title="${this.messages.publish}">
                                    📢 ${this.messages.publish}
                                </button>
                            ` : ''}
                            <button class="btn btn-outline-danger delete-recipe"
                                    data-recipe-id="${recipe.id}"
                                    data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}"
                                    title="${this.messages.delete}">
                                🗑️ ${this.messages.delete}
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
        // View recipe
        document.querySelectorAll('.view-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.view-recipe').dataset.recipeId;
                console.log("View recipe:", recipeId);
                this.loadRecipeDetails(recipeId);
            });
        });

        // Edit recipe
        document.querySelectorAll('.edit-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.edit-recipe').dataset.recipeId;
                console.log("Edit recipe:", recipeId);
                this.editRecipe(recipeId);
            });
        });

        // Publish recipe
        document.querySelectorAll('.publish-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.publish-recipe').dataset.recipeId;
                console.log("Publish recipe:", recipeId);
                this.publishRecipe(recipeId);
            });
        });

        // Delete recipe
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

                if (!recipeId || recipeId === 'undefined') {
                    console.error('Invalid recipeId:', recipeId);
                    CommonUtils.showToast('Неверный идентификатор рецепта', 'error');
                    return;
                }

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

            // Используем прямой fetch для комментариев
            const response = await fetch(`/api/recipes/${recipeId}/comments`, {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            console.log(`📨 Comments response status: ${response.status}`);

            if (!response.ok) {
                if (response.status === 404 || response.status === 403) {
                    console.log('Comments not available for this recipe');
                    return [];
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log(`✅ Comments loaded:`, result);

            if (result.success) {
                return result.data || [];
            } else {
                console.warn('API returned error for comments:', result.message);
                return [];
            }
        } catch (error) {
            console.error('Error loading recipe comments:', error);
            throw error;
        }
    }

    showRecipeModal(data) {
        console.log("Recipe detailed data for modal:", data);

        // Извлекаем данные из правильной структуры
        const recipe = data.recipe || data;
        const inventoryItems = data.inventoryItems || recipe.inventoryItems || [];
        const comments = data.comments || [];
        const totalCommentCount = data.totalCommentCount || comments.length;

        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        modalTitle.textContent = `📖 ${CommonUtils.escapeHtml(recipe.title)}`;

        // Получаем данные с учетом вложенной структуры
        const categoryName = this.getCategoryNameFromRecipe(recipe);
        const authorName = this.getAuthorNameFromRecipe(recipe);
        const ingredients = recipe.ingredients || [];
        const description = recipe.description || 'Описание отсутствует';

        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-category']}:</h6>
                        <p><span class="badge bg-primary">${CommonUtils.escapeHtml(categoryName)}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-author']}:</h6>
                        <p class="text-muted">${CommonUtils.escapeHtml(authorName)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-ingredients']}:</h6>
                        <div class="list-group">
                            ${this.getIngredientsList(ingredients)}
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-description']}:</h6>
                        <p class="text-muted border-start border-3 border-primary ps-3 py-2 bg-light">
                            ${CommonUtils.escapeHtml(description)}
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>${this.messages['recipe-details-inventory']}:</h6>
                        <div class="d-flex flex-wrap gap-2">
                            ${this.getInventoryItems(inventoryItems)}
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
                                💬 ${totalCommentCount} ${CommonUtils.getCommentText(totalCommentCount)}
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

                if (!recipeId || recipeId === 'undefined') {
                    console.error('Invalid recipeId in modal badge:', recipeId);
                    CommonUtils.showToast('Неверный идентификатор рецепта', 'error');
                    return;
                }

                this.showCommentsModal(recipeId, recipeTitle);

                // Закрываем текущее модальное окно
                const recipeModal = bootstrap.Modal.getInstance(document.getElementById('recipeModal'));
                if (recipeModal) {
                    recipeModal.hide();
                }
            });
        }

        const modal = new bootstrap.Modal(document.getElementById('recipeModal'));
        modal.show();
    }

    // Специальные методы для извлечения данных из вложенной структуры
    getCategoryNameFromRecipe(recipe) {
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

    getAuthorNameFromRecipe(recipe) {
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

    getInventoryItems(inventoryItems) {
        if (!inventoryItems || inventoryItems.length === 0) {
            return '<span class="text-muted">Инвентарь не указан</span>';
        }

        return inventoryItems.map(item => {
            const itemName = item.name || item;
            return `<span class="badge bg-warning text-dark">🍴 ${CommonUtils.escapeHtml(itemName)}</span>`;
        }).join('');
    }

    async showCommentsModal(recipeId, recipeTitle) {
        console.log("showCommentsModal called with:", { recipeId, recipeTitle });

        if (!recipeId || recipeId === 'undefined') {
            console.error('Invalid recipeId:', recipeId);
            CommonUtils.showToast('Неверный идентификатор рецепта', 'error');
            return;
        }

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
                    <p class="small text-muted">Ошибка: ${error.message}</p>
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
                                        👤 ${CommonUtils.escapeHtml(comment.userName || comment.author || comment.user?.username || this.messages['comment-author'])}
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
            if (modal) {
                modal.hide();
            }
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

    async publishRecipe(recipeId) {
        try {
            console.log("Publishing recipe:", recipeId);

            // Показываем индикатор загрузки
            const publishButton = document.querySelector(`.publish-recipe[data-recipe-id="${recipeId}"]`);
            if (publishButton) {
                const originalText = publishButton.innerHTML;
                publishButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Публикация...';
                publishButton.disabled = true;
            }

            const response = await this.patch(`/${recipeId}/publish`, {});

            if (response.success) {
                CommonUtils.showToast(this.messages['publish-success'], 'success');

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

            const errorMessage = CommonUtils.handleApiError(error, this.messages['publish-error']);
            CommonUtils.showToast(errorMessage, 'error');
        }
    }

    setupEventListeners() {
        console.log("Event listeners setup");

        // Фильтры
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