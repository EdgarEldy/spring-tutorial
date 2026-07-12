package edgareldy.springtutorial.service.event;

/**
 * Published after an order is successfully persisted. A plain POJO record, not a
 * subclass of ApplicationEvent: Spring has published events published via
 * ApplicationEventPublisher.publishEvent(Object) since 4.2, so this event carries no
 * dependency on the Spring API itself.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record OrderCreatedEvent(Long orderId, Long customerId, Long productId, int quantity, double total) {
}
