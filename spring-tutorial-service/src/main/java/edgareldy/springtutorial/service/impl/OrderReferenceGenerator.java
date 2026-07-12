package edgareldy.springtutorial.service.impl;

import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Generates a human-facing order reference. Deliberately prototype-scoped (a fresh
 * instance per lookup, contrasted with the singleton services): each order creation asks
 * the container for a new one instead of reusing one long-lived instance across
 * concurrent requests. The constructor depends on Clock rather than calling
 * Clock.systemDefaultZone() itself, which is what makes it possible to inject a fixed
 * Clock in tests without touching the real system clock; @Qualifier disambiguates
 * between the two Clock beans declared in service-context.xml.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Component
@Scope("prototype")
public class OrderReferenceGenerator {

    private final Clock clock;

    public OrderReferenceGenerator(@Qualifier("systemClock") Clock clock) {
        this.clock = clock;
    }

    public String generate() {
        return "ORD-" + clock.millis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
