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
}
