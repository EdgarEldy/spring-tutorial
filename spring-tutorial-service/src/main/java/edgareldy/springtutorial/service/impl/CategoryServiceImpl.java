package edgareldy.springtutorial.service.impl;

import edgareldy.springtutorial.common.exception.BusinessRuleException;
import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CategoryDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Default CategoryService implementation, backed by CategoryDao/ProductDao.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Service
@Validated
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final ProductDao productDao;
    private final PaginationDefaults paginationDefaults;

    public CategoryServiceImpl(CategoryDao categoryDao, ProductDao productDao, PaginationDefaults paginationDefaults) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
        this.paginationDefaults = paginationDefaults;
    }

    @Override
    public List<Category> findAll(int page, Integer size) {
        return categoryDao.findAll(page, paginationDefaults.pageSize(size));
    }

    @Override
    public long count() {
        return categoryDao.count();
    }

    @Override
    public Category findById(Long id) {
        return getOrThrow(id);
    }

    @Override
    @Transactional
    public Category create(@Valid Category category) {
        return categoryDao.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, @Valid Category category) {
        Category existing = getOrThrow(id);
        existing.setCategoryName(category.getCategoryName());
        return categoryDao.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        if (productDao.countByCategoryId(id) > 0) {
            throw new BusinessRuleException("Category " + id + " still has products and cannot be deleted");
        }
        categoryDao.deleteById(id);
    }

    private Category getOrThrow(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }
}
