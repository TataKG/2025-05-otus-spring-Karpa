class CategoriesAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.categories = [];
        this.init();
    }

    async init() {
        await this.loadCategories();
        this.setupEventListeners();
    }

    async loadCategories() {
        try {
            const response = await this.get('/categories');
            if (response.success) {
                this.categories = response.data;
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
        createModal.addEventListener('hidden.bs.modal', () => {
            document.getElementById('createCategoryForm').reset();
        });
    }

    async createCategory() {
        const name = document.getElementById('categoryName').value.trim();
        const description = document.getElementById('categoryDescription').value.trim();

        if (!name) {
            CommonUtils.showToast('Введите название категории', 'error');
            return;
        }

        try {
            const response = await this.post('/categories', { name, description });
            if (response.success) {
                CommonUtils.showToast('Категория создана успешно');
                document.getElementById('createCategoryModal').querySelector('.btn-close').click();
                await this.loadCategories();
            }
        } catch (error) {
            console.error('Error creating category:', error);
            CommonUtils.showToast('Ошибка создания категории', 'error');
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
                await this.delete(`/categories/${categoryId}`);
                CommonUtils.showToast('Категория удалена успешно');
                document.getElementById('deleteCategoryModal').querySelector('.btn-close').click();
                await this.loadCategories();
            } catch (error) {
                console.error('Error deleting category:', error);
                if (error.message.includes('409')) {
                    CommonUtils.showToast('Невозможно удалить категорию, так как она используется в рецептах', 'error');
                } else {
                    CommonUtils.showToast('Ошибка удаления категории', 'error');
                }
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
}

let categoriesApp;
document.addEventListener('DOMContentLoaded', () => {
    categoriesApp = new CategoriesAdminApp();
});