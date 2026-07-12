package edgareldy.springtutorial.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edgareldy.springtutorial.web.config.WebMvcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * End-to-end test starting the real WebMvcConfig context (dev profile, H2, the
 * real service-context.xml and PersistenceConfig assembled together) behind MockMvc,
 * unlike the four *ControllerTest classes which mock the service layer entirely. This
 * is the only test in the web module that actually proves two things a mocked
 * controller test cannot: that the context refreshes successfully once a profile is
 * active (see WebAppInitializer), and that GET /api/v1/orders does not throw
 * LazyInitializationException now that OrderDaoImpl fetches product.category eagerly.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@SpringJUnitWebConfig(classes = WebMvcConfig.class)
@ActiveProfiles("dev")
@Transactional
class WebIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Long createCategory(String name) throws Exception {
        String body = objectMapper.writeValueAsString(new CategoryPayload(name));
        String response = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    private Long createProduct(String name, double price, Long categoryId) throws Exception {
        String body = objectMapper.writeValueAsString(new ProductPayload(name, price, categoryId));
        String response = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    private Long createCustomer() throws Exception {
        String body = objectMapper.writeValueAsString(
                new CustomerPayload("Ada", "Lovelace", "0123456789", "ada@example.com", "1 Analytical Engine Way"));
        String response = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).at("/data/id").asLong();
    }

    @Test
    void listingAndReadingOrdersResolvesTheNestedCategoryWithoutError() throws Exception {
        Long categoryId = createCategory("Books");
        Long productId = createProduct("Clean Code", 42.0, categoryId);
        Long customerId = createCustomer();

        String orderBody = objectMapper.writeValueAsString(new OrderPayload(2, customerId, productId));
        String createResponse = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = objectMapper.readTree(createResponse);
        assertEquals("Books", created.at("/data/product/category/categoryName").asText());
        long orderId = created.at("/data/id").asLong();

        String listResponse = mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(
                "Books",
                objectMapper
                        .readTree(listResponse)
                        .at("/data/content/0/product/category/categoryName")
                        .asText());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)).andExpect(status().isOk());
    }

    @Test
    void validationMessageIsResolvedThroughTheRealMessageSourcePerLocale() throws Exception {
        String invalidBody = objectMapper.writeValueAsString(new CategoryPayload(""));

        String englishMessage = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String frenchMessage = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "fr")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String englishText = objectMapper.readTree(englishMessage).at("/message").asText();
        String frenchText = objectMapper.readTree(frenchMessage).at("/message").asText();
        assertEquals("Validation failed", englishText);
        assertEquals("Échec de la validation", frenchText);
        assertNotEquals(englishText, frenchText);
    }

    private record CategoryPayload(String categoryName) {
    }

    private record ProductPayload(String productName, Double unitPrice, Long categoryId) {
    }

    private record CustomerPayload(
            String firstName, String lastName, String telephone, String email, String address) {
    }

    private record OrderPayload(Integer quantity, Long customerId, Long productId) {
    }
}
