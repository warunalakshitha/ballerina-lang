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
package org.ballerinalang.persistence.serializable.responses.impl;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.SerializableWorkerSignal;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.HashSet;

/**
 * This class implements @{@link SerializableResponseContext} to serialize @{@link ForkJoinWorkerResponseContext}.
 *
 * @since 0.981.2
 */
public class SerializableAsyncResponse extends SerializableResponseContext {

    public SerializableWorkerSignal signal;

    public boolean fulfilled;

    public boolean cancelled;

    private boolean errored;

    public SerializableAsyncResponse(String respCtxKey, AsyncInvocableWorkerResponseContext respCtx,
                                     SerializableState state, HashSet<String> updatedObjectSet) {
        super(respCtxKey, respCtx.getHaltCount());
        this.respCtxKey = respCtxKey;
        if (respCtx.getCurrentSignal() != null) {
            this.signal = new SerializableWorkerSignal(respCtx.getCurrentSignal(), state, updatedObjectSet);
        }
        this.fulfilled = respCtx.isFulfilled();
        this.cancelled = respCtx.isCancelled();
        this.errored = respCtx.isErrored();
    }

    @Override
    public void addTargetContexts(WorkerResponseContext respCtx, SerializableState state) {
    }

    @Override
    public void update(WorkerResponseContext respCtx, SerializableState state, HashSet<String> updatedObjectSet) {
        AsyncInvocableWorkerResponseContext asyncRespCtx = (AsyncInvocableWorkerResponseContext) respCtx;
        if (!isDone()) {
            if (asyncRespCtx.getCurrentSignal() != null) {
                this.signal = new SerializableWorkerSignal(asyncRespCtx.getCurrentSignal(), state, updatedObjectSet);
            }
            this.fulfilled = asyncRespCtx.isFulfilled();
            this.cancelled = asyncRespCtx.isCancelled();
            this.errored = asyncRespCtx.isErrored();
        }
        this.haltCount = asyncRespCtx.getHaltCount();
    }

    @Override
    public WorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                    SerializableState state, Deserializer deserializer) {
        AsyncInvocableWorkerResponseContext asyncRespCtx =
                new AsyncInvocableWorkerResponseContext(callableUnitInfo,
                                                        callableUnitInfo.getWorkerSet().generalWorkers.length);
        if (signal != null) {
            asyncRespCtx.setCurrentSignal(signal.getWorkerData(programFile, state, deserializer));
        }
        if (fulfilled) {
            asyncRespCtx.setAsFulfilled();
        }
        if (errored) {
            asyncRespCtx.setAsErrored();
        }
        if (cancelled) {
            asyncRespCtx.setAsCancelled();
        }
        asyncRespCtx.setHaltCount(haltCount);
        return asyncRespCtx;
    }

    public boolean isDone() {
        return fulfilled || errored || cancelled;
    }
}
