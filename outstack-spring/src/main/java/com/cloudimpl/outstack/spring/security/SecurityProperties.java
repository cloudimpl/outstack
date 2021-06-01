package com.cloudimpl.outstack.spring.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Data
@ConfigurationProperties(prefix = "jwt-auth")
public class SecurityProperties {
    private Resource publicKeyFile = new ClassPathResource("jwtauth.crt");
    private Resource privateKeyFile = new ClassPathResource("jwtauth.jks");
}
