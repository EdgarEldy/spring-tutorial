package edgareldy.springtutorial.web.controller;

import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.service.OrderService;
import edgareldy.springtutorial.web.dto.common.ApiResponse;
import edgareldy.springtutorial.web.dto.common.PageResponse;
import edgareldy.springtutorial.web.dto.order.OrderRequest;
import edgareldy.springtutorial.web.dto.order.OrderResponse;
import edgareldy.springtutorial.web.mapper.OrderMapper;
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
 * REST endpoints for Order, delegating only to OrderService. total is never accepted
 * from the client: OrderRequest has no total field, so it is always the value computed
 * server-side by OrderServiceImpl from quantity and the resolved product's unitPrice.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> findAll(
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        List<OrderResponse> content = orderService.findAll(customerId, productId, page, size).stream()
                .map(OrderMapper::toResponse)
                .toList();
        return ApiResponse.success(PageResponse.of(content, page, size, orderService.count()), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findById(@PathVariable("id") Long id) {
        return ApiResponse.success(OrderMapper.toResponse(orderService.findById(id)), null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order created = orderService.create(OrderMapper.toEntity(request), request.customerId(), request.productId());
        return ApiResponse.success(OrderMapper.toResponse(created), "Order created");
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> update(@PathVariable("id") Long id, @Valid @RequestBody OrderRequest request) {
        Order updated =
                orderService.update(id, OrderMapper.toEntity(request), request.customerId(), request.productId());
        return ApiResponse.success(OrderMapper.toResponse(updated), "Order updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        orderService.delete(id);
        return ApiResponse.success(null, "Order deleted");
    }
}
