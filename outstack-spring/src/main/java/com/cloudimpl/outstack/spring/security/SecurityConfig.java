package com.cloudimpl.outstack.spring.security;

import java.io.IOException;
import java.security.cert.CertificateException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Autowired
    ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver;

//    @Bean
//    //@SneakyThrows
//    RSAPublicKey tokenVerificationKey(SecurityProperties securityProperties) {
//
//        try {
//            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(securityProperties.getPublicKeyFile().getInputStream());
//            return RSAPublicKey.class.cast(cert.getPublicKey());
//        } catch (CertificateException | IOException ex) {
//            Logger.getLogger(SecurityConfig.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//    @Bean
//    BearerTokenAuthenticationManager authenticationManager(RSAPublicKey publicKey) {
//        return new BearerTokenAuthenticationManager(new NimbusReactiveJwtDecoder(publicKey));
//    }
    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            SecurityProperties securityProperties) {
        http.csrf().disable()
                .authorizeExchange()
                .pathMatchers("/authorize") 
                .permitAll()
                .anyExchange().authenticated()
                .and()
                .httpBasic().and()
                .formLogin()
                .loginPage("/authorize")
                .and()
                .oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver));
        // .jwt()
        ///  .authenticationManager(new BasicTokenAuthenticationManager());
        return http.build();
    }

}
