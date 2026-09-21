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
    void _01_ShouldReturnCategory_WhenCategoryExists() {
        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Electronics");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));

        Category found = categoryService.findById(1L);

        assertEquals("Electronics", found.getCategoryName());
    }

    @Test
    void _02_ShouldThrowNotFound_WhenCategoryDoesNotExist() {
        when(categoryDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(1L));
    }

    @Test
    void _03_ShouldRemoveCategory_WhenCategoryHasNoProducts() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.countByCategoryId(1L)).thenReturn(0L);

        categoryService.delete(1L);

        verify(categoryDao).deleteById(1L);
    }

    @Test
    void _04_ShouldThrowException_WhenCategoryStillHasProducts() {
        Category category = new Category();
        category.setId(1L);
        when(categoryDao.findById(1L)).thenReturn(Optional.of(category));
        when(productDao.countByCategoryId(1L)).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> categoryService.delete(1L));

        verify(categoryDao, never()).deleteById(any());
    }

    @Test
    void _05_ShouldUseDefaultPageSize_WhenNoneIsRequested() {
        categoryService.findAll(0, null);

        verify(categoryDao).findAll(0, 20);
    }

    @Test
    void _06_ShouldDelegateToDao_WhenCounting() {
        when(categoryDao.count()).thenReturn(5L);

        assertEquals(5L, categoryService.count());
    }

    @Test
    void _07_ShouldChangeCategoryName_WhenExistingCategoryIsUpdated() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setCategoryName("Old name");
        when(categoryDao.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryDao.save(existing)).thenReturn(existing);

        Category payload = new Category();
        payload.setCategoryName("New name");
        Category updated = categoryService.update(1L, payload);

        assertEquals("New name", updated.getCategoryName());
    }

    @Test
    void _08_ShouldThrowNotFound_WhenUpdatedCategoryDoesNotExist() {
        when(categoryDao.findById(1L)).thenReturn(Optional.empty());
        Category payload = new Category();
        payload.setCategoryName("New name");

        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(1L, payload));
    }
}
