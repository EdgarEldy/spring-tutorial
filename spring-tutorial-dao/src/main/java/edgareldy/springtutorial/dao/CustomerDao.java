package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Contract for data access on Customer.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public interface CustomerDao {

    Optional<Customer> findById(Long id);

    List<Customer> findAll(int page, int size);

    long count();

    Customer save(Customer customer);

    void deleteById(Long id);
}
