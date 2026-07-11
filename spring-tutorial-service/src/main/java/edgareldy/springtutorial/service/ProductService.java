package edgareldy.springtutorial.service;

import edgareldy.springtutorial.domain.Product;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Business contract for Product. @Valid is declared here too, not only on the
 * implementation, since Bean Validation's method validation forbids an overriding
 * method from redefining the parameter constraints of the method it overrides
 * (HV000151).
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public interface ProductService {

    List<Product> findAll(Long categoryId, int page, Integer size);

    long count(Long categoryId);

    Product findById(Long id);

    Product create(@Valid Product product, Long categoryId);

    Product update(Long id, @Valid Product product, Long categoryId);

    void delete(Long id);
}
