/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.transactions;

import org.ballerinalang.bre.bvm.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * {@code TransactionLocalContext} stores the transaction related information.
 *
 * @since 0.964.0
 */
public class TransactionLocalContext {

    private String globalTransactionId;
    private String url;
    private String protocol;

    private int transactionLevel;
    private Map<String, Integer> allowedTransactionRetryCounts;
    private Map<String, Integer> currentTransactionRetryCounts;
    private Map<String, BallerinaTransactionContext> transactionContextStore;
    private Stack<String> transactionBlockIdStack;
    private Stack<TransactionFailure> transactionFailure;
    private static final TransactionResourceManager transactionResourceManager =
            TransactionResourceManager.getInstance();
    private boolean isResourceParticipant;

    private TransactionLocalContext(String globalTransactionId, String url, String protocol) {
        this.globalTransactionId = globalTransactionId;
        this.url = url;
        this.protocol = protocol;
        this.transactionLevel = 0;
        this.allowedTransactionRetryCounts = new HashMap<>();
        this.currentTransactionRetryCounts = new HashMap<>();
        this.transactionContextStore = new HashMap<>();
        transactionBlockIdStack = new Stack<>();
        transactionFailure = new Stack<>();
    }

    public static TransactionLocalContext createTransactionParticipantLocalCtx(String globalTransactionId,
                                                                               String url, String protocol) {
        TransactionLocalContext localContext = new TransactionLocalContext(globalTransactionId, url, protocol);
        localContext.setResourceParticipant(true);
        return localContext;
    }

    public static TransactionLocalContext create(String globalTransactionId, String url, String protocol) {
        return new TransactionLocalContext(globalTransactionId, url, protocol);
    }

    public String getGlobalTransactionId() {
        return this.globalTransactionId;
    }

    public String getCurrentTransactionBlockId() {
        return transactionBlockIdStack.peek();
    }

    public boolean hasTransactionBlock() {
        return !transactionBlockIdStack.empty();
    }

    public String getURL() {
        return this.url;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void beginTransactionBlock(String localTransactionID, int retryCount) {
        transactionBlockIdStack.push(localTransactionID);
        allowedTransactionRetryCounts.put(localTransactionID, retryCount);
        currentTransactionRetryCounts.put(localTransactionID, 0);
        ++transactionLevel;
    }

    public void incrementCurrentRetryCount(String localTransactionID) {
        currentTransactionRetryCounts.putIfAbsent(localTransactionID, 0);
        currentTransactionRetryCounts.computeIfPresent(localTransactionID, (k, v) -> v + 1);
    }

    public BallerinaTransactionContext getTransactionContext(String connectorid) {
        return transactionContextStore.get(connectorid);
    }

    public void registerTransactionContext(String connectorid, BallerinaTransactionContext txContext) {
        transactionContextStore.put(connectorid, txContext);
    }

    /**
     * Is this a retry attempt or initial transaction run.
     *
     * Current retry count = 0 is initial run.
     *
     * @param transactionId transaction block id
     * @return this is a retry runs
     */
    public boolean isRetryAttempt(String transactionId) {
        return  getCurrentRetryCount(transactionId) > 0;
    }

    public boolean isRetryPossible(Strand context, String transactionId) {
        int allowedRetryCount = getAllowedRetryCount(transactionId);
        int currentRetryCount = getCurrentRetryCount(transactionId);
        if (currentRetryCount >= allowedRetryCount) {
            if (currentRetryCount != 0) {
                return false; //Retry count exceeded
            }
        }
        return true;
    }

    public boolean onTransactionFailed(Strand context, String transactionBlockId) {
        if (isRetryPossible(context, transactionBlockId)) {
            transactionContextStore.clear();
            transactionResourceManager.rollbackTransaction(globalTransactionId, transactionBlockId);
            return false;
        } else {
            return true;
        }
    }

    public void notifyLocalParticipantFailure() {
        String bockId = transactionBlockIdStack.peek();
        transactionResourceManager.notifyLocalParticipantFailure(globalTransactionId, bockId);
    }

    public void notifyLocalRemoteParticipantFailure() {
        TransactionResourceManager.getInstance().notifyResourceFailure(globalTransactionId);
    }

    public boolean onTransactionEnd(String transactionBlockId) {
        boolean isOuterTx = false;
        transactionBlockIdStack.pop();
        --transactionLevel;
        if (transactionLevel == 0) {
            transactionResourceManager.endXATransaction(globalTransactionId, transactionBlockId);
            resetTransactionInfo();
            isOuterTx = true;
        }
        return isOuterTx;

    }

    public int getAllowedRetryCount(String localTransactionID) {
        return allowedTransactionRetryCounts.get(localTransactionID);
    }

    private int getCurrentRetryCount(String localTransactionID) {
        return currentTransactionRetryCounts.get(localTransactionID);
    }

    private void resetTransactionInfo() {
        allowedTransactionRetryCounts.clear();
        currentTransactionRetryCounts.clear();
        transactionContextStore.clear();
    }

    public void markFailure() {
        transactionFailure.push(TransactionFailure.at(-1));
    }

    public TransactionFailure getAndClearFailure() {
        if (transactionFailure.empty()) {
            return null;
        }
        TransactionFailure failure = transactionFailure.pop();
        transactionFailure.clear();
        return failure;
    }

    public TransactionFailure getFailure() {
        if (transactionFailure.empty()) {
            return null;
        }
        return transactionFailure.peek();
    }

    public boolean isResourceParticipant() {
        return isResourceParticipant;
    }

    public void setResourceParticipant(boolean resourceParticipant) {
        isResourceParticipant = resourceParticipant;
    }

    /**
     * Carrier for transaction failure information.
     */
    public static class TransactionFailure {
        private final int offendingIp;

        private TransactionFailure(int offendingIp) {
            this.offendingIp = offendingIp;
        }

        private static TransactionFailure at(int offendingIp) {
            return new TransactionFailure(offendingIp);
        }

        public int getOffendingIp() {
            return offendingIp;
        }
    }
}
