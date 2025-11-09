// recipe-edit.js - для страницы создания/редактирования рецептов
class RecipeEditApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        this.recipeId = null;
        this.authorId = null;
        this.categories = [];
        this.inventoryItems = [];
        this.selectedInventory = []; // Храним выбранные объекты инвентаря
        this.availableInventory = []; // Доступный для выбора инвентарь
        this.init();
    }

    async init() {
        // Показываем спиннер загрузки
        this.showLoadingState();

        try {
            await this.loadFormData();
            this.setupIngredientHandlers();
            this.setupInventoryHandlers();
            this.setupFormHandlers();
            this.setupValidation();
        } catch (error) {
            this.showLoadError('Ошибка инициализации: ' + error.message);
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

        const errorAlert = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');

        errorMessage.textContent = message;
        errorAlert.style.display = 'block';
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
        if (!formData) {
            throw new Error('Form data is empty');
        }

        const { recipe, categories, inventoryItems } = formData;

        console.log('Populating form with:', {
            recipe: recipe ? {
                id: recipe.id,
                title: recipe.title,
                author: recipe.author ? { id: recipe.author.id } : null,
                category: recipe.category,
                ingredients: recipe.ingredients ? recipe.ingredients.length : 0,
                inventoryItems: recipe.inventoryItems ? recipe.inventoryItems.length : 0
            } : null,
            categoriesCount: categories ? categories.length : 0,
            inventoryCount: inventoryItems ? inventoryItems.length : 0
        });

        // Сохраняем данные для использования
        this.categories = categories || [];
        this.inventoryItems = inventoryItems || [];
        this.availableInventory = [...this.inventoryItems]; // Копируем для доступного инвентаря

        // Безопасное получение authorId
        if (recipe && recipe.author && recipe.author.id) {
            this.authorId = recipe.author.id;
            console.log('Author ID set to:', this.authorId);
        } else {
            throw new Error('Author not found in recipe data');
        }

        // Заполняем основные поля
        if (recipe.title) {
            document.getElementById('title').value = recipe.title;
        }

        if (recipe.description) {
            document.getElementById('description').value = recipe.description;
        }

        // Заполняем категории
        this.populateCategories(recipe.category);

        // Заполняем ингредиенты
        this.populateIngredients(recipe.ingredients || []);

        // Заполняем инвентарь
        this.populateInventory(recipe.inventoryItems || []);

        // Показываем форму после заполнения
        document.getElementById('loadingSpinner').style.display = 'none';
        document.getElementById('recipeFormContainer').style.display = 'block';
        document.getElementById('errorAlert').style.display = 'none';
    }

    populateCategories(selectedCategory) {
        const categorySelect = document.getElementById('category');
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
        // Очищаем выбранный инвентарь
        this.selectedInventory = [];

        // Заполняем выбранный инвентарь
        if (selectedInventory && selectedInventory.length > 0) {
            selectedInventory.forEach(item => {
                // Находим полный объект инвентаря по ID
                const fullInventoryItem = this.inventoryItems.find(inv => inv.id === item.id);
                if (fullInventoryItem) {
                    this.selectedInventory.push(fullInventoryItem);
                }
            });
        }

        // Обновляем доступный инвентарь (исключаем уже выбранные)
        this.updateAvailableInventory();

        // Отображаем выбранный инвентарь
        this.renderSelectedInventory();
    }

    updateAvailableInventory() {
        const selectedIds = this.selectedInventory.map(item => item.id);
        this.availableInventory = this.inventoryItems.filter(item => !selectedIds.includes(item.id));

        // Заполняем выпадающий список
        this.renderInventorySelect();
    }

    renderInventorySelect() {
        const select = document.getElementById('inventorySelect');
        select.innerHTML = '<option value="">-- Выберите инвентарь --</option>';

        this.availableInventory.forEach(inventory => {
            const option = document.createElement('option');
            option.value = inventory.id;
            option.textContent = inventory.name;
            if (inventory.description) {
                option.setAttribute('data-description', inventory.description);
            }
            select.appendChild(option);
        });

        // Если доступного инвентаря нет, показываем сообщение
        if (this.availableInventory.length === 0) {
            const option = document.createElement('option');
            option.value = "";
            option.textContent = "Весь инвентарь уже выбран";
            option.disabled = true;
            select.appendChild(option);
            document.getElementById('addInventoryBtn').disabled = true;
        } else {
            document.getElementById('addInventoryBtn').disabled = false;
        }
    }

    renderSelectedInventory() {
        const container = document.getElementById('inventoryContainer');
        const noSelectionText = document.getElementById('noInventorySelected');

        if (this.selectedInventory.length === 0) {
            container.innerHTML = '<div class="text-muted" id="noInventorySelected">Инвентарь не выбран</div>';
            return;
        }

        // Скрываем сообщение "нет инвентаря"
        if (noSelectionText) {
            noSelectionText.style.display = 'none';
        }

        let html = '';
        this.selectedInventory.forEach((inventory, index) => {
            html += this.createInventoryRow(inventory, index === 0);
        });

        container.innerHTML = html;

        // Добавляем обработчики для кнопок удаления
        container.querySelectorAll('.remove-inventory').forEach(button => {
            button.addEventListener('click', (e) => {
                const inventoryId = parseInt(e.target.closest('.remove-inventory').dataset.inventoryId);
                this.removeInventory(inventoryId);
            });
        });
    }

    createInventoryRow(inventory, isFirst = false) {
        return `
            <div class="inventory-row d-flex justify-content-between align-items-center">
                <div class="flex-grow-1">
                    <div class="inventory-name">${CommonUtils.escapeHtml(inventory.name)}</div>
                    ${inventory.description ? `<div class="inventory-description">${CommonUtils.escapeHtml(inventory.description)}</div>` : ''}
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm remove-inventory"
                        data-inventory-id="${inventory.id}"
                        ${isFirst && this.selectedInventory.length === 1 ? 'style="display: none;"' : ''}>
                    🗑️
                </button>
            </div>
        `;
    }

    setupInventoryHandlers() {
        // Кнопка добавления инвентаря
        document.getElementById('addInventoryBtn').addEventListener('click', () => {
            this.addInventory();
        });

        // Добавление по Enter в селекте
        document.getElementById('inventorySelect').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.addInventory();
            }
        });
    }

    addInventory() {
        const select = document.getElementById('inventorySelect');
        const selectedId = parseInt(select.value);

        if (!selectedId) {
            this.showError('Пожалуйста, выберите инвентарь из списка');
            return;
        }

        // Находим выбранный инвентарь
        const selectedInventory = this.inventoryItems.find(item => item.id === selectedId);
        if (!selectedInventory) {
            this.showError('Выбранный инвентарь не найден');
            return;
        }

        // Проверяем, не добавлен ли уже этот инвентарь
        if (this.selectedInventory.some(item => item.id === selectedId)) {
            this.showError('Этот инвентарь уже добавлен');
            return;
        }

        // Добавляем в выбранные
        this.selectedInventory.push(selectedInventory);

        // Обновляем интерфейс
        this.updateAvailableInventory();
        this.renderSelectedInventory();

        // Сбрасываем выбор
        select.value = '';

        this.showSuccess(`Инвентарь "${selectedInventory.name}" добавлен`);
    }

    removeInventory(inventoryId) {
        // Удаляем из выбранных
        this.selectedInventory = this.selectedInventory.filter(item => item.id !== inventoryId);

        // Обновляем интерфейс
        this.updateAvailableInventory();
        this.renderSelectedInventory();

        this.showSuccess('Инвентарь удален');
    }

    createIngredientRow(value = '', isFirst = false) {
        const row = document.createElement('div');
        row.className = 'ingredient-row input-group mb-2';

        // Экранируем значение для безопасности
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
        document.getElementById('addIngredient').addEventListener('click', () => {
            this.addIngredientField();
        });

        document.getElementById('ingredientsContainer').addEventListener('click', (e) => {
            if (e.target.classList.contains('remove-ingredient')) {
                this.removeIngredientField(e.target);
            }
        });

        document.getElementById('ingredientsContainer').addEventListener('input', (e) => {
            if (e.target.classList.contains('ingredient-input')) {
                this.handleIngredientInput(e.target);
            }
        });
    }

    addIngredientField() {
        const container = document.getElementById('ingredientsContainer');
        container.appendChild(this.createIngredientRow());
        this.updateRemoveButtons();
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
        const inventoryRemoveButtons = document.querySelectorAll('.remove-inventory');

        // Обновляем кнопки удаления ингредиентов
        ingredientRemoveButtons.forEach(btn => {
            btn.style.display = rows.length > 1 ? 'block' : 'none';
        });

        // Обновляем кнопки удаления инвентаря (скрываем для последнего элемента если он один)
        if (inventoryRemoveButtons.length > 0) {
            inventoryRemoveButtons.forEach((btn, index) => {
                if (this.selectedInventory.length === 1) {
                    btn.style.display = 'none';
                } else {
                    btn.style.display = 'block';
                }
            });
        }
    }

    setupFormHandlers() {
        document.getElementById('saveDraftBtn').addEventListener('click', () => {
            this.saveRecipe(false);
        });

        document.getElementById('publishBtn').addEventListener('click', () => {
            this.saveRecipe(true);
        });
    }

    setupValidation() {
        // Базовая валидация при вводе
        const fields = ['title', 'category', 'description'];
        fields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (field) {
                field.addEventListener('blur', () => this.validateField(field));
            }
        });
    }

    validateField(field) {
        if (field.tagName === 'SELECT') {
            if (!field.value) {
                field.classList.add('is-invalid');
                return false;
            }
        } else if (!field.value.trim()) {
            field.classList.add('is-invalid');
            return false;
        }

        field.classList.remove('is-invalid');
        return true;
    }

    validateForm() {
        let isValid = true;

        // Валидация основных полей
        const title = document.getElementById('title');
        const category = document.getElementById('category');
        const description = document.getElementById('description');

        if (!this.validateField(title)) isValid = false;
        if (!this.validateField(category)) isValid = false;
        if (!this.validateField(description)) isValid = false;

        // Валидация ингредиентов
        const ingredients = this.getIngredients();
        if (!ingredients || ingredients.length === 0) {
            this.showError('Добавьте хотя бы один ингредиент');
            isValid = false;
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

        const recipeData = {
            title: document.getElementById('title').value.trim(),
            categoryId: parseInt(document.getElementById('category').value),
            authorId: this.authorId,
            ingredients: this.getIngredients(),
            description: document.getElementById('description').value.trim(),
            inventoryIds: this.getSelectedInventory(),
            published: publish
        };

        console.log('Saving recipe data:', recipeData);

        try {
            let response;
            if (this.recipeId) {
                response = await this.put(`/${this.recipeId}`, recipeData);
            } else {
                response = await this.post('', recipeData);
            }

            if (response.success) {
                const message = this.recipeId ?
                    (publish ? 'Рецепт успешно обновлен и опубликован' : 'Рецепт успешно сохранен как черновик') :
                    (publish ? 'Рецепт успешно создан и опубликован' : 'Рецепт успешно сохранен как черновик');

                this.showSuccess(message);

                setTimeout(() => {
                    window.location.href = '/my-recipes';
                }, 2000);
            } else {
                this.showError(response.message || 'Ошибка при сохранении рецепта');
            }
        } catch (error) {
            console.error('Error saving recipe:', error);
            const errorMessage = CommonUtils.handleApiError(error, 'Не удалось сохранить рецепт');
            this.showError(errorMessage);
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
    new RecipeEditApp();
});