package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Category;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для AuthorServiceImpl")
class AuthorServiceImplTest {

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long EXISTING_AUTHOR_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = 999L;
    private static final Long NON_EXISTING_AUTHOR_ID = 999L;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorConverter authorConverter;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private User testUser;
    private Author testAuthor;
    private UserDto testUserDto;
    private AuthorDto testAuthorDto;

    @BeforeEach
    void setUp() {
        testUser = new User("test_user", "test@example.com", "password");
        testUser.setId(EXISTING_USER_ID);
        testUser.addRole("USER");

        testAuthor = new Author(testUser, "Тестовая биография");
        testAuthor.setId(EXISTING_AUTHOR_ID);

        testUserDto = new UserDto(EXISTING_USER_ID, "test_user", "test@example.com", true,
                Set.of("USER"), LocalDateTime.now(), true);

        testAuthorDto = new AuthorDto(EXISTING_AUTHOR_ID, testUserDto, "Тестовая биография",
                LocalDateTime.now(), 5, List.of("USER"));
    }

    @Test
    @DisplayName("Создание автора с дефолтной биографией - успешно")
    void createAuthorForUser_ShouldUseDefaultBio_WhenBioIsNullOrEmpty() {
        // Arrange
        String defaultBio = "Биография по умолчанию";
        String userNotFoundMessage = "Пользователь не найден";

        when(authorRepository.findByUserId(EXISTING_USER_ID)).thenReturn(Optional.empty());

        when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(testUser));

        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

        when(authorConverter.toBasicDto(testAuthor)).thenReturn(testAuthorDto);

        when(messageProvider.getMessage("author.default_bio")).thenReturn(defaultBio);

