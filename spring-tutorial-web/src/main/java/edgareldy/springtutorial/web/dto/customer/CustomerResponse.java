package edgareldy.springtutorial.web.dto.customer;

/**
 * Customer representation exposed to API clients.
 * <p>
 * Created edgar.muhamyangabo on 7/11/26
 * Author : edgar.muhamyangabo
 * Date : 7/11/26
 * Project : spring-tutorial
 */
public record CustomerResponse(
        Long id, String firstName, String lastName, String telephone, String email, String address) {
}
