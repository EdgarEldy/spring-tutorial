package edgareldy.springtutorial.service.validation;

import edgareldy.springtutorial.domain.Customer;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Cross-field rule that a single Bean Validation annotation cannot express, since it
 * compares two different fields of the same object rather than constraining one field in
 * isolation: a customer's first and last name must not be identical (a common
 * data-entry mistake, e.g. pasting the same value into both fields).
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Component
public class CustomerValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Customer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Customer customer = (Customer) target;
        if (customer.getFirstName() != null
                && customer.getFirstName().equalsIgnoreCase(customer.getLastName())) {
            errors.rejectValue("lastName", "customer.lastName.sameAsFirstName",
                    "Last name must not be identical to first name");
        }
    }
}
