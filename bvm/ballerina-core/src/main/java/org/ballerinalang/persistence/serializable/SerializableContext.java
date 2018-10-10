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

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.ballerinalang.util.program.BLangVMUtils.SERVICE_INFO_KEY;

/**
 * This class represents a serializable Ballerina execution context.
 *
 * @since 0.981.1
 */
public class SerializableContext {

    public String ctxKey;

    String parentCtxKey;

    String respCtxKey;

    public WorkerState state = WorkerState.CREATED;

    public int ip;

    public int[] retRegIndexes;

    private boolean runInCaller;

    private String enclosingServiceName;

    private String callableUnitName;

    private String callableUnitPkgPath;

    public String workerName;

    public Type type = Type.PARENT;

    public HashMap<String, Object> globalProps = new HashMap<>();

    private HashMap<String, Object> localProps = new HashMap<>();

    public SerializableWorkerData workerLocal;

    SerializableWorkerData workerResult;

    public SerializableContext() {
    }

    SerializableContext(String ctxKey, WorkerExecutionContext ctx, SerializableState state, int ip,
                        boolean updateParent, HashSet<String> updatedObjectSet) {
        this.ctxKey = ctxKey;
        if (ctx.workerInfo != null) {
            workerName = ctx.workerInfo.getWorkerName();
            if (ctx.respCtx instanceof AsyncInvocableWorkerResponseContext) {
                type = Type.ASYNC;
            } else if (workerName.equals(Constants.DEFAULT)) {
                type = Type.DEFAULT;
            } else {
                type = Type.WORKER;
            }
        }
        retRegIndexes = ctx.retRegIndexes;
        runInCaller = ctx.runInCaller;
        if (ctx.callableUnitInfo != null) {
            if (ctx.callableUnitInfo instanceof ResourceInfo) {
                enclosingServiceName = ((ResourceInfo) ctx.callableUnitInfo).getServiceInfo().getName();
            }
            if (ctx.callableUnitInfo.attachedToType != null) {
                callableUnitName = ctx.callableUnitInfo.attachedToType.getName() + "." + ctx.callableUnitInfo.getName();
            } else {
                callableUnitName = ctx.callableUnitInfo.getName();
            }
            callableUnitPkgPath = ctx.callableUnitInfo.getPkgPath();
        }
        // Add or Update the parent contexts.
        populateParentContexts(ctx, state, updateParent, updatedObjectSet);
        // Update worker execution context data.
        populateData(ctx, ip, state, updatedObjectSet);
        // Register in state
        state.registerContext(this);
    }

    public void update(WorkerExecutionContext ctx, SerializableState state, int ip, boolean updateParent,
                       HashSet<String> updatedObjectSet) {
        // Add or Update the parent contexts.
        populateParentContexts(ctx, state, updateParent, updatedObjectSet);
        // Update worker execution context data.
        populateData(ctx, ip, state, updatedObjectSet);
    }

    public void update(WorkerExecutionContext ctx, SerializableState state, boolean updateParent,
                       HashSet<String> updatedObjectSet) {
        // Add or Update the parent contexts.
        populateParentContexts(ctx, state, updateParent, updatedObjectSet);
        // Update worker execution context data.
        populateData(ctx, state, updatedObjectSet);
    }

    private void populateParentContexts(WorkerExecutionContext ctx, SerializableState state, boolean updateParent,
                                        HashSet<String> updatedObjectSet) {
        String sParentCtxKey = state.getObjectKey(ctx.parent);
        if (!type.equals(Type.ASYNC) &&  ctx.parent != null && !ctx.parent.state.equals(WorkerState.DONE)) {
            SerializableContext sParentCtx = state.getSerializableContext(sParentCtxKey);
            if (sParentCtx == null) {
                sParentCtx = new SerializableContext(sParentCtxKey, ctx.parent, state, ctx.parent.ip, updateParent,
                                                     updatedObjectSet);
            } else if (updateParent) {
                if (type.equals(Type.DEFAULT)) {
                    sParentCtx.update(ctx.parent, state, ctx.parent.ip, true, updatedObjectSet);
                } else {
                    sParentCtx.update(ctx.parent, state, true, updatedObjectSet);
                }
            }
            this.parentCtxKey = sParentCtx.ctxKey;
        }
    }

    private void populateData(WorkerExecutionContext ctx, int ip, SerializableState state,
                              HashSet<String> updatedObjectSet) {
        this.ip = ip;
        populateData(ctx, state, updatedObjectSet);
    }

    private void populateData(WorkerExecutionContext ctx, SerializableState state, HashSet<String> updatedObjectSet) {
        populateProperties(ctx.globalProps, state.globalProps, state, updatedObjectSet);
        populateProperties(ctx.localProps, this.localProps, state, updatedObjectSet);
        if (ctx.respCtx != null) {
            respCtxKey = state.populateRespContext(ctx.respCtx, updatedObjectSet).getRespCtxKey();
        }
        if (ctx.workerLocal != null) {
            workerLocal = new SerializableWorkerData(ctx.workerLocal, state, updatedObjectSet);
        }
        if (ctx.workerResult != null) {
            workerResult = new SerializableWorkerData(ctx.workerResult, state, updatedObjectSet);
        }
    }

    public static SerializableContext deserialize(String jsonString) {
        JsonSerializer serializer = Serializer.getJsonSerializer();
        return serializer.deserialize(jsonString, SerializableContext.class);
    }

