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
        this.isSaving = false;
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

        if (recipe && recipe.author && recipe.author.id) {
            this.authorId = recipe.author.id;
        } else if (formData.authorId) {
            this.authorId = formData.authorId;
        } else if (formData.currentAuthorId) {
            this.authorId = formData.currentAuthorId;
        } else {
            throw new Error(this.getMessage('error.author_not_found'));
        }

        const savedData = localStorage.getItem('pendingFormData');
        const languageChanged = localStorage.getItem('pendingLanguageChange');

        if (savedData && languageChanged === 'true') {
            this.isRestoringData = true;
            this.populateWithRestoration(recipe, savedData);
        } else {
            this.populateNormal(recipe);
        }

        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeFormContainer').style.display = 'block';
        document.getElementById('errorAlert').style.display = 'none';

        if (this.isRestoringData) {
            setTimeout(() => {
                localStorage.removeItem('pendingFormData');
                localStorage.removeItem('pendingLanguageChange');
                this.isRestoringData = false;
            }, 500);
        }
    }

    populateNormal(recipe) {
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

            if (document.getElementById('title')) {
                document.getElementById('title').value = formData.title || '';
            }

            if (document.getElementById('description')) {
                document.getElementById('description').value = formData.description || '';
            }

            this.populateCategoriesWithRestore(formData.category);

            if (formData.ingredients && Array.isArray(formData.ingredients)) {
                this.populateIngredients(formData.ingredients);
            } else {
                this.populateIngredients([]);
            }

            if (formData.selectedInventory && Array.isArray(formData.selectedInventory)) {
                this.selectedInventory = formData.selectedInventory;
                this.updateAvailableInventory();
                this.renderSelectedInventory();
                this.renderInventorySelect();
            } else {
                this.populateInventory([]);
            }

        } catch (error) {
            this.populateNormal(recipe);
        }
    }

    populateCategories(selectedCategory) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) return;

        categorySelect.innerHTML = '';

        const emptyOption = document.createElement('option');
        emptyOption.value = "";
        emptyOption.textContent = this.getMessage('recipe.category.placeholder');
        categorySelect.appendChild(emptyOption);

        if (this.categories && this.categories.length > 0) {
            this.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = String(category.id);
                option.textContent = category.name;

                if (selectedCategory && selectedCategory.id === category.id) {
                    option.selected = true;
                }
                categorySelect.appendChild(option);
            });
        }
    }

    populateCategoriesWithRestore(savedCategoryId) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) return;

        categorySelect.innerHTML = '';

        const emptyOption = document.createElement('option');
        emptyOption.value = "";
        emptyOption.textContent = this.getMessage('recipe.category.placeholder');
        categorySelect.appendChild(emptyOption);

        if (this.categories && this.categories.length > 0) {
            this.categories.forEach(category => {
                const option = document.createElement('option');
                option.value = String(category.id);
                option.textContent = category.name;
                categorySelect.appendChild(option);
            });

            if (savedCategoryId) {
                const categoryIdStr = String(savedCategoryId);
                setTimeout(() => {
                    categorySelect.value = categoryIdStr;
                }, 0);
            }
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

            const emptyRow = this.createIngredientRow('', false);
            container.appendChild(emptyRow);
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
                    ${isFirst && document.querySelectorAll('.ingredient-row').length === 1 ? 'style="display: none;"' : ''}>🗑️</button>
        `;
        return row;
    }

    setupIngredientHandlers() {
        const container = document.getElementById('ingredientsContainer');

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

            container.addEventListener('keydown', (e) => {
                if (e.target.classList.contains('ingredient-input')) {
                    this.handleIngredientKeydown(e);
                }
            });
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

    handleIngredientKeydown(e) {
        const input = e.target;
        const rows = document.querySelectorAll('.ingredient-row');
        const lastRow = rows[rows.length - 1];
        const lastInput = lastRow.querySelector('.ingredient-input');

        if ((e.key === 'Enter' || e.key === 'Tab') && input === lastInput && input.value.trim() !== '') {
            e.preventDefault();
            this.addIngredientField();

            setTimeout(() => {
                const newRows = document.querySelectorAll('.ingredient-row');
                const newLastRow = newRows[newRows.length - 1];
                const newInput = newLastRow.querySelector('.ingredient-input');
                if (newInput) {
                    newInput.focus();
                }
            }, 10);
        }
    }

    addIngredientField() {
        const container = document.getElementById('ingredientsContainer');
        if (container) {
            const newRow = this.createIngredientRow();
            container.appendChild(newRow);
            this.updateRemoveButtons();
        }
    }

    removeIngredientField(button) {
        const row = button.closest('.ingredient-row');
        const rows = document.querySelectorAll('.ingredient-row');

        if (row && rows.length > 1) {
            row.remove();
            this.updateRemoveButtons();
        }
    }

    updateRemoveButtons() {
        const rows = document.querySelectorAll('.ingredient-row');
        const ingredientRemoveButtons = document.querySelectorAll('.remove-ingredient');

        ingredientRemoveButtons.forEach((btn, index) => {
            if (rows.length === 1 && index === 0) {
                btn.style.display = 'none';
            } else {
                btn.style.display = 'block';
            }
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
        if (this.isSaving) {
            return;
        }

        if (!this.validateForm()) {
            return;
        }

        this.isSaving = true;

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
            this.originalSaveDraftText = document.getElementById('saveDraftBtn').innerHTML;
            this.originalPublishText = document.getElementById('publishBtn').innerHTML;

            this.showSavingState(true);

            let response;
            if (this.recipeId) {
                response = await this.put(`/${this.recipeId}`, recipeData);
            } else {
                response = await this.post('', recipeData);
            }

            if (response && response.success) {
                this.showSuccess(response.message);

                setTimeout(() => {
                    window.location.href = '/my-recipes';
                }, 2000);
            } else {
                const errorMessage = response?.message || response?.error || this.getMessage('error.save_unknown');
                throw new Error(errorMessage);
            }
        } catch (error) {
            this.showError(error.message || this.getMessage('error.save_failed'));
            this.showSavingState(false);
        } finally {
            this.isSaving = false;
        }
    }

    showSavingState(show) {
        const saveDraftBtn = document.getElementById('saveDraftBtn');
        const publishBtn = document.getElementById('publishBtn');

        if (saveDraftBtn) {
            saveDraftBtn.disabled = show;
            if (show) {
                saveDraftBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.getMessage('common.saving')}`;
            } else {
                saveDraftBtn.innerHTML = this.originalSaveDraftText || `💾 ${this.getMessage('recipe.save_draft')}`;
            }
        }

        if (publishBtn) {
            publishBtn.disabled = show;
            if (show) {
                publishBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> ${this.getMessage('common.publishing')}`;
            } else {
                publishBtn.innerHTML = this.originalPublishText || `🚀 ${this.getMessage('recipe.publish')}`;
            }
        }
    }

    showSuccess(message) {
        CommonUtils.showToast(message, 'success');
    }

    showError(message) {
        CommonUtils.showToast(message, 'error');
    }
}

let recipeApp;
document.addEventListener('DOMContentLoaded', () => {
    try {
        recipeApp = new RecipeEditApp();
        window.recipeApp = recipeApp;
    } catch (error) {
        const errorAlert = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');
        const loadingSpinner = document.getElementById('loadingSpinner');

        if (errorAlert && errorMessage) {
            const errorMsg = (window.i18nMessages?.['error.initialization'] || 'Ошибка инициализации: ') + error.message;
            errorMessage.textContent = errorMsg;
            errorAlert.style.display = 'block';
        }

        if (loadingSpinner) {
            loadingSpinner.style.display = 'none';
        }
    }
});