// app.js - для главной страницы с исправленным обновлением комментариев
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
        this.currentUserId = null;
        this.currentRecipeId = null;
        this.currentRecipeTitle = null;

        window.addEventListener('languageChange', () => {
            this.handleLanguageChange();
        });

        this.init();
    }

    handleLanguageChange() {
        this.loadCategories();
        this.loadAuthors();
        this.loadRecipes();
    }

    getFallbackMessages() {
        return {
            loading: 'Загрузка...',
            close: 'Закрыть',
            view: 'Просмотр',
            retry: 'Повторить',
            cancel: 'Отмена',
            save: 'Сохранить',
            delete: 'Удалить',
            edit: 'Редактировать',
            confirm: 'Подтвердить',
            'common-saving': 'Сохранение...',
            'common-adding': 'Добавление...',
            'common-deleting': 'Удаление...',
            'common-confirm-delete': 'Подтвердить удаление',
            'common-action-irreversible': 'Это действие нельзя отменить.',
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
            'comment-author': 'Автор комментария',
            'comment-date': 'Дата',
            'comment-content': 'Комментарий',
            'comments-loading': 'Загрузка комментариев...',
            'recipe-details-category': '📂 Категория',
            'recipe-details-author': '👨‍🍳 Автор',
            'recipe-details-ingredients': '🛒 Ингредиенты',
            'recipe-details-description': '📝 Описание',
            'recipe-details-inventory': '🔧 Необходимый инвентарь',
            'recipe-details-comments': '💬 Комментарии',
            'comment-edit': 'Редактировать комментарий',
            'comment-edit-title': 'Редактирование комментария',
            'comment-delete-confirm-title': 'Подтверждение удаления',
            'comment-delete-confirm-message': 'Вы уверены, что хотите удалить этот комментарий?',
            'comment-delete-confirm-warning': 'Это действие нельзя отменить.',
            'comment-placeholder': 'Введите ваш комментарий...',
            'comment-add': 'Добавить комментарий',
            'comment-add-button': 'Добавить комментарий',
            'comment-save': 'Сохранить комментарий',
            'comment-cancel-edit': 'Отменить редактирование',
            'comment-saving': 'Сохранение...',
            'comment-adding': 'Добавление...',
            'comment-deleting': 'Удаление...',
            'comment-login-required': 'Войдите, чтобы оставить комментарий',
            'comment-updated': 'Комментарий успешно обновлен',
            'comment-edit-success': 'Комментарий успешно обновлен',
            'comment-delete-success': 'Комментарий успешно удален',
            'comment-add-success': 'Комментарий успешно добавлен'
        };
    }

    async init() {
        await this.loadCurrentUser();
        await this.loadCategories();
        await this.loadAuthors();
        await this.loadRecipes();
        this.setupEventListeners();
    }

    async loadCurrentUser() {
        try {
            const response = await fetch('/api/auth/user', {
                credentials: 'include'
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data && result.data.authenticated) {
                    this.currentUserId = result.data.id;
                }
            }
        } catch (error) {
            this.currentUserId = null;
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
        }
    }

    async loadAuthors() {
        try {
            const response = await this.get('/recipes');
            if (response.success) {
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
            categoryFilter.innerHTML = '<option value="">' + (this.messages['filter.all_categories'] || 'Все категории') + '</option>' +
                this.categories.map(cat =>
                    `<option value="${CommonUtils.escapeHtml(cat.name)}">${CommonUtils.escapeHtml(cat.name)}</option>`
                ).join('');
        }
    }

    populateAuthorFilter() {
        const authorFilter = document.getElementById('authorFilter');
        if (authorFilter) {
            authorFilter.innerHTML = '<option value="">' + (this.messages['filter.all_authors'] || 'Все авторы') + '</option>' +
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
                `${total} ${this.getRecipeCountText(total)}` :
                `${filtered} ${this.getRecipeCountText(filtered)} из ${total}`;
            countElement.textContent = countText;
        }
    }

    getRecipeCountText(count) {
        if (count % 10 === 1 && count % 100 !== 11) {
            return 'рецепт';
        } else if ([2, 3, 4].includes(count % 10) && ![12, 13, 14].includes(count % 100)) {
            return 'рецепта';
        } else {
            return 'рецептов';
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
                const recipeData = response.data;
                this.showRecipeModal(recipeData);
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
            const response = await this.get(`/recipes/${recipeId}/comments`);
            return response.success ? response.data : [];
        } catch (error) {
            console.error('Error loading recipe comments:', error);
            throw error;
        }
    }

    showRecipeModal(recipe) {
        const modalElement = document.getElementById('recipeModal');
        if (!modalElement) {
            CommonUtils.showToast('Ошибка: модальное окно не найдено', 'error');
            return;
        }

        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        if (!modalTitle || !modalBody) return;

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
                            ${this.getInventoryItemsList(inventoryItems)}
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
                                💬 ${commentCount} ${CommonUtils.getCommentText(commentCount)}
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

        try {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
        } catch (error) {
            CommonUtils.showToast('Ошибка при открытии модального окна', 'error');
        }
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
        this.currentRecipeId = recipeId;
        this.currentRecipeTitle = recipeTitle;

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
            this.displayComments(comments, modalBody, recipeId);
        } catch (error) {
            modalBody.innerHTML = `
                <div class="alert alert-warning">
                    <p>Ошибка при загрузке комментариев</p>
                    <button class="btn btn-sm btn-outline-primary" onclick="app.showCommentsModal('${recipeId}', '${CommonUtils.escapeHtml(recipeTitle)}')">
                        Повторить
                    </button>
                </div>
            `;
        }
    }

    displayComments(comments, modalBody, recipeId) {
        const isAuthenticated = this.currentUserId !== null;

        modalBody.innerHTML = `
            <div class="comments-section">
                ${isAuthenticated ? `
                    <div class="comment-form mb-4">
                        <h6>Добавить комментарий</h6>
                        <textarea class="form-control" id="newCommentContent" rows="3"
                                  placeholder="Введите ваш комментарий..."></textarea>
                        <button class="btn btn-primary mt-2" id="addCommentBtn">
                            Добавить комментарий
                        </button>
                    </div>
                ` : `
                    <div class="alert alert-info">
                        Войдите, чтобы оставить комментарий
                    </div>
                `}

                <div class="comments-list">
                    ${comments && comments.length > 0 ?
                        comments.map(comment => this.renderComment(comment)).join('')
                        : `
                        <div class="text-center py-4 text-muted">
                            💬 Комментариев пока нет
                        </div>
                    `}
                </div>
            </div>
        `;

        if (isAuthenticated) {
            this.setupCommentForm(recipeId);
        }
        this.setupCommentActions(recipeId);
    }

    renderComment(comment) {
        return `
            <div class="card mb-3" data-comment-id="${comment.id}">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <h6 class="card-title mb-1">
                                👤 ${CommonUtils.escapeHtml(comment.user.username)}
                                ${comment.user.isAuthor ? '<span class="badge bg-success ms-1">Автор</span>' : ''}
                            </h6>
                            <small class="text-muted">
                                📅 ${new Date(comment.createdAt).toLocaleString()}
                            </small>
                        </div>
                        ${comment.canEdit || comment.canDelete ? `
                            <div class="btn-group btn-group-sm">
                                ${comment.canEdit ? `
                                    <button class="btn btn-outline-primary edit-comment-btn"
                                            data-comment-id="${comment.id}">
                                        ✏️ Редактировать
                                    </button>
                                ` : ''}
                                ${comment.canDelete ? `
                                    <button class="btn btn-outline-danger delete-comment-btn"
                                            data-comment-id="${comment.id}">
                                        🗑️ Удалить
                                    </button>
                                ` : ''}
                            </div>
                        ` : ''}
                    </div>

                    <div class="comment-content">
                        <p class="card-text">${CommonUtils.escapeHtml(comment.content)}</p>
                    </div>

                    <div class="comment-edit-form d-none mt-3">
                        <textarea class="form-control edit-comment-textarea" rows="3">${CommonUtils.escapeHtml(comment.content)}</textarea>
                        <div class="mt-2">
                            <button class="btn btn-sm btn-success save-edit-btn">Сохранить</button>
                            <button class="btn btn-sm btn-secondary cancel-edit-btn">Отмена</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    setupCommentForm(recipeId) {
        const addCommentBtn = document.getElementById('addCommentBtn');
        if (addCommentBtn) {
            addCommentBtn.addEventListener('click', () => {
                this.addNewComment(recipeId);
            });
        }
    }

    setupCommentActions(recipeId) {
        // Обработчики редактирования
        document.querySelectorAll('.edit-comment-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentId = e.target.closest('.edit-comment-btn').dataset.commentId;
                this.enableEditMode(commentId);
            });
        });

        // Обработчики удаления
        document.querySelectorAll('.delete-comment-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentId = e.target.closest('.delete-comment-btn').dataset.commentId;
                this.deleteComment(recipeId, commentId);
            });
        });

        // Обработчики сохранения редактирования
        document.querySelectorAll('.save-edit-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentCard = e.target.closest('.card');
                const commentId = commentCard.dataset.commentId;
                this.saveEditedComment(commentId, recipeId);
            });
        });

        // Обработчики отмены редактирования
        document.querySelectorAll('.cancel-edit-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentCard = e.target.closest('.card');
                const commentId = commentCard.dataset.commentId;
                this.cancelEditMode(commentId);
            });
        });
    }

    enableEditMode(commentId) {
        const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
        if (commentCard) {
            commentCard.querySelector('.comment-content').classList.add('d-none');
            commentCard.querySelector('.comment-edit-form').classList.remove('d-none');
        }
    }

    cancelEditMode(commentId) {
        const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
        if (commentCard) {
            commentCard.querySelector('.comment-content').classList.remove('d-none');
            commentCard.querySelector('.comment-edit-form').classList.add('d-none');
        }
    }

    async addNewComment(recipeId) {
        const contentInput = document.getElementById('newCommentContent');
        const content = contentInput.value.trim();
        const addBtn = document.getElementById('addCommentBtn');

        const originalText = addBtn.innerHTML;
        addBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Добавление...';
        addBtn.disabled = true;

        try {
            await this.createComment(recipeId, content);
            contentInput.value = '';
            await this.refreshComments(recipeId);
            await this.updateAllCommentCounts(recipeId);
        } catch (error) {
            // Ошибка показывается через CommonUtils.showToast в методах API
        } finally {
            addBtn.innerHTML = originalText;
            addBtn.disabled = false;
        }
    }

    async saveEditedComment(commentId, recipeId) {
        const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
        if (!commentCard) return;

        const textarea = commentCard.querySelector('.edit-comment-textarea');
        const content = textarea.value.trim();
        const saveBtn = commentCard.querySelector('.save-edit-btn');

        const originalText = saveBtn.innerHTML;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Сохранение...';
        saveBtn.disabled = true;

        try {
            await this.updateComment(recipeId, commentId, content);
            await this.refreshComments(recipeId);
        } catch (error) {
            // Ошибка показывается через CommonUtils.showToast в методах API
        } finally {
            saveBtn.innerHTML = originalText;
            saveBtn.disabled = false;
        }
    }

    async deleteComment(recipeId, commentId) {
        if (!confirm('Вы уверены, что хотите удалить этот комментарий?')) {
            return;
        }

        try {
            await this.deleteCommentApi(recipeId, commentId);
            await this.refreshComments(recipeId);
            await this.updateAllCommentCounts(recipeId);
        } catch (error) {
            // Ошибка показывается через CommonUtils.showToast в методах API
        }
    }

    async refreshComments(recipeId) {
        try {
            const comments = await this.loadRecipeComments(recipeId);
            const modalBody = document.getElementById('commentsModalBody');
            this.displayComments(comments, modalBody, recipeId);
        } catch (error) {
            CommonUtils.showToast('Ошибка при обновлении комментариев', 'error');
        }
    }

    // ОБНОВЛЕННЫЙ МЕТОД: Обновление всех счетчиков комментариев
    async updateAllCommentCounts(recipeId) {
        try {
            // Загружаем обновленные данные рецепта
            const response = await this.get(`/recipes/${recipeId}/detailed`);
            if (response.success) {
                const updatedRecipe = response.data;
                const newCommentCount = updatedRecipe.commentCount || 0;

                // 1. Обновляем счетчик в основном списке рецептов
                const recipeIndex = this.allRecipes.findIndex(recipe => recipe.id === recipeId);
                if (recipeIndex !== -1) {
                    this.allRecipes[recipeIndex].commentCount = newCommentCount;
                }

                // 2. Обновляем счетчик в отфильтрованном списке
                const filteredIndex = this.filteredRecipes.findIndex(recipe => recipe.id === recipeId);
                if (filteredIndex !== -1) {
                    this.filteredRecipes[filteredIndex].commentCount = newCommentCount;
                }

                // 3. Обновляем счетчик в таблице на главной странице
                this.updateTableCommentCount(recipeId, newCommentCount);

                // 4. Обновляем счетчик в модальном окне рецепта, если оно открыто
                this.updateRecipeModalCommentCount(recipeId, newCommentCount);

                // 5. Обновляем счетчик в модальном окне комментариев
                this.updateCommentsModalTitle(newCommentCount);

                console.log(`Updated comment count for recipe ${recipeId}: ${newCommentCount}`);
            }
        } catch (error) {
            console.error('Error updating recipe comment count:', error);
        }
    }

    // Обновление счетчика в таблице
    updateTableCommentCount(recipeId, newCount) {
        const tableBadge = document.querySelector(`.comments-badge[data-recipe-id="${recipeId}"]`);
        if (tableBadge) {
            tableBadge.textContent = `💬 ${newCount}`;
            tableBadge.title = `${this.messages.view} ${newCount} ${this.messages.comments.toLowerCase()}`;
        }
    }

    // Обновление счетчика в модальном окне рецепта
    updateRecipeModalCommentCount(recipeId, newCount) {
        const recipeModalBadge = document.querySelector('#recipeModal .comments-badge');
        if (recipeModalBadge && recipeModalBadge.dataset.recipeId === recipeId.toString()) {
            recipeModalBadge.textContent = `💬 ${newCount} ${CommonUtils.getCommentText(newCount)}`;
        }
    }

    // Обновление заголовка модального окна комментариев
    updateCommentsModalTitle(newCount) {
        const modalTitle = document.getElementById('commentsModalTitle');
        if (modalTitle && this.currentRecipeTitle) {
            modalTitle.textContent = `💬 Комментарии: ${CommonUtils.escapeHtml(this.currentRecipeTitle)} (${newCount})`;
        }
    }

    // API методы для комментариев
    async createComment(recipeId, content) {
        const response = await this.post(`/recipes/${recipeId}/comments`, { content });
        if (response.success) {
            return response.data;
        }
        throw new Error(response.message);
    }

    async updateComment(recipeId, commentId, content) {
        const response = await this.put(`/recipes/${recipeId}/comments/${commentId}`, { content });
        if (response.success) {
            CommonUtils.showToast(response.message, 'success');
            return response.data;
        }
        throw new Error(response.message);
    }

    async deleteCommentApi(recipeId, commentId) {
        const response = await this.delete(`/recipes/${recipeId}/comments/${commentId}`);
        if (response.success) {
            CommonUtils.showToast(response.message, 'success');
            return true;
        }
        throw new Error(response.message);
    }

    setupEventListeners() {
        const categoryFilter = document.getElementById('categoryFilter');
        if (categoryFilter) {
            categoryFilter.addEventListener('change', (e) => {
                this.currentFilters.category = e.target.value;
                this.applyFilters();
            });
        }

        const authorFilter = document.getElementById('authorFilter');
        if (authorFilter) {
            authorFilter.addEventListener('change', (e) => {
                this.currentFilters.author = e.target.value;
                this.applyFilters();
            });
        }

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