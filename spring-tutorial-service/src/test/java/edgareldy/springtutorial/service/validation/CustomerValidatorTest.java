package edgareldy.springtutorial.service.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edgareldy.springtutorial.domain.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * Unit test for CustomerValidator's cross-field rule.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
class CustomerValidatorTest {

    private final CustomerValidator validator = new CustomerValidator();

    @Test
    void rejectsWhenLastNameMatchesFirstNameIgnoringCase() {
        Customer customer = new Customer();
        customer.setFirstName("Grace");
        customer.setLastName("GRACE");
        Errors errors = new BeanPropertyBindingResult(customer, "customer");

        validator.validate(customer, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    void acceptsDistinctFirstAndLastNames() {
        Customer customer = new Customer();
        customer.setFirstName("Grace");
        customer.setLastName("Hopper");
        Errors errors = new BeanPropertyBindingResult(customer, "customer");

        validator.validate(customer, errors);

        assertFalse(errors.hasErrors());
    }
}
