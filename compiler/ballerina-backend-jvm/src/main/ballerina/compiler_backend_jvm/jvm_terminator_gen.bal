// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type TerminatorGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    LabelGenerator labelGen;
    ErrorHandlerGenerator errorGen;
    int lambdaIndex = 0;
    bir:Package module;


    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, LabelGenerator labelGen, 
                            ErrorHandlerGenerator errorGen, bir:Package module) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.labelGen = labelGen;
        self.errorGen = errorGen;
        self.module = module;
    }

    function genTerminator(bir:Terminator terminator, bir:Function func, string funcName,
                           int localVarOffset, int returnVarRefIndex, bir:BType? attachedType) {
        if (terminator is bir:Lock) {
            self.genLockTerm(terminator, funcName);
        } else if (terminator is bir:Unlock) {
            self.genUnlockTerm(terminator, funcName);
        } else if (terminator is bir:GOTO) {
            self.genGoToTerm(terminator, funcName);
        } else if (terminator is bir:Call) {
            self.genCallTerm(terminator, funcName, localVarOffset);
        } else if (terminator is bir:AsyncCall) {
            self.genAsyncCallTerm(terminator, funcName, localVarOffset, attachedType);
        } else if (terminator is bir:Branch) {
            self.genBranchTerm(terminator, funcName);
        } else if (terminator is bir:Return) {
            self.genReturnTerm(terminator, returnVarRefIndex, func);
        } else if (terminator is bir:Panic) {
            self.errorGen.genPanic(terminator);
        } else if (terminator is bir:Wait) {
            self.generateWaitIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WaitAll) {
            self.genWaitAllIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:FPCall) {
            self.genFPCallIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WorkerSend) {
            self.genWorkerSendIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:WorkerReceive) {
            self.genWorkerReceiveIns(terminator, funcName, localVarOffset);
        } else if (terminator is bir:Flush) {
            self.genFlushIns(terminator, funcName, localVarOffset);
        } else {
            error err = error( "JVM generation is not supported for terminator instruction " +
                io:sprintf("%s", terminator));
            panic err;
        }
    }

    function genGoToTerm(bir:GOTO gotoIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + gotoIns.targetBB.id.value);
        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genLockTerm(bir:Lock lockIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + lockIns.lockBB.id.value);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        foreach var globleVar in lockIns.globleVars {
            var varClassName = lookupGlobalVarClassName(currentPackageName + globleVar);
            var lockName = computeLockNameFromString(globleVar);
            self.mv.visitFieldInsn(GETSTATIC, varClassName, lockName, "Ljava/lang/Object;");
            self.mv.visitInsn(MONITORENTER);
        }

        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genUnlockTerm(bir:Unlock unlockIns, string funcName) {
        jvm:Label gotoLabel = self.labelGen.getLabel(funcName + unlockIns.unlockBB.id.value);

        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);

        // unlocked in the same order https://yarchive.net/comp/linux/lock_ordering.html
        foreach var globleVar in unlockIns.globleVars {
            var varClassName = lookupGlobalVarClassName(currentPackageName + globleVar);
            var lockName = computeLockNameFromString(globleVar);
            self.mv.visitFieldInsn(GETSTATIC, varClassName, lockName, "Ljava/lang/Object;");
            self.mv.visitInsn(MONITOREXIT);
        }

        self.mv.visitJumpInsn(GOTO, gotoLabel);
    }

    function genReturnTerm(bir:Return returnIns, int returnVarRefIndex, bir:Function func) {
        bir:BType bType = func.typeValue.retType;
        if (bType is bir:BTypeNil) {
            self.mv.visitInsn(RETURN);
        } else if (bType is bir:BTypeInt) {
            self.mv.visitVarInsn(LLOAD, returnVarRefIndex);
            self.mv.visitInsn(LRETURN);
        } else if (bType is bir:BTypeByte) {
            self.mv.visitVarInsn(ILOAD, returnVarRefIndex);
            self.mv.visitInsn(IRETURN);
        } else if (bType is bir:BTypeFloat) {
            self.mv.visitVarInsn(DLOAD, returnVarRefIndex);
            self.mv.visitInsn(DRETURN);
        } else if (bType is bir:BTypeString) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BTypeBoolean) {
            self.mv.visitVarInsn(ILOAD, returnVarRefIndex);
            self.mv.visitInsn(IRETURN);
        } else if (bType is bir:BMapType ||
                bType is bir:BArrayType ||
                bType is bir:BTypeAny ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
                bType is bir:BTypeAnyData ||
                bType is bir:BObjectType ||
                bType is bir:BServiceType ||
                bType is bir:BTypeDecimal ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BJSONType ||
                bType is bir:BFutureType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeDesc) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BUnionType) {
            self.handleErrorRetInUnion(returnVarRefIndex, func.workerChannels, bType);
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else if (bType is bir:BErrorType) {
            self.notifyChannels(func.workerChannels, returnVarRefIndex);
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitInsn(ARETURN);
        } else {
            error err = error( "JVM generation is not supported for type " +
                            io:sprintf("%s", func.typeValue.retType));
            panic err;
        }
    }

    function handleErrorRetInUnion(int returnVarRefIndex, bir:ChannelDetail[] channels, bir:BUnionType bType) {
        if (channels.length() == 0) {
            return;
        }

        boolean errorIncluded = false;
        foreach var member in bType.members {
            if (member is bir:BErrorType) {
                errorIncluded = true;
                break;
            }
        }

        if (errorIncluded) {
            self.mv.visitVarInsn(ALOAD, returnVarRefIndex);
            self.mv.visitVarInsn(ALOAD, 0);
            loadChannelDetails(self.mv, channels);
            self.mv.visitMethodInsn(INVOKESTATIC, WORKER_UTILS, "handleWorkerError", 
                io:sprintf("(L%s;L%s;[L%s;)V", REF_VALUE, STRAND, CHANNEL_DETAILS), false);
        }
    }

    function notifyChannels(bir:ChannelDetail[] channels, int retIndex) {
        if (channels.length() == 0) {
            return;
        }

        self.mv.visitVarInsn(ALOAD, 0);
        loadChannelDetails(self.mv, channels);
        self.mv.visitVarInsn(ALOAD, retIndex);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleChannelError", io:sprintf("([L%s;L%s;)V", 
            CHANNEL_DETAILS, ERROR_VALUE), false);
    }

    function genBranchTerm(bir:Branch branchIns, string funcName) {
        string trueBBId = branchIns.trueBB.id.value;
        string falseBBId = branchIns.falseBB.id.value;

        int opIndex = self.getJVMIndexOfVarRef(branchIns.op.variableDcl);
        self.mv.visitVarInsn(ILOAD, opIndex);

        jvm:Label trueBBLabel = self.labelGen.getLabel(funcName + trueBBId);
        self.mv.visitJumpInsn(IFGT, trueBBLabel);

        jvm:Label falseBBLabel = self.labelGen.getLabel(funcName + falseBBId);
        self.mv.visitJumpInsn(GOTO, falseBBLabel);
    }

    function genCallTerm(bir:Call callIns, string funcName, int localVarOffset) {
        string orgName = callIns.pkgID.org;
        string moduleName = callIns.pkgID.name;

        // check for native blocking call
        if (isExternStaticFunctionCall(callIns)) {
            jvm:Label blockedOnExternLabel = new;
            jvm:Label notBlockedOnExternLabel = new;

            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "blockedOnExtern", "Z");
            self.mv.visitJumpInsn(IFEQ, blockedOnExternLabel);

            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitInsn(ICONST_0);
            self.mv.visitFieldInsn(PUTFIELD, "org/ballerinalang/jvm/Strand", "blockedOnExtern", "Z");

            if (callIns.lhsOp.variableDcl is bir:VariableDcl) {
                self.mv.visitVarInsn(ALOAD, localVarOffset);
                self.mv.visitFieldInsn(GETFIELD, "org/ballerinalang/jvm/Strand", "returnValue", "Ljava/lang/Object;");
                addUnboxInsn(self.mv, callIns.lhsOp.typeValue);
                // store return
                self.storeReturnFromCallIns(callIns);
            }

            self.mv.visitJumpInsn(GOTO, notBlockedOnExternLabel);

            self.mv.visitLabel(blockedOnExternLabel);
            // invoke the function
            self.genCall(callIns, orgName, moduleName, localVarOffset);

            // store return
            self.storeReturnFromCallIns(callIns);

            self.mv.visitLabel(notBlockedOnExternLabel);
        } else {
            // invoke the function
            self.genCall(callIns, orgName, moduleName, localVarOffset);

            // store return
            self.storeReturnFromCallIns(callIns);
        }
    }

    private function storeReturnFromCallIns(bir:Call callIns) {
        bir:VariableDcl? lhsOpVarDcl = callIns.lhsOp.variableDcl;

        if (lhsOpVarDcl is bir:VariableDcl) {
            int lhsLndex = self.getJVMIndexOfVarRef(lhsOpVarDcl);
            bir:BType? bType = callIns.lhsOp.typeValue;
            genStoreInsn(self.mv, <bir:BType>bType, lhsLndex);
        }
    }

    private function genCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        if (!callIns.isVirtual) {
            self.genFuncCall(callIns, orgName, moduleName, localVarOffset);
            return;
        }

        bir:VariableDcl selfArg = getVariableDcl(callIns.args[0].variableDcl);
        if (selfArg.typeValue is bir:BObjectType || selfArg.typeValue is bir:BServiceType) {
            self.genVirtualCall(callIns, orgName, moduleName, localVarOffset);
        } else {
            // then this is a function attached to a built-in type
            self.genBuiltinTypeAttachedFuncCall(callIns, orgName, moduleName, localVarOffset);
        }
    }

    private function genFuncCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        string methodName = callIns.name.value;
        self.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodName);
    }

    private function genBuiltinTypeAttachedFuncCall(bir:Call callIns, string orgName, string moduleName, 
                                                    int localVarOffset) {
        string methodLookupName = callIns.name.value;
        int index = methodLookupName.indexOf(".") + 1;
        string methodName = methodLookupName.substring(index, methodLookupName.length());
        self.genStaticCall(callIns, orgName, moduleName, localVarOffset, methodName, methodLookupName);
    }

    private function genStaticCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset, 
                                   string methodName, string methodLookupName) {
        // load strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        string lookupKey = getPackageName(orgName, moduleName) + methodLookupName;
        boolean isExternFunction = isBIRFunctionExtern(lookupKey);
        int argsCount = callIns.args.length();
        int i = 0;
        while (i < argsCount) {
            bir:VarRef? arg = callIns.args[i];
            boolean userProvidedArg = self.visitArg(arg);
            self.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            i += 1;
        }

        string methodDesc = lookupJavaMethodDescription(lookupKey);
        string jvmClass = lookupFullQualifiedClassName(lookupKey);
        self.mv.visitMethodInsn(INVOKESTATIC, jvmClass, cleanupFunctionName(methodName), methodDesc, false);
    }

    private function genVirtualCall(bir:Call callIns, string orgName, string moduleName, int localVarOffset) {
        bir:VariableDcl selfArg = getVariableDcl(callIns.args[0].variableDcl);
        int argIndex = self.getJVMIndexOfVarRef(selfArg);

        // load self
        self.mv.visitVarInsn(ALOAD, argIndex);
        self.mv.visitTypeInsn(CHECKCAST, OBJECT_VALUE);

        // load the strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);

        // load the function name as the second argument
        self.mv.visitLdcInsn(cleanupObjectTypeName(callIns.name.value));

        // create an Object[] for the rest params
        int argsCount = callIns.args.length() - 1;
        // arg count doubled and 'isExist' boolean variables added for each arg.
        self.mv.visitLdcInsn(argsCount * 2);
        self.mv.visitInsn(L2I);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);

        int i = 0;
        int j = 0;
        while (i < argsCount) {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(j);
            self.mv.visitInsn(L2I);
            j += 1;
            // i + 1 is used since we skip the first argument (self)
            bir:VarRef? arg = callIns.args[i + 1];
            boolean userProvidedArg = self.visitArg(arg);

            // Add the to the rest params array
            addBoxInsn(self.mv, arg.typeValue);
            self.mv.visitInsn(AASTORE);

            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(j);
            self.mv.visitInsn(L2I);
            j += 1;

            self.loadBooleanArgToIndicateUserProvidedArg(orgName, moduleName, userProvidedArg);
            addBoxInsn(self.mv, "boolean");
            self.mv.visitInsn(AASTORE);

            i += 1;
        }

        // call method
        string methodDesc = io:sprintf("(L%s;L%s;[L%s;)L%s;", STRAND, STRING_VALUE, OBJECT, OBJECT);
        self.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "call", methodDesc, true);

        bir:BType? returnType = callIns.lhsOp.typeValue;
        if (returnType is ()) {
            self.mv.visitInsn(POP);
        } else {
            addUnboxInsn(self.mv, returnType);
        }
    }

    function loadBooleanArgToIndicateUserProvidedArg(string orgName, string moduleName, boolean userProvided) {
        if isBallerinaBuiltinModule(orgName, moduleName) {
            return;
        }
         // Extra boolean is not gen for extern functions for now until the wrapper function is implemented.
        // We need to refactor this method. I am not sure whether userProvided flag make sense
        if (userProvided) {
            self.mv.visitInsn(ICONST_1);
        } else {
            self.mv.visitInsn(ICONST_0);
        }
    }

    function visitArg(bir:VarRef? arg) returns boolean {
        bir:VarRef argRef = getVarRef(arg);
        if (argRef.variableDcl.name.value.hasPrefix("_")) {
            loadDefaultValue(self.mv, getVarRef(arg).typeValue);
            return false;
        }

        bir:BType bType = argRef.typeValue;
        int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(argRef.variableDcl));
        genLoadInsn(self.mv, bType, argIndex);
        return true;
    }

    function genAsyncCallTerm(bir:AsyncCall callIns, string funcName, int localVarOffset, bir:BType? attachedType) {

        // Load the scheduler from strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));

        //create an object array of args
        self.mv.visitIntInsn(BIPUSH, callIns.args.length() * 2 + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        
        int paramIndex = 1;
        foreach var arg in callIns.args {
            bir:VarRef argRef = getVarRef(arg);
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);

            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(argRef.variableDcl));
            bir:BType bType = argRef.typeValue;

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LLOAD, argIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, argIndex);
            } else if (bType is bir:BTypeDecimal) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeString ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BErrorType ||
                        bType is bir:BObjectType ||
                        bType is bir:BServiceType ||
                        bType is bir:BStreamType ||
                        bType is bir:BTableType ||
                        bType is bir:BMapType ||
                        bType is bir:BRecordType ||
                        bType is bir:BArrayType ||
                        bType is bir:BTupleType ||
                        bType is bir:BFutureType ||
                        bType is bir:BJSONType ||
                        bType is bir:BXMLType ||
                        bType is bir:BFiniteType ||
                        bType is bir:BTypeDesc ||
                        bType is bir:BInvokableType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                                    io:sprintf("%s", argRef.typeValue));
                panic err;
            }
            addBoxInsn(self.mv, bType);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;

            self.loadTrueValueAsArg(paramIndex);
            paramIndex += 1;
        }

        string lambdaName = "$" + funcName + "$lambda$" + self.lambdaIndex + "$";
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        string lookupKey = "";
        if (attachedType is bir:BObjectType) {
            lookupKey = currentPackageName + attachedType.name.value + "." + funcName;
        } else {
            lookupKey = currentPackageName + funcName;
        }
        string methodClass = lookupFullQualifiedClassName(lookupKey);
        bir:BType? futureType = callIns.lhsOp.typeValue;
        bir:BType returnType = bir:TYPE_NIL;
        if (futureType is bir:BFutureType) {
            returnType = futureType.returnType;
        }
        boolean isVoid = returnType is bir:BTypeNil;
        createFunctionPointer(self.mv, methodClass, lambdaName, isVoid, 0);
        lambdas[lambdaName] = callIns;
        self.lambdaIndex += 1;
        
        self.submitToScheduler(callIns.lhsOp, localVarOffset);
    }

    function generateWaitIns(bir:Wait waitInst, string funcName, int localVarOffset) {
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitTypeInsn(NEW, ARRAY_LIST);
        self.mv.visitInsn(DUP);
        self.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, "<init>", "()V", false);

        int i = 0;
        while (i < waitInst.exprList.length()) {
            self.mv.visitInsn(DUP);
            bir:VarRef? futureVal = waitInst.exprList[i];
            if (futureVal is bir:VarRef) {
                generateVarLoad(self.mv, futureVal.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(futureVal.variableDcl));
            }
            self.mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", io:sprintf("(L%s;)Z", OBJECT), true);
            self.mv.visitInsn(POP);
            i += 1;
        }

        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitAny", io:sprintf("(L%s;)L%s$WaitResult;", LIST, STRAND), false);
        bir:VariableDcl tempVar = { typeValue: "any",
                                 name: { value: "waitResult" },
                                 kind: "ARG" };
        int resultIndex = self.getJVMIndexOfVarRef(tempVar);
        self.mv.visitVarInsn(ASTORE, resultIndex);
        
        // assign result if result available
        jvm:Label afterIf = new;
        self.mv.visitVarInsn(ALOAD, resultIndex);
        self.mv.visitFieldInsn(GETFIELD, io:sprintf("%s$WaitResult", STRAND), "done", "Z");
        self.mv.visitJumpInsn(IFEQ, afterIf);
        jvm:Label withinIf = new;
        self.mv.visitLabel(withinIf);
        self.mv.visitVarInsn(ALOAD, resultIndex);
        self.mv.visitFieldInsn(GETFIELD, io:sprintf("%s$WaitResult", STRAND), "result", io:sprintf("L%s;", OBJECT));
        addUnboxInsn(self.mv, waitInst.lhsOp.typeValue);
        generateVarStore(self.mv, waitInst.lhsOp.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(waitInst.lhsOp.variableDcl));

        self.mv.visitLabel(afterIf);
    }

    function genWaitAllIns(bir:WaitAll waitAll, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitTypeInsn(NEW, "java/util/HashMap");
        self.mv.visitInsn(DUP);
        self.mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        int i = 0;
        while (i < waitAll.keys.length()) {
            self.mv.visitInsn(DUP);
            self.mv.visitLdcInsn(waitAll.keys[i]);
            bir:VarRef? futureRef = waitAll.futures[i];
            if (futureRef is bir:VarRef) {
                generateVarLoad(self.mv, futureRef.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(futureRef.variableDcl));
            }
            self.mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
            self.mv.visitInsn(POP);
            i += 1;
        }

        generateVarLoad(self.mv, waitAll.lhsOp.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(waitAll.lhsOp.variableDcl));
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleWaitMultiple", io:sprintf("(L%s;L%s;)V", MAP, MAP_VALUE), false);
    }

    function genFPCallIns(bir:FPCall fpCall, string funcName, int localVarOffset) {
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        if (fpCall.isAsync) {
            // Load the scheduler from strand
            self.mv.visitVarInsn(ALOAD, localVarOffset);
            self.mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER));    
        } else {
            // load function ref, going to directly call the fp
            generateVarLoad(self.mv, fpCall.fp.variableDcl, currentPackageName, 
                self.getJVMIndexOfVarRef(fpCall.fp.variableDcl));
        }
        
        // create an object array of args
        self.mv.visitIntInsn(BIPUSH, fpCall.args.length() * 2 + 1);
        self.mv.visitTypeInsn(ANEWARRAY, OBJECT);
        
        // load strand
        self.mv.visitInsn(DUP);

        // 0th index
        self.mv.visitIntInsn(BIPUSH, 0);

        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitInsn(AASTORE);

        // load args
        int paramIndex = 1;
        foreach var arg in fpCall.args {
            self.mv.visitInsn(DUP);
            self.mv.visitIntInsn(BIPUSH, paramIndex);
            
            int argIndex = self.getJVMIndexOfVarRef(getVariableDcl(arg.variableDcl));
            bir:BType? bType = arg.typeValue;

            if (bType is bir:BTypeInt) {
                self.mv.visitVarInsn(LLOAD, argIndex);
            } else if (bType is bir:BTypeFloat) {
                self.mv.visitVarInsn(DLOAD, argIndex);
            } else if (bType is bir:BTypeDecimal) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else if (bType is bir:BTypeBoolean) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeByte) {
                self.mv.visitVarInsn(ILOAD, argIndex);
            } else if (bType is bir:BTypeString ||
                        bType is bir:BTypeAny ||
                        bType is bir:BTypeAnyData ||
                        bType is bir:BTypeNil ||
                        bType is bir:BUnionType ||
                        bType is bir:BErrorType ||
                        bType is bir:BObjectType ||
                        bType is bir:BServiceType ||
                        bType is bir:BStreamType ||
                        bType is bir:BTableType ||
                        bType is bir:BMapType ||
                        bType is bir:BRecordType ||
                        bType is bir:BArrayType ||
                        bType is bir:BTupleType ||
                        bType is bir:BFutureType ||
                        bType is bir:BJSONType ||
                        bType is bir:BXMLType ||
                        bType is bir:BFiniteType ||
                        bType is bir:BTypeDesc ||
                        bType is bir:BInvokableType) {
                self.mv.visitVarInsn(ALOAD, argIndex);
            } else {
                error err = error( "JVM generation is not supported for type " +
                                                    io:sprintf("%s", arg.typeValue));
                panic err;
            }
            addBoxInsn(self.mv, bType);
            self.mv.visitInsn(AASTORE);
            paramIndex += 1;

            self.loadTrueValueAsArg(paramIndex);
            paramIndex += 1;
        }

        // if async, we submit this to sceduler (worker scenario)
        boolean isVoid = false;
        bir:BType returnType = fpCall.fp.typeValue;
        if (returnType is bir:BInvokableType) {
            isVoid = returnType.retType is bir:BTypeNil;
        } 
        if (fpCall.isAsync) {
            // load function ref now
            generateVarLoad(self.mv, fpCall.fp.variableDcl, currentPackageName, 
                self.getJVMIndexOfVarRef(fpCall.fp.variableDcl));
            self.submitToScheduler(fpCall.lhsOp, localVarOffset);           
        } else if (isVoid) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "accept", io:sprintf("(L%s;)V", OBJECT), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_POINTER, "apply", io:sprintf("(L%s;)L%s;", OBJECT, OBJECT), false);
            // store reult
            int lhsIndex = self.getJVMIndexOfVarRef(getVariableDcl(fpCall.lhsOp.variableDcl));
            bir:BType? lhsType = fpCall.lhsOp.typeValue;
            if (lhsType is bir:BType) {
                addUnboxInsn(self.mv, lhsType);
            }

            bir:VariableDcl? lhsVar = fpCall.lhsOp.variableDcl;
            if (lhsVar is bir:VariableDcl) {
                generateVarStore(self.mv, lhsVar, currentPackageName, lhsIndex);
            }
        }
    }

    function loadTrueValueAsArg(int paramIndex) {
        self.mv.visitInsn(DUP);
        self.mv.visitIntInsn(BIPUSH, paramIndex);
        self.mv.visitInsn(ICONST_1);
        addBoxInsn(self.mv, "boolean");
        self.mv.visitInsn(AASTORE);
    }

    function genWorkerSendIns(bir:WorkerSend ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;", 
            STRING_VALUE, WORKER_DATA_CHANNEL), false);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        generateVarLoad(self.mv, ins.dataOp.variableDcl, currentPackageName, 
            self.getJVMIndexOfVarRef(ins.dataOp.variableDcl));
        addBoxInsn(self.mv, ins.dataOp.typeValue);
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSync) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "sendData", io:sprintf("(L%s;L%s;)V", OBJECT, 
                STRAND), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "syncSendData", io:sprintf("(L%s;L%s;)L%s;", 
                OBJECT, STRAND, OBJECT), false);
            bir:VarRef? lhsOp = ins.lhsOp;
            if (lhsOp is bir:VarRef) {
                generateVarStore(self.mv, lhsOp.variableDcl, currentPackageName, 
                    self.getJVMIndexOfVarRef(lhsOp.variableDcl));
            }      
        } 
    }

    function genWorkerReceiveIns(bir:WorkerReceive ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (!ins.isSameStrand) {
            self.mv.visitFieldInsn(GETFIELD, STRAND, "parent", io:sprintf("L%s;", STRAND));
        }     
        self.mv.visitFieldInsn(GETFIELD, STRAND, "wdChannels", io:sprintf("L%s;", WD_CHANNELS));
        self.mv.visitLdcInsn(ins.channelName.value);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WD_CHANNELS, "getWorkerDataChannel", io:sprintf("(L%s;)L%s;", 
            STRING_VALUE, WORKER_DATA_CHANNEL), false);
 
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, WORKER_DATA_CHANNEL, "tryTakeData", io:sprintf("(L%s;)L%s;", STRAND, OBJECT), false);
        
        bir:VariableDcl tempVar = { typeValue: "any",
                                 name: { value: "wrkMsg" },
                                 kind: "ARG" };
        int wrkResultIndex = self.getJVMIndexOfVarRef(tempVar);
        self.mv.visitVarInsn(ASTORE, wrkResultIndex);

        jvm:Label jumpAfterReceive = new;
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        self.mv.visitJumpInsn(IFNULL, jumpAfterReceive);

        jvm:Label withinReceiveSuccess = new;
        self.mv.visitLabel(withinReceiveSuccess);
        self.mv.visitVarInsn(ALOAD, wrkResultIndex);
        addUnboxInsn(self.mv, ins.lhsOp.typeValue);
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        generateVarStore(self.mv, ins.lhsOp.variableDcl, currentPackageName, self.getJVMIndexOfVarRef(ins.lhsOp.variableDcl));

        self.mv.visitLabel(jumpAfterReceive);
    }

    function genFlushIns(bir:Flush ins, string funcName, int localVarOffset) {
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        loadChannelDetails(self.mv, ins.workerChannels);
        self.mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "handleFlush", 
                io:sprintf("([L%s;)L%s;", CHANNEL_DETAILS, ERROR_VALUE), false);
        
        string currentPackageName = getPackageName(self.module.org.value, self.module.name.value);
        generateVarStore(self.mv, ins.lhsOp.variableDcl, currentPackageName, 
                self.getJVMIndexOfVarRef(ins.lhsOp.variableDcl));
    }
        
    function submitToScheduler(bir:VarRef? lhsOp, int localVarOffset) {
        bir:BType? futureType = lhsOp.typeValue;
        boolean isVoid = false;
        if (futureType is bir:BFutureType) {
            isVoid = futureType.returnType is bir:BTypeNil;
        }
        // load strand
        self.mv.visitVarInsn(ALOAD, localVarOffset);
        if (isVoid) {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "scheduleConsumer",
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
        } else {
            self.mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, "scheduleFunction",
                io:sprintf("([L%s;L%s;L%s;)L%s;", OBJECT, FUNCTION_POINTER, STRAND, FUTURE_VALUE), false);
        }

        // store return
        if (lhsOp is bir:VarRef) {
            bir:VariableDcl? lhsOpVarDcl = lhsOp.variableDcl;
            // store the returned strand as the future
            self.mv.visitVarInsn(ASTORE, self.getJVMIndexOfVarRef(getVariableDcl(lhsOpVarDcl)));
        }
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }
};

