/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.NativeCallContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.State;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.HashSet;

/**
 * This class represents a serializable native context.
 *
 * @since 0.983.0
 */
class SerializableAsyncNativeContext {

    private String parentCtxKey;

    private String respCtxKey;

    private SerializableWorkerData workerData;

    private String callableName;

    private String pkgPath;

    SerializableAsyncNativeContext(Context nativeCtx, WorkerResponseContext respCtx,
                                   SerializableState state, HashSet<String> updatedObjectSet) {
        CallableUnitInfo callableUnitInfo = nativeCtx.getCallableUnitInfo();
        if (callableUnitInfo.attachedToType != null) {
            callableName = callableUnitInfo.attachedToType.getName() + "." + callableUnitInfo.getName();
        } else {
            callableName = callableUnitInfo.getName();
        }
        pkgPath = callableUnitInfo.getPkgPath();
        parentCtxKey = state.getObjectKey(nativeCtx.getParentWorkerExecutionContext());
        respCtxKey = state.getObjectKey(respCtx);
        workerData = new SerializableWorkerData(nativeCtx.getLocalWorkerData(), state,
                                                updatedObjectSet);
    }

    State.AsyncNativeContext getAsyncNativeContext(ProgramFile programFile, Deserializer deserializer,
                                                   SerializableState state) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaException("Package cannot be found  for path: " + pkgPath);
        } else {
            CallableUnitInfo callableUnitInfo = packageInfo.getFunctionInfo(callableName);
            NativeCallContext nativeCtx =
                    new NativeCallContext(state.getExecutionContext(parentCtxKey, programFile, deserializer),
                                          callableUnitInfo, workerData.getWorkerData(programFile, state, deserializer));
            return new State.AsyncNativeContext(nativeCtx, state.getResponseContext(respCtxKey, programFile,
                                                                                    callableUnitInfo, deserializer));
        }
    }
}
