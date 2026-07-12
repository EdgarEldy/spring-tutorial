package edgareldy.springtutorial.web.mapper;

import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.web.dto.customer.CustomerRequest;
import edgareldy.springtutorial.web.dto.customer.CustomerResponse;

/**
 * Manual entity/DTO conversion for Customer, no MapStruct in this project.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getTelephone(),
                customer.getEmail(),
                customer.getAddress());
    }

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setTelephone(request.telephone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        return customer;
    }
}