    WorkerExecutionContext getWorkerExecutionContext(ProgramFile programFile, SerializableState state,
                                                     Deserializer deserializer) {
        WorkerExecutionContext workerExecutionContext = deserializer.getContexts().get(ctxKey);
        if (workerExecutionContext != null) {
            return workerExecutionContext;
        }
        CallableUnitInfo callableUnitInfo = null;
        WorkerData workerLocalData = null;
        WorkerData workerResultData = null;
        PackageInfo packageInfo = null;
        HashMap<String, Object> globalProps = deserializer.getGlobalPropertyMap().get(state.getId());
        if (globalProps == null) {
            globalProps = prepareProps(state.globalProps, state, programFile, deserializer);
            deserializer.getGlobalPropertyMap().put(state.getId(), globalProps);
        }
        if (callableUnitPkgPath != null) {
            packageInfo = programFile.getPackageInfo(callableUnitPkgPath);
            if (enclosingServiceName != null) {
                ServiceInfo serviceInfo = packageInfo.getServiceInfo(enclosingServiceName);
                globalProps.put(SERVICE_INFO_KEY, serviceInfo);
                callableUnitInfo = serviceInfo.getResourceInfo(callableUnitName);
            } else {
                callableUnitInfo = packageInfo.getFunctionInfo(callableUnitName);
            }
        }
        if (workerLocal != null) {
            workerLocalData = workerLocal.getWorkerData(programFile, state, deserializer);
        }
        if (workerResult != null) {
            workerResultData = workerResult.getWorkerData(programFile, state, deserializer);
        }
        WorkerExecutionContext parentCtx;
        if (parentCtxKey == null || (parentCtx = state.getExecutionContext(parentCtxKey, programFile, deserializer))
                == null || callableUnitInfo == null) {
            if (callableUnitInfo != null) {
                WorkerResponseContext respCtx = null;
                if (respCtxKey != null) {
                    respCtx = state.getResponseContext(this.respCtxKey, programFile, callableUnitInfo, deserializer);
                }
                WorkerInfo workerInfo = getWorkerInfo(respCtx, null, packageInfo, callableUnitInfo);
                workerExecutionContext = new WorkerExecutionContext(respCtx, callableUnitInfo, workerInfo,
                                                                    workerLocalData, workerResultData,
                                                                    retRegIndexes, runInCaller);
            } else {
                // This is the root context
                workerExecutionContext = new WorkerExecutionContext(programFile);
                workerExecutionContext.workerLocal = workerLocalData;
                workerExecutionContext.workerResult = workerResultData;
            }
        } else {
            WorkerResponseContext respCtx = null;
            if (respCtxKey != null) {
                respCtx = state.getResponseContext(this.respCtxKey, programFile, callableUnitInfo, deserializer);
            }
            WorkerInfo workerInfo = getWorkerInfo(respCtx, parentCtx, packageInfo, callableUnitInfo);
            workerExecutionContext = new WorkerExecutionContext(parentCtx, respCtx, callableUnitInfo,
                                                                workerInfo, workerLocalData, workerResultData,
                                                                retRegIndexes, runInCaller);
        }
        workerExecutionContext.globalProps = globalProps;
        workerExecutionContext.localProps = prepareProps(localProps, state, programFile, deserializer);
        workerExecutionContext.ip = ip;
        workerExecutionContext.interruptible = true;
        deserializer.getContexts().put(ctxKey, workerExecutionContext);
        return workerExecutionContext;
    }

    private HashMap<String, Object>  prepareProps(HashMap<String, Object> sourcePropertyMap, SerializableState
            state, ProgramFile programFile, Deserializer deserializer) {
        HashMap<String, Object> targetPropertyMap = new HashMap<>();
        if (sourcePropertyMap != null) {
            sourcePropertyMap.forEach((s, o) -> {
                Object deserialize = state.deserialize(o, programFile, deserializer);
                targetPropertyMap.put(s, deserialize);
            });
        }
        return targetPropertyMap;
    }

    private void populateProperties(Map<String, Object> sourcePropertyMap, HashMap<String, Object> targetPropertyMap,
                                    SerializableState state, HashSet<String> updatedObjectSet) {
        String targetPropertyMapKey = state.getObjectKey(targetPropertyMap);
        if (!updatedObjectSet.contains(targetPropertyMapKey) && sourcePropertyMap != null &&
                targetPropertyMap != null) {
            targetPropertyMap.clear();
            sourcePropertyMap.forEach((s, o) -> targetPropertyMap.put(s, state.serialize(o, updatedObjectSet)));
            updatedObjectSet.contains(targetPropertyMapKey);
        }
    }

    private WorkerInfo getWorkerInfo(WorkerResponseContext respCtx, WorkerExecutionContext parentCtx,
                                     PackageInfo packageInfo, CallableUnitInfo callableUnitInfo) {
        if (parentCtx != null && respCtx instanceof ForkJoinWorkerResponseContext) {
            return ((Instruction.InstructionFORKJOIN) packageInfo.getInstructions()[parentCtx.ip - 1])
                    .forkJoinCPEntry.getForkjoinInfo().getWorkerInfo(workerName);
        } else if (Constants.DEFAULT.equals(workerName)) {
            return callableUnitInfo.getDefaultWorkerInfo();
        } else {
            return callableUnitInfo.getWorkerInfo(workerName);
        }
    }

    /**
     * Execution Type of the @{@link SerializableContext}.
     */
    public enum Type {
        DEFAULT,
        ASYNC,
        WORKER,
        PARENT
    }
}
