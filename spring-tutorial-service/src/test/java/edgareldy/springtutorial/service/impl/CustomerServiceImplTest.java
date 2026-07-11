package edgareldy.springtutorial.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springtutorial.common.exception.BusinessRuleException;
import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CustomerDao;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.service.validation.CustomerValidator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for CustomerServiceImpl, with CustomerDao mocked. Uses a real
 * CustomerValidator, since it has no dependencies of its own and exercising the actual
 * cross-field rule is the point of these tests.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerDao customerDao;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        PaginationDefaults paginationDefaults = new PaginationDefaults();
        ReflectionTestUtils.setField(paginationDefaults, "defaultPageSize", 20);
        customerService = new CustomerServiceImpl(customerDao, paginationDefaults, new CustomerValidator());
    }

    private Customer validCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Grace");
        customer.setLastName("Hopper");
        customer.setTelephone("1111111111");
        customer.setEmail("grace@example.com");
        customer.setAddress("1 Compiler Lane");
        return customer;
    }

    @Test
    void createSavesAValidCustomer() {
        Customer customer = validCustomer();
        when(customerDao.save(customer)).thenReturn(customer);

        customerService.create(customer);

        verify(customerDao).save(customer);
    }

    @Test
    void createRejectsACustomerWhoseLastNameMatchesTheFirstName() {
        Customer customer = validCustomer();
        customer.setLastName("Grace");

        assertThrows(BusinessRuleException.class, () -> customerService.create(customer));

        verify(customerDao, never()).save(customer);
    }

    @Test
    void deleteThrowsWhenTheCustomerDoesNotExist() {
        when(customerDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(1L));
    }

    @Test
    void countDelegatesToTheDao() {
        when(customerDao.count()).thenReturn(4L);

        assertEquals(4L, customerService.count());
    }

    @Test
    void updateAppliesTheNewValuesToTheExistingCustomer() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setTelephone("0000000000");
        existing.setEmail("old@example.com");
        existing.setAddress("Old address");
        when(customerDao.findById(1L)).thenReturn(Optional.of(existing));
        when(customerDao.save(existing)).thenReturn(existing);

        Customer payload = validCustomer();
        Customer updated = customerService.update(1L, payload);

        assertEquals("Grace", updated.getFirstName());
        assertEquals("Hopper", updated.getLastName());
        assertEquals("grace@example.com", updated.getEmail());
    }

    @Test
    void updateRejectsACustomerWhoseLastNameMatchesTheFirstName() {
        Customer existing = new Customer();
        existing.setId(1L);
        when(customerDao.findById(1L)).thenReturn(Optional.of(existing));

        Customer payload = validCustomer();
        payload.setLastName(payload.getFirstName());

        assertThrows(BusinessRuleException.class, () -> customerService.update(1L, payload));

        verify(customerDao, never()).save(existing);
    }
}
