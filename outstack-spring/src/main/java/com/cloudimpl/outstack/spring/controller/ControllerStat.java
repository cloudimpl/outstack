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
package com.cloudimpl.outstack.spring.controller;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author nuwan
 */
@Data
public class ControllerStat {

    private String serviceName;
    private String action;
    private String rootType;
    private String rootId;
    private String childType;
    private String childId;
    private long startTime;
    private long checkpointTime;

    public ControllerStat(String serviceName, String action, String rootType) {
        this.serviceName = serviceName;
        this.action = action;
        this.rootType = rootType;
        this.startTime = System.currentTimeMillis();
    }

    public String stats() {
        long diff = System.currentTimeMillis() - startTime;
        StringBuilder builder = new StringBuilder();
        builder.append("service: [").append(serviceName).append("] action = [").append(action).append("] rootType = [").append(rootType).append("] rootId = [").append(rootId).append("] childType = [")
                .append(childType).append("] childId = [").append(childId).append("] time = [").append(checkpointTime).append("][").append(diff).append("]");
        return builder.toString();
    }

    public void checkpoint() {
        this.checkpointTime = System.currentTimeMillis() - startTime;
    }
}
