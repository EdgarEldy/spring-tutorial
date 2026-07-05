package edgareldy.springtutorial.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.domain.Product;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for OrderDaoImpl, verifying the explicit JOIN FETCH queries that
 * resolve the Order -> Customer and Order -> Product relations.
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
class OrderDaoTest {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private OrderDao orderDao;

    private Customer customer;
    private Product product;

    @BeforeEach
    void createCustomerAndProduct() {
        Category category = new Category();
        category.setCategoryName("Category for orders");
        category = categoryDao.save(category);

        product = new Product();
        product.setCategory(category);
        product.setProductName("Desk");
        product.setUnitPrice(129.90);
        product = productDao.save(product);

        customer = new Customer();
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setTelephone("0000000000");
        customer.setEmail("ada@example.com");
        customer.setAddress("1 Analytical Engine Way");
        customer = customerDao.save(customer);
    }

    @Test
    void saveAndFindByIdResolveCustomerAndProduct() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(2);
        order.setTotal(product.getUnitPrice() * 2);

        Order saved = orderDao.save(order);

        Order found = orderDao.findById(saved.getId()).orElseThrow();
        assertEquals(customer.getId(), found.getCustomer().getId());
        assertEquals(product.getId(), found.getProduct().getId());
    }

    @Test
    void findByCustomerIdReturnsOnlyThatCustomerOrders() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(1);
        order.setTotal(product.getUnitPrice());
        orderDao.save(order);

        List<Order> orders = orderDao.findByCustomerId(customer.getId(), 0, 10);

        assertTrue(orders.stream().allMatch(o -> o.getCustomer().getId().equals(customer.getId())));
    }

    @Test
    void findByProductIdReturnsOnlyThatProductOrders() {
        Order order = new Order();
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(3);
        order.setTotal(product.getUnitPrice() * 3);
        orderDao.save(order);

        List<Order> orders = orderDao.findByProductId(product.getId(), 0, 10);

        assertTrue(orders.stream().allMatch(o -> o.getProduct().getId().equals(product.getId())));
    }
}
