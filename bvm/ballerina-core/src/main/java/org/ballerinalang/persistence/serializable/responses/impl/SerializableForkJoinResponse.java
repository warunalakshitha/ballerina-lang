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

import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ForkjoinInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.HashSet;

/**
 * This class implements @{@link SerializableResponseContext} to serialize @{@link ForkJoinWorkerResponseContext}.
 *
 * @since 0.983.0
 */
public class SerializableForkJoinResponse extends SerializableResponseContext {

    public int[] retRegIndexes;

    public SerializableForkJoinResponse(String respCtxKey, ForkJoinWorkerResponseContext respCtx) {
        super(respCtxKey, respCtx.getHaltCount());
        retRegIndexes = respCtx.getRetRegIndexes();
    }

    @Override
    public void addTargetContexts(WorkerResponseContext respCtx, SerializableState state) {
        ForkJoinWorkerResponseContext forkJoinCtx = (ForkJoinWorkerResponseContext) respCtx;
        SerializableContext sTargetCtx = state
                .getSerializableContext(state.getObjectKey(forkJoinCtx.getTargetContext()));
        targetCtxKey = sTargetCtx.ctxKey;
    }

    @Override
    public void update(WorkerResponseContext respCtx, SerializableState state, HashSet<String> updatedObjectSet) {
        this.haltCount = ((ForkJoinWorkerResponseContext) respCtx).getHaltCount();
    }

    @Override
    public WorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                    SerializableState state, Deserializer deserializer) {
        WorkerExecutionContext ctx = state.getExecutionContext(targetCtxKey, programFile, deserializer);
        Instruction.InstructionFORKJOIN forkJoinIns = (Instruction.InstructionFORKJOIN) callableUnitInfo
                .getPackageInfo().getInstructions()[ctx.ip - 1];
        ForkjoinInfo forkjoinInfo = forkJoinIns.forkJoinCPEntry.getForkjoinInfo();
        ForkJoinWorkerResponseContext respCtx = BLangFunctions
                .createForkJoinResponseContext(ctx, forkjoinInfo, forkJoinIns.joinBlockAddr,
                                               forkJoinIns.joinVarRegIndex, forkJoinIns.timeoutBlockAddr,
                                               forkJoinIns.timeoutVarRegIndex);
        respCtx.setHaltCount(haltCount);
        BLangFunctions.scheduleForkJoinTimeout(ctx, respCtx, forkjoinInfo, forkJoinIns.timeoutRegIndex);
        respCtx.joinTargetContextInfo(ctx, retRegIndexes);
        return respCtx;
    }
}
