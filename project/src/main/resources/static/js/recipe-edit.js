class RecipeEditApp {
    constructor() {
        this.baseUrl = '/api/recipes';
        this.init();
    }

    init() {
        this.setupIngredientHandlers();
        this.setupFormHandlers();
        this.setupValidation();
    }

    // Настройка обработчиков для ингредиентов
    setupIngredientHandlers() {
        // Добавление нового поля ингредиента
        document.getElementById('addIngredient').addEventListener('click', () => {
            this.addIngredientField();
        });

        // Обработчик для удаления ингредиентов
        document.getElementById('ingredientsContainer').addEventListener('click', (e) => {
            if (e.target.classList.contains('remove-ingredient')) {
                this.removeIngredientField(e.target);
            }
        });

        // Автоматическое добавление нового поля при заполнении последнего
        document.getElementById('ingredientsContainer').addEventListener('input', (e) => {
            if (e.target.classList.contains('ingredient-input')) {
                this.handleIngredientInput(e.target);
            }
        });
    }

    // Добавление поля ингредиента
    addIngredientField() {
        const container = document.getElementById('ingredientsContainer');
        const newRow = document.createElement('div');
        newRow.className = 'ingredient-row input-group';
        newRow.innerHTML = `
            <input type="text" class="form-control ingredient-input" placeholder="Например: Мука - 200г">
            <button type="button" class="btn btn-outline-danger remove-ingredient">🗑️</button>
        `;
        container.appendChild(newRow);
    }

    // Удаление поля ингредиента
    removeIngredientField(button) {
        const row = button.closest('.ingredient-row');
        if (row && document.querySelectorAll('.ingredient-row').length > 1) {
            row.remove();
        }
    }

    // Обработка ввода в поле ингредиента
    handleIngredientInput(input) {
        const rows = document.querySelectorAll('.ingredient-row');
        const lastRow = rows[rows.length - 1];
        const lastInput = lastRow.querySelector('.ingredient-input');

        // Если это последнее поле и оно заполнено, добавляем новое
        if (input === lastInput && input.value.trim() !== '') {
            this.addIngredientField();
        }
    }

    // Получение списка ингредиентов
    getIngredients() {
        const inputs = document.querySelectorAll('.ingredient-input');
        const ingredients = Array.from(inputs)
            .map(input => input.value.trim())
            .filter(ingredient => ingredient !== '');

        return ingredients.length > 0 ? ingredients : null;
    }

    // Получение выбранного инвентаря
    getSelectedInventory() {
        const checkboxes = document.querySelectorAll('.inventory-checkbox:checked');
        return Array.from(checkboxes).map(checkbox => parseInt(checkbox.value));
    }

    // Настройка обработчиков формы
    setupFormHandlers() {
        document.getElementById('saveDraftBtn').addEventListener('click', () => {
            this.saveRecipe(false); // false - черновик
        });

        document.getElementById('publishBtn').addEventListener('click', () => {
            this.saveRecipe(true); // true - опубликованный
        });
    }

    // Настройка валидации
    setupValidation() {
        const form = document.getElementById('recipeForm');

        form.addEventListener('submit', (e) => {
            e.preventDefault();
        });

        // Валидация при вводе
        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateField(input);
            });
        });
    }

    // Валидация поля
    validateField(field) {
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            return false;
        } else {
            field.classList.remove('is-invalid');
            return true;
        }
    }

    // Валидация всей формы
    validateForm() {
        const title = document.getElementById('title');
        const category = document.getElementById('category');
        const description = document.getElementById('description');
        const ingredients = this.getIngredients();

        let isValid = true;

        if (!this.validateField(title)) isValid = false;
        if (!this.validateField(category)) isValid = false;
        if (!this.validateField(description)) isValid = false;

        if (!ingredients || ingredients.length === 0) {
            this.showError('Добавьте хотя бы один ингредиент');
            isValid = false;
        }

        return isValid;
    }

    // Сохранение рецепта
    async saveRecipe(publish) {
        if (!this.validateForm()) {
            return;
        }

        const recipeId = document.getElementById('recipeId').value;
        const isEdit = recipeId !== 'null' && recipeId !== '';

        const recipeData = {
            title: document.getElementById('title').value.trim(),
            categoryId: parseInt(document.getElementById('category').value),
            authorId: parseInt(document.getElementById('authorId').value),
            ingredients: this.getIngredients(),
            description: document.getElementById('description').value.trim(),
            inventoryIds: this.getSelectedInventory(),
            published: publish
        };

        try {
            let response;
            if (isEdit) {
                // Редактирование существующего рецепта
                response = await fetch(`${this.baseUrl}/${recipeId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(recipeData)
                });
            } else {
                // Создание нового рецепта
                response = await fetch(this.baseUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(recipeData)
                });
            }

            const result = await response.json();

            if (result.success) {
                const message = isEdit ?
                    (publish ? 'Рецепт успешно обновлен и опубликован' : 'Рецепт успешно сохранен как черновик') :
                    (publish ? 'Рецепт успешно создан и опубликован' : 'Рецепт успешно сохранен как черновик');

                this.showSuccess(message);

                // Перенаправляем на страницу "Мои рецепты" через 2 секунды
                setTimeout(() => {
                    window.location.href = '/my-recipes';
                }, 2000);
            } else {
                this.showError(result.message || 'Произошла ошибка при сохранении рецепта');
            }
        } catch (error) {
            console.error('Error saving recipe:', error);
            this.showError('Не удалось сохранить рецепт: ' + error.message);
        }
    }

    // Показать успешное сообщение
    showSuccess(message) {
        this.showToast(message, 'success');
    }

    // Показать сообщение об ошибке
    showError(message) {
        this.showToast(message, 'error');
    }

    // Показать toast уведомление
    showToast(message, type = 'success') {
        const toastElement = type === 'success'
            ? document.getElementById('successToast')
            : document.getElementById('errorToast');

        const toastMessage = type === 'success'
            ? document.getElementById('successToastMessage')
            : document.getElementById('errorToastMessage');

        toastMessage.textContent = message;

        const toast = new bootstrap.Toast(toastElement);
        toast.show();
    }
}

// Инициализация приложения после загрузки DOM
document.addEventListener('DOMContentLoaded', () => {
    new RecipeEditApp();
});