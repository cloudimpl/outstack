package com.cloudimpl.outstack.spring.security;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
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
        extends PlatformAuthenticationManager {

    private final ReactiveJwtDecoder jwtDecoder;

    public BearerTokenAuthenticationManager(ReactiveJwtDecoder jwtDecoder, AuthenticationProvider authenticationProvider, AuthorizationProvider authorizationProvider, TokenProvider tokenProvider) {
        super(authenticationProvider, authorizationProvider, tokenProvider);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected Mono<PlatformAuthenticationToken> convertToPlatformToken(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .doOnNext(t -> Auth2Util.validateAuthentcationMeta(t.getDetails()))
                .flatMap(this::decodeJwt)
                .onErrorMap(JwtException.class, this::onError);

    }

    private Mono<PlatformAuthenticationToken> decodeJwt(BearerTokenAuthenticationToken token) {
        return Mono.just(token)
                .doOnNext(t -> log.info("token value={}", t))
                .map(t->t.getToken())
                .flatMap(this.jwtDecoder::decode)
                .map(jwt -> new PlatformAuthenticationToken((AuthenticationMeta) token.getDetails(), jwt.getSubject(), Collections.EMPTY_LIST, null).setJwtToken(jwt));
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

}
