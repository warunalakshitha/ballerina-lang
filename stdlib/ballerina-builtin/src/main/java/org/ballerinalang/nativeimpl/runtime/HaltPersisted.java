/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.model.InterruptibleNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "haltPersisted",
        args = {@Argument(name = "millis", type = TypeKind.INT)},
        isPublic = true
)
public class HaltPersisted implements InterruptibleNativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        long delayMillis = context.getIntArgument(0);
        WorkerExecutionContext parentCtx = context.getParentWorkerExecutionContext();
        SerializableState state = new SerializableState(parentCtx);
        WorkerExecutionContext deserializedContext = state.getExecutionContext(context.getProgramFile());
        int a = 10;
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        callback.notifySuccess();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean persistBeforeOperation() {
        return true;
    }

    @Override
    public boolean persistAfterOperation() {
        return true;
    }
}
