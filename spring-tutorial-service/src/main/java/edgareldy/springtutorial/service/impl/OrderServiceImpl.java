package edgareldy.springtutorial.service.impl;

import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CustomerDao;
import edgareldy.springtutorial.dao.OrderDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.OrderService;
import edgareldy.springtutorial.service.event.OrderCreatedEvent;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Default OrderService implementation, backed by OrderDao/CustomerDao/ProductDao.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Service
@Validated
public class OrderServiceImpl implements OrderService {

    private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

    private final OrderDao orderDao;
    private final CustomerDao customerDao;
    private final ProductDao productDao;
    private final PaginationDefaults paginationDefaults;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<OrderReferenceGenerator> orderReferenceGeneratorProvider;

    public OrderServiceImpl(
            OrderDao orderDao,
            CustomerDao customerDao,
            ProductDao productDao,
            PaginationDefaults paginationDefaults,
            ApplicationEventPublisher eventPublisher,
            ObjectProvider<OrderReferenceGenerator> orderReferenceGeneratorProvider) {
        this.orderDao = orderDao;
        this.customerDao = customerDao;
        this.productDao = productDao;
        this.paginationDefaults = paginationDefaults;
        this.eventPublisher = eventPublisher;
        this.orderReferenceGeneratorProvider = orderReferenceGeneratorProvider;
    }

    @Override
    public List<Order> findAll(Long customerId, Long productId, int page, Integer size) {
        int pageSize = paginationDefaults.pageSize(size);
        if (customerId != null) {
            return orderDao.findByCustomerId(customerId, page, pageSize);
        }
        if (productId != null) {
            return orderDao.findByProductId(productId, page, pageSize);
        }
        return orderDao.findAll(page, pageSize);
    }

    @Override
    public long count() {
        return orderDao.count();
    }

    @Override
    public Order findById(Long id) {
        return getOrThrow(id);
    }

    @Override
    @Transactional
    public Order create(@Valid Order order, Long customerId, Long productId) {
        Customer customer = getCustomerOrThrow(customerId);
        Product product = getProductOrThrow(productId);
        order.setCustomer(customer);
        order.setProduct(product);
        order.setTotal(order.getQuantity() * product.getUnitPrice());
        Order saved = orderDao.save(order);

        // A fresh OrderReferenceGenerator per call, since it is prototype-scoped: unlike
        // the singleton services here, ObjectProvider.getObject() asks the container for a
        // brand new instance every time instead of reusing one.
        String reference = orderReferenceGeneratorProvider.getObject().generate();
        log.info("Order " + saved.getId() + " reference: " + reference);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                saved.getId(), customer.getId(), product.getId(), saved.getQuantity(), saved.getTotal()));
        return saved;
    }

    @Override
    @Transactional
    public Order update(Long id, @Valid Order order, Long customerId, Long productId) {
        Order existing = getOrThrow(id);
        Customer customer = getCustomerOrThrow(customerId);
        Product product = getProductOrThrow(productId);
        existing.setCustomer(customer);
        existing.setProduct(product);
        existing.setQuantity(order.getQuantity());
        existing.setTotal(order.getQuantity() * product.getUnitPrice());
        return orderDao.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        orderDao.deleteById(id);
    }

    private Order getOrThrow(Long id) {
        return orderDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private Customer getCustomerOrThrow(Long customerId) {
        return customerDao.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));
    }

    private Product getProductOrThrow(Long productId) {
        return productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
    }
}