function loadChannelDetails(jvm:MethodVisitor mv, bir:ChannelDetail[] channels) {
    mv.visitIntInsn(BIPUSH, channels.length());
    mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
    int index = 0;
    foreach bir:ChannelDetail ch in channels {
        // generating array[i] = new ChannelDetails(name, onSameStrand, isSend);
        mv.visitInsn(DUP);
        mv.visitIntInsn(BIPUSH, index);
        index += 1;

        mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(ch.name.value);

        if (ch.onSameStrand) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        if (ch.isSend) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        mv.visitMethodInsn(INVOKESPECIAL, CHANNEL_DETAILS, "<init>", io:sprintf("(L%s;ZZ)V", STRING_VALUE),
            false);
        mv.visitInsn(AASTORE);
    }
}


function cleanupObjectTypeName(string typeName) returns string {
    int index = typeName.lastIndexOf(".");
    if (index > 0) {
        return typeName.substring(index + 1, typeName.length());
    } else {
        return typeName;
    }
}

function isExternStaticFunctionCall(bir:Call|bir:AsyncCall|bir:FPLoad callIns) returns boolean {
    string methodName;
    string orgName;
    string moduleName;

    if (callIns is bir:Call) {
        if (callIns.isVirtual) {
            return false; 
        }
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    } else if (callIns is bir:AsyncCall) {
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    } else {
        methodName = callIns.name.value;
        orgName = callIns.pkgID.org;
        moduleName = callIns.pkgID.name;
    }

    string key = getPackageName(orgName, moduleName) + methodName;

    if (birFunctionMap.hasKey(key)) {
        BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap[key]);
        return isExternFunc(functionWrapper.func);
    }

    return false;
}

