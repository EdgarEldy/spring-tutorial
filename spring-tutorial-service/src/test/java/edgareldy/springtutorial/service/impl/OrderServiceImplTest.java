package edgareldy.springtutorial.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springtutorial.dao.CustomerDao;
import edgareldy.springtutorial.dao.OrderDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.event.OrderCreatedEvent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for OrderServiceImpl, with every collaborator mocked, including
 * ApplicationEventPublisher and the ObjectProvider supplying OrderReferenceGenerator
 * instances.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private CustomerDao customerDao;

    @Mock
    private ProductDao productDao;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ObjectProvider<OrderReferenceGenerator> orderReferenceGeneratorProvider;

    @Mock
    private OrderReferenceGenerator orderReferenceGenerator;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        PaginationDefaults paginationDefaults = new PaginationDefaults();
        ReflectionTestUtils.setField(paginationDefaults, "defaultPageSize", 20);
        orderService = new OrderServiceImpl(
                orderDao, customerDao, productDao, paginationDefaults, eventPublisher, orderReferenceGeneratorProvider);
    }

    @Test
    void createComputesTotalAndPublishesAnEvent() {
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(2L);
        product.setUnitPrice(10.0);
        when(customerDao.findById(1L)).thenReturn(Optional.of(customer));
        when(productDao.findById(2L)).thenReturn(Optional.of(product));
        when(orderReferenceGeneratorProvider.getObject()).thenReturn(orderReferenceGenerator);
        when(orderReferenceGenerator.generate()).thenReturn("ORD-TEST");

        Order order = new Order();
        order.setQuantity(3);
        Order saved = new Order();
        saved.setId(9L);
        saved.setQuantity(3);
        saved.setTotal(30.0);
        when(orderDao.save(order)).thenReturn(saved);

        Order result = orderService.create(order, 1L, 2L);

        assertEquals(30.0, order.getTotal());
        assertEquals(saved, result);
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createAsksTheProviderForAFreshOrderReferenceGeneratorEachTime() {
        Customer customer = new Customer();
        customer.setId(1L);
        Product product = new Product();
        product.setId(2L);
        product.setUnitPrice(5.0);
        when(customerDao.findById(1L)).thenReturn(Optional.of(customer));
        when(productDao.findById(2L)).thenReturn(Optional.of(product));
        when(orderReferenceGeneratorProvider.getObject()).thenReturn(orderReferenceGenerator);
        when(orderReferenceGenerator.generate()).thenReturn("ORD-TEST");
        Order order = new Order();
        order.setQuantity(1);
        when(orderDao.save(order)).thenReturn(order);

        orderService.create(order, 1L, 2L);
        orderService.create(order, 1L, 2L);

        verify(orderReferenceGeneratorProvider, org.mockito.Mockito.times(2)).getObject();
    }
}
