package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.security.TestSecurityConfig;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@Import(TestSecurityConfig.class)
class AuthorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("Доступ к авторам для ADMIN - разрешен")
    @WithMockUser(roles = "ADMIN")
    void getAllAuthors_WithAdminRole_ShouldReturnOk() throws Exception {
        given(authorService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Доступ к авторам для USER - запрещен")
    @WithMockUser(roles = "USER")
    void getAllAuthors_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Доступ к авторам без аутентификации - запрещен")
    void getAllAuthors_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isForbidden());
    }
}
