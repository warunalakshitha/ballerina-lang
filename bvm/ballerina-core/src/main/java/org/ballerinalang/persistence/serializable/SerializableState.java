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
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.model.InterruptibleNativeCallableUnit;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.persistence.State;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBFuture;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContextFactory;
import org.ballerinalang.persistence.serializable.responses.impl.SerializableAsyncResponse;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a serializable state. This holds the required functionality to persist the context.
 *
 * @since 0.981.1
 */
public class SerializableState {

    private String id;

    // Set of worker execution context hashcodes which resides as leaf nodes of the context hierarchy
    private Set<String> sCurrentCtxKeys = new HashSet<>();

    // Map of worker execution context hashcode vs serializable context
    private Map<String, SerializableContext> sContexts = new HashMap<>();

    // Map of response context hashcode vs serializable response context
    private Map<String, SerializableResponseContext> sRespContexts = new HashMap<>();

    // Map of BRefType object hashcode vs serializable BRefType
    private Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    // Map of NativeCallExecutor object hashcode vs serializable NativeCallExecutor
    private Map<String, SerializableAsyncNativeContext> sAsyncNativeContexts = new HashMap<>();

    // Map of global properties used in context hierarchy
    public HashMap<String, Object> globalProps = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SerializableState(String id, List<WorkerExecutionContext> ctxList,
                             List<State.AsyncNativeContext> asyncNativeContexts) {
        this.id = id;
        HashSet<String> updatedObjectSet = new HashSet<>();
        ctxList.forEach(ctx -> populateContext(ctx, ctx.ip, false, updatedObjectSet));
        asyncNativeContexts.forEach(nativeCtx -> sAsyncNativeContexts
                .put(getObjectKey(nativeCtx), new SerializableAsyncNativeContext(nativeCtx.nativeCallContext,
                                                                                 nativeCtx.respCtx, this,
                                                                                 updatedObjectSet)));
    }

    public SerializableState(String id, WorkerExecutionContext executionContext) {
        this.id = id;
        HashSet<String> updatedObjectSet = new HashSet<>();
        populateContext(executionContext, executionContext.ip, false, updatedObjectSet);
    }

    /**
     * Create a checkpoint on runtime state.
     *
     * @param ctx Worker Execution context to be updated
     * @param ip  Instruction point
     * @return Updated serialized state as a string
     */
    public synchronized String checkPoint(WorkerExecutionContext ctx, int ip) {
        populateContext(ctx, ip, true, new HashSet<>());
        cleanCompletedRespContexts();
        return serialize();
    }

    /**
     * Register new worker execution contexts in the serialization state.
     *
     * @param parentCtx Parent worker execution context
     * @param ctxList   List of worker execution contexts to be updated
     */
    public synchronized void registerContexts(WorkerExecutionContext parentCtx, List<WorkerExecutionContext> ctxList) {
        // Since all ctx list have one parent, we can update the parent recursively at once.
        HashSet<String> updatedObjectSet = new HashSet<>();
        SerializableContext sParentCtx = populateContext(parentCtx, parentCtx.ip, true, updatedObjectSet);
        ctxList.forEach(ctx -> {
            SerializableContext sCtx = new SerializableContext(getObjectKey(ctx), ctx, this,
                                                               ctx.ip + 1, false, updatedObjectSet);
            sCurrentCtxKeys.add(sCtx.ctxKey);
        });
        if (!ctxList.isEmpty() && ctxList.get(0).respCtx instanceof AsyncInvocableWorkerResponseContext) {
            sParentCtx.ip++;
        } else {
            sCurrentCtxKeys.remove(sParentCtx.ctxKey);
        }
        cleanCompletedRespContexts();
    }

    public synchronized void registerAsyncNativeContext(Context nativeCtx, WorkerResponseContext respCtx) {
        WorkerExecutionContext parentCtx = nativeCtx.getParentWorkerExecutionContext();
        populateContext(parentCtx, parentCtx.ip + 1, true, new HashSet<>());
        sAsyncNativeContexts.put(getObjectKey(nativeCtx), new SerializableAsyncNativeContext(nativeCtx, respCtx,
                                                                                             this, new HashSet<>()));
        NativeCallableUnit nativeCallable = nativeCtx.getCallableUnitInfo().getNativeCallableUnit();
        if (nativeCallable instanceof InterruptibleNativeCallableUnit
                && ((InterruptibleNativeCallableUnit) nativeCallable)
                .persistBeforeOperation()) {
            PersistenceStore.getStorageProvider().persistState(this.id, serialize());
        }
    }

