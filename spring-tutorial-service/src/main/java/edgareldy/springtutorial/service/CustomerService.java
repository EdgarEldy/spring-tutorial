package edgareldy.springtutorial.service;

import edgareldy.springtutorial.domain.Customer;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Business contract for Customer. @Valid is declared here too, not only on the
 * implementation, since Bean Validation's method validation forbids an overriding
 * method from redefining the parameter constraints of the method it overrides
 * (HV000151).
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public interface CustomerService {

    List<Customer> findAll(int page, Integer size);

    long count();

    Customer findById(Long id);

    Customer create(@Valid Customer customer);

    Customer update(Long id, @Valid Customer customer);

    void delete(Long id);
}
