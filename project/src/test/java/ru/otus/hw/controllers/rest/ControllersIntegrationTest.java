package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@Sql(scripts = "classpath:test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Интеграционный тест - получение автора по ID")
    @WithMockUser
    void getAuthorById_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/authors/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.bio").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - получение автора по ID пользователя")
    @WithMockUser
    void getAuthorByUserId_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/authors/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.user.id").value(1));
    }

    @Test
    @DisplayName("Интеграционный тест - получение всех авторов")
    @WithMockUser
    void getAllAuthors_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5));
    }

    @Test
    @DisplayName("Интеграционный тест - получение категории по ID")
    @WithMockUser
    void getCategoryById_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - получение категории по имени")
    @WithMockUser
    void getCategoryByName_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/categories/name/{name}", "Супы")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Супы"));
    }

    @Test
    @DisplayName("Интеграционный тест - получение всех категорий")
    @WithMockUser
    void getAllCategories_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(7));
    }

    @Test
    @DisplayName("Интеграционный тест - проверка существования категории")
    void checkCategoryExists_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/categories/exists/{name}", "Супы")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Интеграционный тест - получение инвентаря по ID")
    @WithMockUser
    void getInventoryById_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/inventories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - поиск инвентаря по имени")
    @WithMockUser
    void searchInventoryByName_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/inventories/search")
                        .param("name", "кастрюля")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Интеграционный тест - получение всего инвентаря")
    @WithMockUser
    void getAllInventory_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Интеграционный тест - получение рецепта по ID")
    void getRecipeById_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - получение всех рецептов")
    void getAllRecipes_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Интеграционный тест - получение количества комментариев для рецепта")
    void getCommentCount_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/{id}/comment-count", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("Интеграционный тест - поиск рецептов по названию")
    void searchRecipesByTitle_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/search/title")
                        .param("title", "борщ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Интеграционный тест - получение комментариев для рецепта")
    void getCommentsByRecipe_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/{recipeId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }


    @Test
    @DisplayName("Интеграционный тест - проверка существования username")
    void checkUsernameExists_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/users/exists/username/{username}", "test_user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isBoolean());
    }

    @Test
    @DisplayName("Интеграционный тест - проверка существования email")
    void checkEmailExists_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/users/exists/email/{email}", "user1@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isBoolean());
    }

    @Test
    @DisplayName("Интеграционный тест - получение пользователя по username")
    @WithMockUser
    void getUserByUsername_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", "chef_ivan")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("chef_ivan"));
    }

    @Test
    @DisplayName("Интеграционный тест - получение текущего пользователя (аутентифицированный)")
    @WithMockUser(username = "chef_ivan", roles = {"USER"})
    void getCurrentUser_Authenticated_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.name").value("chef_ivan"));
    }

    @Test
    @DisplayName("Интеграционный тест - автор не найден по несуществующему ID")
    @WithMockUser
    void getAuthorById_NotFound_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/authors/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - категория не найдена по несуществующему ID")
    @WithMockUser
    void getCategoryById_NotFound_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - рецепт не найден по несуществующему ID")
    void getRecipeById_NotFound_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Интеграционный тест - создание комментария")
    @WithMockUser(username = "chef_ivan")
    void createComment_IntegrationTest() throws Exception {
        String commentContent = "Тестовый комментарий";
        String requestBody = String.format("{\"content\": \"%s\"}", commentContent);

        mockMvc.perform(post("/api/recipes/{recipeId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value(commentContent))
                .andExpect(jsonPath("$.data.user.username").value("chef_ivan"));
    }

    @Test
    @DisplayName("Интеграционный тест - создание категории")
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_IntegrationTest() throws Exception {
        String requestBody = """
                {
                    "name": "Новая категория",
                    "description": "Описание новой категории"
                }
                """;

        mockMvc.perform(post("/api/admin/categories")  // Исправлен путь
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Новая категория"));
    }

    @Test
    @DisplayName("Интеграционный тест - создание инвентаря")
    @WithMockUser(roles = {"ADMIN"})
    void createInventory_IntegrationTest() throws Exception {
        String requestBody = """
            {
                "name": "Новый инвентарь",
                "description": "Описание нового инвентаря"
            }
            """;

        mockMvc.perform(post("/api/admin/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Новый инвентарь"));
    }


    @Test
    @DisplayName("Интеграционный тест - обновление комментария")
    @WithMockUser(username = "chef_ivan")
    void updateComment_IntegrationTest() throws Exception {
        String createRequestBody = "{\"content\": \"Комментарий для обновления\"}";

        var result = mockMvc.perform(post("/api/recipes/{recipeId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long commentId = extractIdFromResponse(response);

        String updateRequestBody = "{\"content\": \"Обновленный комментарий\"}";

        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", 1L, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Обновленный комментарий"));
    }

    @Test
    @DisplayName("Интеграционный тест - удаление комментария")
    @WithMockUser(username = "chef_ivan")
    void deleteComment_IntegrationTest() throws Exception {
        String createRequestBody = "{\"content\": \"Комментарий для удаления\"}";

        var result = mockMvc.perform(post("/api/recipes/{recipeId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long commentId = extractIdFromResponse(response);

        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", 1L, commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }


    private Long extractIdFromResponse(String response) {
        try {
            String idStr = response.split("\"id\":")[1].split(",")[0].trim();
            return Long.parseLong(idStr);
        } catch (Exception e) {
            return 1L;
        }
    }

    @Test
    @DisplayName("Интеграционный тест - доступ запрещен для не аутентифицированного пользователя")
    void accessDenied_ForUnauthenticated_IntegrationTest() throws Exception {
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Интеграционный тест - доступ запрещен для недостаточных прав")
    @WithMockUser(username = "food_lover", roles = {"USER"})
    void accessDenied_ForInsufficientPermissions_IntegrationTest() throws Exception {
        mockMvc.perform(delete("/api/recipes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Интеграционный тест - получение моих рецептов")
    @WithMockUser(username = "chef_ivan")
    void getMyRecipes_IntegrationTest() throws Exception {
        mockMvc.perform(get("/api/recipes/my-recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}