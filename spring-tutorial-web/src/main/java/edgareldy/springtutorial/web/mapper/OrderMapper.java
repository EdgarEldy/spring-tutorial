package edgareldy.springtutorial.web.mapper;

import edgareldy.springtutorial.domain.Order;
import edgareldy.springtutorial.web.dto.order.OrderRequest;
import edgareldy.springtutorial.web.dto.order.OrderResponse;

/**
 * Manual entity/DTO conversion for Order, no MapStruct in this project.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getQuantity(),
                order.getTotal(),
                CustomerMapper.toResponse(order.getCustomer()),
                ProductMapper.toResponse(order.getProduct()));
    }

    public static Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setQuantity(request.quantity());
        return order;
    }
}