    public synchronized void removeAsyncNativeContext(Context nativeCtx, WorkerResponseContext respCtx) {
        sAsyncNativeContexts.remove(getObjectKey(nativeCtx));
        populateRespContext(respCtx);
        NativeCallableUnit nativeCallable = nativeCtx.getCallableUnitInfo().getNativeCallableUnit();
        if (nativeCallable instanceof InterruptibleNativeCallableUnit &&
                ((InterruptibleNativeCallableUnit) nativeCallable).persistAfterOperation()) {
            PersistenceStore.getStorageProvider().persistState(id, serialize());
        }
    }

    public synchronized void handleWorkerHalt(WorkerExecutionContext ctx) {
        String ctxKey = getObjectKey(ctx);
        SerializableContext sCtx = sContexts.get(ctxKey);
        if (sCtx != null) {
            removeContextData(ctxKey, sCtx);
            populateRespContext(ctx.respCtx);
            if (ctx.parent != null && !ctx.parent.state.equals(WorkerState.DONE)) {
                if (sCtx.type.equals(SerializableContext.Type.WORKER)) {
                    SerializableContext parentCtx = sContexts.get(sCtx.parentCtxKey);
                    parentCtx.update(ctx.parent, this, true, new HashSet<>());
                }
            }
            if (sContexts.size() <= 1 && (ctx.parent == null || ctx.parent.isRootContext() || ctx.parent.state.equals
                    (WorkerState.DONE))) {
                BLangScheduler.cleanCompletedState(this.id);
            }
        }
    }

    public synchronized void handleWorkerReturn(WorkerExecutionContext ctx) {
        String ctxKey = getObjectKey(ctx);
        SerializableContext sCtx = sContexts.get(ctxKey);
        if (sCtx != null) {
            removeContextData(ctxKey, sCtx);
            populateRespContext(ctx.respCtx);
            if (ctx.parent != null && !ctx.parent.state.equals(WorkerState.DONE)) {
                if (sCtx.type.equals(SerializableContext.Type.WORKER)) {
                    SerializableContext parentCtx = sContexts.get(sCtx.parentCtxKey);
                    parentCtx.update(ctx.parent, this, parentCtx.ip + 1, true, new HashSet<>());
                    sCurrentCtxKeys.add(parentCtx.ctxKey);
                }
            }
            if (sContexts.size() <= 1 && (ctx.parent == null || ctx.parent.isRootContext() ||
                    ctx.parent.state.equals(WorkerState.DONE))) {
                BLangScheduler.cleanCompletedState(this.id);
            }
        }
    }

    public synchronized void handleWorkerStop(WorkerExecutionContext ctx) {
        String ctxKey = getObjectKey(ctx);
        SerializableContext sCtx = sContexts.get(ctxKey);
        if (sCtx != null) {
            removeContextData(ctxKey, sCtx);
            String respCtxKey = getObjectKey(ctx.respCtx);
            SerializableResponseContext sRespCtx = sRespContexts.get(respCtxKey);
            if (sRespCtx instanceof SerializableAsyncResponse) {
                if (!((SerializableAsyncResponse) sRespCtx).cancelled) {
                    populateRespContext(ctx.respCtx);
                }
            }
        }
    }

    private void removeContextData(String ctxKey, SerializableContext sCtx) {
        removeRefTypes(sCtx);
        sCurrentCtxKeys.remove(ctxKey);
        sContexts.remove(ctxKey);

    }

    private SerializableContext populateContext(WorkerExecutionContext ctx, int ip, boolean updateParent,
                                                HashSet<String> updatedObjectSet) {
        String ctxKey = getObjectKey(ctx);
        SerializableContext sCtx = sContexts.get(ctxKey);
        if (sCtx != null) {
            sCtx.update(ctx, this, ip, updateParent, updatedObjectSet);
        } else {
            sCtx = new SerializableContext(ctxKey, ctx, this, ip, updateParent, updatedObjectSet);
        }
        sCurrentCtxKeys.add(ctxKey);
        if (!isAsync(ctx)) {
            sCurrentCtxKeys.remove(sCtx.parentCtxKey);
        }
        return sCtx;
    }

