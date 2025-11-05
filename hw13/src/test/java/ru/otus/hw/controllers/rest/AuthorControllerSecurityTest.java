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
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.UserDetailService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@Import(SecurityConfig.class)
class AuthorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private UserDetailService userDetailService;

    @Test
    @DisplayName("Доступ к API авторов для ADMIN - разрешен")
    @WithMockUser(roles = "ADMIN")
    void getAllAuthors_WithAdminRole_ShouldReturnOk() throws Exception {
        given(authorService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ к API авторов для USER - разрешен")
    @WithMockUser(roles = "USER")
    void getAllAuthors_WithUserRole_ShouldReturnOk() throws Exception {
        given(authorService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ к API авторов без аутентификации - перенаправление на логин")
    void getAllAuthors_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @DisplayName("Доступ к Web интерфейсу авторов для USER - запрещен")
    @WithMockUser(roles = "USER")
    void getAuthorsPage_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isForbidden());
    }
}
