package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Order;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based implementation of OrderDao. Every query joins Customer, Product
 * and the Product's Category eagerly (JOIN FETCH): order listings always need all
 * three, and none of the finder methods below run inside a transaction (see
 * OrderServiceImpl), so a lazy Category proxy would otherwise throw
 * LazyInitializationException once the session used to load it is closed, in
 * particular when the web module's OrderMapper walks order.getProduct().getCategory().
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public class OrderDaoImpl implements OrderDao {

    private static final String BASE_QUERY =
            "SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.product p JOIN FETCH p.category";

    private final EntityManager entityManager;

    public OrderDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return entityManager
                .createQuery(BASE_QUERY + " WHERE o.id = :id", Order.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Order> findAll(int page, int size) {
        return entityManager
                .createQuery(BASE_QUERY + " ORDER BY o.id", Order.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<Order> findByCustomerId(Long customerId, int page, int size) {
        return entityManager
                .createQuery(BASE_QUERY + " WHERE o.customer.id = :customerId ORDER BY o.id", Order.class)
                .setParameter("customerId", customerId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<Order> findByProductId(Long productId, int page, int size) {
        return entityManager
                .createQuery(BASE_QUERY + " WHERE o.product.id = :productId ORDER BY o.id", Order.class)
                .setParameter("productId", productId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult();
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        }
        return entityManager.merge(order);
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }
}
