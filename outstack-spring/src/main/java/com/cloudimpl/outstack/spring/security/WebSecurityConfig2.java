///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cloudimpl.outstack.spring.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
//import org.springframework.stereotype.Component;
//
///**
// *
// * @author nuwan
// */
//@Component
//public class WebSecurityConfig2 extends WebSecurityConfigurerAdapter {
//	
//	private final PasswordEncoder passwordEncoder;
//	
//	public WebSecurityConfig2(PasswordEncoder passwordEncoder) {
//		this.passwordEncoder = passwordEncoder;
//	}
//	
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http
//				.cors()
//				.and()
//				.csrf().disable()
//				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//				.and()
//				.authorizeRequests(configurer ->
//						configurer
//								.antMatchers(
//										"/error",
//										"/login"
//								)
//								.permitAll()
//								.anyRequest()
//								.authenticated()
//				)
//				.exceptionHandling().disable()
//				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
//	}
//	
//	@Bean
//	@Override
//	protected UserDetailsService userDetailsService() {
//		UserDetails user1 = User
//				.withUsername("user")
//				.authorities("USER")
//				.passwordEncoder(passwordEncoder::encode)
//				.password("1234")
//				.build();
//		
//		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//		manager.createUser(user1);
//		return manager;
//	}
//}
