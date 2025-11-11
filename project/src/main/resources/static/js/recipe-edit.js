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
            this.showLoadError('Ошибка загрузки формы: ' + error.message);
        }
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

            console.log('Loading form data from:', url);

            const response = await this.get(url);
            if (!response) {
                        throw new Error('Пустой ответ от сервера');
            }

            if (response.success) {
                this.populateForm(response.data);
            } else {
                throw new Error(response.message || 'Failed to load form data');
            }
        } catch (error) {
            console.error('Error loading form data:', error);
            throw new Error('Не удалось загрузить данные формы: ' + error.message);
        }
    }

    populateForm(formData) {
        console.log('FULL FORM DATA:', formData);

        if (!formData) {
            throw new Error('Form data is empty');
        }

        const { recipe, categories, inventoryItems } = formData;

        console.log('Detailed form data analysis:', {
            hasRecipe: !!recipe,
            recipeAuthor: recipe?.author,
            recipeAuthorId: recipe?.author?.id,
            categoriesCount: categories ? categories.length : 0,
            inventoryCount: inventoryItems ? inventoryItems.length : 0,
            formDataKeys: Object.keys(formData)
        });

        // Сохраняем данные
        this.categories = categories || [];
        this.inventoryItems = inventoryItems || [];
        this.availableInventory = [...this.inventoryItems];

        // Получаем authorId - проверяем разные возможные места
        if (recipe && recipe.author && recipe.author.id) {
            this.authorId = recipe.author.id;
            console.log('Author ID from recipe.author:', this.authorId);
        } else if (formData.authorId) {
            this.authorId = formData.authorId;
            console.log('Author ID from formData.authorId:', this.authorId);
        } else if (formData.currentAuthorId) {
            this.authorId = formData.currentAuthorId;
            console.log('Author ID from formData.currentAuthorId:', this.authorId);
        } else {
            console.warn('Author ID not found in form data. Available keys:', Object.keys(formData));
            throw new Error('Author not found. Please make sure you are logged in.');
        }

        console.log('Final Author ID:', this.authorId);

        // Проверяем категории
        if (!this.categories || this.categories.length === 0) {
            console.warn('No categories available in form data');
        }

        // Проверяем инвентарь
        if (!this.inventoryItems || this.inventoryItems.length === 0) {
            console.warn('No inventory items available in form data');
        }

        // Заполняем основные поля
        if (recipe && recipe.title) {
            document.getElementById('title').value = recipe.title;
        }

        if (recipe && recipe.description) {
            document.getElementById('description').value = recipe.description;
        }

        // Заполняем категории
        this.populateCategories(recipe?.category);

        // Заполняем ингредиенты
        this.populateIngredients(recipe?.ingredients || []);

        // Заполняем инвентарь
        this.populateInventory(recipe?.inventoryItems || []);

        // Показываем форму
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeFormContainer').style.display = 'block';
        document.getElementById('errorAlert').style.display = 'none';
    }

    populateCategories(selectedCategory) {
        const categorySelect = document.getElementById('category');
        if (!categorySelect) return;

        categorySelect.innerHTML = '<option value="">Выберите категорию</option>';

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
            // Показываем все доступные опции
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
            // Фильтруем по поисковому запросу
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
                noResultsOption.textContent = `Ничего не найдено для "${searchTerm}"`;
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
            this.showError('Пожалуйста, выберите инвентарь из списка');
            return;
        }

        const selectedId = parseInt(select.value);
        if (!selectedId) return;

        const selectedInventory = this.inventoryItems.find(item => item.id === selectedId);
        if (!selectedInventory) {
            this.showError('Выбранный инвентарь не найден');
            return;
        }

        if (this.selectedInventory.some(item => item.id === selectedId)) {
            this.showError('Этот инвентарь уже добавлен');
            return;
        }

        this.selectedInventory.push(selectedInventory);
        this.updateAvailableInventory();
        this.renderSelectedInventory();
        this.renderInventorySelect();

        if (searchInput) {
            searchInput.value = '';
        }

        this.showSuccess(`Инвентарь "${selectedInventory.name}" добавлен`);
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
            container.innerHTML = '<div class="text-muted">Инвентарь не выбран</div>';
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
                            title="Удалить инвентарь">
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
        this.showSuccess('Инвентарь удален');
    }

    createIngredientRow(value = '', isFirst = false) {
        const row = document.createElement('div');
        row.className = 'ingredient-row input-group mb-2';

        const escapedValue = CommonUtils.escapeHtml(value);

        row.innerHTML = `
            <input type="text" class="form-control ingredient-input"
                   value="${escapedValue}"
                   placeholder="Например: Мука - 200г">
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

        if (!title.value.trim() || title.value.trim().length < 2) {
            errors.push('Название рецепта должно содержать минимум 2 символа');
            isValid = false;
        }

        if (!category.value) {
            errors.push('Пожалуйста, выберите категорию');
            isValid = false;
        }

        const ingredients = this.getIngredients();
        if (ingredients.length === 0) {
            errors.push('Добавьте хотя бы один ингредиент');
            isValid = false;
        }

        if (!description.value.trim() || description.value.trim().length < 10) {
            errors.push('Описание рецепта должно содержать минимум 10 символов');
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

        // Собираем данные с дополнительной проверкой
        const title = document.getElementById('title').value.trim();
        const categoryId = parseInt(document.getElementById('category').value);
        const description = document.getElementById('description').value.trim();
        const ingredients = this.getIngredients();
        const inventoryIds = this.getSelectedInventory();

        console.log('Form data before validation:', {
            title,
            categoryId,
            authorId: this.authorId,
            ingredients,
            description,
            inventoryIds,
            published: publish
        });

        // Детальная валидация
        if (!title || title.length < 2) {
            this.showError('Название рецепта должно содержать минимум 2 символа');
            return;
        }

        if (!categoryId || isNaN(categoryId)) {
            this.showError('Пожалуйста, выберите категорию');
            return;
        }

        if (!this.authorId || isNaN(this.authorId)) {
            this.showError('Ошибка авторизации. Пожалуйста, перезагрузите страницу');
            return;
        }

        if (!ingredients || ingredients.length === 0) {
            this.showError('Добавьте хотя бы один ингредиент');
            return;
        }

        if (!description || description.length < 10) {
            this.showError('Описание рецепта должно содержать минимум 10 символов');
            return;
        }

        // Подготавливаем данные для отправки
        const recipeData = {
            title: title,
            categoryId: categoryId,
            authorId: this.authorId,
            ingredients: ingredients,
            description: description,
            inventoryIds: Array.isArray(inventoryIds) ? inventoryIds : [],
            published: publish
        };

        console.log('Saving recipe data:', recipeData);

        try {
            this.showSavingState(true);

            let response;
            if (this.recipeId) {
                console.log(`Updating recipe ${this.recipeId}`);
                response = await this.put(`/${this.recipeId}`, recipeData);
            } else {
                console.log('Creating new recipe');
                response = await this.post('', recipeData);
            }

            console.log('Save response:', response);

            if (response && response.success) {
                const message = this.recipeId ?
                    (publish ? 'Рецепт успешно обновлен и опубликован' : 'Рецепт успешно сохранен как черновик') :
                    (publish ? 'Рецепт успешно создан и опубликован' : 'Рецепт успешно сохранен как черновик');

                this.showSuccess(message);

                setTimeout(() => {
                    window.location.href = '/my-recipes';
                }, 1500);
            } else {
                const errorMessage = response?.message || response?.error || 'Неизвестная ошибка при сохранении рецепта';
                console.error('Save failed with response:', response);
                throw new Error(errorMessage);
            }
        } catch (error) {
            console.error('Error saving recipe:', error);

            // Детальный анализ ошибки
            let errorMessage = 'Не удалось сохранить рецепт';

            if (error.message.includes('401')) {
                errorMessage = 'Требуется авторизация. Пожалуйста, войдите в систему.';
            } else if (error.message.includes('403')) {
                errorMessage = 'Доступ запрещен. У вас нет прав для сохранения рецептов.';
            } else if (error.message.includes('404')) {
                errorMessage = 'Ресурс не найден. Проверьте корректность данных категории.';
            } else if (error.message.includes('500')) {
                errorMessage = 'Ошибка сервера. Возможные причины:\n' +
                              '- Неверный формат данных\n' +
                              '- Проблема с базой данных\n' +
                              '- Ошибка валидации на сервере\n\n' +
                              'Проверьте консоль сервера для деталей.';
            } else if (error.message.includes('Произошла непредвиденная ошибка')) {
                errorMessage = 'Серверная ошибка. Пожалуйста:\n' +
                              '1. Проверьте что все поля заполнены корректно\n' +
                              '2. Убедитесь что категория выбрана\n' +
                              '3. Проверьте консоль сервера для детальной информации';
            } else {
                errorMessage = error.message || 'Неизвестная ошибка';
            }

            this.showError(errorMessage);

            // Показываем дополнительные детали для отладки
            console.error('Detailed error analysis:', {
                recipeData: recipeData,
                authorId: this.authorId,
                isEdit: !!this.recipeId,
                error: error
            });
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
                '<span class="spinner-border spinner-border-sm" role="status"></span> Сохранение...' :
                '💾 Сохранить черновик';
        }

        if (publishBtn) {
            publishBtn.disabled = show;
            publishBtn.innerHTML = show ?
                '<span class="spinner-border spinner-border-sm" role="status"></span> Публикация...' :
                '🚀 Опубликовать';
        }
    }

    showSuccess(message) {
        CommonUtils.showToast(message, 'success');
    }

    showError(message) {
        CommonUtils.showToast(message, 'error');
    }
}

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', () => {
    try {
        new RecipeEditApp();
    } catch (error) {
        console.error('Failed to initialize RecipeEditApp:', error);
        const errorAlert = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');
        const loadingSpinner = document.getElementById('loadingSpinner');

        if (errorAlert && errorMessage) {
            errorMessage.textContent = 'Ошибка инициализации: ' + error.message;
            errorAlert.style.display = 'block';
        }

        if (loadingSpinner) {
            loadingSpinner.style.display = 'none';
        }
    }
});