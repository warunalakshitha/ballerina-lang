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

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.HashSet;

/**
 * This class implements @{@link SerializableResponseContext} to serialize @{@link CallableWorkerResponseContext}.
 *
 * @since 0.983.0
 */
public class SerializableCallableResponse extends SerializableResponseContext {

    public int[] retRegIndexes;

    public SerializableCallableResponse(String respCtxKey, CallableWorkerResponseContext respCtx) {
        super(respCtxKey, respCtx.getHaltCount());
        retRegIndexes = respCtx.getRetRegIndexes();
    }

    @Override
    public void addTargetContexts(WorkerResponseContext respCtx, SerializableState state) {
        CallableWorkerResponseContext callableCtx = (CallableWorkerResponseContext) respCtx;
        SerializableContext sTargetCtx = state
                .getSerializableContext(state.getObjectKey(callableCtx.getTargetContext()));
        targetCtxKey = sTargetCtx.ctxKey;
    }

    @Override
    public void update(WorkerResponseContext respCtx, SerializableState state,
                       HashSet<String> updatedObjectSet) {
        this.haltCount = ((CallableWorkerResponseContext) respCtx).getHaltCount();
    }

    @Override
    public WorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                    SerializableState state, Deserializer deserializer) {
        CallableWorkerResponseContext respCtx = new CallableWorkerResponseContext(callableUnitInfo.getRetParamTypes(),
                                                                                  callableUnitInfo.getWorkerSet()
                                                                                          .generalWorkers.length);
        respCtx.joinTargetContextInfo(state.getExecutionContext(targetCtxKey, programFile, deserializer),
                                      retRegIndexes);
        respCtx.setHaltCount(haltCount);
        return respCtx;
    }
}
