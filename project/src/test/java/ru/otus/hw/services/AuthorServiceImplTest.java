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
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Category;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для AuthorServiceImpl")
class AuthorServiceImplTest {

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
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        testUser.addRole("USER");

        testAuthor = new Author(testUser, "Test bio");
        testAuthor.setId(1L);

        testUserDto = new UserDto(1L, "testuser", "test@example.com", true,
                Set.of("USER"), LocalDateTime.now(), true);

        testAuthorDto = new AuthorDto(1L, testUserDto, "Test bio",
                LocalDateTime.now(), 5, List.of("USER"));
    }

    @Test
    @DisplayName("Создание автора для пользователя - успешное создание")
    void createAuthorForUser_ShouldCreateAuthor_WhenUserExistsAndAuthorNotExists() {
        // Given
        Long userId = 1L;
        String bio = "Test bio";

        when(authorRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.of(testUser));
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(authorRepository.findByIdWithUserAndRoles(anyLong())).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        AuthorDto result = authorService.createAuthorForUser(userId, bio);

        // Then
        assertNotNull(result);
        assertEquals(testAuthorDto, result);
        verify(authorRepository).save(any(Author.class));

        // Проверяем, что конкретные методы messageProvider не вызывались
        verify(messageProvider, never()).getMessage("author.default_bio");
        verify(messageProvider, never()).getMessage(eq("author.not_found"), anyLong());
        verify(messageProvider, never()).getMessage("user.not_found", userId);
        verify(messageProvider, never()).getMessage("author.already_exists");
    }

    @Test
    @DisplayName("Создание автора для пользователя - автор уже существует")
    void createAuthorForUser_ShouldThrowException_WhenAuthorAlreadyExists() {
        // Given
        Long userId = 1L;
        String bio = "Test bio";

        when(authorRepository.findByUserId(userId)).thenReturn(Optional.of(testAuthor));
        when(messageProvider.getMessage("author.already_exists")).thenReturn("Author already exists");

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> authorService.createAuthorForUser(userId, bio));
    }

    @Test
    @DisplayName("Создание автора с дефолтной биографией - успешно")
    void createAuthorForUser_ShouldUseDefaultBio_WhenBioIsNullOrEmpty() {
        // Given
        Long userId = 1L;
        String defaultBio = "Default bio";

        when(authorRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.of(testUser));
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(authorRepository.findByIdWithUserAndRoles(anyLong())).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);
        when(messageProvider.getMessage("author.default_bio")).thenReturn(defaultBio);

        // When
        AuthorDto result = authorService.createAuthorForUser(userId, null);

        // Then
        assertNotNull(result);
        assertEquals(testAuthorDto, result);
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    @DisplayName("Получение автора по ID - автор найден")
    void getAuthorById_ShouldReturnAuthor_WhenAuthorExists() {
        // Given
        Long authorId = 1L;
        when(authorRepository.findByIdWithUserAndRoles(authorId)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        Optional<AuthorDto> result = authorService.getAuthorById(authorId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthorDto, result.get());
    }

    @Test
    @DisplayName("Получение автора по ID - автор не найден")
    void getAuthorById_ShouldReturnEmpty_WhenAuthorNotExists() {
        // Given
        Long authorId = 1L;
        when(authorRepository.findByIdWithUserAndRoles(authorId)).thenReturn(Optional.empty());

        // When
        Optional<AuthorDto> result = authorService.getAuthorById(authorId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение автора по ID пользователя - автор найден")
    void getAuthorByUserId_ShouldReturnAuthor_WhenAuthorExists() {
        // Given
        Long userId = 1L;
        when(authorRepository.findByUserId(userId)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        Optional<AuthorDto> result = authorService.getAuthorByUserId(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthorDto, result.get());
    }

    @Test
    @DisplayName("Получение автора по имени пользователя - успешно")
    void getAuthorByUsername_ShouldReturnAuthor_WhenAuthorExists() {
        // Given
        String username = "testuser";
        when(authorRepository.findByUserUsername(username)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        Optional<AuthorDto> result = authorService.getAuthorByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testAuthorDto, result.get());
    }

    @Test
    @DisplayName("Получение автора по имени пользователя - автор не найден")
    void getAuthorByUsername_ShouldReturnEmpty_WhenAuthorNotExists() {
        // Given
        String username = "nonexistent";
        when(authorRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<AuthorDto> result = authorService.getAuthorByUsername(username);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение всех авторов - успешно")
    void getAllAuthors_ShouldReturnAllAuthors() {
        // Given
        List<Author> authors = List.of(testAuthor);
        when(authorRepository.findAllWithUser()).thenReturn(authors);
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        List<AuthorDto> result = authorService.getAllAuthors();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testAuthorDto, result.get(0));
    }

    @Test
    @DisplayName("Проверка существования автора по ID пользователя - существует")
    void existsByUserId_ShouldReturnTrue_WhenAuthorExists() {
        // Given
        Long userId = 1L;
        when(authorRepository.findByUserId(userId)).thenReturn(Optional.of(testAuthor));

        // When
        boolean result = authorService.existsByUserId(userId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Проверка существования автора по ID пользователя - не существует")
    void existsByUserId_ShouldReturnFalse_WhenAuthorNotExists() {
        // Given
        Long userId = 1L;
        when(authorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        boolean result = authorService.existsByUserId(userId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Получение автора с рецептами - успешно")
    void getAuthorWithRecipes_ShouldReturnAuthorWithRecipeCount() {
        // Given
        Long authorId = 1L;

        // Создаем автора с рецептами
        Recipe recipe1 = new Recipe("Recipe 1", new Category("Cat1", "Desc"), testAuthor, "Desc1");
        Recipe recipe2 = new Recipe("Recipe 2", new Category("Cat2", "Desc"), testAuthor, "Desc2");
        testAuthor.getRecipes().addAll(List.of(recipe1, recipe2));

        int actualRecipeCount = testAuthor.getRecipes().size(); // Будет 2
        AuthorDto authorWithRecipesDto = new AuthorDto(1L, testUserDto, "Test bio",
                LocalDateTime.now(), actualRecipeCount, List.of("USER"));

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor, actualRecipeCount)).thenReturn(authorWithRecipesDto);

        // When
        Optional<AuthorDto> result = authorService.getAuthorWithRecipes(authorId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(actualRecipeCount, result.get().recipeCount());
    }

    @Test
    @DisplayName("Конвертация пользователя в автора - успешно")
    void convertUserToAuthor_ShouldCallCreateAuthorForUser() {
        // Given
        Long userId = 1L;
        String bio = "Test bio";

        when(authorRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.of(testUser));
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(authorRepository.findByIdWithUserAndRoles(anyLong())).thenReturn(Optional.of(testAuthor));
        when(authorConverter.toDto(testAuthor)).thenReturn(testAuthorDto);

        // When
        AuthorDto result = authorService.convertUserToAuthor(userId, bio);

        // Then
        assertNotNull(result);
        assertEquals(testAuthorDto, result);
        verify(authorRepository).save(any(Author.class));

        // Проверяем, что messageProvider не вызывался в успешном сценарии
        verify(messageProvider, never()).getMessage(anyString());
        verify(messageProvider, never()).getMessage(anyString(), any());
    }
}