package com.cloudimpl.outstack.spring.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtBearerTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        return new JwtAuthenticationToken(JwtToken.builder().build());
    }

//    private JWTUser buildUser(Jwt source) {
// 
//        JWTUser user =  JWTUser.builder()
//                .sub(source.getSubject())
//                .scopes(parseScopes(source.getClaims().get("scope")))
//                .build();
//
//        return user;
//    }
//
//    private Set<Scope> parseScopes(Object scopeClaim) {
//        return Arrays
//                .stream(String.class.cast(scopeClaim).split(" "))
//                .map(Scope::new)
//                .collect(Collectors.toSet());
//    }
}
 