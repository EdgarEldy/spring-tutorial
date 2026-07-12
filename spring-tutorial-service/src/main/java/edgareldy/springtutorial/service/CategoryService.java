package edgareldy.springtutorial.service;

import edgareldy.springtutorial.domain.Category;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Business contract for Category. Implementations manipulate domain entities directly:
 * this module has no notion of DTOs, which belong to the web module only. @Valid is
 * declared here too, not only on the implementation: Bean Validation's method
 * validation forbids an overriding method from redefining the parameter constraints of
 * the method it overrides (HV000151), so the interface and implementation must agree.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public interface CategoryService {

    List<Category> findAll(int page, Integer size);

    long count();

    Category findById(Long id);

    Category create(@Valid Category category);

    Category update(Long id, @Valid Category category);

    void delete(Long id);
}
