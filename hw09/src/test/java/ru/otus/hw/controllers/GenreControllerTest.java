package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = {GenreController.class})
public class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GenreService genreService;

    @MockBean
    private GenreRepository genreRepository;

    @Test
    @DisplayName("GET /genres - должен отобразить список жанров")
    void ShouldReturnAllGenres() throws Exception {
        List<GenreDto> genres = List.of(
                new GenreDto("1", "Genre 1"),
                new GenreDto("2", "Genre 2")
        );

        given(genreService.findAll()).willReturn(genres);

        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-list"))
                .andExpect(model().attribute("genres", hasSize(2)));

        verify(genreService, Mockito.times(1)).findAll();
    }
}