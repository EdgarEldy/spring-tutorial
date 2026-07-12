package edgareldy.springtutorial.web.controller;

import edgareldy.springtutorial.domain.Customer;
import edgareldy.springtutorial.service.CustomerService;
import edgareldy.springtutorial.web.dto.common.ApiResponse;
import edgareldy.springtutorial.web.dto.common.PageResponse;
import edgareldy.springtutorial.web.dto.customer.CustomerRequest;
import edgareldy.springtutorial.web.dto.customer.CustomerResponse;
import edgareldy.springtutorial.web.mapper.CustomerMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Customer, delegating only to CustomerService.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> findAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        List<CustomerResponse> content =
                customerService.findAll(page, size).stream().map(CustomerMapper::toResponse).toList();
        return ApiResponse.success(PageResponse.of(content, page, size, customerService.count()), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> findById(@PathVariable("id") Long id) {
        return ApiResponse.success(CustomerMapper.toResponse(customerService.findById(id)), null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer created = customerService.create(CustomerMapper.toEntity(request));
        return ApiResponse.success(CustomerMapper.toResponse(created), "Customer created");
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(
            @PathVariable("id") Long id, @Valid @RequestBody CustomerRequest request) {
        Customer updated = customerService.update(id, CustomerMapper.toEntity(request));
        return ApiResponse.success(CustomerMapper.toResponse(updated), "Customer updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        customerService.delete(id);
        return ApiResponse.success(null, "Customer deleted");
    }
}
