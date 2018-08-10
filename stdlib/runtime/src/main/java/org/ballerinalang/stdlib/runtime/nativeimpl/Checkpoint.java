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
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.InterruptibleNativeCallableUnit;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native implementation for check point the execution context.
 *
 * @since 0.976.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "checkpoint",
        isPublic = true
)
public class Checkpoint implements InterruptibleNativeCallableUnit {
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        callback.notifySuccess();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean persistBeforeOperation() {
        return false;
    }

    @Override
    public boolean persistAfterOperation() {
        return true;
    }
}