        // Act
        AuthorDto result = authorService.createAuthorForUser(EXISTING_USER_ID, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testAuthorDto);
        verify(authorRepository).save(any(Author.class));
        verify(messageProvider).getMessage("author.default_bio");
    }

    @Test
    @DisplayName("Создание автора для пользователя - автор уже существует")
    void createAuthorForUser_ShouldThrowException_WhenAuthorAlreadyExists() {
        // Arrange
        String bio = "Тестовая биография";
        when(authorRepository.findByUserId(EXISTING_USER_ID)).thenReturn(Optional.of(testAuthor));
        when(messageProvider.getMessage("author.already_exists")).thenReturn("Автор уже существует");

        // Act & Assert
        assertThatThrownBy(() -> authorService.createAuthorForUser(EXISTING_USER_ID, bio))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Автор уже существует");
    }

    @Test
    @DisplayName("Получение автора по ID - автор найден")
    void getAuthorById_ShouldReturnAuthor_WhenAuthorExists() {
        // Arrange
        when(authorRepository.findByIdWithUserAndRoles(EXISTING_AUTHOR_ID)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // Act
        Optional<AuthorDto> result = authorService.getAuthorById(EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAuthorDto);
    }

    @Test
    @DisplayName("Получение автора по ID - автор не найден")
    void getAuthorById_ShouldReturnEmpty_WhenAuthorNotExists() {
        // Arrange
        when(authorRepository.findByIdWithUserAndRoles(NON_EXISTING_AUTHOR_ID)).thenReturn(Optional.empty());

        // Act
        Optional<AuthorDto> result = authorService.getAuthorById(NON_EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение автора по ID пользователя - автор найден")
    void getAuthorByUserId_ShouldReturnAuthor_WhenAuthorExists() {
        // Arrange
        when(authorRepository.findByUserId(EXISTING_USER_ID)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // Act
        Optional<AuthorDto> result = authorService.getAuthorByUserId(EXISTING_USER_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAuthorDto);
    }

    @Test
    @DisplayName("Получение автора по имени пользователя - успешно")
    void getAuthorByUsername_ShouldReturnAuthor_WhenAuthorExists() {
        // Arrange
        String username = "test_user";
        when(authorRepository.findByUserUsername(username)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // Act
        Optional<AuthorDto> result = authorService.getAuthorByUsername(username);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAuthorDto);
    }

    @Test
    @DisplayName("Получение автора по имени пользователя - автор не найден")
    void getAuthorByUsername_ShouldReturnEmpty_WhenAuthorNotExists() {
        // Arrange
        String username = "nonexistent";
        when(authorRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<AuthorDto> result = authorService.getAuthorByUsername(username);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение всех авторов - успешно")
    void getAllAuthors_ShouldReturnAllAuthors() {
        // Arrange
        List<Author> authors = List.of(testAuthor);
        when(authorRepository.findAllWithUser()).thenReturn(authors);
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // Act
        List<AuthorDto> result = authorService.getAllAuthors();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testAuthorDto);
    }

    @Test
    @DisplayName("Проверка существования автора по ID пользователя - существует")
    void existsByUserId_ShouldReturnTrue_WhenAuthorExists() {
        // Arrange
        when(authorRepository.findByUserId(EXISTING_USER_ID)).thenReturn(Optional.of(testAuthor));

        // Act
        boolean result = authorService.existsByUserId(EXISTING_USER_ID);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка существования автора по ID пользователя - не существует")
    void existsByUserId_ShouldReturnFalse_WhenAuthorNotExists() {
        // Arrange
        when(authorRepository.findByUserId(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = authorService.existsByUserId(NON_EXISTING_USER_ID);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Получение автора с рецептами - успешно")
    void getAuthorWithRecipes_ShouldReturnAuthorWithRecipeCount() {
        // Arrange
        // Создаем автора с рецептами
        Recipe recipe1 = new Recipe("Рецепт 1", new Category("Категория 1", "Описание"), testAuthor, "Описание 1");
        Recipe recipe2 = new Recipe("Рецепт 2", new Category("Категория 2", "Описание"), testAuthor, "Описание 2");
        testAuthor.getRecipes().addAll(List.of(recipe1, recipe2));

        int actualRecipeCount = testAuthor.getRecipes().size(); // Будет 2
        AuthorDto authorWithRecipesDto = new AuthorDto(EXISTING_AUTHOR_ID, testUserDto, "Тестовая биография",
                LocalDateTime.now(), actualRecipeCount, List.of("USER"));

        when(authorRepository.findById(EXISTING_AUTHOR_ID)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor, actualRecipeCount)).thenReturn(authorWithRecipesDto);

        // Act
        Optional<AuthorDto> result = authorService.getAuthorWithRecipes(EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().recipeCount()).isEqualTo(actualRecipeCount);
    }

    @Test
    @DisplayName("Конвертация пользователя в автора - успешно")
    void convertUserToAuthor_ShouldCallCreateAuthorForUser() {
        // Arrange
        String bio = "Тестовая биография";

        when(authorRepository.findByUserId(EXISTING_USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(testUser)); // ИСПРАВЛЕНО: убрали WithRolesAndAuthor
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(authorConverter.toBasicDto(testAuthor)).thenReturn(testAuthorDto); // ИСПРАВЛЕНО: используем toBasicDto

        // Act
        AuthorDto result = authorService.convertUserToAuthor(EXISTING_USER_ID, bio);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testAuthorDto);
        verify(authorRepository).save(any(Author.class));
        verify(messageProvider, never()).getMessage(anyString());
        verify(messageProvider, never()).getMessage(anyString(), any());
    }

    @Test
    @DisplayName("Создание автора - пользователь не найден")
    void createAuthorForUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String bio = "Тестовая биография";
        when(authorRepository.findByUserId(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty()); // ИСПРАВЛЕНО: убрали WithRolesAndAuthor
        when(messageProvider.getMessage("user.not_found", NON_EXISTING_USER_ID))
                .thenReturn("Пользователь не найден: " + NON_EXISTING_USER_ID);

        // Act & Assert
        assertThatThrownBy(() -> authorService.createAuthorForUser(NON_EXISTING_USER_ID, bio))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь не найден: " + NON_EXISTING_USER_ID);
    }

    @Test
    @DisplayName("Получение автора с рецептами - автор не найден")
    void getAuthorWithRecipes_ShouldReturnEmpty_WhenAuthorNotExists() {
        // Arrange
        when(authorRepository.findById(NON_EXISTING_AUTHOR_ID)).thenReturn(Optional.empty());

        // Act
        Optional<AuthorDto> result = authorService.getAuthorWithRecipes(NON_EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isEmpty();
    }
}