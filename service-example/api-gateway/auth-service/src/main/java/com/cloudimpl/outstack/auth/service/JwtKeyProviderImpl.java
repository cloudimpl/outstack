/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.auth.service;

import com.cloudimpl.outstack.spring.security.JwtKeyProvider;
import com.cloudimpl.outstack.spring.security.SecurityProperties;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author nuwan
 */
@Component
public class JwtKeyProviderImpl implements JwtKeyProvider {

    private final SecurityProperties prop;
    @Autowired
    @Qualifier("tokenVerificationKey")
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyProviderImpl(SecurityProperties prop) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        this.prop = prop;
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(prop.getPrivateKeyFile().getInputStream(), "passwd".toCharArray());

        Key key = keyStore.getKey("jwtkey", "passwd".toCharArray());
        privateKey = RSAPrivateKey.class.cast(key);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(prop.getPublicKeyFile().getInputStream());
        publicKey = RSAPublicKey.class.cast(cert.getPublicKey());
    }

    @Override
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

}
