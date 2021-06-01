package com.cloudimpl.outstack.spring.security;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("Basic")
public class BasicTokenAuthenticationManager
        extends PlatformAuthenticationManager {

    public BasicTokenAuthenticationManager(@Autowired(required = false)AuthenticationProvider authenticationProvider,@Autowired(required = false) AuthorizationProvider authorizationProvider,@Autowired(required = false) TokenProvider tokenProvider) {
        super(authenticationProvider, authorizationProvider, tokenProvider);
    }

    @Override
    protected Mono<PlatformAuthenticationToken> convertToPlatformToken(Authentication autentication) {
        return Mono.justOrEmpty(autentication).filter(a -> a instanceof UsernamePasswordAuthenticationToken)
                .cast(UsernamePasswordAuthenticationToken.class)
                .doOnNext(a -> Auth2Util.validateAuthentcationMeta(a.getDetails()))
                .map(a -> new PlatformAuthenticationToken((AuthenticationMeta) a.getDetails(), (String) a.getPrincipal(), Collections.EMPTY_LIST, null))
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

}
