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
package org.ballerinalang.persistence.serializable.reftypes;

import org.ballerinalang.model.values.BFuture;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Interface which is used to represent the serializable @{@link BRefType}.
 *
 * @since 0.981.1
 */
public interface SerializableRefType {

    /**
     * Deserialize the serializable object and provide its @{@link BRefType} object.
     *
     * @param programFile  Program file
     * @param state        State
     * @param deserializer Deserializer
     * @return Deserialized @{@link BRefType} object
     */
    BRefType getBRefType(ProgramFile programFile, SerializableState state, Deserializer deserializer);

    /**
     * Used to set any worker execution contexts or response contexts to deserialized @{@link BRefType}.
     * ex. @{@link BFuture}
     *
     * @param refType      BRefType
     * @param programFile  Program file
     * @param state        State
     * @param deserializer Deserializer
     */
    void setContexts(BRefType refType, ProgramFile programFile, SerializableState state,
                     Deserializer deserializer);
}
