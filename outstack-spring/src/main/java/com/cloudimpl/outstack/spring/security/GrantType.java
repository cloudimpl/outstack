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
package com.cloudimpl.outstack.spring.security;

import com.cloudimpl.outstack.runtime.ValidationErrorException;
import java.util.Objects;

/**
 *
 * @author nuwan
 */
public enum GrantType {
    AUTHORIZATION_CODE,
    PASSWORD,
    REFRESH_TOKEN,
    TENANT_TOKEN,
    CUSTOM_TOKEN;

    public static GrantType from(String type) {
        Objects.requireNonNull(type,"grant type cannot be null");
        type = type.toLowerCase();
        switch (type) {
            case "authorization_code": {
                return AUTHORIZATION_CODE;
            }
            case "password": {
                return PASSWORD;
            }
            case "refresh_token": {
                return REFRESH_TOKEN;
            }
            case "tenant_token": {
                return TENANT_TOKEN;
            }
            case "custom_token": {
                return CUSTOM_TOKEN;
            }

            default:
                throw new ValidationErrorException("unknown grant type:" + type);
        }
    }
}
