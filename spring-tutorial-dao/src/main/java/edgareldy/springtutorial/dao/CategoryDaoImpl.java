package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Category;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based implementation of CategoryDao.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public class CategoryDaoImpl implements CategoryDao {

    private final EntityManager entityManager;

    public CategoryDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Category.class, id));
    }

    @Override
    public List<Category> findAll(int page, int size) {
        return entityManager.createQuery("SELECT c FROM Category c ORDER BY c.id", Category.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            entityManager.persist(category);
            return category;
        }
        return entityManager.merge(category);
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }
}
