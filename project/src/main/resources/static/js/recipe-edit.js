// recipe-edit.js - для страницы создания/редактирования рецептов
class RecipeEditApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        this.recipeId = null;
        this.authorId = null;
        this.categories = [];
        this.inventoryItems = [];
        this.selectedInventory = [];
        this.availableInventory = [];
        this.isInitialized = false;
        this.isRestoringData = false;
        this.init();
    }

    async init() {
        this.showLoadingState();
        try {
            await this.loadFormData();
            this.setupIngredientHandlers();
            this.setupInventoryHandlers();
            this.setupFormHandlers();
            this.setupValidation();
            this.isInitialized = true;
        } catch (error) {
            console.error('Initialization error:', error);
            this.showLoadError(this.getMessage('error.load_form') + error.message);
        }
    }

    getMessage(key) {
        return window.i18nMessages?.[key] || key;
    }

    showLoadingState() {
        document.getElementById('loadingSpinner').style.display = 'block';
        document.getElementById('recipeFormContainer').style.display = 'none';
        document.getElementById('errorAlert').style.display = 'none';
    }

    showLoadError(message) {
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeFormContainer').style.display = 'none';
        document.getElementById('errorAlert').style.display = 'block';
        document.getElementById('errorMessage').textContent = message;
    }

    async loadFormData() {
        try {
            const isEdit = window.location.pathname.includes('/edit/');
            let url;

            if (isEdit) {
                const recipeId = window.location.pathname.split('/').pop();
                this.recipeId = recipeId;
                url = `/edit-form-data/${recipeId}`;
            } else {
                url = '/create-form-data';
            }

            const response = await this.get(url);
            if (!response) {
                throw new Error(this.getMessage('error.empty_response'));
            }

            if (response.success) {
                this.populateForm(response.data);
            } else {
                throw new Error(response.message || this.getMessage('error.load_form_data'));
            }
        } catch (error) {
            console.error('Error loading form data:', error);
            throw new Error(this.getMessage('error.load_form') + error.message);
        }
    }

    populateForm(formData) {
        if (!formData) {
            throw new Error(this.getMessage('error.form_data_empty'));
        }

        const { recipe, categories, inventoryItems } = formData;

        this.categories = categories || [];
        this.inventoryItems = inventoryItems || [];
        this.availableInventory = [...this.inventoryItems];

        console.log('📥 Данные формы получены:', {
            categoriesCount: this.categories.length,
            inventoryCount: this.inventoryItems.length,
            recipe: recipe ? recipe.title : 'нет'
        });

        // Получаем authorId из различных возможных источников
        if (recipe && recipe.author && recipe.author.id) {
            this.authorId = recipe.author.id;
        } else if (formData.authorId) {
            this.authorId = formData.authorId;
        } else if (formData.currentAuthorId) {
            this.authorId = formData.currentAuthorId;
        } else {
            throw new Error(this.getMessage('error.author_not_found'));
        }

        // ВОССТАНОВЛЕНИЕ ДАННЫХ: проверяем, есть ли сохраненные данные
        const savedData = localStorage.getItem('pendingFormData');
        const languageChanged = localStorage.getItem('pendingLanguageChange');

        if (savedData && languageChanged === 'true') {
            this.isRestoringData = true;
            console.log('🔄 Восстанавливаем данные формы после смены языка...');

            // При восстановлении используем специальный метод
            this.populateWithRestoration(recipe, savedData);
        } else {
            // Обычная загрузка
            this.populateNormal(recipe);
        }

        // Обновляем UI
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeFormContainer').style.display = 'block';
        document.getElementById('errorAlert').style.display = 'none';

        // Очищаем флаги после восстановления
        if (this.isRestoringData) {
            setTimeout(() => {
                localStorage.removeItem('pendingFormData');
                localStorage.removeItem('pendingLanguageChange');
                this.isRestoringData = false;
                console.log('✅ Данные формы восстановлены');
            }, 500);
        }
    }

    populateNormal(recipe) {
        // Обычное заполнение формы
        if (recipe && recipe.title) {
            document.getElementById('title').value = recipe.title;
        }

        if (recipe && recipe.description) {
            document.getElementById('description').value = recipe.description;
        }

        this.populateCategories(recipe?.category);
        this.populateIngredients(recipe?.ingredients || []);
        this.populateInventory(recipe?.inventoryItems || []);
    }

    populateWithRestoration(recipe, savedData) {
        try {
            const formData = JSON.parse(savedData);
            console.log('📋 Восстанавливаем сохраненные данные:', formData);

            // 1. Восстанавливаем основные поля
            if (document.getElementById('title')) {
                document.getElementById('title').value = formData.title || '';
            }

            if (document.getElementById('description')) {
                document.getElementById('description').value = formData.description || '';
            }

            // 2. Заполняем категории СРАЗУ с восстановлением значения
            this.populateCategoriesWithRestore(formData.category);

            // 3. Восстанавливаем ингредиенты
            if (formData.ingredients && Array.isArray(formData.ingredients)) {
                this.populateIngredients(formData.ingredients);
            } else {
                this.populateIngredients([]);
            }

            // 4. Восстанавливаем инвентарь
            if (formData.selectedInventory && Array.isArray(formData.selectedInventory)) {
                this.selectedInventory = formData.selectedInventory;
                this.updateAvailableInventory();
                this.renderSelectedInventory();
                this.renderInventorySelect();
            } else {
                this.populateInventory([]);
            }

        } catch (error) {
            console.error('❌ Ошибка при восстановлении данных:', error);
            // В случае ошибки восстанавливаем пустую форму
            this.populateNormal(recipe);
        }
    }

    populateCategories(selectedCategory) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) {
            console.error('❌ Элемент category не найден в DOM');
            return;
        }

        // Очищаем select
        categorySelect.innerHTML = '';

        // Добавляем пустую опцию
        const emptyOption = document.createElement('option');
        emptyOption.value = "";
        emptyOption.textContent = this.getMessage('recipe.category.placeholder');
        categorySelect.appendChild(emptyOption);

        // Добавляем категории из БД
        if (this.categories && this.categories.length > 0) {
            this.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = String(category.id);
                option.textContent = category.name;

                // Автоматически выбираем категорию если указана
                if (selectedCategory && selectedCategory.id === category.id) {
                    option.selected = true;
                }
                categorySelect.appendChild(option);
            });

            console.log('✅ Категории загружены в select:', this.categories.length, 'шт');
        } else {
            console.warn('⚠️ Нет категорий для загрузки в select');
        }
    }

    populateCategoriesWithRestore(savedCategoryId) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) {
            console.error('❌ Элемент category не найден в DOM');
            return;
        }

        // Очищаем select
        categorySelect.innerHTML = '';

        // Добавляем пустую опцию
        const emptyOption = document.createElement('option');
        emptyOption.value = "";
        emptyOption.textContent = this.getMessage('recipe.category.placeholder');
        categorySelect.appendChild(emptyOption);

        // Добавляем категории из БД
        if (this.categories && this.categories.length > 0) {
            this.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = String(category.id);
                option.textContent = category.name;
                categorySelect.appendChild(option);
            });

            console.log('✅ Категории загружены в select:', this.categories.length, 'шт');

            // НЕМЕДЛЕННО восстанавливаем сохраненное значение
            if (savedCategoryId) {
                const categoryIdStr = String(savedCategoryId);
                setTimeout(() => {
                    categorySelect.value = categoryIdStr;
                    console.log('🎯 Категория восстановлена:', categoryIdStr, '->', categorySelect.value);

                    // Дополнительная проверка через небольшой таймаут
                    setTimeout(() => {
                        if (categorySelect.value !== categoryIdStr) {
                            console.warn('⚠️ Категория не установилась, пробуем снова...');
                            categorySelect.value = categoryIdStr;
                        }
                    }, 50);
                }, 0);
            }
        } else {
            console.warn('⚠️ Нет категорий для загрузки в select');
        }
    }

    populateIngredients(ingredients) {
        const container = document.getElementById('ingredientsContainer');
        if (!container) return;

        container.innerHTML = '';

        if (ingredients && ingredients.length > 0) {
            ingredients.forEach((ingredient, index) => {
                const row = this.createIngredientRow(ingredient, index === 0);
                container.appendChild(row);
            });
        } else {
            container.appendChild(this.createIngredientRow('', true));
        }

        this.updateRemoveButtons();
    }

    populateInventory(selectedInventory) {
        this.selectedInventory = [];

        if (selectedInventory && selectedInventory.length > 0) {
            selectedInventory.forEach(item => {
                const fullInventoryItem = this.inventoryItems.find(inv => inv.id === item.id);
                if (fullInventoryItem) {
                    this.selectedInventory.push(fullInventoryItem);
                }
            });
        }

        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();
    }

    updateAvailableInventory() {
        const selectedIds = this.selectedInventory.map(item => item.id);
        this.availableInventory = this.inventoryItems.filter(item => !selectedIds.includes(item.id));
    }

    setupInventoryHandlers() {
        const searchInput = document.getElementById('inventorySearch');
        const select = document.getElementById('inventorySelect');
        const addButton = document.getElementById('addInventoryBtn');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterInventoryOptions(e.target.value);
            });

            searchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.addSelectedInventory();
                }
            });
        }

        if (select) {
            select.addEventListener('change', () => {
                this.updateAddButtonState();
            });

            select.addEventListener('dblclick', () => {
                this.addSelectedInventory();
            });
        }

        if (addButton) {
            addButton.addEventListener('click', () => {
                this.addSelectedInventory();
            });
        }
    }

    filterInventoryOptions(searchTerm) {
        const select = document.getElementById('inventorySelect');
        if (!select) return;

        const searchLower = searchTerm.toLowerCase().trim();
        select.innerHTML = '';

        if (!searchTerm) {
            this.availableInventory.forEach(inventory => {
                const option = document.createElement('option');
                option.value = inventory.id;
                option.textContent = inventory.name;
                if (inventory.description) {
                    option.title = inventory.description;
                }
                select.appendChild(option);
            });
        } else {
            const filteredInventory = this.availableInventory.filter(inventory =>
                inventory.name.toLowerCase().includes(searchLower) ||
                (inventory.description && inventory.description.toLowerCase().includes(searchLower))
            );

            if (filteredInventory.length > 0) {
                filteredInventory.forEach(inventory => {
                    const option = document.createElement('option');
                    option.value = inventory.id;
                    option.textContent = inventory.name;
                    if (inventory.description) {
                        option.title = inventory.description;
                    }
                    select.appendChild(option);
                });
                select.selectedIndex = 0;
            } else {
                const noResultsOption = document.createElement('option');
                noResultsOption.value = "";
                noResultsOption.textContent = this.getMessage('inventory.search.no_results').replace('{searchTerm}', searchTerm);
                noResultsOption.disabled = true;
                select.appendChild(noResultsOption);
            }
        }

        this.updateAddButtonState();
    }

    addSelectedInventory() {
        const select = document.getElementById('inventorySelect');
        const searchInput = document.getElementById('inventorySearch');

        if (!select || !select.value) {
            this.showError(this.getMessage('inventory.select_required'));
            return;
        }

        const selectedId = parseInt(select.value);
        if (!selectedId) return;

        const selectedInventory = this.inventoryItems.find(item => item.id === selectedId);
        if (!selectedInventory) {
            this.showError(this.getMessage('inventory.not_found'));
            return;
        }

        if (this.selectedInventory.some(item => item.id === selectedId)) {
            this.showError(this.getMessage('inventory.already_added'));
            return;
        }

        this.selectedInventory.push(selectedInventory);
        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();

        if (searchInput) {
            searchInput.value = '';
        }

        this.showSuccess(this.getMessage('inventory.added').replace('{name}', selectedInventory.name));
    }

    updateAddButtonState() {
        const addButton = document.getElementById('addInventoryBtn');
        const select = document.getElementById('inventorySelect');

        if (addButton && select) {
            addButton.disabled = !select.value || select.options[select.selectedIndex]?.disabled;
        }
    }

    renderInventorySelect() {
        const select = document.getElementById('inventorySelect');
        if (!select) return;

        const searchInput = document.getElementById('inventorySearch');
        if (searchInput && searchInput.value) {
            this.filterInventoryOptions(searchInput.value);
        } else {
            this.filterInventoryOptions('');
        }
    }

    renderSelectedInventory() {
        const container = document.getElementById('inventoryContainer');
        if (!container) return;

        if (this.selectedInventory.length === 0) {
            container.innerHTML = `<div class="text-muted">${this.getMessage('recipe.inventory.none')}</div>`;
            return;
        }

        let html = '';
        this.selectedInventory.forEach((inventory) => {
            html += `
                <div class="inventory-row d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                    <div class="flex-grow-1">
                        <div class="fw-medium">${CommonUtils.escapeHtml(inventory.name)}</div>
                        ${inventory.description ? `<div class="text-muted small">${CommonUtils.escapeHtml(inventory.description)}</div>` : ''}
                    </div>
                    <button type="button" class="btn btn-outline-danger btn-sm remove-inventory"
                            data-inventory-id="${inventory.id}"
                            title="${this.getMessage('inventory.remove')}">
                        🗑️
                    </button>
                </div>
            `;
        });

        container.innerHTML = html;

        container.querySelectorAll('.remove-inventory').forEach(button => {
            button.addEventListener('click', (e) => {
                const inventoryId = parseInt(e.target.closest('.remove-inventory').dataset.inventoryId);
                this.removeInventory(inventoryId);
            });
        });
    }

    removeInventory(inventoryId) {
        this.selectedInventory = this.selectedInventory.filter(item => item.id !== inventoryId);
        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();
        this.showSuccess(this.getMessage('inventory.removed'));
    }

    createIngredientRow(value = '', isFirst = false) {
        const row = document.createElement('div');
        row.className = 'ingredient-row input-group mb-2';

        const escapedValue = CommonUtils.escapeHtml(value);

        row.innerHTML = `
            <input type="text" class="form-control ingredient-input"
                   value="${escapedValue}"
                   placeholder="${this.getMessage('recipe.ingredient.placeholder')}">
            <button type="button" class="btn btn-outline-danger remove-ingredient"
                    ${isFirst ? 'style="display: none;"' : ''}>🗑️</button>
        `;
        return row;
    }

    setupIngredientHandlers() {
        const addButton = document.getElementById('addIngredient');
        const container = document.getElementById('ingredientsContainer');

        if (addButton) {
            addButton.addEventListener('click', () => {
                this.addIngredientField();
            });
        }

        if (container) {
            container.addEventListener('click', (e) => {
                if (e.target.classList.contains('remove-ingredient')) {
                    this.removeIngredientField(e.target);
                }
            });

            container.addEventListener('input', (e) => {
                if (e.target.classList.contains('ingredient-input')) {
                    this.handleIngredientInput(e.target);
                }
            });
        }
    }

    addIngredientField() {
        const container = document.getElementById('ingredientsContainer');
        if (container) {
            container.appendChild(this.createIngredientRow());
            this.updateRemoveButtons();
        }
    }

    removeIngredientField(button) {
        const row = button.closest('.ingredient-row');
        if (row && document.querySelectorAll('.ingredient-row').length > 1) {
            row.remove();
            this.updateRemoveButtons();
        }
    }

    handleIngredientInput(input) {
        const rows = document.querySelectorAll('.ingredient-row');
        const lastRow = rows[rows.length - 1];
        const lastInput = lastRow.querySelector('.ingredient-input');

        if (input === lastInput && input.value.trim() !== '') {
            this.addIngredientField();
        }
    }

    updateRemoveButtons() {
        const rows = document.querySelectorAll('.ingredient-row');
        const ingredientRemoveButtons = document.querySelectorAll('.remove-ingredient');

        ingredientRemoveButtons.forEach(btn => {
            btn.style.display = rows.length > 1 ? 'block' : 'none';
        });
    }

    setupFormHandlers() {
        const saveDraftBtn = document.getElementById('saveDraftBtn');
        const publishBtn = document.getElementById('publishBtn');

        if (saveDraftBtn) {
            saveDraftBtn.addEventListener('click', () => {
                this.saveRecipe(false);
            });
        }

        if (publishBtn) {
            publishBtn.addEventListener('click', () => {
                this.saveRecipe(true);
            });
        }
    }

    setupValidation() {
        const fields = ['title', 'category', 'description'];
        fields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (field) {
                field.addEventListener('blur', () => this.validateField(field));
                field.addEventListener('input', () => {
                    field.classList.remove('is-invalid');
                });
            }
        });
    }

    validateField(field) {
        let isValid = true;

        if (field.tagName === 'SELECT') {
            if (!field.value) {
                isValid = false;
            }
        } else if (!field.value.trim()) {
            isValid = false;
        }

        if (!isValid) {
            field.classList.add('is-invalid');
        } else {
            field.classList.remove('is-invalid');
        }

        return isValid;
    }

    validateForm() {
        let isValid = true;
        const errors = [];

        const title = document.getElementById('title');
        const category = document.getElementById('category');
        const description = document.getElementById('description');

        if (!title.value.trim()) {
            errors.push(this.getMessage('validation.title.required'));
            isValid = false;
        }

        if (!category.value) {
            errors.push(this.getMessage('validation.category.required'));
            isValid = false;
        }

        const ingredients = this.getIngredients();
        if (ingredients.length === 0) {
            errors.push(this.getMessage('validation.ingredients.required'));
            isValid = false;
        }

        if (!description.value.trim()) {
            errors.push(this.getMessage('validation.description.required'));
            isValid = false;
        }

        if (!isValid && errors.length > 0) {
            this.showError(errors.join('\n'));
        }

        return isValid;
    }

    getIngredients() {
        const inputs = document.querySelectorAll('.ingredient-input');
        return Array.from(inputs)
            .map(input => input.value.trim())
            .filter(ingredient => ingredient !== '');
    }

    getSelectedInventory() {
        return this.selectedInventory.map(item => item.id);
    }

    async saveRecipe(publish) {
        if (!this.validateForm()) {
            return;
        }

        const title = document.getElementById('title').value.trim();
        const categoryId = parseInt(document.getElementById('category').value);
        const description = document.getElementById('description').value.trim();
        const ingredients = this.getIngredients();
        const inventoryIds = this.getSelectedInventory();

        const recipeData = {
            title: title,
            categoryId: categoryId,
            authorId: this.authorId,
            ingredients: ingredients,
            description: description,
            inventoryIds: Array.isArray(inventoryIds) ? inventoryIds : [],
            published: publish
        };

        try {
            this.showSavingState(true);

            let response;
            if (this.recipeId) {
                response = await this.put(`/${this.recipeId}`, recipeData);
            } else {
                response = await this.post('', recipeData);
            }

            if (response && response.success) {
                const message = this.recipeId ?
                    (publish ? this.getMessage('recipe.updated_published') : this.getMessage('recipe.updated_draft')) :
                    (publish ? this.getMessage('recipe.created_published') : this.getMessage('recipe.created_draft'));

                this.showSuccess(message);

                setTimeout(() => {
                    window.location.href = '/my-recipes';
                }, 1500);
            } else {
                const errorMessage = response?.message || response?.error || this.getMessage('error.save_unknown');
                throw new Error(errorMessage);
            }
        } catch (error) {
            console.error('Error saving recipe:', error);
            this.showError(error.message || this.getMessage('error.save_failed'));
        } finally {
            this.showSavingState(false);
        }
    }

    showSavingState(show) {
        const saveDraftBtn = document.getElementById('saveDraftBtn');
        const publishBtn = document.getElementById('publishBtn');

        if (saveDraftBtn) {
            saveDraftBtn.disabled = show;
            saveDraftBtn.innerHTML = show ?
                `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.getMessage('common.saving')}` :
                `💾 ${this.getMessage('recipe.save_draft')}`;
        }

        if (publishBtn) {
            publishBtn.disabled = show;
            publishBtn.innerHTML = show ?
                `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.getMessage('common.publishing')}` :
                `🚀 ${this.getMessage('recipe.publish')}`;
        }
    }

    showSuccess(message) {
        CommonUtils.showToast(message, 'success');
    }

    showError(message) {
        CommonUtils.showToast(message, 'error');
    }
}

// Делаем app глобально доступной для функций
let recipeApp;

document.addEventListener('DOMContentLoaded', () => {
    try {
        recipeApp = new RecipeEditApp();
        window.recipeApp = recipeApp; // Делаем глобально доступной
    } catch (error) {
        console.error('Failed to initialize RecipeEditApp:', error);
        const errorAlert = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');
        const loadingSpinner = document.getElementById('loadingSpinner');

        if (errorAlert && errorMessage) {
            // Используем прямое обращение к i18nMessages, так как this недоступен
            const errorMsg = (window.i18nMessages?.['error.initialization'] || 'Ошибка инициализации: ') + error.message;
            errorMessage.textContent = errorMsg;
            errorAlert.style.display = 'block';
        }

        if (loadingSpinner) {
            loadingSpinner.style.display = 'none';
        }
    }
});