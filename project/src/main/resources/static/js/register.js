class RegistrationForm {
    constructor() {
        this.form = document.getElementById('registerForm');
        this.username = document.getElementById('username');
        this.email = document.getElementById('email');
        this.bio = document.getElementById('bio');
        this.password = document.getElementById('password');
        this.confirmPassword = document.getElementById('confirmPassword');
        this.passwordMatch = document.getElementById('passwordMatch');
        this.passwordStrength = document.getElementById('passwordStrength');
        this.bioCounter = document.getElementById('bioCounter');
        this.registerButton = document.getElementById('registerButton');

        // Элементы для статусов
        this.usernameStatus = document.getElementById('usernameStatus');
        this.usernameError = document.getElementById('usernameError');
        this.emailStatus = document.getElementById('emailStatus');
        this.emailError = document.getElementById('emailError');

        this.usernameValid = false;
        this.emailValid = false;

        this.init();
    }

    init() {
        if (!this.form) {
            console.error('Registration form not found');
            return;
        }

        this.setupEventListeners();
        this.initializeBioCounter();
    }

    setupEventListeners() {
        // Счетчик символов для биографии
        if (this.bio) {
            this.bio.addEventListener('input', () => this.updateBioCounter());
        }

        // Валидация пароля
        if (this.password) {
            this.password.addEventListener('input', () => {
                this.checkPasswordStrength(this.password.value);
                this.checkPasswordMatch();
            });
        }

        if (this.confirmPassword) {
            this.confirmPassword.addEventListener('input', () => this.checkPasswordMatch());
        }

        // Проверка уникальности имени пользователя
        if (this.username) {
            this.username.addEventListener('blur', () => {
                const usernameValue = this.username.value.trim();
                if (usernameValue.length >= 3) {
                    this.checkUsernameExists(usernameValue);
                }
            });

            this.username.addEventListener('input', () => {
                this.clearFieldStatus(this.usernameStatus, this.usernameError);
                if (this.username.value.length >= 3) {
                    this.username.classList.remove('is-invalid');
                    this.username.classList.add('is-valid');
                } else {
                    this.username.classList.remove('is-valid');
                    this.username.classList.remove('is-invalid');
                }
            });
        }

        // Проверка уникальности email
        if (this.email) {
            this.email.addEventListener('blur', () => {
                const emailValue = this.email.value.trim();
                if (emailValue.includes('@')) {
                    this.checkEmailExists(emailValue);
                }
            });

            this.email.addEventListener('input', () => {
                this.clearFieldStatus(this.emailStatus, this.emailError);
                if (this.email.value.includes('@')) {
                    this.email.classList.remove('is-invalid');
                    this.email.classList.add('is-valid');
                } else {
                    this.email.classList.remove('is-valid');
                    this.email.classList.remove('is-invalid');
                }
            });
        }

        // Обработка формы
        if (this.form) {
            this.form.addEventListener('submit', (e) => this.handleSubmit(e));
        }
    }

    initializeBioCounter() {
        if (this.bio && this.bioCounter) {
            this.bioCounter.textContent = this.bio.value.length;
        }
    }

    updateBioCounter() {
        const count = this.bio.value.length;
        this.bioCounter.textContent = count;

        if (count > 500) {
            this.bioCounter.classList.add('text-danger');
            this.bio.classList.add('is-invalid');
        } else {
            this.bioCounter.classList.remove('text-danger');
            this.bio.classList.remove('is-invalid');
        }
    }

    checkPasswordStrength(password) {
        let strength = 0;
        if (password.length >= 6) strength++;
        if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
        if (password.match(/\d/)) strength++;
        if (password.match(/[^a-zA-Z\d]/)) strength++;

        this.passwordStrength.className = 'password-strength';
        if (password.length > 0) {
            if (strength < 2) {
                this.passwordStrength.classList.add('strength-weak');
            } else if (strength < 4) {
                this.passwordStrength.classList.add('strength-medium');
            } else {
                this.passwordStrength.classList.add('strength-strong');
            }
        }
    }

    checkPasswordMatch() {
        if (this.password.value && this.confirmPassword.value) {
            if (this.password.value === this.confirmPassword.value) {
                this.passwordMatch.textContent = '✓ Пароли совпадают';
                this.passwordMatch.className = 'form-text text-success';
                this.confirmPassword.classList.remove('is-invalid');
                this.confirmPassword.classList.add('is-valid');
            } else {
                this.passwordMatch.textContent = '✗ Пароли не совпадают';
                this.passwordMatch.className = 'form-text text-danger';
                this.confirmPassword.classList.remove('is-valid');
                this.confirmPassword.classList.add('is-invalid');
            }
        } else {
            this.passwordMatch.textContent = '';
        }
    }

    async checkUsernameExists(username) {
        try {
            const response = await fetch(`/api/users/exists/username/${encodeURIComponent(username)}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();

            if (data.success) {
                if (data.data) {
                    this.showFieldError(this.usernameError, 'Пользователь с таким именем уже существует');
                    this.usernameValid = false;
                } else {
                    this.showFieldSuccess(this.usernameStatus, 'Имя пользователя доступно');
                    this.usernameValid = true;
                }
            }
        } catch (error) {
            console.error('Error checking username:', error);
            this.usernameValid = false;
        }
    }

    async checkEmailExists(email) {
        try {
            const response = await fetch(`/api/users/exists/email/${encodeURIComponent(email)}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();

            if (data.success) {
                if (data.data) {
                    this.showFieldError(this.emailError, 'Пользователь с таким email уже существует');
                    this.emailValid = false;
                } else {
                    this.showFieldSuccess(this.emailStatus, 'Email доступен');
                    this.emailValid = true;
                }
            }
        } catch (error) {
            console.error('Error checking email:', error);
            this.emailValid = false;
        }
    }

    showFieldError(errorElement, message) {
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }

    showFieldSuccess(statusElement, message) {
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.style.display = 'block';
        }
    }

    clearFieldStatus(statusElement, errorElement) {
        if (statusElement) statusElement.style.display = 'none';
        if (errorElement) errorElement.style.display = 'none';
    }

    async handleSubmit(e) {
        e.preventDefault();

        // Базовая проверка паролей на клиенте
        if (this.password.value !== this.confirmPassword.value) {
            this.showToast('Ошибка', 'Пароли не совпадают', 'error');
            return;
        }

        // Проверка длины биографии на клиенте
        if (this.bio.value.length > 500) {
            this.showToast('Ошибка', 'Биография не должна превышать 500 символов', 'error');
            return;
        }

        const formData = {
            username: this.username.value,
            email: this.email.value,
            password: this.password.value,
            bio: this.bio.value || ''
        };

        try {
            this.registerButton.disabled = true;
            this.registerButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Регистрация...';

            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                this.showToast('Успех', 'Регистрация прошла успешно! Теперь вы можете войти.', 'success');
                setTimeout(() => {
                    window.location.href = '/login?registered=true';
                }, 2000);
            } else {
                // Отображаем ошибку с сервера
                this.showToast('Ошибка', result.message || 'Ошибка при регистрации', 'error');

                // Показываем ошибки для конкретных полей если они есть
                if (result.message && result.message.includes('имя пользователя')) {
                    this.showFieldError(this.usernameError, result.message);
                } else if (result.message && result.message.includes('email')) {
                    this.showFieldError(this.emailError, result.message);
                }
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.showToast('Ошибка', 'Ошибка при регистрации. Попробуйте позже.', 'error');
        } finally {
            this.registerButton.disabled = false;
            this.registerButton.innerHTML = '🚀 Зарегистрироваться';
        }
    }

    showToast(title, message, type) {
        const toastElement = document.getElementById('registerToast');
        const toastTitle = document.getElementById('toastTitle');
        const toastMessage = document.getElementById('toastMessage');

        if (!toastElement || !toastTitle || !toastMessage) {
            console.error('Toast elements not found');
            return;
        }

        toastTitle.textContent = title;
        toastMessage.textContent = message;

        const toastHeader = toastElement.querySelector('.toast-header');
        if (toastHeader) {
            toastHeader.className = 'toast-header';
            if (type === 'success') {
                toastHeader.classList.add('bg-success', 'text-white');
            } else if (type === 'error') {
                toastHeader.classList.add('bg-danger', 'text-white');
            } else if (type === 'warning') {
                toastHeader.classList.add('bg-warning', 'text-dark');
            }
        }

        const toast = new bootstrap.Toast(toastElement);
        toast.show();
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    new RegistrationForm();
});