package edgareldy.springtutorial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edgareldy.springtutorial.common.exception.BusinessRuleException;
import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end integration test for the service module: a two-level context hierarchy,
 * PersistenceConfig (Java Config) as the parent and service-context.xml (XML bean
 * definitions, component-scan, &lt;tx:annotation-driven/&gt;,
 * &lt;aop:aspectj-autoproxy/&gt;) as the child, mirroring the classic Spring MVC root
 * context/servlet context split the web module will use in production. The child
 * resolves the parent's DataSource/EntityManagerFactory/transactionManager beans, so
 * &lt;tx:annotation-driven/&gt; finds a transactionManager without either context needing
 * to redeclare it. This is the XML equivalent of feature/dao's
 * AnnotationConfigApplicationContext-based tests: SpringExtension loads both contexts
 * through the same mechanism a manually created ClassPathXmlApplicationContext would
 * use, just not instantiated by hand.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@ExtendWith(SpringExtension.class)
@ContextHierarchy({
    @ContextConfiguration(classes = PersistenceConfig.class),
    @ContextConfiguration(locations = "classpath:spring/service-context.xml")
})
@ActiveProfiles("dev")
@Transactional
class ServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Test
    void categoryWithProductsCannotBeDeleted() {
        Category category = new Category();
        category.setCategoryName("Electronics");
        category = categoryService.create(category);

        Product product = new Product();
        product.setProductName("Keyboard");
        product.setUnitPrice(49.90);
        productService.create(product, category.getId());

        Long categoryId = category.getId();
        assertThrows(BusinessRuleException.class, () -> categoryService.delete(categoryId));
    }

    @Test
    void creatingAnOrderResolvesCustomerAndProductAndComputesTotal() {
        Category category = new Category();
        category.setCategoryName("Furniture");
        category = categoryService.create(category);

        Product product = new Product();
        product.setProductName("Desk");
        product.setUnitPrice(129.90);
        product = productService.create(product, category.getId());

        Customer customer = new Customer();
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setTelephone("0000000000");
        customer.setEmail("ada@example.com");
        customer.setAddress("1 Analytical Engine Way");
        customer = customerService.create(customer);

        Order order = new Order();
        order.setQuantity(2);
        Order created = orderService.create(order, customer.getId(), product.getId());

        assertNotNull(created.getId());
        assertEquals(259.80, created.getTotal(), 0.001);
    }
}
