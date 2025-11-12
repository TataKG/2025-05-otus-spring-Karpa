// language-switcher.js - единый обработчик смены языка для всех страниц
window.changeLanguage = function(lang) {
    try {
        console.log('🌐 Смена языка на:', lang);

        // Проверяем, находимся ли мы на странице с формой рецепта
        const isRecipeFormPage = window.location.pathname.includes('/recipe/create') ||
                                window.location.pathname.includes('/recipe/edit');

        if (isRecipeFormPage) {
            // Для страниц с формой рецепта сохраняем данные
            console.log('📝 Страница с формой - сохраняем данные перед сменой языка');
            const formData = window.collectFormData ? window.collectFormData() : {};
            localStorage.setItem('pendingFormData', JSON.stringify(formData));
            localStorage.setItem('pendingLanguageChange', 'true');
            console.log('💾 Данные формы сохранены:', formData);
        } else {
            // Для других страниц просто очищаем флаги
            localStorage.removeItem('pendingFormData');
            localStorage.removeItem('pendingLanguageChange');
        }

        // Сохраняем текущий язык
        localStorage.setItem('preferredLanguage', lang);

        // Устанавливаем язык через cookie
        document.cookie = `lang=${lang};path=/;max-age=31536000`;

        // Получаем текущий URL и добавляем/обновляем параметр lang
        const currentUrl = new URL(window.location.href);
        currentUrl.searchParams.set('lang', lang);
        currentUrl.searchParams.set('t', new Date().getTime());

        // Переходим по новому URL
        window.location.href = currentUrl.toString();

    } catch (error) {
        console.error('❌ Language change error:', error);
        // Fallback - обычная смена языка
        window.location.href = `?lang=${lang}`;
    }
};

// Восстановление предпочтительного языка при загрузке
document.addEventListener('DOMContentLoaded', function() {
    const savedLang = localStorage.getItem('preferredLanguage');
    const currentLang = new URLSearchParams(window.location.search).get('lang');

    if (savedLang && !currentLang) {
        // Если есть сохраненный язык и он не установлен в URL
        console.log('🌐 Восстанавливаем предпочтительный язык:', savedLang);
        document.cookie = `lang=${savedLang};path=/;max-age=31536000`;
    }
});

// Глобальное событие смены языка
window.dispatchLanguageChange = function() {
    window.dispatchEvent(new CustomEvent('languageChange'));
};