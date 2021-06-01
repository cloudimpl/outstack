package com.cloudimpl.outstack.spring.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
//@Component("Bearer")
public class BearerTokenAuthenticationManager
        extends PlatformAuthenticationManager{
    private final JwtBearerTokenConverter jwtAuthenticationConverter = new JwtBearerTokenConverter();

    private final ReactiveJwtDecoder jwtDecoder;

    public BearerTokenAuthenticationManager(ReactiveJwtDecoder jwtDecoder,AuthenticationProvider authenticationProvider, AuthorizationProvider authorizationProvider, TokenProvider tokenProvider) {
        super(authenticationProvider, authorizationProvider, tokenProvider);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .doOnNext(a->System.out.println(a.getClass().getName()))
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .map(BearerTokenAuthenticationToken::getToken)
                .doOnNext(token->log.info("token value={}", token))
                .flatMap(this.jwtDecoder::decode)
                .map(this.jwtAuthenticationConverter::convert)
                .cast(Authentication.class)
                .onErrorMap(JwtException.class, this::onError);
    }

    private OAuth2AuthenticationException onError(JwtException e) {
        log.info("Invalid Token", e);
        OAuth2Error invalidRequest = invalidToken();
        return new OAuth2AuthenticationException(invalidRequest, e.getMessage());
    }

    private static OAuth2Error invalidToken() {
        return new BearerTokenError(
                BearerTokenErrorCodes.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED,
                "Invalid Access Token",
                "https://tools.ietf.org/html/rfc6750#section-3.1");
    }

    @Override
    protected Mono<PlatformAuthenticationToken> convertToPlatformToken(Authentication autentication) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
