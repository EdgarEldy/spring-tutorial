package edgareldy.springtutorial.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edgareldy.springtutorial.common.exception.BusinessRuleException;
import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CategoryDao;
import edgareldy.springtutorial.dao.ProductDao;
import edgareldy.springtutorial.domain.Category;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for CategoryServiceImpl, with CategoryDao/ProductDao mocked: no Spring
 * context, no database.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private ProductDao productDao;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        PaginationDefaults paginationDefaults = new PaginationDefaults();
        ReflectionTestUtils.setField(paginationDefaults, "defaultPageSize", 20);
        categoryService = new CategoryServiceImpl(categoryDao, productDao, paginationDefaults);
    }

    @Test
    void findByIdReturnsTheCategoryWhenItExists() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Electronics");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));

        Category found = categoryService.findById(1L);

        assertEquals("Electronics", found.getCategoryName());
    }

    @Test
    void findByIdThrowsWhenTheCategoryDoesNotExist() {
        when(categoryDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(1L));
    }

    @Test
    void deleteRemovesTheCategoryWhenItHasNoProducts() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.countByCategoryId(1L)).thenReturn(0L);

        categoryService.delete(1L);

        verify(categoryDao).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenTheCategoryStillHasProducts() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.countByCategoryId(1L)).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> categoryService.delete(1L));

        verify(categoryDao, never()).deleteById(any());
    }

    @Test
    void findAllUsesTheDefaultPageSizeWhenNoneIsRequested() {
        categoryService.findAll(0, null);

        verify(categoryDao).findAll(0, 20);
    }
}
