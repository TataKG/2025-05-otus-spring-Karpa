// admin-inventory.js - исправленная версия
(function() {
    'use strict';

    // Проверяем, что базовые классы загружены
    if (typeof BaseApiClient === 'undefined') {
        console.error('BaseApiClient не загружен. Проверьте common.js');
        return;
    }

    class InventoryAdminApp extends BaseApiClient {
        constructor() {
            super('/api/admin/inventory');
            this.inventories = [];
            this.translations = this.getTranslations();
            this.init();
        }

        // Получение переводов из data-атрибутов или использование значений по умолчанию
        getTranslations() {
            return {
                loading: 'Загрузка...',
                create: 'Создать',
                save: 'Сохранить',
                delete: 'Удалить',
                cancel: 'Отмена',
                required: 'Это поле обязательно для заполнения',
                errorLoad: 'Ошибка загрузки инвентаря',
                errorCreate: 'Ошибка создания инвентаря',
                errorUpdate: 'Ошибка обновления инвентаря',
                errorDelete: 'Ошибка удаления инвентаря',
                errorUsageCheck: 'Ошибка проверки использования',
                successCreated: 'Инвентарь создан успешно',
                successUpdated: 'Инвентарь обновлен успешно',
                successDeleted: 'Инвентарь удален успешно',
                noData: 'Инвентарь не найден',
                cannotDelete: 'Нельзя удалить (используется)',
                usedIn: 'Используется в',
                notUsed: 'Не используется в рецептах',
                recipes: 'рецептах',
                checkUsage: 'Проверить использование'
            };
        }

        async init() {
            console.log('InventoryAdminApp initialized');
            await this.loadInventories();
            this.setupEventListeners();
            this.setupCreateFormHandlers();
        }

        async loadInventories() {
            try {
                console.log('Loading inventories...');
                CommonUtils.showLoadingState('inventoriesTableBody', this.translations.loading, 6);

                const response = await this.get('');
                console.log('Inventories loaded:', response);

                if (response.success) {
                    this.inventories = response.data;
                    console.log('Inventories count:', this.inventories.length);
                    this.displayInventories();
                } else {
                    CommonUtils.showError('inventoriesTableBody', this.translations.errorLoad, 6);
                }
            } catch (error) {
                console.error('Error loading inventories:', error);
                CommonUtils.showError('inventoriesTableBody', this.translations.errorLoad, 6);
            }
        }

        displayInventories() {
            const tbody = document.getElementById('inventoriesTableBody');

            if (this.inventories.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">
                            ${this.translations.noData}
                        </td>
                    </tr>
                `;
                return;
            }

            tbody.innerHTML = this.inventories.map(inventory => `
                <tr>
                    <td>${inventory.id}</td>
                    <td class="fw-bold">${CommonUtils.escapeHtml(inventory.name)}</td>
                    <td>${CommonUtils.escapeHtml(inventory.description || '---')}</td>
                    <td class="small">${CommonUtils.formatDateShort(inventory.createdAt)}</td>
                    <td>
                        <span class="badge bg-secondary usage-badge"
                              data-inventory-id="${inventory.id}"
                              style="cursor: pointer;"
                              title="${this.translations.checkUsage}">
                            ${this.translations.checkUsage}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-1 edit-inventory"
                                data-inventory-id="${inventory.id}">
                            ✏️
                        </button>
                        <button class="btn btn-sm btn-outline-danger delete-inventory"
                                data-inventory-id="${inventory.id}"
                                data-inventory-name="${CommonUtils.escapeHtml(inventory.name)}">
                            🗑️
                        </button>
                    </td>
                </tr>
            `).join('');

            this.addEventListeners();
        }

        addEventListeners() {
            // Редактирование
            document.querySelectorAll('.edit-inventory').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const inventoryId = e.target.closest('.edit-inventory').dataset.inventoryId;
                    this.openEditModal(inventoryId);
                });
            });

            // Удаление
            document.querySelectorAll('.delete-inventory').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const inventoryId = e.target.closest('.delete-inventory').dataset.inventoryId;
                    const inventoryName = e.target.closest('.delete-inventory').dataset.inventoryName;
                    this.openDeleteModal(inventoryId, inventoryName);
                });
            });

            // Проверка использования
            document.querySelectorAll('.usage-badge').forEach(badge => {
                badge.addEventListener('click', (e) => {
                    const inventoryId = e.target.dataset.inventoryId;
                    this.checkInventoryUsage(inventoryId);
                });
            });
        }

        setupEventListeners() {
            // Очистка формы при закрытии модального окна создания
            const createModal = document.getElementById('createInventoryModal');
            if (createModal) {
                createModal.addEventListener('hidden.bs.modal', () => {
                    this.resetCreateForm();
                });
            }

            // Обработчики для кнопок модальных окон
            const createBtn = document.getElementById('createInventoryBtn');
            if (createBtn) {
                createBtn.addEventListener('click', () => {
                    this.createInventory();
                });
            }

            const updateBtn = document.getElementById('updateInventoryBtn');
            if (updateBtn) {
                updateBtn.addEventListener('click', () => {
                    this.updateInventory();
                });
            }

            const deleteBtn = document.getElementById('confirmDeleteBtn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', () => {
                    this.deleteInventory();
                });
            }
        }

        setupCreateFormHandlers() {
            const createForm = document.getElementById('createInventoryForm');

            if (createForm) {
                // Обработка ввода для сброса состояния валидации
                createForm.addEventListener('input', (e) => {
                    if (e.target.id === 'inventoryName') {
                        e.target.classList.remove('is-invalid');
                    }
                });

                // Обработка отправки формы по Enter
                createForm.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        this.createInventory();
                    }
                });
            }

            // Очистка формы при открытии модального окна
            const createModal = document.getElementById('createInventoryModal');
            if (createModal) {
                createModal.addEventListener('show.bs.modal', () => {
                    this.resetCreateForm();
                });
            }
        }

        resetCreateForm() {
            const form = document.getElementById('createInventoryForm');
            if (form) {
                form.reset();
                // Сбрасываем состояния валидации
                const nameInput = document.getElementById('inventoryName');
                if (nameInput) {
                    nameInput.classList.remove('is-invalid');
                }
            }
        }

        async createInventory() {
            const nameInput = document.getElementById('inventoryName');
            const descriptionInput = document.getElementById('inventoryDescription');
            const createBtn = document.getElementById('createInventoryBtn');

            const name = nameInput.value.trim();
            const description = descriptionInput.value.trim();

            // Валидация на фронтенде
            if (!name) {
                nameInput.classList.add('is-invalid');
                nameInput.focus();
                CommonUtils.showToast(this.translations.required, 'error');
                return;
            }

            // Блокируем кнопку во время запроса
            if (createBtn) {
                createBtn.disabled = true;
                createBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' +
                                     this.translations.loading;
            }

            try {
                console.log('Sending inventory creation request:', { name, description });
                const response = await this.post('', { name, description });
                console.log('Inventory creation response:', response);

                if (response.success) {
                    CommonUtils.showToast(response.message || this.translations.successCreated);
                    this.resetCreateForm();

                    // Закрываем модальное окно
                    const modal = bootstrap.Modal.getInstance(document.getElementById('createInventoryModal'));
                    if (modal) {
                        modal.hide();
                    }

                    // Перезагружаем список инвентаря
                    await this.loadInventories();
                } else {
                    CommonUtils.showToast(response.message || this.translations.errorCreate, 'error');
                }
            } catch (error) {
                console.error('Error creating inventory:', error);
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
                // Разблокируем кнопку
                if (createBtn) {
                    createBtn.disabled = false;
                    createBtn.innerHTML = this.translations.create;
                }
            }
        }

        openEditModal(inventoryId) {
            const inventory = this.inventories.find(i => i.id == inventoryId);
            if (!inventory) return;

            document.getElementById('editInventoryId').value = inventory.id;
            document.getElementById('editInventoryName').value = inventory.name;
            document.getElementById('editInventoryDescription').value = inventory.description || '';

            new bootstrap.Modal(document.getElementById('editInventoryModal')).show();
        }

        async updateInventory() {
            const id = document.getElementById('editInventoryId').value;
            const description = document.getElementById('editInventoryDescription').value.trim();
            const updateBtn = document.getElementById('updateInventoryBtn');

            if (!id) {
                CommonUtils.showToast(this.translations.errorUpdate, 'error');
                return;
            }

            // Блокируем кнопку во время запроса
            if (updateBtn) {
                updateBtn.disabled = true;
                updateBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' +
                                     this.translations.loading;
            }

            try {
                const response = await this.put(`/${id}`, { description });

                if (response.success) {
                    CommonUtils.showToast(response.message || this.translations.successUpdated);
                    document.getElementById('editInventoryModal').querySelector('.btn-close').click();
                    await this.loadInventories();
                } else {
                    CommonUtils.showToast(response.message || this.translations.errorUpdate, 'error');
                }
            } catch (error) {
                console.error('Error updating inventory:', error);
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
                // Разблокируем кнопку
                if (updateBtn) {
                    updateBtn.disabled = false;
                    updateBtn.innerHTML = this.translations.save;
                }
            }
        }

        async openDeleteModal(inventoryId, inventoryName) {
            document.getElementById('deleteInventoryName').textContent = inventoryName;

            const warningDiv = document.getElementById('deleteWarning');
            const recipeCountSpan = document.getElementById('recipeCount');
            const deleteBtn = document.getElementById('confirmDeleteBtn');

            // Показываем состояние загрузки
            warningDiv.style.display = 'none';
            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.loading;

            const modal = new bootstrap.Modal(document.getElementById('deleteInventoryModal'));
            modal.show();

            document.getElementById('deleteInventoryModal').dataset.inventoryId = inventoryId;

            try {
                const response = await this.get(`/${inventoryId}/usage`);
                if (response.success) {
                    const usage = response.data;

                    if (usage.isUsed) {
                        recipeCountSpan.textContent = usage.recipeCount;
                        warningDiv.style.display = 'block';
                        deleteBtn.disabled = true;
                        deleteBtn.innerHTML = '❌ ' + this.translations.cannotDelete;

                        // Добавляем информацию о связях
                        const usageInfo = document.createElement('div');
                        usageInfo.className = 'alert alert-info mt-2';
                        usageInfo.innerHTML = `<strong>${this.translations.usedIn} ${usage.recipeCount} ${this.translations.recipes}</strong>`;
                        warningDiv.appendChild(usageInfo);
                    } else {
                        warningDiv.style.display = 'none';
                        deleteBtn.disabled = false;
                        deleteBtn.innerHTML = this.translations.delete;
                    }
                }
            } catch (error) {
                console.error('Error checking inventory usage:', error);
                // В случае ошибки разрешаем удаление, но предупреждаем
                warningDiv.style.display = 'block';
                warningDiv.innerHTML = `
                    <div class="alert alert-warning">
                        ⚠️ ${this.translations.errorUsageCheck}
                    </div>
                `;
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = this.translations.delete;
            }
        }

        async deleteInventory() {
            const inventoryId = document.getElementById('deleteInventoryModal').dataset.inventoryId;
            const deleteBtn = document.getElementById('confirmDeleteBtn');

            console.log('Attempting to delete inventory with ID:', inventoryId);

            if (!inventoryId) {
                console.error('No inventory ID found for deletion');
                CommonUtils.showToast('Ошибка: ID инвентаря не найден', 'error');
                return;
            }

            // Блокируем кнопку во время удаления
            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.delete + '...';

            try {
                console.log('Sending DELETE request to:', `/api/admin/inventory/${inventoryId}`);

                const response = await this.delete(`/${inventoryId}`);
                console.log('Delete response:', response);

                if (response.success) {
                    CommonUtils.showToast(response.message || this.translations.successDeleted);
                    console.log('Inventory deleted successfully');

                    // Закрываем модальное окно
                    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteInventoryModal'));
                    if (modal) {
                        modal.hide();
                    }

                    // Перезагружаем список инвентаря
                    await this.loadInventories();
                } else {
                    console.error('Delete failed:', response.message);
                    CommonUtils.showToast(response.message || this.translations.errorDelete, 'error');

                    // Разблокируем кнопку при ошибке
                    deleteBtn.disabled = false;
                    deleteBtn.innerHTML = this.translations.delete;
                }
            } catch (error) {
                console.error('Error deleting inventory:', error);
                let errorMessage = this.translations.errorDelete;

                if (error.response) {
                    try {
                        const errorData = await error.response.json();
                        errorMessage = errorData.message || errorMessage;
                        console.error('Server error response:', errorData);
                    } catch (e) {
                        errorMessage = error.message || errorMessage;
                    }
                } else if (error.message) {
                    errorMessage = error.message;
                }

                CommonUtils.showToast(errorMessage, 'error');

                // Разблокируем кнопку при ошибке
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = this.translations.delete;
            }
        }

        async checkInventoryUsage(inventoryId) {
            try {
                const response = await this.get(`/${inventoryId}/usage`);
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
    let inventoryApp;
    document.addEventListener('DOMContentLoaded', () => {
        console.log('DOM loaded, initializing InventoryAdminApp...');
        inventoryApp = new InventoryAdminApp();
    });

})();