function genStoreInsn(jvm:MethodVisitor mv, bir:BType bType, int localVarIndex) {
    if (bType is bir:BTypeInt) {
        mv.visitVarInsn(LSTORE, localVarIndex);
    } else if (bType is bir:BTypeByte) {
        mv.visitVarInsn(ISTORE, localVarIndex);
    } else if (bType is bir:BTypeFloat) {
        mv.visitVarInsn(DSTORE, localVarIndex);
    } else if (bType is bir:BTypeString) {
        mv.visitVarInsn(ASTORE, localVarIndex);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitVarInsn(ISTORE, localVarIndex);
    } else if (bType is bir:BArrayType ||
                bType is bir:BMapType ||
                bType is bir:BTableType ||
                bType is bir:BStreamType ||
                bType is bir:BErrorType ||
                bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BTypeNil ||
                bType is bir:BObjectType ||
                bType is bir:BServiceType ||
                bType is bir:BTypeDecimal ||
                bType is bir:BUnionType ||
                bType is bir:BRecordType ||
                bType is bir:BTupleType ||
                bType is bir:BFutureType ||
                bType is bir:BJSONType ||
                bType is bir:BXMLType ||
                bType is bir:BInvokableType ||
                bType is bir:BFiniteType ||
                bType is bir:BTypeDesc) {
        mv.visitVarInsn(ASTORE, localVarIndex);
    } else {
        panic error( "JVM generation is not supported for type " +
                                    io:sprintf("%s", bType));
    }
}

