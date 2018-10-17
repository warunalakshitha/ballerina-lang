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

import org.ballerinalang.bre.bvm.SignalType;
import org.ballerinalang.bre.bvm.WorkerSignal;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.HashSet;

/**
 * This class represents a serializable Ballerina worker signal.
 *
 * @since 0.983.0
 */
public class SerializableWorkerSignal {

    private String ctxKey;

    private SignalType type;

    private SerializableWorkerData result;

    public SerializableWorkerSignal(WorkerSignal workerSignal, SerializableState state,
                             HashSet<String> updatedObjectSet) {
        this.ctxKey = state.getObjectKey(workerSignal.getSourceContext());
        this.type = workerSignal.getType();
        this.result = new SerializableWorkerData(workerSignal.getResult(), state, updatedObjectSet);
    }

    public WorkerSignal getWorkerData(ProgramFile programFile, SerializableState state, Deserializer deserializer) {
        return new WorkerSignal(state.getExecutionContext(ctxKey, programFile, deserializer), type,
                                result.getWorkerData(programFile, state, deserializer));

    }
}
