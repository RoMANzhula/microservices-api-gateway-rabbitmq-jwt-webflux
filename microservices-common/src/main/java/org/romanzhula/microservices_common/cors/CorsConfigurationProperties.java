package org.romanzhula.microservices_common.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "spring-hoc.cors")
public class CorsConfigurationProperties {

    /**
     * Comma-separated list of origins to allow. '*' allows all origins. When not set,
     * CORS support is disabled.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Comma-separated list of methods to allow. '*' allows all methods. When not set,
     * defaults to GET.
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * Comma-separated list of headers to allow in a request. '*' allows all headers.
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * Comma-separated list of headers to include in a response.
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Set whether credentials are supported. When not set, credentials are not supported.
     */
    private Boolean allowCredentials;

    /**
     * How long, in seconds, the response from a pre-flight request can be cached by
     * clients.
     */
    private Long maxAge = 1800L;

}
