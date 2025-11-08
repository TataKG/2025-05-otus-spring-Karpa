class InventoryAdminApp extends BaseApiClient {
    constructor() {
        super('/api/admin');
        this.inventories = [];
        this.init();
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
            CommonUtils.showLoadingState('inventoriesTableBody', 'Загрузка инвентаря...', 6);

            const response = await this.get('/inventory');
            console.log('Inventories loaded:', response);

            if (response.success) {
                this.inventories = response.data;
                console.log('Inventories count:', this.inventories.length);
                this.displayInventories();
            } else {
                CommonUtils.showError('inventoriesTableBody', 'Ошибка загрузки инвентаря', 6);
            }
        } catch (error) {
            console.error('Error loading inventories:', error);
            CommonUtils.showError('inventoriesTableBody', 'Ошибка загрузки инвентаря', 6);
        }
    }

    displayInventories() {
        const tbody = document.getElementById('inventoriesTableBody');

        if (this.inventories.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        Инвентарь не найден
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
                          style="cursor: pointer;">
                        Проверить использование
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
    }

    setupCreateFormHandlers() {
        const createForm = document.getElementById('createInventoryForm');
        const createBtn = document.getElementById('createInventoryBtn');

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
            CommonUtils.showToast('Введите название инвентаря', 'error');
            return;
        }

        // Блокируем кнопку во время запроса
        if (createBtn) {
            createBtn.disabled = true;
            createBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Создание...';
        }

        try {
            console.log('Sending inventory creation request:', { name, description });
            const response = await this.post('/inventory', { name, description });
            console.log('Inventory creation response:', response);

            if (response.success) {
                CommonUtils.showToast(response.message || 'Инвентарь создан успешно');
                this.resetCreateForm();

                // Закрываем модальное окно
                const modal = bootstrap.Modal.getInstance(document.getElementById('createInventoryModal'));
                if (modal) {
                    modal.hide();
                }

                // Перезагружаем список инвентаря
                await this.loadInventories();
            } else {
                CommonUtils.showToast(response.message || 'Ошибка создания инвентаря', 'error');
            }
        } catch (error) {
            console.error('Error creating inventory:', error);
            let errorMessage = 'Ошибка создания инвентаря';

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
            CommonUtils.showToast('Ошибка: ID инвентаря не найден', 'error');
            return;
        }

        // Блокируем кнопку во время запроса
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Сохранение...';
        }

        try {
            const response = await this.put(`/inventory/${id}`, { description });

            if (response.success) {
                CommonUtils.showToast(response.message || 'Инвентарь обновлен успешно');
                document.getElementById('editInventoryModal').querySelector('.btn-close').click();
                await this.loadInventories();
            } else {
                CommonUtils.showToast(response.message || 'Ошибка обновления инвентаря', 'error');
            }
        } catch (error) {
            console.error('Error updating inventory:', error);
            let errorMessage = 'Ошибка обновления инвентаря';

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
                updateBtn.innerHTML = 'Сохранить';
            }
        }
    }

    async openDeleteModal(inventoryId, inventoryName) {
        document.getElementById('deleteInventoryName').textContent = inventoryName;

        // Проверяем использование инвентаря
        try {
            const response = await this.get(`/inventory/${inventoryId}/usage`);
            if (response.success) {
                const usage = response.data;
                const warningDiv = document.getElementById('deleteWarning');
                const recipeCountSpan = document.getElementById('recipeCount');

                if (usage.isUsed) {
                    recipeCountSpan.textContent = usage.recipeCount;
                    warningDiv.style.display = 'block';
                    // Делаем кнопку удаления неактивной
                    document.querySelector('#deleteInventoryModal .btn-danger').disabled = true;
                    document.querySelector('#deleteInventoryModal .btn-danger').innerHTML =
                        '❌ Нельзя удалить (используется)';
                } else {
                    warningDiv.style.display = 'none';
                    document.querySelector('#deleteInventoryModal .btn-danger').disabled = false;
                    document.querySelector('#deleteInventoryModal .btn-danger').innerHTML = 'Удалить';
                }
            }
        } catch (error) {
            console.error('Error checking inventory usage:', error);
        }

        document.getElementById('deleteInventoryModal').dataset.inventoryId = inventoryId;
        new bootstrap.Modal(document.getElementById('deleteInventoryModal')).show();
    }

    async deleteInventory() {
        const inventoryId = document.getElementById('deleteInventoryModal').dataset.inventoryId;

        try {
            const response = await this.delete(`/inventory/${inventoryId}`);
            CommonUtils.showToast(response.message || 'Инвентарь удален успешно');
            document.getElementById('deleteInventoryModal').querySelector('.btn-close').click();
            await this.loadInventories();
        } catch (error) {
            console.error('Error deleting inventory:', error);
            let errorMessage = 'Ошибка удаления инвентаря';

            if (error.response) {
                try {
                    const errorData = await error.response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = error.message || errorMessage;
                }
            }

            CommonUtils.showToast(errorMessage, 'error');
        }
    }

    async checkInventoryUsage(inventoryId) {
        try {
            const response = await this.get(`/inventory/${inventoryId}/usage`);
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

// Инициализация приложения
let inventoryApp;
document.addEventListener('DOMContentLoaded', () => {
    inventoryApp = new InventoryAdminApp();
});