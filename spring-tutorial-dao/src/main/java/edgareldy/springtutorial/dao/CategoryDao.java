package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Category;
import java.util.List;
import java.util.Optional;

/**
 * Contract for data access on Category, implemented with a manual EntityManager-based
 * DAO (no Spring Data JPA in this module).
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public interface CategoryDao {

    Optional<Category> findById(Long id);

    List<Category> findAll(int page, int size);

    Category save(Category category);

    void deleteById(Long id);
}
