package edgareldy.springtutorial.common.exception;

/**
 * Thrown when an operation violates a business rule (e.g. deleting a category that still
 * contains products). Mapped to an HTTP 422 response by the web module's exception handler.
 * <p>
 * Created edgar.muhamyangabo on 7/5/26
 * Author : edgar.muhamyangabo
 * Date : 7/5/26
 * Project : spring-tutorial
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
