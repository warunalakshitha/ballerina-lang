/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence;

import org.ballerinalang.bre.NativeCallContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.serializable.SerializableState;

import java.util.List;

/**
 * Represents execution state for given @{@link WorkerExecutionContext}.
 *
 * @since 0.983.0
 */
public class State {

    SerializableState sState;

    public List<WorkerExecutionContext> executableCtxList;

    List<AsyncNativeContext> asyncNativeContexts;

    public State(SerializableState sState, List<WorkerExecutionContext> executableCtxList,
                 List<AsyncNativeContext> asyncNativeContexts) {
        this.sState = sState;
        this.executableCtxList = executableCtxList;
        this.asyncNativeContexts = asyncNativeContexts;
    }

    /**
     * This is used as data transfer class to hold @{@link NativeCallContext} and @{@link WorkerResponseContext}.
     */
    public static class AsyncNativeContext {

        public NativeCallContext nativeCallContext;

        public WorkerResponseContext respCtx;

        public AsyncNativeContext(NativeCallContext nativeCallContext, WorkerResponseContext respCtx) {
            this.nativeCallContext = nativeCallContext;
            this.respCtx = respCtx;
        }
    }
}
