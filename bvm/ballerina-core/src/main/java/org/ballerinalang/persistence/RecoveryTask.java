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
package org.ballerinalang.persistence;

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.program.BLangVMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is used to recover persisted states during startup.
 *
 * @since 0.981.1
 */
public class RecoveryTask implements Runnable {

    private ProgramFile programFile;

    private static final Logger logger = LoggerFactory.getLogger(RecoveryTask.class);

    public RecoveryTask(ProgramFile programFile) {
        this.programFile = programFile;
    }

    @Override
    public void run() {
        Deserializer deserializer = new Deserializer();
        List<State> states = PersistenceStore.getStates(programFile, deserializer);
        if (states.isEmpty()) {
            return;
        }
        states.forEach(state -> {
            RuntimeStates.add(state.sState);
            state.executableCtxList.forEach(ctx -> {
                if (ctx.callableUnitInfo instanceof ResourceInfo) {
                    ResourceInfo resourceInfo = (ResourceInfo) ctx.callableUnitInfo;
                    BLangVMUtils.setServiceInfo(ctx, resourceInfo.getServiceInfo());
                }
                // have to decrement ip as CPU class increments it as soon as instruction is fetched
                ctx.ip--;
                // As we don't have any running context at this point, none of the contexts can run in caller.
                // Even though sync functions run in caller under normal conditions, we have to override
                // that to start a new thread for the function.
                ctx.runInCaller = false;
                BLangScheduler.schedule(ctx);
            });
        });
        deserializer.cleanUpDeserializer();
    }
}
