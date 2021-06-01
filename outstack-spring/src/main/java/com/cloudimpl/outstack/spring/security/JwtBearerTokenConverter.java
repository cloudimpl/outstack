package com.cloudimpl.outstack.spring.security;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;


public class JwtBearerTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        
       return new JwtAuthenticatedToken(JwtToken.builder().build(), source.getTokenValue());
    }
}
 