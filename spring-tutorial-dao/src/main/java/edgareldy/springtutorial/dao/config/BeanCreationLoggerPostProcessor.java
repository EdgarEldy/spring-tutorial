package edgareldy.springtutorial.dao.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Logs the name of every bean instantiated in this context, to make the
 * BeanPostProcessor extension point visible: with Spring Boot this machinery runs
 * silently, here it is registered explicitly and its effect is observable in the logs.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-tutorial
 */
public class BeanCreationLoggerPostProcessor implements BeanPostProcessor {

    private static final Log log = LogFactory.getLog(BeanCreationLoggerPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        log.info("Bean ready: " + beanName + " (" + bean.getClass().getSimpleName() + ")");
        return bean;
    }
}
