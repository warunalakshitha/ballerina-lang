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
package org.ballerinalang.bre.bvm.persistency.reftypes;

import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.charset.StandardCharsets;

public class SerializableBBLOB implements SerializableRefType {

    private String blobContent;

    public SerializableBBLOB(BBlob bBlob) {
        blobContent = bBlob.toString();
    }

    @Override
    public BRefType getBRefType(ProgramFile programFile, SerializableState state) {
        if (blobContent == null) {
            throw new BallerinaException("Blob content is null.");
        }
        return new BBlob(blobContent.getBytes(StandardCharsets.UTF_8));
    }
}
