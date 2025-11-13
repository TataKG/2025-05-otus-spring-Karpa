// app.js - для главной страницы
class CookbookApp extends BaseApiClient {
    handleLanguageChange() {
        // Перезагружаем все данные при смене языка
        this.loadCategories();
        this.loadAuthors();
        this.loadRecipes();
    }

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

        // Слушаем события смены языка
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
            'comment-validation-empty': 'Комментарий не может быть пустым',
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
        console.log('🚀 CookbookApp initialization started');

        // Проверяем наличие необходимых DOM элементов
        if (!document.getElementById('recipeModal')) {
            console.error('❌ recipeModal not found in DOM');
        }

        if (!document.getElementById('commentsModal')) {
            console.error('❌ commentsModal not found in DOM');
        }

        await this.loadCurrentUser();
        await this.loadCategories();
        await this.loadAuthors();
        await this.loadRecipes();
        this.setupEventListeners();

        console.log('✅ CookbookApp initialization completed');
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
                    console.log('Current user ID:', this.currentUserId);
                }
            }
        } catch (error) {
            console.log('Cannot determine current user:', error);
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
                console.log('🔍 View recipe button clicked, recipeId:', recipeId);
                if (recipeId) {
                    this.loadRecipeDetails(recipeId);
                } else {
                    console.error('❌ Recipe ID not found');
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
            console.log('📥 Loading recipe details for:', recipeId);
            const response = await this.get(`/recipes/${recipeId}/detailed`);

            console.log('📋 FULL Recipe details API response:', response);

            if (response.success) {
                console.log('✅ Recipe details loaded successfully');

                // Используем данные напрямую из response.data (RecipeDto)
                const recipeData = response.data;
                console.log('🎯 Final recipe data to display:', recipeData);
                this.showRecipeModal(recipeData);
            } else {
                console.error('❌ API error loading recipe details:', response);
                CommonUtils.showToast(this.messages['error-loading-details'], 'error');
            }
        } catch (error) {
            console.error('💥 Error loading recipe details:', error);
            CommonUtils.showToast(this.messages['error-loading-details'], 'error');
        }
    }

    async loadRecipeComments(recipeId) {
        try {
            console.log(`🔗 Loading comments for recipe ${recipeId}`);

            const response = await fetch(`${this.baseUrl}/recipes/${recipeId}/comments`, {
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

    showRecipeModal(recipe) {
        console.log('📋 FULL Recipe data structure for modal:', recipe);

        const modalElement = document.getElementById('recipeModal');
        if (!modalElement) {
            console.error('❌ recipeModal element not found');
            CommonUtils.showToast('Ошибка: модальное окно не найдено', 'error');
            return;
        }

        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        if (!modalTitle || !modalBody) {
            console.error('❌ Modal title or body not found');
            return;
        }

        modalTitle.textContent = `📖 ${CommonUtils.escapeHtml(recipe.title)}`;

        // Используем данные напрямую из RecipeDto
        const categoryName = recipe.category ? recipe.category.name : 'Не указана';
        const authorName = recipe.author ? this.getAuthorDisplayName(recipe.author) : 'Неизвестен';
        const ingredients = recipe.ingredients || [];
        const description = recipe.description || 'Описание отсутствует';
        const inventoryItems = recipe.inventoryItems || [];
        const commentCount = recipe.commentCount || 0;

        console.log('📊 Extracted data:', {
            categoryName,
            authorName,
            ingredients,
            description,
            inventoryItems,
            commentCount
        });

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
            console.log('✅ Recipe modal shown successfully');
        } catch (error) {
            console.error('❌ Error showing modal:', error);
            CommonUtils.showToast('Ошибка при открытии модального окна', 'error');
        }
    }

    getAuthorDisplayName(author) {
        if (!author) return 'Неизвестен';

        // Пробуем разные пути к данным автора в AuthorDto
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
            this.displayComments(comments, modalBody, recipeId);
        } catch (error) {
            console.error('Failed to load comments:', error);
            modalBody.innerHTML = `
                <div class="alert alert-warning">
                    <p>${this.messages['error-loading-comments']}</p>
                    <small class="text-muted">${this.messages['common.retry'] || 'Попробуйте обновить страницу или зайти позже'}</small>
                </div>
            `;
        }
    }

    displayComments(comments, modalBody, recipeId) {
        const isAuthenticated = this.currentUserId !== null;

        if (!comments || comments.length === 0) {
            modalBody.innerHTML = `
                <div class="text-center py-4">
                    <div class="text-muted mb-2">💬</div>
                    <p class="text-muted">${this.messages['no-comments']}</p>
                </div>
                ${isAuthenticated ? `
                    <div class="mt-4">
                        <h6>${this.messages['comment-add']}</h6>
                        <div class="comment-form">
                            <textarea class="form-control" id="newCommentContent" rows="3"
                                      placeholder="${this.messages['comment-placeholder']}"></textarea>
                            <button class="btn btn-primary mt-2" id="addCommentBtn">
                                <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                                ${this.messages['comment-add-button']}
                            </button>
                        </div>
                    </div>
                ` : `
                    <div class="text-center mt-4">
                        <p class="text-muted">${this.messages['comment-login-required']}</p>
                    </div>
                `}
            `;

            if (isAuthenticated) {
                this.setupCommentActions(recipeId);
            }
            return;
        }

        modalBody.innerHTML = `
            <div class="comments-list">
                ${comments.map(comment => `
                    <div class="card mb-3" data-comment-id="${comment.id}">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <div>
                                    <h6 class="card-title mb-1">
                                        👤 ${CommonUtils.escapeHtml(comment.user.username)}
                                        ${comment.user.isAuthor ? '<span class="badge bg-success ms-1">' + (this.messages['author'] || 'Автор') + '</span>' : ''}
                                        ${comment.user.roles && comment.user.roles.includes('ADMIN') ?
                                          '<span class="badge bg-danger ms-1">Admin</span>' : ''}
                                    </h6>
                                    <small class="text-muted">
                                        📅 ${new Date(comment.createdAt).toLocaleString()}
                                        ${comment.updatedAt && comment.updatedAt !== comment.createdAt ?
                                            ` (${this.messages['common.edit'] || 'изменен'} ${new Date(comment.updatedAt).toLocaleString()})` : ''}
                                    </small>
                                </div>
                                ${comment.canEdit || comment.canDelete ? `
                                    <div class="btn-group btn-group-sm">
                                        ${comment.canEdit ? `
                                            <button class="btn btn-outline-primary edit-comment-btn"
                                                    data-comment-id="${comment.id}">
                                                ✏️ ${this.messages['comment-edit']}
                                            </button>
                                        ` : ''}
                                        ${comment.canDelete ? `
                                            <button class="btn btn-outline-danger delete-comment-btn"
                                                    data-comment-id="${comment.id}">
                                                🗑️ ${this.messages['common.delete']}
                                            </button>
                                        ` : ''}
                                    </div>
                                ` : ''}
                            </div>
                            <div class="comment-content-view">
                                <p class="card-text mt-3">${CommonUtils.escapeHtml(comment.content)}</p>
                            </div>
                            <div class="comment-edit-form d-none mt-3">
                                <textarea class="form-control edit-comment-textarea" rows="3" placeholder="${this.messages['comment-placeholder']}">${CommonUtils.escapeHtml(comment.content)}</textarea>
                                <div class="mt-2">
                                    <button class="btn btn-sm btn-success save-edit-btn">${this.messages['common.save']}</button>
                                    <button class="btn btn-sm btn-secondary cancel-edit-btn">${this.messages['common.cancel']}</button>
                                </div>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
            ${isAuthenticated ? `
                <div class="mt-4">
                    <h6>${this.messages['comment-add']}</h6>
                    <div class="comment-form">
                        <textarea class="form-control" id="newCommentContent" rows="3"
                                  placeholder="${this.messages['comment-placeholder']}"></textarea>
                        <button class="btn btn-primary mt-2" id="addCommentBtn">
                            <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                            ${this.messages['comment-add-button']}
                        </button>
                    </div>
                </div>
            ` : `
                <div class="text-center mt-4">
                    <p class="text-muted">${this.messages['comment-login-required']}</p>
                </div>
            `}
        `;

        if (isAuthenticated) {
            this.setupCommentActions(recipeId);
        }

        this.setupEditCommentHandlers(recipeId);
    }

    setupCommentActions(recipeId) {
        const addCommentBtn = document.getElementById('addCommentBtn');
        if (addCommentBtn) {
            addCommentBtn.addEventListener('click', () => {
                this.addNewComment(recipeId);
            });
        }
    }

    setupEditCommentHandlers(recipeId) {
        document.querySelectorAll('.edit-comment-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentId = e.target.closest('.edit-comment-btn').dataset.commentId;
                this.enableEditMode(commentId);
            });
        });

        document.querySelectorAll('.delete-comment-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentId = e.target.closest('.delete-comment-btn').dataset.commentId;
                this.showDeleteConfirmation(commentId);
            });
        });

        document.querySelectorAll('.save-edit-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const commentCard = e.target.closest('.card');
                const commentId = commentCard.dataset.commentId;
                this.saveEditedComment(commentId, recipeId);
            });
        });

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
            const contentView = commentCard.querySelector('.comment-content-view');
            const editForm = commentCard.querySelector('.comment-edit-form');

            contentView.classList.add('d-none');
            editForm.classList.remove('d-none');
        }
    }

    cancelEditMode(commentId) {
        const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
        if (commentCard) {
            const contentView = commentCard.querySelector('.comment-content-view');
            const editForm = commentCard.querySelector('.comment-edit-form');

            contentView.classList.remove('d-none');
            editForm.classList.add('d-none');
        }
    }

    async saveEditedComment(commentId, recipeId) {
        const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
        if (!commentCard) return;

        const textarea = commentCard.querySelector('.edit-comment-textarea');
        const newContent = textarea.value.trim();

        if (!newContent) {
            CommonUtils.showToast(this.messages['comment-validation-empty'], 'error');
            return;
        }

        const saveBtn = commentCard.querySelector('.save-edit-btn');
        const originalText = saveBtn.innerHTML;
        saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.messages['common-saving']}`;
        saveBtn.disabled = true;

        try {
            await this.updateComment(recipeId, commentId, newContent);

            const contentView = commentCard.querySelector('.comment-content-view p');
            contentView.textContent = newContent;

            this.cancelEditMode(commentId);

            CommonUtils.showToast(this.messages['comment-edit-success'], 'success');
        } catch (error) {
            // Ошибка уже обработана в updateComment
        } finally {
            saveBtn.innerHTML = originalText;
            saveBtn.disabled = false;
        }
    }

    async showDeleteConfirmation(commentId) {
        const confirmationModal = `
            <div class="modal fade" id="deleteConfirmationModal" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">${this.messages['comment-delete-confirm-title']}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p>${this.messages['comment-delete-confirm-message']}</p>
                            <p class="text-muted small">${this.messages['common-action-irreversible']}</p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${this.messages['common.cancel']}</button>
                            <button type="button" class="btn btn-danger" id="confirmDeleteBtn">
                                <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                                ${this.messages['common-confirm-delete']}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        if (!document.getElementById('deleteConfirmationModal')) {
            document.body.insertAdjacentHTML('beforeend', confirmationModal);
        }

        const modalElement = document.getElementById('deleteConfirmationModal');
        const modal = new bootstrap.Modal(modalElement);

        modalElement.querySelector('.modal-footer').innerHTML = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${this.messages['common.cancel']}</button>
            <button type="button" class="btn btn-danger" id="confirmDeleteBtn">
                <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                ${this.messages['common-confirm-delete']}
            </button>
        `;

        const confirmBtn = document.getElementById('confirmDeleteBtn');
        confirmBtn.addEventListener('click', async () => {
            const spinner = confirmBtn.querySelector('.spinner-border');
            spinner.classList.remove('d-none');
            confirmBtn.disabled = true;

            try {
                await this.deleteComment(this.currentRecipeId, commentId);

                const commentCard = document.querySelector(`[data-comment-id="${commentId}"]`);
                if (commentCard) {
                    commentCard.style.opacity = '0';
                    setTimeout(() => {
                        commentCard.remove();
                        this.updateCommentsCount();
                    }, 300);
                }

                modal.hide();
                CommonUtils.showToast(this.messages['comment-delete-success'], 'success');
            } catch (error) {
                // Ошибка уже обработана в deleteComment
            } finally {
                spinner.classList.add('d-none');
                confirmBtn.disabled = false;
            }
        });

        modal.show();
    }

    updateCommentsCount() {
        // Обновляем счетчик в модальном окне комментариев
        const commentsCount = document.querySelectorAll('.comments-list .card').length;
        const modalBadge = document.querySelector(`#commentsModal .comments-badge`);
        if (modalBadge) {
            modalBadge.textContent = `💬 ${commentsCount} ${CommonUtils.getCommentText(commentsCount)}`;
        }

        // Обновляем счетчик в таблице рецептов
        const tableBadge = document.querySelector(`.comments-badge[data-recipe-id="${this.currentRecipeId}"]`);
        if (tableBadge) {
            tableBadge.textContent = `💬 ${commentsCount}`;
        }

        // Обновляем счетчик в модальном окне рецепта
        const recipeModalBadge = document.querySelector(`#recipeModal .comments-badge`);
        if (recipeModalBadge) {
            recipeModalBadge.textContent = `💬 ${commentsCount} ${CommonUtils.getCommentText(commentsCount)}`;
        }

        console.log(`🔄 Updated comment count to ${commentsCount} for recipe ${this.currentRecipeId}`);
    }

    async addNewComment(recipeId) {
        const content = document.getElementById('newCommentContent').value.trim();
        if (!content) {
            CommonUtils.showToast(this.messages['comment-validation-empty'], 'error');
            return;
        }

        const addBtn = document.getElementById('addCommentBtn');
        const originalText = addBtn.innerHTML;
        addBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.messages['common-adding']}`;
        addBtn.disabled = true;

        try {
            await this.createComment(recipeId, content);
            document.getElementById('newCommentContent').value = '';

            await this.refreshCommentsSilently(recipeId);

            CommonUtils.showToast(this.messages['comment-add-success'], 'success');
        } catch (error) {
            // Ошибка уже обработана в createComment
        } finally {
            addBtn.innerHTML = originalText;
            addBtn.disabled = false;
        }
    }

    async refreshCommentsSilently(recipeId) {
        try {
            const comments = await this.loadRecipeComments(recipeId);
            const modalBody = document.getElementById('commentsModalBody');
            this.displayComments(comments, modalBody, recipeId);

            // Обновляем счетчик после успешной загрузки комментариев
            this.updateCommentsCount();
        } catch (error) {
            console.error('Failed to refresh comments:', error);
        }
    }

    async createComment(recipeId, content) {
        try {
            const response = await this.post(`/recipes/${recipeId}/comments`, {
                content: content
            });

            if (response.success) {
                return response.data;
            }
        } catch (error) {
            CommonUtils.handleApiError(error, this.messages['error.adding_comment'] || 'Ошибка при добавлении комментария');
            throw error;
        }
    }

    async updateComment(recipeId, commentId, content) {
        try {
            const response = await this.put(`/recipes/${recipeId}/comments/${commentId}`, {
                content: content
            });

            if (response.success) {
                return response.data;
            }
        } catch (error) {
            CommonUtils.handleApiError(error, this.messages['error.updating_comment'] || 'Ошибка при обновлении комментария');
            throw error;
        }
    }

    async deleteComment(recipeId, commentId) {
        try {
            const response = await this.delete(`/recipes/${recipeId}/comments/${commentId}`);

            if (response.success) {
                return true;
            }
        } catch (error) {
            CommonUtils.handleApiError(error, this.messages['error.deleting_comment'] || 'Ошибка при удалении комментария');
            throw error;
        }
        return false;
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