package edgareldy.springtutorial.dao;

import edgareldy.springtutorial.domain.Customer;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * EntityManager-based implementation of CustomerDao.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public class CustomerDaoImpl implements CustomerDao {

    private final EntityManager entityManager;

    public CustomerDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id));
    }

    @Override
    public List<Customer> findAll(int page, int size) {
        return entityManager.createQuery("SELECT c FROM Customer c ORDER BY c.id", Customer.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            entityManager.persist(customer);
            return customer;
        }
        return entityManager.merge(customer);
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }
}