    void registerContext(SerializableContext sCtx) {
        sContexts.put(sCtx.ctxKey, sCtx);
    }

    /**
     * Provides worker execution contexts which will be reschedule to recover the state.
     *
     * @param programFile  Program file
     * @param deserializer Deserializer
     * @return List of worker execution contexts
     */
    public synchronized List<WorkerExecutionContext> getExecutionContexts(ProgramFile programFile,
                                                                          Deserializer deserializer) {
        return sCurrentCtxKeys
                .stream()
                .map(sCtxKey -> sContexts.get(sCtxKey).getWorkerExecutionContext(programFile, this, deserializer))
                .collect(Collectors.toList());

    }

    public synchronized List<State.AsyncNativeContext> getAsyncNativeContexts(ProgramFile programFile,
                                                                              Deserializer deserializer) {
        return sAsyncNativeContexts.values()
                                   .stream()
                                   .map(sCtx -> sCtx.getAsyncNativeContext(programFile, deserializer, this))
                                   .collect(Collectors.toList());
    }

    public SerializableResponseContext populateRespContext(WorkerResponseContext respCtx,
                                                           HashSet<String> updatedObjectSet) {
        String respCtxKey = getObjectKey(respCtx);
        SerializableResponseContext sRespCtx = sRespContexts.get(respCtxKey);
        if (!updatedObjectSet.contains(respCtxKey)) {
            if (sRespCtx != null) {
                sRespCtx.update(respCtx, this, updatedObjectSet);
            } else {
                sRespCtx = addRespContext(respCtxKey, respCtx, updatedObjectSet);
            }
            updatedObjectSet.add(respCtxKey);

        }
        return sRespCtx;
    }

    private void populateRespContext(WorkerResponseContext respCtx) {
        String respCtxKey = getObjectKey(respCtx);
        SerializableResponseContext sRespCtx = sRespContexts.get(respCtxKey);
        if (sRespCtx != null) {
            sRespCtx.update(respCtx, this, new HashSet<>());
        } else {
            addRespContext(respCtxKey, respCtx, new HashSet<>());
        }
    }

    private SerializableResponseContext addRespContext(String respCtxKey, WorkerResponseContext respCtx,
                                                       HashSet<String> updatedObjectSet) {
        SerializableResponseContext sRespCtx = SerializableResponseContextFactory
                .getResponseContext(respCtxKey, respCtx, this, updatedObjectSet);
        sRespContexts.put(sRespCtx.getRespCtxKey(), sRespCtx);
        WorkerExecutionContext targetCtx = respCtx.getTargetContext();
        if (targetCtx != null && !targetCtx.state.equals(WorkerState.DONE)) {
            sRespCtx.addTargetContexts(respCtx, this);
        }
        return sRespCtx;
    }

    public WorkerExecutionContext getExecutionContext(String ctxKey, ProgramFile programFile,
                                                      Deserializer deserializer) {
        SerializableContext serializableContext = sContexts.get(ctxKey);
        if (serializableContext != null) {
            return serializableContext.getWorkerExecutionContext(programFile, this, deserializer);
        }
        return null;
    }

    public WorkerResponseContext getResponseContext(String respCtxKey, ProgramFile programFile,
                                                    CallableUnitInfo callableUnitInfo,
                                                    Deserializer deserializer) {
        WorkerResponseContext responseContext = deserializer.getRespContexts().get(respCtxKey);
        if (responseContext != null) {
            return responseContext;
        }
        SerializableResponseContext sRespContext = sRespContexts.get(respCtxKey);
        if (sRespContext != null) {
            responseContext = sRespContext.getResponseContext(programFile, callableUnitInfo, this, deserializer);
            deserializer.getRespContexts().put(respCtxKey, responseContext);
        }
        return responseContext;
    }

    public String serialize() {
        return Serializer.getJsonSerializer().serialize(this);
    }

