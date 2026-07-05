package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Product;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based implementation of ProductDao. Every query that returns a Product
 * joins its Category eagerly (JOIN FETCH) to avoid a separate lazy-loading round trip.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public class ProductDaoImpl implements ProductDao {

    private final EntityManager entityManager;

    public ProductDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return entityManager
                .createQuery("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id", Product.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Product> findAll(int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.id", Product.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId, int page, int size) {
        return entityManager
                .createQuery(
                        "SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId ORDER BY p.id",
                        Product.class)
                .setParameter("categoryId", categoryId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId", Long.class)
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        }
        return entityManager.merge(product);
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }
}
