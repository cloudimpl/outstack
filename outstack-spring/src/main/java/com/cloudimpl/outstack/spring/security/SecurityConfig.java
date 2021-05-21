///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cloudimpl.outstack.spring.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
//import org.springframework.web.server.ServerWebExchange;
//
//
///**
// *
// * @author nuwan
// */
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Autowired
//    ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver;
//     
//    @Bean
//    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
//        return http
//                .cors()
//                .and()
//                .csrf().disable()
//                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//               	.authorizeExchange((exchanges) -> 
//				exchanges
//					.pathMatchers("/login","/error","/logout").permitAll()
//					.anyExchange().authenticated()
//			)
//                .httpBasic().disable()
//	        .oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver))
//                .formLogin()
//                .loginPage("/login")
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .and()
//                .build();
//    }
//
//    //in case you want to encrypt password
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//}
////@Component
////public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
////	
////	private final PasswordEncoder passwordEncoder;
////	
////        @Autowired
////        private ReactiveUserDetailsService userDetailService;
////        
////	public WebSecurityConfig(PasswordEncoder passwordEncoder) {
////		this.passwordEncoder = passwordEncoder;
////	}
////	
////	@Override
////	protected void configure(HttpSecurity http) throws Exception {
////		http
////				.cors()
////				.and()
////				.csrf().disable()
////				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////				.and()
////				.authorizeRequests(configurer ->
////						configurer
////								.antMatchers(
////										"/error",
////										"/login"
////								)
////								.permitAll()
////								.anyRequest()
////								.authenticated()
////				)
////				.exceptionHandling().disable()
////				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
////	}
////	
////	@Bean
////	//@Override
////	protected ReactiveUserDetailsService userDetailsService() {
////		return userDetailService;
////	}
////}
