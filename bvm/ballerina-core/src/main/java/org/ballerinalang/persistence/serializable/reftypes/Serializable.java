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

import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.serializable.SerializableState;

import java.util.HashSet;

/**
 * Interface used to serialize the given object.
 *
 * @since 0.981.1
 */
public interface Serializable {

    /**
     * Serialize @{@link BRefType} to persist.
     *
     * @param state            Serializable state
     * @param updatedObjectSet Set of already updated objects
     * @return Serialized @{@link BRefType}
     */
    SerializableRefType serialize(SerializableState state, HashSet<String> updatedObjectSet);
}
