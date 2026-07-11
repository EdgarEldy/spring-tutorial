package edgareldy.springtutorial.service.aspect;

import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Logs entry, arguments, execution time, and thrown exceptions for every public method
 * of service.impl, via a single @Around advice enabled by
 * &lt;aop:aspectj-autoproxy/&gt; in service-context.xml. Since every service is injected
 * by interface (CategoryService, ProductService, ...), Spring AOP wraps each one in a
 * JDK dynamic proxy rather than a CGLIB subclass to apply this advice.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Log log = LogFactory.getLog(LoggingAspect.class);

    @Around("execution(* edgareldy.springtutorial.service.impl..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        log.info("Entering " + signature + " with arguments " + Arrays.toString(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            log.info("Exiting " + signature + " in " + (System.currentTimeMillis() - start) + " ms");
            return result;
        } catch (Throwable ex) {
            log.warn("Exception in " + signature + " after " + (System.currentTimeMillis() - start)
                    + " ms: " + ex);
            throw ex;
        }
    }
}
