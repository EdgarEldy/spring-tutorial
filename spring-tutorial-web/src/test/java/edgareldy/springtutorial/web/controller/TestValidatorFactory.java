package edgareldy.springtutorial.web.controller;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Builds a Validator for standalone MockMvc setups, mirroring
 * WebMvcConfig#getValidator(): the default message interpolator needs a jakarta.el
 * implementation this project does not depend on (HV000183), so ParameterMessageInterpolator
 * is set explicitly, otherwise @Valid on @RequestBody DTOs would fail with a 500 instead of
 * the expected 400 in every controller test below.
 * <p>
 * Created edgar.muhamyangabo on 7/12/26
 * Author : edgar.muhamyangabo
 * Date : 7/12/26
 * Project : spring-tutorial
 */
final class TestValidatorFactory {

    private TestValidatorFactory() {
    }

    static Validator create() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        return validator;
    }
}
