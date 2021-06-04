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
import java.util.List;

/**
 *
 * @author nuwan
 */
public class RoleDownloadResponse {
    private final String context;
    private final String subject;
    private final List<String> policyReferenceList;
    public RoleDownloadResponse(String context, String subject,List<String> policyReferenceList) {
        this.context = context;
        this.subject = subject;
        this.policyReferenceList = policyReferenceList;
    }

    public String getContext() {
        return context;
    }

    public String getSubject() {
        return subject;
    }

    public Collection<String> getPolicyReferenceList() {
        return policyReferenceList;
    }
    
    
}
