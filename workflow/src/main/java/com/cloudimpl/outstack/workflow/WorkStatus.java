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
package com.cloudimpl.outstack.workflow;

/**
 *
 * @author nuwan
 */
public class WorkStatus {

    private final Work.Status status;
    private Object data;

    private WorkStatus(Work.Status status) {
        this.status = status;
    }

    public static WorkStatus publish(Work.Status status) {
        return publish(status, null);
    }

    public static WorkStatus publish(Work.Status status, Object data) {
        if (status == Work.Status.PENDING || status == Work.Status.RUNNING) {
            throw new WorkflowException("invalid work status {0} published", status);
        }
        return new WorkStatus(status).setContext(data);
    }

    protected WorkStatus setContext(Object data) {
        this.data = data;
        return this;
    }

    public <T> T getData() {
        return (T) data;
    }

    public Work.Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "WorkStatus{" + "status=" + status + ", data=" + data + '}';
    }

    
}
