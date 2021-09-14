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
package com.cloudimpl.outstack.runtime.iam;

import com.cloudimpl.outstack.runtime.domain.PolicyStatementRequest;
import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domain.PolicyStatement;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public class StatementContext {

    private final Map<String, String> mapAttr = new LinkedHashMap<>();

    public void put(String attr, String value) {
        this.mapAttr.put(MessageFormat.format("'\\{'{0}'\\}'", attr), value);
    }

    public PolicyStatement parse(PolicyStatement stmt) {
        String current = GsonCodecRuntime.encode(stmt);
        for (Map.Entry<String, String> attr : mapAttr.entrySet()) {
            current = current.replaceAll(attr.getKey(), attr.getValue());
        }
        return GsonCodecRuntime.decode(PolicyStatement.class, current);
    }

}
