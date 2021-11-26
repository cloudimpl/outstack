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
package com.cloudimpl.outstack.runtime;

/**
 *
 * @author nuwan
 */
public class CommandResponse {
    private String status;
    private Object value;

    public CommandResponse(String status) {
        this.status = status;
    }

    public CommandResponse(String status, Object value) {
        this.status = status;
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public Object getValue() {
        return value;
    }
    
}
