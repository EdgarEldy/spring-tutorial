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
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.OrderService;
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
 * MockMvc tests for OrderController, standalone setup with OrderService mocked and
 * WebExceptionHandler registered as controller advice.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static Order orderFixture() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Books");

        Product product = new Product();
        product.setId(1L);
        product.setProductName("Clean Code");
        product.setUnitPrice(20.0);
        product.setCategory(category);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setTelephone("0123456789");
        customer.setEmail("ada@example.com");
        customer.setAddress("1 Way");

        Order order = new Order();
        order.setId(1L);
        order.setQuantity(2);
        order.setTotal(40.0);
        order.setCustomer(customer);
        order.setProduct(product);
        return order;
    }

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService);
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
        when(orderService.findAll(isNull(), isNull(), anyInt(), any())).thenReturn(List.of(orderFixture()));
        when(orderService.count()).thenReturn(1L);

        MvcResult result =
                mockMvc.perform(get("/api/v1/orders")).andExpect(status().isOk()).andReturn();

        assertEquals(40.0, bodyOf(result).at("/data/content/0/total").asDouble());
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        when(orderService.findById(99L)).thenThrow(new ResourceNotFoundException("Order 99 not found"));

        mockMvc.perform(get("/api/v1/orders/{id}", 99L)).andExpect(status().isNotFound());
    }

    @Test
    void createDelegatesToTheServiceWithTheResolvedCustomerAndProductIds() throws Exception {
        when(orderService.create(any(), eq(1L), eq(1L))).thenReturn(orderFixture());

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderPayload(2, 1L, 1L))))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(40.0, bodyOf(result).at("/data/total").asDouble());
    }

    @Test
    void createRejectsANonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderPayload(0, 1L, 1L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDelegatesToTheService() throws Exception {
        when(orderService.update(eq(1L), any(), eq(1L), eq(1L))).thenReturn(orderFixture());

        mockMvc.perform(put("/api/v1/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderPayload(3, 1L, 1L))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDelegatesToTheService() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{id}", 1L)).andExpect(status().isOk());

        verify(orderService).delete(1L);
    }

    private record OrderPayload(Integer quantity, Long customerId, Long productId) {
    }
}
