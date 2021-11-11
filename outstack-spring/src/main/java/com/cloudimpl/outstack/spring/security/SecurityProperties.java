package com.cloudimpl.outstack.spring.security;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Data
@ConfigurationProperties(prefix = "jwt-auth")
public class SecurityProperties {

    private Resource publicKeyFile = ResourceSelector.load("jwtauth.crt");
    private Resource privateKeyFile = ResourceSelector.load("jwtauth.jks");

    public static final class ResourceSelector {

        public static Resource load(String resourceName) {
            String r = resourceName.replaceAll("\\.", "_");
            String val = System.getenv(r);
            if (val == null) {
                return new ClassPathResource(val);
            } else {
                try {
                    return new ByteArrayResource(val.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

    }

}
