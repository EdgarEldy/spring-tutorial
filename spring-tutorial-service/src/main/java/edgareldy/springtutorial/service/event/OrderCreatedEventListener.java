package edgareldy.springtutorial.service.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to OrderCreatedEvent with a simple audit log line. Decoupled from
 * OrderServiceImpl: the service does not know this listener exists, and this listener
 * does not know how or why the order was created.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Component
public class OrderCreatedEventListener {

    private static final Log log = LogFactory.getLog(OrderCreatedEventListener.class);

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order " + event.orderId() + " created for customer " + event.customerId()
                + ": " + event.quantity() + " x product " + event.productId() + " = " + event.total());
    }
}
