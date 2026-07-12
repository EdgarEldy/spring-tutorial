package edgareldy.springtutorial.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.ProductService;
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
 * MockMvc tests for ProductController, standalone setup with ProductService mocked and
 * WebExceptionHandler registered as controller advice.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static Product productFixture() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Books");

        Product product = new Product();
        product.setId(1L);
        product.setProductName("Clean Code");
        product.setUnitPrice(42.0);
        product.setCategory(category);
        return product;
    }

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
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
        when(productService.findAll(isNull(), anyInt(), any())).thenReturn(List.of(productFixture()));
        when(productService.count(isNull())).thenReturn(1L);

        MvcResult result =
                mockMvc.perform(get("/api/v1/products")).andExpect(status().isOk()).andReturn();

        JsonNode body = bodyOf(result);
        assertEquals("Clean Code", body.at("/data/content/0/productName").asText());
        assertEquals("Books", body.at("/data/content/0/category/categoryName").asText());
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product 99 not found"));

        mockMvc.perform(get("/api/v1/products/{id}", 99L)).andExpect(status().isNotFound());
    }

    @Test
    void createDelegatesToTheServiceWithTheResolvedCategoryId() throws Exception {
        when(productService.create(any(), eq(1L))).thenReturn(productFixture());

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductPayload("Clean Code", 42.0, 1L))))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals("Clean Code", bodyOf(result).at("/data/productName").asText());
    }

    @Test
    void createRejectsANonPositivePrice() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductPayload("Clean Code", -1.0, 1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDelegatesToTheService() throws Exception {
        when(productService.update(eq(1L), any(), eq(1L))).thenReturn(productFixture());

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductPayload("Clean Code", 45.0, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDelegatesToTheService() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 1L)).andExpect(status().isOk());

        verify(productService).delete(1L);
    }

    private record ProductPayload(String productName, Double unitPrice, Long categoryId) {
    }
}
