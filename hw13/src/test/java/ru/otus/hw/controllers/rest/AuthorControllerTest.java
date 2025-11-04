package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

//    @Mock
//    private AuthorService authorService;
//
//    @InjectMocks
//    private AuthorController authorController;
//
//    @Test
//    void getAllAuthors_ShouldReturnListOfAuthors() {
//        // Given
//        List<AuthorDto> expectedAuthors = Arrays.asList(
//                new AuthorDto("1", "Author 1"),
//                new AuthorDto("2", "Author 2")
//        );
//        when(authorService.findAll()).thenReturn(expectedAuthors);
//
//        // When
//        List<AuthorDto> result = authorController.getAllAuthors();
//
//        // Then
//        assertEquals(2, result.size());
//        assertEquals(expectedAuthors, result);
//        verify(authorService, times(1)).findAll();
//    }
//
//    @Test
//    void getAuthorById_WithSpecialCharactersId_ShouldHandleCorrectly() {
//        // Given
//        String specialId = "author-123_abc";
//        AuthorDto expectedAuthor = new AuthorDto(specialId, "Special Author");
//        when(authorService.findById(specialId)).thenReturn(Optional.of(expectedAuthor));
//
//        // When
//        ResponseEntity<AuthorDto> response = authorController.getAuthorById(specialId);
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expectedAuthor, response.getBody());
//    }
//
//    @Test
//    void getAuthorById_WithValidId_ShouldReturnAuthor() {
//        // Given
//        String authorId = "1";
//        AuthorDto expectedAuthor = new AuthorDto(authorId, "Test Author");
//        when(authorService.findById(authorId)).thenReturn(Optional.of(expectedAuthor));
//
//        // When
//        ResponseEntity<AuthorDto> response = authorController.getAuthorById(authorId);
//
//        // Then
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(expectedAuthor, response.getBody());
//        verify(authorService, times(1)).findById(authorId);
//    }
}