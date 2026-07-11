package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Order;
import java.util.List;
import java.util.Optional;

/**
 * Contract for data access on Order. Customer and Product are resolved through
 * explicit JPQL JOIN FETCH queries rather than automatic Spring Data fetching.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public interface OrderDao {

    Optional<Order> findById(Long id);

    List<Order> findAll(int page, int size);

    List<Order> findByCustomerId(Long customerId, int page, int size);

    List<Order> findByProductId(Long productId, int page, int size);

    long count();

    Order save(Order order);

    void deleteById(Long id);
}
