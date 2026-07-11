package edgareldy.springtutorial.service.impl;

import edgareldy.springtutorial.common.exception.BusinessRuleException;
import edgareldy.springtutorial.common.exception.ResourceNotFoundException;
import edgareldy.springtutorial.dao.CustomerDao;
import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.service.CustomerService;
import edgareldy.springtutorial.service.validation.CustomerValidator;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

/**
 * Default CustomerService implementation, backed by CustomerDao.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Service
@Validated
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;
    private final PaginationDefaults paginationDefaults;
    private final CustomerValidator customerValidator;

    public CustomerServiceImpl(
            CustomerDao customerDao, PaginationDefaults paginationDefaults, CustomerValidator customerValidator) {
        this.customerDao = customerDao;
        this.paginationDefaults = paginationDefaults;
        this.customerValidator = customerValidator;
    }

    @Override
    public List<Customer> findAll(int page, Integer size) {
        return customerDao.findAll(page, paginationDefaults.pageSize(size));
    }

    @Override
    public long count() {
        return customerDao.count();
    }

    @Override
    public Customer findById(Long id) {
        return getOrThrow(id);
    }

    @Override
    @Transactional
    public Customer create(@Valid Customer customer) {
        validateCrossFieldRules(customer);
        return customerDao.save(customer);
    }

    @Override
    @Transactional
    public Customer update(Long id, @Valid Customer customer) {
        Customer existing = getOrThrow(id);
        validateCrossFieldRules(customer);
        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setTelephone(customer.getTelephone());
        existing.setEmail(customer.getEmail());
        existing.setAddress(customer.getAddress());
        return customerDao.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        customerDao.deleteById(id);
    }

    private void validateCrossFieldRules(Customer customer) {
        Errors errors = new BeanPropertyBindingResult(customer, "customer");
        customerValidator.validate(customer, errors);
        if (errors.hasErrors()) {
            throw new BusinessRuleException(errors.getFieldError().getDefaultMessage());
        }
    }

    private Customer getOrThrow(Long id) {
        return customerDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }
}