    ArrayList<Object> serializeRefFields(BRefType[] bRefFields, HashSet<String> updatedObjectSet) {
        if (bRefFields == null) {
            return null;
        }
        ArrayList<Object> refFields = new ArrayList<>(bRefFields.length);
        for (int i = 0; i < bRefFields.length; i++) {
            BRefType refType = bRefFields[i];
            refFields.add(i, serialize(refType, updatedObjectSet));
        }
        return refFields;
    }

    BRefType[] deserializeRefFields(List<Object> sRefFields, ProgramFile programFile, Deserializer deserializer) {
        if (sRefFields == null) {
            return null;
        }
        BRefType[] bRefFields = new BRefType[sRefFields.size()];
        for (int i = 0; i < sRefFields.size(); i++) {
            Object s = sRefFields.get(i);
            bRefFields[i] = (BRefType) deserialize(s, programFile, deserializer);
        }
        return bRefFields;
    }

    public Object serialize(Object o, HashSet<String> updatedObjectSet) {
        if (o == null || Serializer.isSerializable(o)) {
            return o;
        } else {
            if (o instanceof Serializable) {
                return addRefType((Serializable) o, updatedObjectSet);
            } else {
                return null;
            }
        }
    }

    public Object deserialize(Object o, ProgramFile programFile, Deserializer deserializer) {
        if (o instanceof SerializedKey) {
            SerializedKey serializedKey = (SerializedKey) o;
            BRefType bRefType = deserializer.getRefTypes().get(serializedKey.key);
            if (bRefType != null) {
                return bRefType;
            } else {
                SerializableRefType sRefType = sRefTypes.get(serializedKey.key);
                bRefType = sRefType.getBRefType(programFile, this, deserializer);
                deserializer.getRefTypes().put(serializedKey.key, bRefType);
                sRefType.setContexts(bRefType, programFile, this, deserializer);
                return bRefType;
            }
        } else {
            return o;
        }
    }

    public SerializableContext getSerializableContext(String ctxKey) {
        return sContexts.get(ctxKey);
    }

    private SerializedKey addRefType(Serializable serializable, HashSet<String> updatedObjectSet) {
        String refKey = getObjectKey(serializable);
        if (!updatedObjectSet.contains(refKey)) {
            updatedObjectSet.add(refKey);
            SerializableRefType sRefType = serializable.serialize(this, updatedObjectSet);
            if (sRefType != null) {
                sRefTypes.put(refKey, sRefType);
                return new SerializedKey(refKey);
            }
            return null;
        } else {
            return new SerializedKey(refKey);
        }
    }

    private void cleanCompletedRespContexts() {
        Set<String> runningRespCtx = sContexts.values()
                                              .stream()
                                              .map(sCtx -> sCtx.respCtxKey)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toSet());
        sRespContexts.entrySet()
                     .removeIf(respCtx -> !(respCtx.getValue() instanceof SerializableAsyncResponse) &&
                             !runningRespCtx.contains(respCtx.getKey()));
    }

    private void removeRefTypes(SerializableContext ctx) {
        if (ctx.workerLocal != null && ctx.workerLocal.refFields != null) {
            ctx.workerLocal.refFields.stream()
                                     .filter(o -> o instanceof SerializedKey)
                                     .forEach(o -> removeRefType((SerializedKey) o));
        }
        if (ctx.workerResult != null && ctx.workerResult.refFields != null) {
            ctx.workerResult.refFields.stream()
                                      .filter(o -> o instanceof SerializedKey)
                                      .forEach(o -> removeRefType((SerializedKey) o));
        }
    }

    private void removeRefType(SerializedKey sKey) {
        SerializableRefType refType = sRefTypes.remove(sKey.key);
        if (refType instanceof SerializableBFuture) {
            SerializableBFuture bFuture = (SerializableBFuture) refType;
            SerializableAsyncResponse sAsyncResp = (SerializableAsyncResponse) sRespContexts.get(bFuture.respCtxKey);
            if (sAsyncResp.isDone()) {
                sRespContexts.remove(bFuture.respCtxKey);
            }
        }
    }

    public boolean isAsync(WorkerExecutionContext ctx) {
        return ctx.respCtx instanceof AsyncInvocableWorkerResponseContext;
    }

    public String getObjectKey(Object o) {
        return String.valueOf(System.identityHashCode(o));
    }
}
