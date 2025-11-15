// recipe-edit.js - улучшенная обработка ошибок валидации с бэкенда
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
        this.isSaving = false;
        this.init();
    }

    async init() {
        this.showLoadingState();
        try {
            await this.loadFormData();
            this.setupEventHandlers();
            this.isInitialized = true;
        } catch (error) {
            this.showLoadError(this.getMessage('error.load_form') + error.message);
        }
    }

    getMessage(key) {
        return window.i18nMessages?.[key] || key;
    }

    showLoadingState() {
        document.getElementById('loadingSpinner').style.display = 'block';
        document.getElementById('recipeForm').style.display = 'none';
        document.getElementById('errorAlert').style.display = 'none';
    }

    showLoadError(message) {
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeForm').style.display = 'none';
        document.getElementById('errorAlert').style.display = 'block';
        document.getElementById('errorMessage').textContent = message;
    }

    async loadFormData() {
        try {
            const isEdit = window.location.pathname.includes('/edit/');
            const url = isEdit
                ? `/edit-form-data/${window.location.pathname.split('/').pop()}`
                : '/create-form-data';

            const response = await this.get(url);

            if (!response?.success) {
                throw new Error(response?.message || this.getMessage('error.load_form_data'));
            }

            this.populateForm(response.data);
        } catch (error) {
            throw new Error(this.getMessage('error.load_form') + error.message);
        }
    }

    populateForm(formData) {
        if (!formData?.recipe) {
            throw new Error(this.getMessage('error.form_data_empty'));
        }

        const { recipe, categories, inventoryItems } = formData;

        this.categories = categories || [];
        this.inventoryItems = inventoryItems || [];
        this.availableInventory = [...this.inventoryItems];
        this.authorId = recipe.author?.id;

        if (!this.authorId) {
            throw new Error(this.getMessage('error.author_not_found'));
        }

        // Заполняем форму данными
        document.getElementById('authorId').value = this.authorId;

        if (recipe.title) {
            document.getElementById('title').value = recipe.title;
        }

        if (recipe.description) {
            document.getElementById('description').value = recipe.description;
        }

        this.populateCategories(recipe.category);
        this.populateIngredients(recipe.ingredients || []);
        this.populateInventory(recipe.inventoryItems || []);

        // Показываем форму
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeForm').style.display = 'block';
    }

    populateCategories(selectedCategory) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) return;

        categorySelect.innerHTML = '<option value="">' + this.getMessage('recipe.category.placeholder') + '</option>';

        this.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            if (selectedCategory && selectedCategory.id === category.id) {
                option.selected = true;
            }
            categorySelect.appendChild(option);
        });
    }

    populateIngredients(ingredients) {
        const container = document.getElementById('ingredientsContainer');
        if (!container) return;

        container.innerHTML = '';

        const ingredientsToShow = ingredients.length > 0 ? ingredients : [''];

        ingredientsToShow.forEach((ingredient, index) => {
            const row = this.createIngredientRow(ingredient, index === 0);
            container.appendChild(row);
        });

        this.updateRemoveButtons();
    }

    populateInventory(selectedInventory) {
        this.selectedInventory = selectedInventory.filter(item =>
            this.inventoryItems.some(inv => inv.id === item.id)
        );

        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();
    }

    setupEventHandlers() {
        this.setupFormHandler();
        this.setupIngredientHandlers();
        this.setupInventoryHandlers();
        this.setupFieldValidation();
    }

    setupFormHandler() {
        const form = document.getElementById('recipeForm');
        if (form) {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const isPublish = e.submitter?.value === 'true';
                this.saveRecipe(isPublish);
            });
        }
    }

    setupFieldValidation() {
        // Базовая подсветка полей при вводе
        const fields = ['title', 'category', 'description'];
        fields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (field) {
                field.addEventListener('input', () => {
                    field.classList.remove('is-invalid');
                    field.classList.remove('is-valid');
                });
            }
        });

        // Подсветка ингредиентов
        const ingredientsContainer = document.getElementById('ingredientsContainer');
        if (ingredientsContainer) {
            ingredientsContainer.addEventListener('input', (e) => {
                if (e.target.classList.contains('ingredient-input')) {
                    e.target.classList.remove('is-invalid');
                    e.target.classList.remove('is-valid');
                }
            });
        }
    }

    setupIngredientHandlers() {
        const container = document.getElementById('ingredientsContainer');
        if (!container) return;

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

        container.addEventListener('keydown', (e) => {
            if (e.target.classList.contains('ingredient-input') && e.key === 'Enter') {
                e.preventDefault();
                this.handleIngredientEnter(e.target);
            }
        });
    }

    setupInventoryHandlers() {
        const searchInput = document.getElementById('inventorySearch');
        const select = document.getElementById('inventorySelect');
        const addButton = document.getElementById('addInventoryBtn');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterInventoryOptions(e.target.value);
            });
        }

        if (select) {
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

    // Методы для работы с ингредиентами
    createIngredientRow(value = '', isFirst = false) {
        const row = document.createElement('div');
        row.className = 'ingredient-row input-group mb-2';
        row.innerHTML = `
            <input type="text" class="form-control ingredient-input" name="ingredients"
                   value="${CommonUtils.escapeHtml(value)}"
                   placeholder="${this.getMessage('recipe.ingredient.placeholder')}">
            <button type="button" class="btn btn-outline-danger remove-ingredient"
                    ${isFirst ? 'style="display: none;"' : ''}>🗑️</button>
        `;
        return row;
    }

    handleIngredientInput(input) {
        const rows = document.querySelectorAll('.ingredient-row');
        const lastInput = rows[rows.length - 1]?.querySelector('.ingredient-input');

        if (input === lastInput && input.value.trim()) {
            this.addIngredientField();
        }
    }

    handleIngredientEnter(input) {
        const rows = document.querySelectorAll('.ingredient-row');
        const lastInput = rows[rows.length - 1]?.querySelector('.ingredient-input');

        if (input === lastInput && input.value.trim()) {
            this.addIngredientField();
            setTimeout(() => {
                const newRows = document.querySelectorAll('.ingredient-row');
                newRows[newRows.length - 1]?.querySelector('.ingredient-input')?.focus();
            }, 10);
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
        const rows = document.querySelectorAll('.ingredient-row');
        if (rows.length > 1) {
            button.closest('.ingredient-row').remove();
            this.updateRemoveButtons();
        }
    }

    updateRemoveButtons() {
        const rows = document.querySelectorAll('.ingredient-row');
        const buttons = document.querySelectorAll('.remove-ingredient');

        buttons.forEach((btn, index) => {
            btn.style.display = rows.length === 1 && index === 0 ? 'none' : 'block';
        });
    }

    // Методы для работы с инвентарем
    filterInventoryOptions(searchTerm) {
        const select = document.getElementById('inventorySelect');
        if (!select) return;

        const searchLower = searchTerm.toLowerCase().trim();
        select.innerHTML = '';

        const filtered = searchTerm
            ? this.availableInventory.filter(item =>
                item.name.toLowerCase().includes(searchLower) ||
                item.description?.toLowerCase().includes(searchLower)
              )
            : this.availableInventory;

        if (filtered.length > 0) {
            filtered.forEach(item => {
                const option = document.createElement('option');
                option.value = item.id;
                option.textContent = item.name;
                option.title = item.description || '';
                select.appendChild(option);
            });
        } else {
            const option = document.createElement('option');
            option.disabled = true;
            option.textContent = this.getMessage('inventory.search.no_results');
            select.appendChild(option);
        }
    }

    addSelectedInventory() {
        const select = document.getElementById('inventorySelect');
        const selectedId = parseInt(select.value);

        if (!selectedId) return;

        const inventory = this.inventoryItems.find(item => item.id === selectedId);
        if (!inventory || this.selectedInventory.some(item => item.id === selectedId)) return;

        this.selectedInventory.push(inventory);
        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();

        document.getElementById('inventorySearch').value = '';
    }

    removeInventory(inventoryId) {
        this.selectedInventory = this.selectedInventory.filter(item => item.id !== inventoryId);
        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();
    }

    updateAvailableInventory() {
        const selectedIds = this.selectedInventory.map(item => item.id);
        this.availableInventory = this.inventoryItems.filter(item => !selectedIds.includes(item.id));
    }

    renderInventorySelect() {
        const searchInput = document.getElementById('inventorySearch');
        this.filterInventoryOptions(searchInput?.value || '');
    }

    renderSelectedInventory() {
        const container = document.getElementById('inventoryContainer');
        if (!container) return;

        if (this.selectedInventory.length === 0) {
            container.innerHTML = `<div class="text-muted">${this.getMessage('recipe.inventory.none')}</div>`;
            return;
        }

        container.innerHTML = this.selectedInventory.map(inventory => `
            <div class="inventory-row d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                <div class="flex-grow-1">
                    <div class="fw-medium">${CommonUtils.escapeHtml(inventory.name)}</div>
                    ${inventory.description ? `<div class="text-muted small">${CommonUtils.escapeHtml(inventory.description)}</div>` : ''}
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm remove-inventory"
                        data-inventory-id="${inventory.id}">
                    🗑️
                </button>
            </div>
        `).join('');

        // Добавляем обработчики для кнопок удаления
        container.querySelectorAll('.remove-inventory').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.removeInventory(parseInt(e.target.closest('.remove-inventory').dataset.inventoryId));
            });
        });
    }

    // Основной метод сохранения
    async saveRecipe(publish) {
        if (this.isSaving) return;
        this.isSaving = true;

        try {
            this.showSavingState(true);
            this.clearFieldErrors();

            const formData = this.prepareFormData(publish);
            const response = this.recipeId
                ? await this.put(`/${this.recipeId}`, formData)
                : await this.post('', formData);

            if (response?.success) {
                this.handleSaveSuccess(response, publish);
            } else {
                throw new Error(response?.message || this.getMessage('error.save_unknown'));
            }
        } catch (error) {
            this.handleSaveError(error);
        } finally {
            this.isSaving = false;
            this.showSavingState(false);
        }
    }

    prepareFormData(publish) {
        return {
            title: document.getElementById('title').value.trim(),
            categoryId: parseInt(document.getElementById('category').value),
            authorId: this.authorId,
            ingredients: this.getIngredients(),
            description: document.getElementById('description').value.trim(),
            inventoryIds: this.selectedInventory.map(item => item.id),
            published: publish
        };
    }

    getIngredients() {
        return Array.from(document.querySelectorAll('.ingredient-input'))
            .map(input => input.value.trim())
            .filter(ingredient => ingredient);
    }

    clearFieldErrors() {
        // Очищаем ошибки со всех полей
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });

        // Очищаем сообщения об ошибках
        document.querySelectorAll('.invalid-feedback').forEach(el => {
            el.remove();
        });
    }

    highlightFieldErrors(errorMessage) {
        const errors = this.parseBackendErrors(errorMessage);

        errors.forEach(error => {
            this.highlightSpecificError(error);
        });
    }

    parseBackendErrors(errorMessage) {
        // Разбиваем сообщение на отдельные ошибки по запятым
        return errorMessage.split(',').map(err => err.trim()).filter(err => err);
    }

    highlightSpecificError(errorText) {
        // Сопоставляем текст ошибки с конкретными полями
        if (errorText.includes('Название рецепта')) {
            this.highlightTitleError(errorText);
        } else if (errorText.includes('Описание')) {
            this.highlightDescriptionError(errorText);
        } else if (errorText.includes('ингредиент') || errorText.includes('Ингредиент')) {
            this.highlightIngredientsError(errorText);
        } else if (errorText.includes('Категория') || errorText.includes('категори')) {
            this.highlightCategoryError(errorText);
        }
    }

    highlightTitleError(errorText) {
        const titleField = document.getElementById('title');
        if (titleField) {
            titleField.classList.add('is-invalid');
            this.addFieldError(titleField, errorText);
        }
    }

    highlightDescriptionError(errorText) {
        const descriptionField = document.getElementById('description');
        if (descriptionField) {
            descriptionField.classList.add('is-invalid');
            this.addFieldError(descriptionField, errorText);
        }
    }

    highlightIngredientsError(errorText) {
        const ingredientsContainer = document.getElementById('ingredientsContainer');
        if (ingredientsContainer) {
            // Подсвечиваем первый ингредиент или контейнер
            const firstIngredient = ingredientsContainer.querySelector('.ingredient-input');
            if (firstIngredient) {
                firstIngredient.classList.add('is-invalid');
                this.addFieldError(ingredientsContainer, errorText);
            } else {
                ingredientsContainer.classList.add('border', 'border-danger', 'rounded', 'p-2');
                this.addFieldError(ingredientsContainer, errorText);
            }
        }
    }

    highlightCategoryError(errorText) {
        const categoryField = document.getElementById('category');
        if (categoryField) {
            categoryField.classList.add('is-invalid');
            this.addFieldError(categoryField, errorText);
        }
    }

    addFieldError(field, errorText) {
        // Удаляем старые сообщения об ошибках для этого поля
        const existingError = field.parentNode.querySelector('.invalid-feedback');
        if (existingError) {
            existingError.remove();
        }

        // Создаем новое сообщение об ошибке
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback d-block';
        errorDiv.textContent = errorText;

        // Добавляем сообщение после поля
        field.parentNode.appendChild(errorDiv);
    }

    handleSaveSuccess(response, publish) {
        this.showSuccess(response.message);

        setTimeout(() => {
            window.location.href = '/my-recipes';
        }, 1500);
    }

    handleSaveError(error) {
        const errorMessage = error.message || this.getMessage('error.save_failed');

        if (this.isValidationError(errorMessage)) {
            // Показываем ошибки валидации с подсветкой полей
            this.showValidationErrors(errorMessage);
        } else {
            // Показываем общую ошибку
            this.showError(this.getMessage('error.save_failed') + ': ' + errorMessage);
        }
    }

    isValidationError(errorMessage) {
        // Проверяем, содержит ли сообщение ошибки валидации
        const validationKeywords = [
            'обязательно', 'должен', 'минимум', 'максимум',
            'required', 'must', 'minimum', 'maximum',
            'символов', 'characters'
        ];

        return validationKeywords.some(keyword =>
            errorMessage.toLowerCase().includes(keyword.toLowerCase())
        );
    }

    showValidationErrors(errorMessage) {
        // Парсим и подсвечиваем ошибки валидации
        this.highlightFieldErrors(errorMessage);

        // Показываем общее сообщение
        const generalError = this.extractGeneralValidationMessage(errorMessage);
        this.showError(generalError);

        // Прокручиваем к первой ошибке
        this.scrollToFirstError();
    }

    extractGeneralValidationMessage(errorMessage) {
        const errors = this.parseBackendErrors(errorMessage);
        if (errors.length === 1) {
            return errors[0];
        } else {
            return this.getMessage('validation.errors_found') + ' (' + errors.length + ')';
        }
    }

    scrollToFirstError() {
        const firstError = document.querySelector('.is-invalid');
        if (firstError) {
            firstError.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });

            // Фокусируемся на поле с ошибкой
            if (firstError.tagName === 'INPUT' || firstError.tagName === 'TEXTAREA' || firstError.tagName === 'SELECT') {
                firstError.focus();
            }
        }
    }

    showSavingState(show) {
        const buttons = ['saveDraftBtn', 'publishBtn'];

        buttons.forEach(btnId => {
            const button = document.getElementById(btnId);
            if (button) {
                button.disabled = show;
                if (show) {
                    const originalText = button.innerHTML;
                    button.dataset.originalText = originalText;
                    const savingText = btnId === 'publishBtn'
                        ? this.getMessage('common.publishing')
                        : this.getMessage('common.saving');
                    button.innerHTML = `<span class="spinner-border spinner-border-sm"></span> ${savingText}`;
                } else {
                    button.innerHTML = button.dataset.originalText;
                }
            }
        });
    }

    showSuccess(message) {
        CommonUtils.showToast(message, 'success');
    }

    showError(message) {
        CommonUtils.showToast(message, 'error');
    }
}

// Добавляем сообщения для обработки ошибок валидации
document.addEventListener('DOMContentLoaded', () => {
    // Расширяем i18nMessages
    window.i18nMessages = {
        ...window.i18nMessages,
        'validation.errors_found': 'Обнаружены ошибки в форме',
        'validation.check_fields': 'Пожалуйста, проверьте заполнение полей'
    };

    try {
        new RecipeEditApp();
    } catch (error) {
        console.error('Failed to initialize RecipeEditApp:', error);
        const errorElement = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');

        if (errorElement && errorMessage) {
            errorMessage.textContent = error.message;
            errorElement.style.display = 'block';
            document.getElementById('loadingSpinner').style.display = 'none';
        }
    }
});