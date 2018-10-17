/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.checkpointing;

import org.awaitility.Awaitility;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for interruptible services check-pointing and resume.
 *
 * @since 0.983.0
 */
public class InterruptibleAsyncServiceTestCase extends BaseInterruptibleTest {

    private String balFilePath;

    @BeforeClass
    public void setup() {
        super.setup("ballerina-async-states");
        balFilePath = new File("src" + File.separator + "test" + File.separator +
                                       "resources" + File.separator + "checkpointing" + File.separator +
                                       "interruptibleAsyncService.bal").getAbsolutePath();
    }

    @Test(description = "Checkpoint will be saved and server interrupt before complete the request.")
    public void testCheckpointSuccess() throws IOException, BallerinaTestException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            LogLeecher asyncFuncLog = new LogLeecher("f1 is done: false");
            LogLeecher workersCompletionLog = new LogLeecher("f2 is done: false");
            LogLeecher asyncFunctionCancelLog = new LogLeecher("f3 is cancelled: false");
            LogLeecher w2Log = new LogLeecher("Worker 2 ended with parameter name : worker 2");
            ballerinaServer.addLogLeecher(asyncFuncLog);
            ballerinaServer.addLogLeecher(workersCompletionLog);
            ballerinaServer.addLogLeecher(w2Log);
            ballerinaServer.addLogLeecher(asyncFunctionCancelLog);
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(servicePort, "s1/r1"));
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() > 0);
            workersCompletionLog.waitForText(3000);
            asyncFuncLog.waitForText(3000);
            w2Log.waitForText(3000);
            asyncFunctionCancelLog.waitForText(3000);
        } finally {
            ballerinaServer.killServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 1, "Checkpoint haven't been save during request processing.");
    }

    @Test(description = "Resume the request after server started from last checkPointed state",
          priority = 1)
    public void testCheckpointResumeSuccess() throws BallerinaTestException, IOException {
        BServerInstance ballerinaServer = new BServerInstance(balServer);
        try {
            ballerinaServer.startServer(balFilePath, args, requiredPorts);
            LogLeecher asyncFuncCompletionLog = new LogLeecher("f1 is done: true");
            LogLeecher asyncFuncReturnLog = new LogLeecher("f1 return value: 30");
            LogLeecher workersCompletionLog = new LogLeecher("f2 is done: true");
            LogLeecher asyncFunctionCancelLog = new LogLeecher("f3 is cancelled: true");
            LogLeecher w1Log = new LogLeecher("Worker 1 ended with parameter name : worker 1");
            ballerinaServer.addLogLeecher(asyncFuncCompletionLog);
            ballerinaServer.addLogLeecher(asyncFuncReturnLog);
            ballerinaServer.addLogLeecher(w1Log);
            ballerinaServer.addLogLeecher(workersCompletionLog);
            ballerinaServer.addLogLeecher(asyncFunctionCancelLog);
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(servicePort, "s1/r2"));
            w1Log.waitForText(3000);
            asyncFuncCompletionLog.waitForText(3000);
            asyncFuncReturnLog.waitForText(3000);
            workersCompletionLog.waitForText(3000);
            asyncFunctionCancelLog.waitForText(3000);
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                      .until(() -> fileStorageProvider.getAllSerializedStates().size() == 0);
        } finally {
            ballerinaServer.shutdownServer();
        }
        List<String> allSerializedStates = fileStorageProvider.getAllSerializedStates();
        Assert.assertEquals(allSerializedStates.size(), 0, "Server hasn't resumed the checkpoint and complete it.");
    }

    @AfterTest
    public void cleanup() {
        super.cleanup();
    }
}
