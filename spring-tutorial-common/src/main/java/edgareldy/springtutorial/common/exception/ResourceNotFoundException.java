package edgareldy.springtutorial.common.exception;

/**
 * Thrown when a requested resource (category, product, customer, order) does not exist.
 * Mapped to an HTTP 404 response by the web module's exception handler.
 * <p>
 * Created edgar.muhamyangabo on 7/5/26
 * Author : edgar.muhamyangabo
 * Date : 7/5/26
 * Project : spring-tutorial
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
