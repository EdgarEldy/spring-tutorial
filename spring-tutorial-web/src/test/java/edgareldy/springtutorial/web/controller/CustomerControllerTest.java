package edgareldy.springtutorial.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.service.CustomerService;
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
 * MockMvc tests for CustomerController, standalone setup with CustomerService mocked and
 * WebExceptionHandler registered as controller advice.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static Customer customerFixture() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setTelephone("0123456789");
        customer.setEmail("ada@example.com");
        customer.setAddress("1 Analytical Engine Way");
        return customer;
    }

    @BeforeEach
    void setUp() {
        CustomerController controller = new CustomerController(customerService);
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
        when(customerService.findAll(anyInt(), any())).thenReturn(List.of(customerFixture()));
        when(customerService.count()).thenReturn(1L);

        MvcResult result =
                mockMvc.perform(get("/api/v1/customers")).andExpect(status().isOk()).andReturn();

        assertEquals("Lovelace", bodyOf(result).at("/data/content/0/lastName").asText());
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        when(customerService.findById(99L)).thenThrow(new ResourceNotFoundException("Customer 99 not found"));

        mockMvc.perform(get("/api/v1/customers/{id}", 99L)).andExpect(status().isNotFound());
    }

    @Test
    void createDelegatesToTheService() throws Exception {
        when(customerService.create(any())).thenReturn(customerFixture());

        MvcResult result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CustomerPayload("Ada", "Lovelace", "0123456789", "ada@example.com", "1 Way"))))
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals("Ada", bodyOf(result).at("/data/firstName").asText());
    }

    @Test
    void createRejectsAnInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CustomerPayload("Ada", "Lovelace", "0123456789", "not-an-email", "1 Way"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDelegatesToTheService() throws Exception {
        when(customerService.update(eq(1L), any())).thenReturn(customerFixture());

        mockMvc.perform(put("/api/v1/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CustomerPayload("Ada", "Lovelace", "0123456789", "ada@example.com", "1 Way"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDelegatesToTheService() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{id}", 1L)).andExpect(status().isOk());

        verify(customerService).delete(1L);
    }

    private record CustomerPayload(
            String firstName, String lastName, String telephone, String email, String address) {
    }
}
