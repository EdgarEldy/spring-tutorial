package edgareldy.springtutorial.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.service.CategoryService;
import edgareldy.springtutorial.web.context.RequestCorrelationContext;
import edgareldy.springtutorial.web.exception.WebExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * MockMvc tests for CategoryController, standalone setup with CategoryService mocked and
 * WebExceptionHandler registered as controller advice, so the 404 mapping is exercised
 * too. Response bodies are parsed with Jackson and asserted with plain JUnit assertions,
 * since this project has no Hamcrest dependency.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService, new RequestCorrelationContext());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new WebExceptionHandler(new StaticMessageSource()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(TestValidatorFactory.create())
                .build();
    }

    private JsonNode bodyOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void findAllReturnsAPagedApiResponse() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Books");
        when(categoryService.findAll(anyInt(), any())).thenReturn(List.of(category));
        when(categoryService.count()).thenReturn(1L);

        MvcResult result =
                mockMvc.perform(get("/api/v1/categories")).andExpect(status().isOk()).andReturn();

        JsonNode body = bodyOf(result);
        assertTrue(body.get("success").asBoolean());
        assertEquals("Books", body.at("/data/content/0/categoryName").asText());
        assertEquals(1, body.at("/data/totalElements").asInt());
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        when(categoryService.findById(99L)).thenThrow(new ResourceNotFoundException("Category 99 not found"));

        MvcResult result = mockMvc.perform(get("/api/v1/categories/{id}", 99L))
                .andExpect(status().isNotFound())
                .andReturn();

        assertFalse(bodyOf(result).get("success").asBoolean());
    }

    @Test
    void createReturnsCreatedWithTheMappedResponse() throws Exception {
        Category created = new Category();
        created.setId(1L);
        created.setCategoryName("Books");
        when(categoryService.create(any())).thenReturn(created);

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryPayload("Books"))))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals("Books", bodyOf(result).at("/data/categoryName").asText());
    }

    @Test
    void createRejectsABlankCategoryName() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryPayload(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDelegatesToTheService() throws Exception {
        Category updated = new Category();
        updated.setId(1L);
        updated.setCategoryName("Renamed");
        when(categoryService.update(eq(1L), any())).thenReturn(updated);

        MvcResult result = mockMvc.perform(put("/api/v1/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryPayload("Renamed"))))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("Renamed", bodyOf(result).at("/data/categoryName").asText());
    }

    @Test
    void deleteDelegatesToTheService() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/{id}", 1L)).andExpect(status().isOk());

        verify(categoryService).delete(1L);
    }

    private record CategoryPayload(String categoryName) {
    }
}
