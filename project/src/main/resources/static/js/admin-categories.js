class CategoriesAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.categories = [];
        this.init();
    }

    async init() {
        await this.loadCategories();
        this.setupEventListeners();
        this.setupCreateFormHandlers(); // Добавьте эту строку
    }

    async loadCategories() {
        try {
                console.log('Loading categories...');
                const response = await this.get('/categories');
                console.log('Categories loaded:', response);
                if (response.success) {
                    this.categories = response.data;
                    console.log('Categories count:', this.categories.length);
                    this.displayCategories();
                }
            } catch (error) {
                console.error('Error loading categories:', error);
                CommonUtils.showToast('Ошибка загрузки категорий', 'error');
            }
    }

    displayCategories() {
        const tbody = document.getElementById('categoriesTableBody');

        if (this.categories.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        Категории не найдены
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
                          style="cursor: pointer;">
                        Проверить использование
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
        // Редактирование
        document.querySelectorAll('.edit-category').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const categoryId = e.target.closest('.edit-category').dataset.categoryId;
                this.openEditModal(categoryId);
            });
        });

        // Удаление
        document.querySelectorAll('.delete-category').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const categoryId = e.target.closest('.delete-category').dataset.categoryId;
                const categoryName = e.target.closest('.delete-category').dataset.categoryName;
                this.openDeleteModal(categoryId, categoryName);
            });
        });

        // Проверка использования
        document.querySelectorAll('.usage-badge').forEach(badge => {
            badge.addEventListener('click', (e) => {
                const categoryId = e.target.dataset.categoryId;
                this.checkCategoryUsage(categoryId);
            });
        });
    }

    setupEventListeners() {
        // Очистка формы при закрытии модального окна создания
        const createModal = document.getElementById('createCategoryModal');
        if (createModal) {
            createModal.addEventListener('hidden.bs.modal', () => {
                this.resetCreateForm();
            });
        }
    }

    async createCategory() {
        const nameInput = document.getElementById('categoryName');
        const descriptionInput = document.getElementById('categoryDescription');
        const createBtn = document.getElementById('createCategoryBtn');

        const name = nameInput.value.trim();
        const description = descriptionInput.value.trim();

        // Валидация на фронтенде
        if (!name) {
            nameInput.classList.add('is-invalid');
            nameInput.focus();
            CommonUtils.showToast('Введите название категории', 'error');
            return;
        }

        // Блокируем кнопку во время запроса
        if (createBtn) {
            createBtn.disabled = true;
            createBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Создание...';
        }

        try {
            console.log('Sending category creation request:', { name, description });
            const response = await this.post('/categories', { name, description });
            console.log('Category creation response:', response);

            if (response.success) {
                CommonUtils.showToast(response.message || 'Категория создана успешно');
                this.resetCreateForm();

                // Закрываем модальное окно
                const modal = bootstrap.Modal.getInstance(document.getElementById('createCategoryModal'));
                if (modal) {
                    modal.hide();
                }

                // Перезагружаем список категорий
                await this.loadCategories();
            } else {
                CommonUtils.showToast(response.message || 'Ошибка создания категории', 'error');
            }
        } catch (error) {
            console.error('Error creating category:', error);
            let errorMessage = 'Ошибка создания категории';

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
            // Разблокируем кнопку
            if (createBtn) {
                createBtn.disabled = false;
                createBtn.innerHTML = 'Создать';
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

        if (!name) {
            CommonUtils.showToast('Введите название категории', 'error');
            return;
        }

        try {
            const response = await this.put(`/categories/${id}`, { name, description });
            if (response.success) {
                CommonUtils.showToast('Категория обновлена успешно');
                document.getElementById('editCategoryModal').querySelector('.btn-close').click();
                await this.loadCategories();
            }
        } catch (error) {
            console.error('Error updating category:', error);
            CommonUtils.showToast('Ошибка обновления категории', 'error');
        }
    }

     async openDeleteModal(categoryId, categoryName) {
            document.getElementById('deleteCategoryName').textContent = categoryName;

            // Проверяем использование категории
            try {
                const response = await this.get(`/categories/${categoryId}/usage`);
                if (response.success) {
                    const usage = response.data;
                    const warningDiv = document.getElementById('deleteWarning');
                    const recipeCountSpan = document.getElementById('recipeCount');

                    if (usage.isUsed) {
                        recipeCountSpan.textContent = usage.recipeCount;
                        warningDiv.style.display = 'block';
                        // Делаем кнопку удаления неактивной
                        document.querySelector('#deleteCategoryModal .btn-danger').disabled = true;
                        document.querySelector('#deleteCategoryModal .btn-danger').innerHTML =
                            '❌ Нельзя удалить (используется)';
                    } else {
                        warningDiv.style.display = 'none';
                        document.querySelector('#deleteCategoryModal .btn-danger').disabled = false;
                        document.querySelector('#deleteCategoryModal .btn-danger').innerHTML = 'Удалить';
                    }
                }
            } catch (error) {
                console.error('Error checking category usage:', error);
            }

            document.getElementById('deleteCategoryModal').dataset.categoryId = categoryId;
            new bootstrap.Modal(document.getElementById('deleteCategoryModal')).show();
        }

        async deleteCategory() {
            const categoryId = document.getElementById('deleteCategoryModal').dataset.categoryId;

            try {
                const response = await this.delete(`/categories/${categoryId}`);
                CommonUtils.showToast(response.message || 'Категория удалена успешно');
                document.getElementById('deleteCategoryModal').querySelector('.btn-close').click();
                await this.loadCategories();
            } catch (error) {
                console.error('Error deleting category:', error);
                const errorMessage = error.message || 'Ошибка удаления категории';
                CommonUtils.showToast(errorMessage, 'error');
            }
        }

    async checkCategoryUsage(categoryId) {
        try {
            const response = await this.get(`/categories/${categoryId}/usage`);
            if (response.success) {
                const usage = response.data;
                const message = usage.isUsed ?
                    `Используется в ${usage.recipeCount} рецептах` :
                    'Не используется в рецептах';

                CommonUtils.showToast(message, usage.isUsed ? 'info' : 'success');
            }
        } catch (error) {
            console.error('Error checking usage:', error);
            CommonUtils.showToast('Ошибка проверки использования', 'error');
        }
    }

    setupCreateFormHandlers() {
        const createForm = document.getElementById('createCategoryForm');
        const createBtn = document.getElementById('createCategoryBtn');

        if (createForm) {
            // Обработка ввода для сброса состояния валидации
            createForm.addEventListener('input', (e) => {
                if (e.target.id === 'categoryName') {
                    e.target.classList.remove('is-invalid');
                }
            });

            // Обработка отправки формы по Enter
            createForm.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.createCategory();
                }
            });
        }

        // Очистка формы при открытии модального окна
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
            // Сбрасываем состояния валидации
            const nameInput = document.getElementById('categoryName');
            if (nameInput) {
                nameInput.classList.remove('is-invalid');
            }
        }
    }

}

let categoriesApp;
document.addEventListener('DOMContentLoaded', () => {
    categoriesApp = new CategoriesAdminApp();
});