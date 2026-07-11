package edgareldy.springtutorial.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * Plain unit test, no Spring context at all: because OrderReferenceGenerator depends on
 * Clock through its constructor rather than calling Clock.systemDefaultZone() itself, a
 * fixed Clock can be passed in directly, exactly as service-context.xml's "fixedClock"
 * bean is meant to illustrate.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
class OrderReferenceGeneratorTest {

    @Test
    void generateUsesTheInjectedClockRatherThanTheSystemClock() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        OrderReferenceGenerator generator = new OrderReferenceGenerator(fixedClock);

        String reference = generator.generate();

        assertTrue(reference.startsWith("ORD-" + fixedClock.millis() + "-"));
    }
}
