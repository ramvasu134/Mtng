package com.Mtng.Mtng.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Opens an additional HTTP listener on port 8080 alongside the primary HTTPS
 * port (8443).  Spring Security's {@code requiresChannel().anyRequest().requiresSecure()}
 * in {@link SecurityConfig} will automatically redirect HTTP → HTTPS.
 *
 * <p>This is needed so that users who type {@code http://host:8080} are seamlessly
 * redirected to {@code https://host:8443}.</p>
 */
@Configuration
public class HttpToHttpsRedirectConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addAdditionalTomcatConnectors(httpConnector());
        return factory;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}

