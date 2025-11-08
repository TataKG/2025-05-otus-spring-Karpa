// recipe-edit.js - для страницы создания/редактирования рецептов
class RecipeEditApp extends BaseApiClient {
    constructor() {
        super('/api/recipes');
        this.recipeId = null;
        this.authorId = null;
        this.categories = [];
        this.inventoryItems = [];
        this.init();
    }

    async init() {
        // Показываем спиннер загрузки
        this.showLoadingState();

        try {
            await this.loadFormData();
            this.setupIngredientHandlers();
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
        const container = document.getElementById('inventoryContainer');
        container.innerHTML = '';

        if (!this.inventoryItems || this.inventoryItems.length === 0) {
            container.innerHTML = '<div class="col-12"><p class="text-muted">Инвентарь не найден</p></div>';
            return;
        }

        this.inventoryItems.forEach(inventory => {
            const isChecked = selectedInventory && selectedInventory.some(item => item.id === inventory.id);

            const col = document.createElement('div');
            col.className = 'col-md-6 mb-2';
            col.innerHTML = `
                <div class="form-check">
                    <input class="form-check-input inventory-checkbox"
                           type="checkbox"
                           value="${inventory.id}"
                           id="inventory_${inventory.id}"
                           ${isChecked ? 'checked' : ''}>
                    <label class="form-check-label" for="inventory_${inventory.id}">
                        <span>${CommonUtils.escapeHtml(inventory.name)}</span>
                        <small class="text-muted d-block">${CommonUtils.escapeHtml(inventory.description || '')}</small>
                    </label>
                </div>
            `;
            container.appendChild(col);
        });
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
                    style="${isFirst ? 'display: none;' : ''}">🗑️</button>
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
        const removeButtons = document.querySelectorAll('.remove-ingredient');

        removeButtons.forEach(btn => {
            btn.style.display = rows.length > 1 ? 'block' : 'none';
        });
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
        const checkboxes = document.querySelectorAll('.inventory-checkbox:checked');
        return Array.from(checkboxes).map(checkbox => parseInt(checkbox.value));
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