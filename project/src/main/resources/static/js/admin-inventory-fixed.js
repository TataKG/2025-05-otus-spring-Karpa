// admin-inventory-fixed.js - полностью автономная версия
(function() {
    'use strict';

    // Защита от повторного выполнения
    if (window.InventoryAdminAppInitialized) {
        console.log('⚠️ InventoryAdminApp already initialized');
        return;
    }
    window.InventoryAdminAppInitialized = true;

    // ВРЕМЕННАЯ КОПИЯ BaseApiClient прямо в этом файле
    class TemporaryBaseApiClient {
        constructor(baseUrl = '/api') {
            this.baseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
            this.pendingRequests = new Map();
        }

        async makeRequest(fullUrl, options) {
            try {
                const response = await fetch(fullUrl, options);
                console.log(`📨 API Response status: ${response.status} ${response.statusText}`);

                if (!response.ok) {
                    let errorMessage = `HTTP error! status: ${response.status}`;
                    try {
                        const errorResult = await response.json();
                        errorMessage = errorResult.message || errorMessage;
                    } catch (e) {
                        // Ignore JSON parsing error
                    }
                    throw new Error(errorMessage);
                }

                // Handle 204 No Content
                if (response.status === 204) {
                    return { success: true };
                }

                const data = await response.json();
                console.log(`✅ API Response data:`, data);
                return data;
            } catch (error) {
                console.error(`💥 Fetch error for ${fullUrl}:`, error);
                throw error;
            }
        }

        async request(url, options = {}) {
            const fullUrl = url.startsWith('/') ? `${this.baseUrl}${url}` : `${this.baseUrl}/${url}`;

            // Защита от дублирующихся запросов
            const requestKey = `${options.method || 'GET'}:${fullUrl}`;
            if (this.pendingRequests.has(requestKey)) {
                console.log(`⚠️ Skipping duplicate request: ${requestKey}`);
                return this.pendingRequests.get(requestKey);
            }

            console.log(`🔗 API ${options.method || 'GET'}: ${fullUrl}`);

            const defaultOptions = {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            };

            try {
                const requestPromise = this.makeRequest(fullUrl, { ...defaultOptions, ...options });
                this.pendingRequests.set(requestKey, requestPromise);

                const response = await requestPromise;
                return response;
            } catch (error) {
                // Преобразуем ошибку для единообразной обработки
                if (error instanceof TypeError && error.message.includes('Failed to fetch')) {
                    throw new Error('Ошибка сети. Проверьте подключение к интернету.');
                }
                throw error;
            } finally {
                this.pendingRequests.delete(requestKey);
            }
        }

        async get(url) {
            return this.request(url, { method: 'GET' });
        }

        async post(url, data) {
            return this.request(url, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }

        async put(url, data) {
            return this.request(url, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        }

        async delete(url) {
            return this.request(url, { method: 'DELETE' });
        }
    }

    // ВРЕМЕННАЯ КОПИЯ CommonUtils
    const TemporaryCommonUtils = {
        escapeHtml(unsafe) {
            if (unsafe === null || unsafe === undefined) return '';
            return unsafe
                .toString()
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        },

        formatDateShort(dateString) {
            if (!dateString) return 'Не указана';
            try {
                const date = new Date(dateString);
                if (isNaN(date.getTime())) {
                    return 'Неверная дата';
                }
                return date.toLocaleDateString('ru-RU', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric'
                });
            } catch (error) {
                console.error('Error formatting date:', error);
                return 'Ошибка даты';
            }
        },

        showToast(message, type = 'success') {
            const toastElement = type === 'success'
                ? document.getElementById('successToast')
                : document.getElementById('errorToast');

            const toastMessage = type === 'success'
                ? document.getElementById('successToastMessage')
                : document.getElementById('errorToastMessage');

            if (toastMessage && toastElement) {
                toastMessage.textContent = message;
                try {
                    const toast = new bootstrap.Toast(toastElement);
                    toast.show();
                } catch (error) {
                    console.error('Error showing toast:', error);
                }
            }
        },

        showLoadingState(containerId, message = 'Загрузка...', columns = 5) {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = `
                    <tr>
                        <td colspan="${columns}" class="text-center py-4">
                            <div class="spinner-border text-primary" role="status">
                                <span class="visually-hidden">Загрузка...</span>
                            </div>
                            <p class="mt-2 text-muted">${message}</p>
                        </td>
                    </tr>
                `;
            }
        },

        showError(containerId, message, columns = 5) {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = `
                    <tr>
                        <td colspan="${columns}" class="text-center py-4">
                            <div class="text-danger mb-2">❌</div>
                            <p class="text-muted">${message}</p>
                            <button class="btn btn-sm btn-outline-primary" onclick="location.reload()">
                                Повторить
                            </button>
                        </td>
                    </tr>
                `;
            }
        }
    };

    // ОСНОВНОЙ КЛАСС ПРИЛОЖЕНИЯ
    class InventoryAdminApp extends TemporaryBaseApiClient {
        constructor() {
            super('/api/admin');
            this.inventories = [];
            this.translations = this.getTranslations();
            this.initialized = false;
        }

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
                checkUsage: 'Проверить использование',
                checkingUsage: 'Проверка использования...'
            };
        }

        async init() {
            if (this.initialized) {
                console.log('⚠️ InventoryAdminApp already initialized');
                return;
            }

            console.log('🚀 InventoryAdminApp initialization started');

            // Проверяем зависимости
            if (typeof bootstrap === 'undefined') {
                console.error('❌ Bootstrap not available');
                this.showError('Bootstrap not loaded');
                return;
            }

            try {
                await this.loadInventories();
                this.setupEventListeners();
                this.setupCreateFormHandlers();
                this.initialized = true;
                console.log('✅ InventoryAdminApp initialized successfully');
            } catch (error) {
                console.error('💥 InventoryAdminApp initialization failed:', error);
                this.showError('Ошибка инициализации: ' + error.message);
            }
        }

        async loadInventories() {
            try {
                console.log('📥 Loading inventories...');
                TemporaryCommonUtils.showLoadingState('inventoriesTableBody', this.translations.loading, 6);

                const response = await this.get('/inventory');
                console.log('✅ Inventories API response:', response);

                if (response && response.success) {
                    this.inventories = response.data || [];
                    console.log(`📊 Loaded ${this.inventories.length} inventory items`);
                    this.displayInventories();
                } else {
                    const errorMsg = response?.message || this.translations.errorLoad;
                    console.error('❌ API returned error:', errorMsg);
                    TemporaryCommonUtils.showError('inventoriesTableBody', errorMsg, 6);
                }
            } catch (error) {
                console.error('💥 Error loading inventories:', error);
                TemporaryCommonUtils.showError('inventoriesTableBody', this.translations.errorLoad, 6);
            }
        }

        displayInventories() {
            const tbody = document.getElementById('inventoriesTableBody');
            if (!tbody) {
                console.error('❌ inventoriesTableBody not found');
                return;
            }

            if (!this.inventories || this.inventories.length === 0) {
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
                    <td class="fw-bold">${TemporaryCommonUtils.escapeHtml(inventory.name)}</td>
                    <td>${TemporaryCommonUtils.escapeHtml(inventory.description || '—')}</td>
                    <td class="small">${TemporaryCommonUtils.formatDateShort(inventory.createdAt)}</td>
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
                                data-inventory-name="${TemporaryCommonUtils.escapeHtml(inventory.name)}">
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
            console.log('🔧 Setting up global event listeners');

            // Создание инвентаря
            const createBtn = document.getElementById('createInventoryBtn');
            if (createBtn) {
                createBtn.addEventListener('click', () => {
                    this.createInventory();
                });
            }

            // Обновление инвентаря
            const updateBtn = document.getElementById('updateInventoryBtn');
            if (updateBtn) {
                updateBtn.addEventListener('click', () => {
                    this.updateInventory();
                });
            }

            // Удаление инвентаря
            const deleteBtn = document.getElementById('confirmDeleteBtn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', () => {
                    this.deleteInventory();
                });
            }

            // Обработчики модальных окон
            const createModal = document.getElementById('createInventoryModal');
            if (createModal) {
                createModal.addEventListener('hidden.bs.modal', () => {
                    this.resetCreateForm();
                });
            }
        }

        setupCreateFormHandlers() {
            const createForm = document.getElementById('createInventoryForm');
            if (createForm) {
                createForm.addEventListener('input', (e) => {
                    if (e.target.id === 'inventoryName') {
                        e.target.classList.remove('is-invalid');
                    }
                });

                createForm.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        this.createInventory();
                    }
                });
            }

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

            if (!name) {
                nameInput.classList.add('is-invalid');
                nameInput.focus();
                TemporaryCommonUtils.showToast(this.translations.required, 'error');
                return;
            }

            if (createBtn) {
                createBtn.disabled = true;
                createBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.loading;
            }

            try {
                console.log('Sending inventory creation request:', { name, description });
                const response = await this.post('/inventory', { name, description });
                console.log('Inventory creation response:', response);

                if (response.success) {
                    TemporaryCommonUtils.showToast(response.message || this.translations.successCreated);
                    this.resetCreateForm();

                    const modal = bootstrap.Modal.getInstance(document.getElementById('createInventoryModal'));
                    if (modal) {
                        modal.hide();
                    }

                    await this.loadInventories();
                } else {
                    TemporaryCommonUtils.showToast(response.message || this.translations.errorCreate, 'error');
                }
            } catch (error) {
                console.error('Error creating inventory:', error);
                TemporaryCommonUtils.showToast(this.translations.errorCreate, 'error');
            } finally {
                if (createBtn) {
                    createBtn.disabled = false;
                    createBtn.innerHTML = this.translations.create;
                }
            }
        }

        openEditModal(inventoryId) {
            const inventory = this.inventories.find(i => i.id == inventoryId);
            if (!inventory) {
                console.error('Inventory not found:', inventoryId);
                return;
            }

            document.getElementById('editInventoryId').value = inventory.id;
            document.getElementById('editInventoryName').value = inventory.name;
            document.getElementById('editInventoryDescription').value = inventory.description || '';

            new bootstrap.Modal(document.getElementById('editInventoryModal')).show();
        }

        async updateInventory() {
            const id = document.getElementById('editInventoryId').value;
            const name = document.getElementById('editInventoryName').value.trim();
            const description = document.getElementById('editInventoryDescription').value.trim();
            const updateBtn = document.getElementById('updateInventoryBtn');

            if (!name) {
                TemporaryCommonUtils.showToast(this.translations.required, 'error');
                return;
            }

            if (updateBtn) {
                updateBtn.disabled = true;
                updateBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.loading;
            }

            try {
                const response = await this.put(`/inventory/${id}`, { name, description });

                if (response.success) {
                    TemporaryCommonUtils.showToast(response.message || this.translations.successUpdated);

                    const modalElement = document.getElementById('editInventoryModal');
                    const closeBtn = modalElement.querySelector('.btn-close');
                    if (closeBtn) closeBtn.click();

                    await this.loadInventories();
                } else {
                    TemporaryCommonUtils.showToast(response.message || this.translations.errorUpdate, 'error');
                }
            } catch (error) {
                console.error('Error updating inventory:', error);
                TemporaryCommonUtils.showToast(this.translations.errorUpdate, 'error');
            } finally {
                if (updateBtn) {
                    updateBtn.disabled = false;
                    updateBtn.innerHTML = this.translations.save;
                }
            }
        }

        async openDeleteModal(inventoryId, inventoryName) {
            document.getElementById('deleteInventoryName').textContent = inventoryName;
            document.getElementById('deleteInventoryModal').dataset.inventoryId = inventoryId;

            const warningDiv = document.getElementById('deleteWarning');
            const deleteBtn = document.getElementById('confirmDeleteBtn');

            warningDiv.style.display = 'none';
            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.checkingUsage;

            const modal = new bootstrap.Modal(document.getElementById('deleteInventoryModal'));
            modal.show();

            try {
                console.log('Checking usage for inventory ID:', inventoryId);
                const response = await this.get(`/inventory/${inventoryId}/usage`);
                console.log('Usage check response:', response);

                if (response.success) {
                    const usage = response.data;
                    console.log('Usage data:', usage);

                    if (usage.isUsed) {
                        warningDiv.style.display = 'block';
                        warningDiv.innerHTML = `
                            <div class="alert alert-warning">
                                ⚠️ Этот инвентарь используется в <strong>${usage.recipeCount}</strong> рецептах и не может быть удален.
                            </div>
                        `;

                        deleteBtn.disabled = true;
                        deleteBtn.innerHTML = '❌ ' + this.translations.cannotDelete;
                    } else {
                        warningDiv.style.display = 'none';
                        deleteBtn.disabled = false;
                        deleteBtn.innerHTML = this.translations.delete;
                    }
                } else {
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

        async deleteInventory() {
            const inventoryId = document.getElementById('deleteInventoryModal').dataset.inventoryId;
            const deleteBtn = document.getElementById('confirmDeleteBtn');

            if (!inventoryId) {
                console.error('No inventory ID found for deletion');
                TemporaryCommonUtils.showToast('Ошибка: ID инвентаря не найден', 'error');
                return;
            }

            if (deleteBtn.disabled) {
                console.log('Delete button is disabled, skipping deletion');
                return;
            }

            deleteBtn.disabled = true;
            deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> ' + this.translations.delete + '...';

            try {
                console.log('Sending DELETE request for inventory ID:', inventoryId);
                const response = await this.delete(`/inventory/${inventoryId}`);
                console.log('Delete response:', response);

                if (response.success) {
                    TemporaryCommonUtils.showToast(response.message || this.translations.successDeleted);
                    console.log('Inventory deleted successfully');

                    const modal = bootstrap.Modal.getInstance(document.getElementById('deleteInventoryModal'));
                    if (modal) {
                        modal.hide();
                    }

                    await this.loadInventories();
                } else {
                    console.error('Delete failed:', response.message);
                    TemporaryCommonUtils.showToast(response.message || this.translations.errorDelete, 'error');
                    deleteBtn.disabled = false;
                    deleteBtn.innerHTML = this.translations.delete;
                }
            } catch (error) {
                console.error('Error deleting inventory:', error);
                TemporaryCommonUtils.showToast(this.translations.errorDelete, 'error');
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = this.translations.delete;
            }
        }

        async checkInventoryUsage(inventoryId) {
            try {
                const response = await this.get(`/inventory/${inventoryId}/usage`);
                if (response.success) {
                    const usage = response.data;
                    const message = usage.isUsed ?
                        `${this.translations.usedIn} ${usage.recipeCount} ${this.translations.recipes}` :
                        this.translations.notUsed;

                    TemporaryCommonUtils.showToast(message, usage.isUsed ? 'info' : 'success');
                }
            } catch (error) {
                console.error('Error checking usage:', error);
                TemporaryCommonUtils.showToast(this.translations.errorUsageCheck, 'error');
            }
        }

        showError(message) {
            const tbody = document.getElementById('inventoriesTableBody');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center py-4 text-danger">
                            ❌ ${message}
                            <br>
                            <button class="btn btn-sm btn-outline-primary mt-2" onclick="location.reload()">
                                Обновить страницу
                            </button>
                        </td>
                    </tr>
                `;
            }
        }
    }

    // Глобальная инициализация
    function initializeInventoryApp() {
        // Проверяем, что мы на странице инвентаря
        if (!document.getElementById('inventoriesTableBody')) {
            console.log('ℹ️ Not on inventory page, skipping initialization');
            return null;
        }

        // Проверяем зависимости
        if (typeof bootstrap === 'undefined') {
            console.error('❌ Bootstrap not available');
            setTimeout(initializeInventoryApp, 100);
            return null;
        }

        try {
            const inventoryApp = new InventoryAdminApp();
            inventoryApp.init().catch(error => {
                console.error('💥 Failed to initialize inventory app:', error);
            });
            return inventoryApp;
        } catch (error) {
            console.error('💥 Error creating inventory app:', error);
            return null;
        }
    }

    // Инициализация при загрузке DOM
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            console.log('📄 DOM Content Loaded - initializing inventory app');
            setTimeout(initializeInventoryApp, 100);
        });
    } else {
        console.log('📄 DOM already loaded - initializing inventory app');
        setTimeout(initializeInventoryApp, 100);
    }

})();