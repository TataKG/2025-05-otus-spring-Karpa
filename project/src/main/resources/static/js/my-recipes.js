// my-recipes.js - для страницы "Мои рецепты"
class MyRecipesApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        this.messages = this.loadMessages();
        this.currentFilter = 'all';
        this.currentUserRecipes = [];
        this.init();
    }

    loadMessages() {
        const messageContainer = document.getElementById('i18n-messages');
        if (!messageContainer) {
            return this.getFallbackMessages();
        }

        const messages = {};
        const attributes = messageContainer.attributes;

        for (let i = 0; i < attributes.length; i++) {
            const attr = attributes[i];
            if (attr.name.startsWith('data-')) {
                const key = attr.name.replace('data-', '');
                let value = attr.value;
                // Убираем Thymeleaf выражения если они есть
                if (value.startsWith('#{') && value.endsWith('}')) {
                    value = value.slice(2, -1);
                }
                messages[key] = this.getFallbackText(key, value);
            }
        }

        return messages;
    }

    getFallbackText(key, thymeleafExpression) {
        const fallbackTexts = {
            'loading': 'Загрузка...',
            'close': 'Закрыть',
            'view': 'Детальнее',
            'edit': 'Редактировать',
            'delete': 'Удалить',
            'recipe-details': 'Детали рецепта',
            'myrecipes-loading': 'Загрузка ваших рецептов...',
            'no-recipes': 'У вас пока нет рецептов',
            'retry': 'Повторить',
            'category': 'Категория',
            'author': 'Автор',
            'comments': 'Комментарии',
            'actions': 'Действия',
            'ingredients': 'Ингредиенты',
            'description': 'Описание',
            'inventory': 'Необходимый инвентарь',
            'status-published': 'Опубликован',
            'status-draft': 'Черновик',
            'delete-confirm-title': 'Подтверждение удаления',
            'delete-confirm-message': 'Вы уверены, что хотите удалить этот рецепт? Это действие нельзя отменить.',
            'cancel': 'Отмена',
            'delete-success': 'Рецепт успешно удален',
            'delete-error': 'Ошибка при удалении рецепта'
        };

        return fallbackTexts[key] || thymeleafExpression || key;
    }

    getFallbackMessages() {
        return {
            loading: 'Загрузка...',
            close: 'Закрыть',
            view: 'Детальнее',
            edit: 'Редактировать',
            delete: 'Удалить',
            recipeDetails: 'Детали рецепта',
            recipesLoading: 'Загрузка ваших рецептов...',
            noRecipesFound: 'У вас пока нет рецептов',
            errorLoadingRecipes: 'Ошибка при загрузке рецептов',
            errorLoadingDetails: 'Ошибка при загрузке деталей рецепта',
            retry: 'Повторить',
            category: 'Категория',
            author: 'Автор',
            comments: 'Комментарии',
            actions: 'Действия',
            ingredients: 'Ингредиенты',
            description: 'Описание',
            inventory: 'Необходимый инвентарь',
            statusPublished: 'Опубликован',
            statusDraft: 'Черновик',
            deleteConfirmTitle: 'Подтверждение удаления',
            deleteConfirmMessage: 'Вы уверены, что хотите удалить этот рецепт? Это действие нельзя отменить.',
            cancel: 'Отмена',
            deleteSuccess: 'Рецепт успешно удален',
            deleteError: 'Ошибка при удалении рецепта'
        };
    }

    init() {
        this.loadMyRecipes();
        this.setupEventListeners();
    }

    async loadMyRecipes() {
        try {
            CommonUtils.showLoadingState('recipesTableBody', this.messages['myrecipes-loading'], 6);
            const response = await this.get('/my-recipes');

            if (response.success) {
                this.currentUserRecipes = response.data;
                this.updateStatistics(response.data);
                this.displayRecipes(response.data);
            } else {
                this.showError('Ошибка при загрузке ваших рецептов');
            }
        } catch (error) {
            console.error('Error loading my recipes:', error);
            this.showError('Не удалось загрузить ваши рецепты');
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
                    <span class="badge bg-info text-dark">
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
                        <button class="btn btn-outline-danger delete-recipe" data-recipe-id="${recipe.id}" data-recipe-title="${CommonUtils.escapeHtml(recipe.title)}">
                            🗑️ ${this.messages.delete}
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        this.addRecipeActionListeners();
    }

    async loadRecipeDetails(recipeId) {
        try {
            const response = await this.get(`/${recipeId}/detailed`);

            if (response.success) {
                this.showRecipeModal(response.data);
            } else {
                this.showError(this.messages['error-loading-details']);
            }
        } catch (error) {
            console.error('Error loading recipe details:', error);
            this.showError(this.messages['error-loading-details']);
        }
    }

    editRecipe(recipeId) {
        window.location.href = `/recipe/edit/${recipeId}`;
    }

    async deleteRecipe(recipeId) {
        try {
            const response = await this.delete(`/${recipeId}`);

            if (response.success) {
                CommonUtils.showToast(response.message || this.messages['delete-success'], 'success');
                this.loadMyRecipes();
            } else {
                CommonUtils.showToast(response.message || this.messages['delete-error'], 'error');
            }
        } catch (error) {
            console.error('Error deleting recipe:', error);
            CommonUtils.showToast(`${this.messages['delete-error']}: ${error.message}`, 'error');
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
                        <h6>📂 ${this.messages.category}:</h6>
                        <p><span class="badge bg-primary">${CommonUtils.escapeHtml(recipe.category.name)}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>👨‍🍳 ${this.messages.author}:</h6>
                        <p class="text-muted">${CommonUtils.escapeHtml(recipe.author.user.username)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>📊 Статус:</h6>
                        <p>
                            ${recipe.published ?
                                `<span class="badge bg-success">${this.messages['status-published']}</span>` :
                                `<span class="badge bg-warning text-dark">${this.messages['status-draft']}</span>`
                            }
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>📅 Дата создания:</h6>
                        <p class="text-muted">${CommonUtils.formatDate(recipe.createdAt)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>🛒 ${this.messages.ingredients}:</h6>
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
                        <h6>📝 ${this.messages.description}:</h6>
                        <p class="text-muted border-start border-3 border-primary ps-3 py-2 bg-light">
                            ${CommonUtils.escapeHtml(recipe.description)}
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>🔧 ${this.messages.inventory}:</h6>
                        <div class="d-flex flex-wrap gap-2">
                            ${(recipe.inventoryItems || []).map(item => `
                                <span class="badge bg-warning text-dark">
                                    🍴 ${CommonUtils.escapeHtml(item.name)}
                                </span>
                            `).join('')}
                        </div>
                    </div>

                    <div class="mb-3">
                        <h6>💬 ${this.messages.comments}:</h6>
                        <p>
                            <span class="badge bg-info text-dark">
                                💬 ${recipe.commentCount || 0} ${CommonUtils.getCommentText(recipe.commentCount || 0)}
                            </span>
                        </p>
                    </div>
                </div>
            </div>
        `;

        const modal = new bootstrap.Modal(document.getElementById('recipeModal'));
        modal.show();
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

        document.querySelectorAll('.delete-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.delete-recipe').dataset.recipeId;
                const recipeTitle = e.target.closest('.delete-recipe').dataset.recipeTitle;
                this.showDeleteConfirmation(recipeId, recipeTitle);
            });
        });
    }

    showDeleteConfirmation(recipeId, recipeTitle) {
        document.getElementById('recipeToDeleteTitle').textContent = recipeTitle;

        const confirmBtn = document.getElementById('confirmDeleteBtn');
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

    setupEventListeners() {
        // Дополнительные обработчики можно добавить здесь
    }

    showError(message) {
        CommonUtils.showError('recipesTableBody', message, 6);
        CommonUtils.showToast(message, 'error');
    }

    showSuccess(message) {
        CommonUtils.showToast(message, 'success');
    }
}

let myRecipesApp;
document.addEventListener('DOMContentLoaded', () => {
    myRecipesApp = new MyRecipesApp();
});