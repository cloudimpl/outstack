package com.cloudimpl.outstack.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
        // -- Swagger UI v2
        "/v2/api-docs",
        "/favicon.ico",
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/**",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        // -- Swagger UI v3 (OpenAPI)
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/platform-service/v2/api-docs",
        "/swagger-ui/",
        "/swagger-ui"
    };

    @Autowired
    AuthenticationManagerResolver authenticationManagerResolver;

//    @Autowired
//    ReactiveAuthenticationManager authenticationManager;
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
    public AuthenticationWebFilter authenticationFilter(ReactiveAuthenticationManager authManager, ServerAuthenticationConverter convertor, String... urls) {
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(authManager);
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, urls));
        // authenticationFilter.setAuthenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/login"));
        authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(new BearerTokenServerAuthenticationEntryPoint()));
        authenticationFilter.setServerAuthenticationConverter(convertor);
        authenticationFilter.setAuthenticationSuccessHandler(new PlatformAuthenticationSuccessHandler());
        return authenticationFilter;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            SecurityProperties securityProperties) {
        http
                .csrf().disable()
                .cors()
                .and()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/public/**").permitAll()
                .pathMatchers(AUTH_WHITELIST).permitAll()
                .pathMatchers("/login/**", "/authorize").permitAll()
                .anyExchange().authenticated()
                .and()
                .httpBasic()
                .disable()
                .formLogin().disable()
                //  .formLogin()
                //   .loginPage("/login")
                //   .and()
                .addFilterAt(authenticationFilter(authenticationManagerResolver.getBasicTokenAuthentication(), new ServerFormLoginAuthenticationConverterEx(), "/login", "/token"), SecurityWebFiltersOrder.FORM_LOGIN)
                .addFilterAt(authenticationFilter(authenticationManagerResolver.getBasicTokenAuthentication(), new BasicLoginAuthenticationConverterEx(), "/login", "/token"), SecurityWebFiltersOrder.HTTP_BASIC)
                .oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver)
                .bearerTokenConverter(new ServerBearerTokenAuthenticationConverterEx(false)))
                .addFilterBefore(authenticationFilter(authenticationManagerResolver.getBearerTokenAuthentication(), new ServerBearerTokenAuthenticationConverterEx(true), "/token"), SecurityWebFiltersOrder.AUTHENTICATION);
        // .jwt()
        ///  .authenticationManager(new BasicTokenAuthenticationManager());
        return http.build();
    }

}
