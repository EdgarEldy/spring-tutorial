package edgareldy.springtutorial.service;

import edgareldy.springtutorial.domain.Order;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Business contract for Order. @Valid is declared here too, not only on the
 * implementation, since Bean Validation's method validation forbids an overriding
 * method from redefining the parameter constraints of the method it overrides
 * (HV000151).
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public interface OrderService {

    List<Order> findAll(Long customerId, Long productId, int page, Integer size);

    long count();

    Order findById(Long id);

    Order create(@Valid Order order, Long customerId, Long productId);

    Order update(Long id, @Valid Order order, Long customerId, Long productId);

    void delete(Long id);
}