function genLoadInsn(jvm:MethodVisitor mv, bir:BType bType, int localVarIndex) {
    if (bType is bir:BTypeInt) {
        mv.visitVarInsn(LLOAD, localVarIndex);
    } else if (bType is bir:BTypeByte) {
        mv.visitVarInsn(ILOAD, localVarIndex);
    } else if (bType is bir:BTypeFloat) {
        mv.visitVarInsn(DLOAD, localVarIndex);
    } else if (bType is bir:BTypeDecimal) {
        mv.visitVarInsn(ALOAD, localVarIndex);
    } else if (bType is bir:BTypeBoolean) {
        mv.visitVarInsn(ILOAD, localVarIndex);
    } else if (bType is bir:BTypeDesc) {
        mv.visitVarInsn(ALOAD, localVarIndex);
        mv.visitTypeInsn(CHECKCAST, TYPEDESC_VALUE);
    } else if (bType is bir:BTypeString ||
                bType is bir:BTypeAny ||
                bType is bir:BTypeAnyData ||
                bType is bir:BTypeNil ||
                bType is bir:BUnionType ||
                bType is bir:BErrorType ||
                bType is bir:BObjectType ||
                bType is bir:BServiceType ||
                bType is bir:BStreamType ||
                bType is bir:BTableType ||
                bType is bir:BMapType ||
                bType is bir:BRecordType ||
                bType is bir:BArrayType ||
                bType is bir:BTupleType ||
                bType is bir:BFutureType ||
                bType is bir:BJSONType ||
                bType is bir:BXMLType ||
                bType is bir:BFiniteType ||
                bType is bir:BInvokableType) {
        mv.visitVarInsn(ALOAD, localVarIndex);
    } else {
        panic error( "JVM generation is not supported for type " + io:sprintf("%s", bType));
    }
}
