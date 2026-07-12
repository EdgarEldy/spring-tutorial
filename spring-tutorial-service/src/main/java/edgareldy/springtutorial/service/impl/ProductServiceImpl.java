package edgareldy.springtutorial.service.impl;

import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CategoryDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.domain.Product;
import edgareldy.springtutorial.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Default ProductService implementation, backed by ProductDao/CategoryDao.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final PaginationDefaults paginationDefaults;

    public ProductServiceImpl(ProductDao productDao, CategoryDao categoryDao, PaginationDefaults paginationDefaults) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
        this.paginationDefaults = paginationDefaults;
    }

    @Override
    public List<Product> findAll(Long categoryId, int page, Integer size) {
        int pageSize = paginationDefaults.pageSize(size);
        return categoryId != null
                ? productDao.findByCategoryId(categoryId, page, pageSize)
                : productDao.findAll(page, pageSize);
    }

    @Override
    public long count(Long categoryId) {
        return categoryId != null ? productDao.countByCategoryId(categoryId) : productDao.count();
    }

    @Override
    public Product findById(Long id) {
        return getOrThrow(id);
    }

    @Override
    @Transactional
    public Product create(@Valid Product product, Long categoryId) {
        product.setCategory(getCategoryOrThrow(categoryId));
        return productDao.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, @Valid Product product, Long categoryId) {
        Product existing = getOrThrow(id);
        existing.setCategory(getCategoryOrThrow(categoryId));
        existing.setProductName(product.getProductName());
        existing.setUnitPrice(product.getUnitPrice());
        return productDao.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        productDao.deleteById(id);
    }

    private Product getOrThrow(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryDao.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    }
}
