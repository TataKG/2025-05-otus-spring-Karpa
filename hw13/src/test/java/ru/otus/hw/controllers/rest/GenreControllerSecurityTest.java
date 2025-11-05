package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.security.SecurityConfig;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@Import(SecurityConfig.class)
class GenreControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @MockBean
    private UserDetailService userDetailService;

    @Test
    @DisplayName("Доступ к жанрам для ADMIN - разрешен")
    @WithMockUser(roles = "ADMIN")
    void getAllGenres_WithAdminRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ к Web интерфейсу жанров для USER - запрещен")
    @WithMockUser(roles = "USER")
    void getGenresPage_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Доступ к жанрам без аутентификации - перенаправление на логин")
    void getAllGenres_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}
