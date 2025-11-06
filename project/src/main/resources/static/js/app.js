class CookbookApp {
    constructor() {
        this.baseUrl = '/api';
        this.init();
    }

    init() {
        this.loadRecipes();
        this.setupEventListeners();
    }

    // Загрузка всех рецептов
    async loadRecipes() {
        try {
            this.showLoadingState();
            const response = await fetch(`${this.baseUrl}/recipes`);
            const result = await response.json();

            if (result.success) {
                this.displayRecipes(result.data);
            } else {
                this.showError('Ошибка при загрузке рецептов');
            }
        } catch (error) {
            console.error('Error loading recipes:', error);
            this.showError('Не удалось загрузить рецепты');
        }
    }

    // Показать состояние загрузки
    showLoadingState() {
        const tbody = document.getElementById('recipesTableBody');
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Загрузка...</span>
                    </div>
                    <p class="mt-2 text-muted">Загрузка рецептов...</p>
                </td>
            </tr>
        `;
    }

    // Отображение рецептов в таблице
    displayRecipes(recipes) {
        const tbody = document.getElementById('recipesTableBody');

        if (!recipes || recipes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-4 text-muted">
                        📝 Рецепты не найдены
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = recipes.map(recipe => `
            <tr>
                <td class="fw-bold text-primary">${this.escapeHtml(recipe.title)}</td>
                <td>
                    <span class="badge bg-secondary">${this.escapeHtml(recipe.categoryName)}</span>
                </td>
                <td class="text-muted">${this.escapeHtml(recipe.authorName)}</td>
                <td>
                    <span class="badge bg-info text-dark">
                        💬 ${recipe.commentCount}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary view-recipe"
                            data-recipe-id="${recipe.id}">
                        👁️ Детальнее
                    </button>
                </td>
            </tr>
        `).join('');

        this.addRecipeViewListeners();
    }

    // Загрузка деталей рецепта
    async loadRecipeDetails(recipeId) {
        try {
            const response = await fetch(`${this.baseUrl}/recipes/${recipeId}/detailed`);
            const result = await response.json();

            if (result.success) {
                this.showRecipeModal(result.data);
            } else {
                this.showError('Ошибка при загрузке деталей рецепта');
            }
        } catch (error) {
            console.error('Error loading recipe details:', error);
            this.showError('Не удалось загрузить детали рецепта');
        }
    }

    // Показ модального окна с деталями рецепта
    showRecipeModal(recipe) {
        const modalTitle = document.getElementById('recipeModalTitle');
        const modalBody = document.getElementById('recipeModalBody');

        modalTitle.textContent = `📖 ${recipe.title}`;

        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>📂 Категория:</h6>
                        <p><span class="badge bg-primary">${this.escapeHtml(recipe.category.name)}</span></p>
                    </div>

                    <div class="mb-3">
                        <h6>👨‍🍳 Автор:</h6>
                        <p class="text-muted">${this.escapeHtml(recipe.author.user.username)}</p>
                    </div>

                    <div class="mb-3">
                        <h6>🛒 Ингредиенты:</h6>
                        <div class="list-group">
                            ${recipe.ingredients.map(ingredient => `
                                <div class="list-group-item list-group-item-action">
                                    ${this.escapeHtml(ingredient)}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="mb-3">
                        <h6>📝 Описание:</h6>
                        <p class="text-muted border-start border-3 border-primary ps-3 py-2 bg-light">
                            ${this.escapeHtml(recipe.description)}
                        </p>
                    </div>

                    <div class="mb-3">
                        <h6>🔧 Необходимый инвентарь:</h6>
                        <div class="d-flex flex-wrap gap-2">
                            ${recipe.inventoryItems.map(item => `
                                <span class="badge bg-warning text-dark">
                                    🍴 ${this.escapeHtml(item.name)}
                                </span>
                            `).join('')}
                        </div>
                    </div>

                    <div class="mb-3">
                        <h6>💬 Комментарии:</h6>
                        <p>
                            <span class="badge bg-info text-dark">
                                💬 ${recipe.commentCount} комментариев
                            </span>
                        </p>
                    </div>
                </div>
            </div>
        `;

        const modal = new bootstrap.Modal(document.getElementById('recipeModal'));
        modal.show();
    }

    // Добавление обработчиков для кнопок просмотра
    addRecipeViewListeners() {
        document.querySelectorAll('.view-recipe').forEach(button => {
            button.addEventListener('click', (e) => {
                const recipeId = e.target.closest('.view-recipe').dataset.recipeId;
                this.loadRecipeDetails(recipeId);
            });
        });
    }

    // Настройка основных обработчиков событий
    setupEventListeners() {
        // Можно добавить обработчики для поиска и фильтрации
    }

    // Вспомогательная функция для экранирования HTML
    escapeHtml(unsafe) {
        if (unsafe === null || unsafe === undefined) return '';
        return unsafe
            .toString()
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // Показать ошибку
    showError(message) {
        const tbody = document.getElementById('recipesTableBody');
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4">
                    <div class="text-danger mb-2">❌</div>
                    <p class="text-muted">${message}</p>
                    <button class="btn btn-sm btn-outline-primary" onclick="app.loadRecipes()">
                        Повторить
                    </button>
                </td>
            </tr>
        `;
    }
}

// Глобальная переменная для доступа к приложению
let app;

// Инициализация приложения после загрузки DOM
document.addEventListener('DOMContentLoaded', () => {
    app = new CookbookApp();
});