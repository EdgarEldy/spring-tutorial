package edgareldy.springtutorial.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edgareldy.springtutorial.dao.config.PersistenceConfig;
import edgareldy.springtutorial.domain.Category;
import edgareldy.springtutorial.domain.Product;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for ProductDaoImpl, verifying the explicit JOIN FETCH queries that
 * resolve the Product -> Category relation.
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
class ProductDaoTest {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private ProductDao productDao;

    private Category category;

    @BeforeEach
    void createCategory() {
        category = new Category();
        category.setCategoryName("Category for products");
        category = categoryDao.save(category);
    }

    @Test
    void saveAndFindByIdResolveTheCategoryRelation() {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Keyboard");
        product.setUnitPrice(49.90);

        Product saved = productDao.save(product);

        Product found = productDao.findById(saved.getId()).orElseThrow();
        assertEquals(category.getId(), found.getCategory().getId());
    }

    @Test
    void findByCategoryIdReturnsOnlyMatchingProducts() {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Mouse");
        product.setUnitPrice(19.90);
        productDao.save(product);

        List<Product> products = productDao.findByCategoryId(category.getId(), 0, 10);

        assertTrue(products.stream().allMatch(p -> p.getCategory().getId().equals(category.getId())));
    }

    @Test
    void countByCategoryIdCountsOnlyThatCategoryProducts() {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Monitor");
        product.setUnitPrice(199.90);
        productDao.save(product);

        long count = productDao.countByCategoryId(category.getId());

        assertTrue(count >= 1);
    }

    @Test
    void findAllReturnsSavedProducts() {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Webcam");
        product.setUnitPrice(29.90);
        productDao.save(product);

        List<Product> products = productDao.findAll(0, 10);

        assertTrue(products.stream().anyMatch(p -> "Webcam".equals(p.getProductName())));
    }

    @Test
    void countReflectsSavedProducts() {
        long before = productDao.count();
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Counted");
        product.setUnitPrice(1.0);
        productDao.save(product);

        long after = productDao.count();

        assertTrue(after == before + 1);
    }

    @Test
    void deleteByIdRemovesTheProduct() {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName("Temporary");
        product.setUnitPrice(9.90);
        Product saved = productDao.save(product);

        productDao.deleteById(saved.getId());

        assertFalse(productDao.findById(saved.getId()).isPresent());
    }
}
