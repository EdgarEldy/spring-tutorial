package edgareldy.springtutorial.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.domain.Customer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for CustomerDaoImpl, run against an embedded H2 database with the
 * dao module's real Spring context (dev profile) rather than mocks.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceConfig.class)
@ActiveProfiles("dev")
@Transactional
class CustomerDaoTest {

    @Autowired
    private CustomerDao customerDao;

    @Test
    void savePersistsANewCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Grace");
        customer.setLastName("Hopper");
        customer.setTelephone("1111111111");
        customer.setEmail("grace@example.com");
        customer.setAddress("1 Compiler Lane");

        Customer saved = customerDao.save(customer);

        assertNotNull(saved.getId());
    }

    @Test
    void findByIdReturnsEmptyWhenCustomerDoesNotExist() {
        Optional<Customer> found = customerDao.findById(-1L);

        assertFalse(found.isPresent());
    }

    @Test
    void deleteByIdRemovesTheCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Temp");
        customer.setLastName("Customer");
        customer.setTelephone("2222222222");
        customer.setEmail("temp@example.com");
        customer.setAddress("Nowhere");
        Customer saved = customerDao.save(customer);

        customerDao.deleteById(saved.getId());

        assertFalse(customerDao.findById(saved.getId()).isPresent());
    }
}
