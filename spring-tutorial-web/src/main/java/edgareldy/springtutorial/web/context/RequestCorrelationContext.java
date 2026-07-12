package edgareldy.springtutorial.web.context;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Holds a correlation id generated once per HTTP request. Injected into a singleton
 * controller through a scoped proxy (the default CGLIB proxy mode for @RequestScope on a
 * concrete class): the controller is created once at startup, but every method call on
 * the injected reference is transparently forwarded to the actual instance bound to the
 * request currently being handled.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Component
@RequestScope
public class RequestCorrelationContext {

    private final String correlationId = UUID.randomUUID().toString();

    public String getCorrelationId() {
        return correlationId;
    }
}
