package edgareldy.springtutorial.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.domain.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for CategoryDaoImpl, run against an embedded H2 database with the
 * dao module's real Spring context (dev profile) rather than mocks.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PersistenceConfig.class)
@ActiveProfiles("dev")
@Transactional
class CategoryDaoTest {

    @Autowired
    private CategoryDao categoryDao;

    @Test
    void savePersistsANewCategory() {
        Category category = new Category();
        category.setCategoryName("Electronics");

        Category saved = categoryDao.save(category);

        assertNotNull(saved.getId());
    }

    @Test
    void findByIdReturnsEmptyWhenCategoryDoesNotExist() {
        Optional<Category> found = categoryDao.findById(-1L);

        assertFalse(found.isPresent());
    }

    @Test
    void findAllReturnsSavedCategories() {
        Category category = new Category();
        category.setCategoryName("Books");
        categoryDao.save(category);

        List<Category> categories = categoryDao.findAll(0, 10);

        assertTrue(categories.stream().anyMatch(c -> "Books".equals(c.getCategoryName())));
    }

    @Test
    void countReflectsSavedCategories() {
        long before = categoryDao.count();
        Category category = new Category();
        category.setCategoryName("Counted");
        categoryDao.save(category);

        long after = categoryDao.count();

        assertTrue(after == before + 1);
    }

    @Test
    void deleteByIdRemovesTheCategory() {
        Category category = new Category();
        category.setCategoryName("Temporary");
        Category saved = categoryDao.save(category);

        categoryDao.deleteById(saved.getId());

        assertFalse(categoryDao.findById(saved.getId()).isPresent());
    }
}
