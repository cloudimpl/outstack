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
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.util.Util;
import com.google.gson.internal.LinkedTreeMap;

/**
 *
 * @author nuwan
 */
public class StreamEvent {

    public enum Action {
        ADD, REMOVE
    }

    private final String eventType;
    private final Action action;
    private final Object event;

    public StreamEvent(Action action, Object event) {
        this.action = action;
        this.event = event;
        this.eventType = event.getClass().getName();
    }

    public Action getAction() {
        return action;
    }

    public Object getEvent() {
        if (event instanceof LinkedTreeMap) {
            return GsonCodecRuntime.decodeTree(Util.classForName(eventType), LinkedTreeMap.class.cast(event));
        }
        return event;
    }

}
