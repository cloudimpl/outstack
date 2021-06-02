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

import java.util.Collection;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author nuwan
 */
public class UserDetail implements UserDetails{

    private final String user;
    private final boolean active;
    private final boolean locked;
    private final String  successRedirectUrl;
    private final String errorRedirectUrl;
    private final Collection<PlatformGrantedAuthority> authorities;
    public UserDetail(String user,boolean active,boolean locked,Collection<PlatformGrantedAuthority> authorities,String successRedirectUrl,String errorRedirectUrl) {
        this.user = user;
        this.active = active;
        this.locked = locked;
        this.authorities = authorities;
        this.successRedirectUrl = successRedirectUrl;
        this.errorRedirectUrl = errorRedirectUrl;
    }

    
    @Override
    public Collection<PlatformGrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user;
    }

   @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }
    
}
