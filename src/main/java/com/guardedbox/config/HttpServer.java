package com.guardedbox.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * HTTP Server.
 *
 * @author s3curitybug@gmail.com
 *
 */
@Component
@RequiredArgsConstructor
public class HttpServer {

    /** Property: server.port. */
    @Value("${server.port}")
    private final Integer serverPort;

    /** Property: server.internal.http.port. */
    @Value("${server.internal.http.port:}")
    private final Integer internalHttpPort;

    /** Property: server.internal.https.port. */
    @Value("${server.internal.https.port:}")
    private final Integer internalHttpsPort;

    /** Property: server.external.https.port. */
    @Value("${server.external.https.port:}")
    private final Integer externalHttpsPort;

    /**
     * Bean: ServletWebServerFactory.
     * Creates a dual port Tomcat, listening both in an http port and an https port. The http port simply redirects to the https one.
     *
     * @return TomcatServletWebServerFactory.
     */
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {

        // Check if there is dual port configuration.
        if (internalHttpPort == null || internalHttpsPort == null || externalHttpsPort == null
                || serverPort.equals(internalHttpPort) || !serverPort.equals(internalHttpsPort)) {
            return new TomcatServletWebServerFactory();
        }

        // Enable OCSP stapling.
        System.setProperty("jdk.tls.server.enableStatusRequestExtension", "true");

        // Create the https Tomcat.
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(
                    Context context) {

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);

            }

        };

        // Add the http connector with a redirection to the https port.
        Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        httpConnector.setScheme("http");
        httpConnector.setPort(internalHttpPort);
        httpConnector.setSecure(false);
        httpConnector.setRedirectPort(externalHttpsPort);
        tomcat.addAdditionalTomcatConnectors(httpConnector);

        return tomcat;

    }

}
