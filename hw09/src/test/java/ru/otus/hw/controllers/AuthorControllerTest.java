package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(value = {AuthorController.class})
public class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private AuthorRepository authorRepository;

    @Test
    @DisplayName("GET /authors - должен отобразить список авторов")
    void ShouldReturnAllAuthors() throws Exception {
        List<AuthorDto> authors = List.of(
                new AuthorDto("1", "Author 1"),
                new AuthorDto("2", "Author 2")
        );

        given(authorService.findAll()).willReturn(authors);

        mvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-list"))
                .andExpect(model().attribute("authors", hasSize(2)));

        verify(authorService, Mockito.times(1)).findAll();
    }
}