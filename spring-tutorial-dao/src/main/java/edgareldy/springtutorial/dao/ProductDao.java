package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Product;
import java.util.List;
import java.util.Optional;

/**
 * Contract for data access on Product. The Category relation is resolved through
 * explicit JPQL JOIN FETCH queries rather than automatic Spring Data fetching.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public interface ProductDao {

    Optional<Product> findById(Long id);

    List<Product> findAll(int page, int size);

    List<Product> findByCategoryId(Long categoryId, int page, int size);

    long countByCategoryId(Long categoryId);

    Product save(Product product);

    void deleteById(Long id);
}
