///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.cloudimpl.outstack.spring.security;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.Key;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.PublicKey;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.interfaces.RSAPrivateKey;
//import java.security.interfaces.RSAPublicKey;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
//import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
//
///**
// *
// * @author nuwan
// */
//@Slf4j
//@Configuration
//@EnableAutoConfiguration
//public class JwtConfiguration {
//	
//	@Value("${com.cloudimpl.outstack.security.jwt.keystore-location}")
//	private String keyStorePath;
//	
//	@Value("${com.cloudimpl.outstack.security.jwt.keystore-password}")
//	private String keyStorePassword;
//	
//	@Value("${com.cloudimpl.outstack.security.jwt.key-alias}")
//	private String keyAlias;
//	
//	@Value("${com.cloudimpl.outstack.security.jwt.private-key-passphrase}")
//	private String privateKeyPassphrase;
//	
//	@Bean
//	public KeyStore keyStore() {
//		try {
//			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//			InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStorePath);
//			keyStore.load(resourceAsStream, keyStorePassword.toCharArray());
//			return keyStore;
//		} catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
//			log.error("Unable to load keystore: {}", keyStorePath, e);
//		}
//		
//		throw new IllegalArgumentException("Unable to load keystore");
//	}
//	
//	@Bean
//	public RSAPrivateKey jwtSigningKey(KeyStore keyStore) {
//		try {
//			Key key = keyStore.getKey(keyAlias, privateKeyPassphrase.toCharArray());
//			if (key instanceof RSAPrivateKey) {
//				return (RSAPrivateKey) key;
//			}
//		} catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
//			log.error("Unable to load private key from keystore: {}", keyStorePath, e);
//		}
//		
//		throw new IllegalArgumentException("Unable to load private key");
//	}
//	
//	@Bean
//	public RSAPublicKey jwtValidationKey(KeyStore keyStore) {
//		try {
//			Certificate certificate = keyStore.getCertificate(keyAlias);
//			PublicKey publicKey = certificate.getPublicKey();
//			
//			if (publicKey instanceof RSAPublicKey) {
//				return (RSAPublicKey) publicKey;
//			}
//		} catch (KeyStoreException e) {
//			log.error("Unable to load private key from keystore: {}", keyStorePath, e);
//		}
//		
//		throw new IllegalArgumentException("Unable to load RSA public key");
//	}
//	
//	@Bean
//	public ReactiveJwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
//		return NimbusReactiveJwtDecoder.withPublicKey(rsaPublicKey).build();
//	}
//}