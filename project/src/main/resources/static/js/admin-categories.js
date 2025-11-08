class CategoriesAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.categories = [];
        this.translations = this.getTranslations();
        this.init();
    }

    getTranslations() {
        return {
            loading: 'Загрузка...',
            create: 'Создать',
            save: 'Сохранить',
            delete: 'Удалить',
            cancel: 'Отмена',
            required: 'Это поле обязательно для заполнения',
            errorLoad: 'Ошибка загрузки категорий',
            errorCreate: 'Ошибка создания категории',
            errorUpdate: 'Ошибка обновления категории',
            errorDelete: 'Ошибка удаления категории',
            errorUsageCheck: 'Ошибка проверки использования',
            successCreated: 'Категория создана успешно',
            successUpdated: 'Категория обновлена успешно',
            successDeleted: 'Категория удалена успешно',
            noData: 'Категории не найдены',
            cannotDelete: 'Нельзя удалить (используется)',
            usedIn: 'Используется в',
            notUsed: 'Не используется в рецептах',
            recipes: 'рецептах',
            checkUsage: 'Проверить использование',
            checkingUsage: 'Проверка использования...'
        };
    }

    async init() {
        console.log('CategoriesAdminApp initialized');
        await this.loadCategories();
        this.setupEventListeners();
        this.setupCreateFormHandlers();
    }

    async loadCategories() {
        try {
            console.log('Loading categories...');
            CommonUtils.showLoadingState('categoriesTableBody', this.translations.loading, 6);

            const response = await this.get('/categories');
            console.log('Categories loaded:', response);

            if (response.success) {
                this.categories = response.data;
                console.log('Categories count:', this.categories.length);
                this.displayCategories();
            } else {
                CommonUtils.showError('categoriesTableBody', this.translations.errorLoad, 6);
            }
        } catch (error) {
            console.error('Error loading categories:', error);
            CommonUtils.showError('categoriesTableBody', this.translations.errorLoad, 6);
        }
    }

    displayCategories() {
        const tbody = document.getElementById('categoriesTableBody');

        if (this.categories.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        ${this.translations.noData}
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.categories.map(category => `
            <tr>
                <td>${category.id}</td>
                <td class="fw-bold">${CommonUtils.escapeHtml(category.name)}</td>
                <td>${CommonUtils.escapeHtml(category.description || '—')}</td>
                <td class="small">${CommonUtils.formatDateShort(category.createdAt)}</td>
                <td>
                    <span class="badge bg-secondary usage-badge"
                          data-category-id="${category.id}"
                          style="cursor: pointer;"
                          title="${this.translations.checkUsage}">
                        ${this.translations.checkUsage}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1 edit-category"
                            data-category-id="${category.id}">
                        ✏️
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-category"
                            data-category-id="${category.id}"
                            data-category-name="${CommonUtils.escapeHtml(category.name)}">
                        🗑️
                    </button>
                </td>
            </tr>
        `).join('');

        this.addEventListeners();
    }

    addEventListeners() {
        document.querySelectorAll('.edit-category').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const categoryId = e.target.closest('.edit-category').dataset.categoryId;
                this.openEditModal(categoryId);
            });
        });

        document.querySelectorAll('.delete-category').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const categoryId = e.target.closest('.delete-category').dataset.categoryId;
                const categoryName = e.target.closest('.delete-category').dataset.categoryName;
                this.openDeleteModal(categoryId, categoryName);
            });
        });

        document.querySelectorAll('.usage-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const categoryId = e.target.dataset.categoryId;
                this.checkCategoryUsage(categoryId);
            });
        });
    }

    setupEventListeners() {
        const createModal = document.getElementById('createCategoryModal');
        if (createModal) {
            createModal.addEventListener('hidden.bs.modal', () => {
                this.resetCreateForm();
            });
        }

        // Обработчики для кнопок модальных окон
        const createBtn = document.getElementById('createCategoryBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => {
                this.createCategory();
            });
        }

        const updateBtn = document.getElementById('updateCategoryBtn');
        if (updateBtn) {
            updateBtn.addEventListener('click', () => {
                this.updateCategory();
            });
        }

        const deleteBtn = document.getElementById('confirmDeleteBtn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => {
                this.deleteCategory();
            });
        }
    }

    setupCreateFormHandlers() {
        const createForm = document.getElementById('createCategoryForm');

        if (createForm) {
            createForm.addEventListener('input', (e) => {
                if (e.target.id === 'categoryName') {
                    e.target.classList.remove('is-invalid');
                }
            });

            createForm.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.createCategory();
                }
            });
        }

        const createModal = document.getElementById('createCategoryModal');
        if (createModal) {
            createModal.addEventListener('show.bs.modal', () => {
                this.resetCreateForm();
            });
        }
    }

    resetCreateForm() {
        const form = document.getElementById('createCategoryForm');
        if (form) {
            form.reset();
            const nameInput = document.getElementById('categoryName');
            if (nameInput) {
                nameInput.classList.remove('is-invalid');
            }
        }
    }

    async createCategory() {
        const nameInput = document.getElementById('categoryName');
        const descriptionInput = document.getElementById('categoryDescription');
        const createBtn = document.getElementById('createCategoryBtn');

        const name = nameInput.value.trim();
        const description = descriptionInput.value.trim();

        if (!name) {
            nameInput.classList.add('is-invalid');
            nameInput.focus();
            CommonUtils.showToast(this.translations.required, 'error');
            return;
        }

        if (createBtn) {
            createBtn.disabled = true;
            createBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.loading;
        }

        try {
            console.log('Sending category creation request:', { name, description });
            const response = await this.post('/categories', { name, description });
            console.log('Category creation response:', response);

            if (response.success) {
                CommonUtils.showToast(response.message || this.translations.successCreated);
                this.resetCreateForm();

                const modal = bootstrap.Modal.getInstance(document.getElementById('createCategoryModal'));
                if (modal) {
                    modal.hide();
                }

                await this.loadCategories();
            } else {
                CommonUtils.showToast(response.message || this.translations.errorCreate, 'error');
            }
        } catch (error) {
            console.error('Error creating category:', error);
            let errorMessage = this.translations.errorCreate;

            if (error.response) {
                try {
                    const errorData = await error.response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = error.message || errorMessage;
                }
            } else if (error.message) {
                errorMessage = error.message;
            }

            CommonUtils.showToast(errorMessage, 'error');
        } finally {
            if (createBtn) {
                createBtn.disabled = false;
                createBtn.innerHTML = this.translations.create;
            }
        }
    }

    openEditModal(categoryId) {
        const category = this.categories.find(c => c.id == categoryId);
        if (!category) return;

        document.getElementById('editCategoryId').value = category.id;
        document.getElementById('editCategoryName').value = category.name;
        document.getElementById('editCategoryDescription').value = category.description || '';

        new bootstrap.Modal(document.getElementById('editCategoryModal')).show();
    }

    async updateCategory() {
        const id = document.getElementById('editCategoryId').value;
        const name = document.getElementById('editCategoryName').value.trim();
        const description = document.getElementById('editCategoryDescription').value.trim();
        const updateBtn = document.getElementById('updateCategoryBtn');

        if (!name) {
            CommonUtils.showToast(this.translations.required, 'error');
            return;
        }

        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.loading;
        }

        try {
            const response = await this.put(`/categories/${id}`, { name, description });

            if (response.success) {
                CommonUtils.showToast(response.message || this.translations.successUpdated);
                document.getElementById('editCategoryModal').querySelector('.btn-close').click();
                await this.loadCategories();
            } else {
                CommonUtils.showToast(response.message || this.translations.errorUpdate, 'error');
            }
        } catch (error) {
            console.error('Error updating category:', error);
            let errorMessage = this.translations.errorUpdate;

            if (error.response) {
                try {
                    const errorData = await error.response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = error.message || errorMessage;
                }
            }

            CommonUtils.showToast(errorMessage, 'error');
        } finally {
            if (updateBtn) {
                updateBtn.disabled = false;
                updateBtn.innerHTML = this.translations.save;
            }
        }
    }

    // УПРОЩЕННАЯ ЛОГИКА УДАЛЕНИЯ (как в инвентаре)
    async openDeleteModal(categoryId, categoryName) {
        document.getElementById('deleteCategoryName').textContent = categoryName;
        document.getElementById('deleteCategoryModal').dataset.categoryId = categoryId;

        const warningDiv = document.getElementById('deleteWarning');
        const deleteBtn = document.getElementById('confirmDeleteBtn');

        // Начальное состояние: скрываем предупреждение, блокируем кнопку до завершения проверки
        warningDiv.style.display = 'none';
        deleteBtn.disabled = true;
        deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.checkingUsage;

        const modal = new bootstrap.Modal(document.getElementById('deleteCategoryModal'));
        modal.show();

        try {
            console.log('Checking usage for category ID:', categoryId);
            const response = await this.get(`/categories/${categoryId}/usage`);
            console.log('Usage check response:', response);

            if (response.success) {
                const usage = response.data;
                console.log('Usage data:', usage);

                if (usage.isUsed) {
                    // Категория используется - показываем информацию и блокируем удаление
                    warningDiv.style.display = 'block';
                    warningDiv.innerHTML = `
                        <div class="alert alert-warning">
                            ⚠️ Эта категория используется в <strong>${usage.recipeCount}</strong> рецептах и не может быть удалена.
                        </div>
                    `;

                    deleteBtn.disabled = true;
                    deleteBtn.innerHTML = '❌ ' + this.translations.cannotDelete;
                } else {
                    // Категория не используется - разрешаем удаление
                    warningDiv.style.display = 'none';
                    deleteBtn.disabled = false;
                    deleteBtn.innerHTML = this.translations.delete;
                }
            } else {
                // Ошибка от сервера при проверке использования
                console.error('Server returned error during usage check:', response.message);
                warningDiv.style.display = 'block';
                warningDiv.innerHTML = `
                    <div class="alert alert-danger">
                        ❌ Ошибка при проверке использования: ${response.message || 'Неизвестная ошибка'}
                    </div>
                `;
                deleteBtn.disabled = true;
                deleteBtn.innerHTML = '❌ Ошибка проверки';
            }
        } catch (error) {
            // Ошибка сети или другая ошибка при запросе
            console.error('Network error during usage check:', error);
            warningDiv.style.display = 'block';
            warningDiv.innerHTML = `
                <div class="alert alert-danger">
                    ❌ Ошибка соединения при проверке использования. Удаление невозможно.
                </div>
            `;
            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '❌ Ошибка сети';
        }
    }

    async deleteCategory() {
        const categoryId = document.getElementById('deleteCategoryModal').dataset.categoryId;
        const deleteBtn = document.getElementById('confirmDeleteBtn');

        if (!categoryId) {
            console.error('No category ID found for deletion');
            CommonUtils.showToast('Ошибка: ID категории не найден', 'error');
            return;
        }

        // Дополнительная проверка - не пытаемся удалить, если кнопка заблокирована
        if (deleteBtn.disabled) {
            console.log('Delete button is disabled, skipping deletion');
            return;
        }

        // Блокируем кнопку во время удаления
        deleteBtn.disabled = true;
        deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.delete + '...';

        try {
            console.log('Sending DELETE request for category ID:', categoryId);
            const response = await this.delete(`/categories/${categoryId}`);
            console.log('Delete response:', response);

            if (response.success) {
                CommonUtils.showToast(response.message || this.translations.successDeleted);
                console.log('Category deleted successfully');

                // Закрываем модальное окно
                const modal = bootstrap.Modal.getInstance(document.getElementById('deleteCategoryModal'));
                if (modal) {
                    modal.hide();
                }

                // Перезагружаем список категорий
                await this.loadCategories();
            } else {
                console.error('Delete failed:', response.message);
                CommonUtils.showToast(response.message || this.translations.errorDelete, 'error');

                // Разблокируем кнопку при ошибке
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = this.translations.delete;
            }
        } catch (error) {
            console.error('Error deleting category:', error);
            let errorMessage = this.translations.errorDelete;

            if (error.response) {
                try {
                    const errorData = await error.response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = error.message || errorMessage;
                }
            }

            CommonUtils.showToast(errorMessage, 'error');

            // Разблокируем кнопку при ошибке
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = this.translations.delete;
        }
    }

    async checkCategoryUsage(categoryId) {
        try {
            const response = await this.get(`/categories/${categoryId}/usage`);
            if (response.success) {
                const usage = response.data;
                const message = usage.isUsed ?
                    `${this.translations.usedIn} ${usage.recipeCount} ${this.translations.recipes}` :
                    this.translations.notUsed;

                CommonUtils.showToast(message, usage.isUsed ? 'info' : 'success');
            }
        } catch (error) {
            console.error('Error checking usage:', error);
            CommonUtils.showToast(this.translations.errorUsageCheck, 'error');
        }
    }
}

// Инициализация приложения
let categoriesApp;
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, initializing CategoriesAdminApp...');
    categoriesApp = new CategoriesAdminApp();
});