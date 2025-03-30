package org.romanzhula.microservices_common.cors;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({CorsAutoConfiguration.class})
public @interface EnableCORS {
}
