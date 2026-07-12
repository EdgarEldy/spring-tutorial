package edgareldy.springtutorial.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CategoryDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.domain.Product;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for ProductServiceImpl, with ProductDao/CategoryDao mocked.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private CategoryDao categoryDao;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        PaginationDefaults paginationDefaults = new PaginationDefaults();
        ReflectionTestUtils.setField(paginationDefaults, "defaultPageSize", 20);
        productService = new ProductServiceImpl(productDao, categoryDao, paginationDefaults);
    }

    @Test
    void createResolvesTheCategoryBeforeSaving() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        Product product = new Product();
        product.setProductName("Keyboard");
        product.setUnitPrice(49.90);
        when(productDao.save(product)).thenReturn(product);

        Product created = productService.create(product, 1L);

        assertEquals(category, created.getCategory());
    }

    @Test
    void createThrowsWhenTheCategoryDoesNotExist() {
        when(categoryDao.findById(1L)).thenReturn(Optional.empty());
        Product product = new Product();

        assertThrows(ResourceNotFoundException.class, () -> productService.create(product, 1L));
    }

    @Test
    void findAllWithoutCategoryIdDelegatesToFindAll() {
        productService.findAll(null, 0, 5);

        verify(productDao).findAll(0, 5);
    }

    @Test
    void findAllWithCategoryIdDelegatesToFindByCategoryId() {
        productService.findAll(1L, 0, 5);

        verify(productDao).findByCategoryId(1L, 0, 5);
    }

    @Test
    void countWithoutCategoryIdDelegatesToCount() {
        when(productDao.count()).thenReturn(7L);

        assertEquals(7L, productService.count(null));
    }

    @Test
    void countWithCategoryIdDelegatesToCountByCategoryId() {
        when(productDao.countByCategoryId(1L)).thenReturn(3L);

        assertEquals(3L, productService.count(1L));
    }

    @Test
    void updateResolvesTheCategoryAgainAndAppliesTheNewValues() {
        Product existing = new Product();
        existing.setId(1L);
        Category newCategory = new Category();
        newCategory.setId(2L);
        when(productDao.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryDao.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productDao.save(existing)).thenReturn(existing);

        Product payload = new Product();
        payload.setProductName("Mechanical keyboard");
        payload.setUnitPrice(79.90);
        Product updated = productService.update(1L, payload, 2L);

        assertEquals(newCategory, updated.getCategory());
        assertEquals("Mechanical keyboard", updated.getProductName());
        assertEquals(79.90, updated.getUnitPrice());
    }
}
