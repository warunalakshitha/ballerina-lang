// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/runtime;
import ballerina/http;
import ballerina/mime;

boolean blockFunction = true;

@http:ServiceConfig {
    basePath: "/s1"
}
service<http:Service> s1 bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/r1"
    }

    @interruptible
    r1(endpoint conn, http:Request req) {
        io:println("Starting flow...");
        http:Response res = new;
        var clientRresponse = conn->respond(res);
        string testString1= "sync";
        string testString2= "async";
        // Native sync blocking
        string result1 = testString1.toUpper();
        // Native sync non blocking
        runtime:sleep(1);
        // Native async blocking
        future<string> future1 =  start  testString2.toUpper();
        // Native async non blocking
        future future2 =  start runtime:sleep(3000);
        runtime:checkpoint();
        while (blockFunction){

        }
        string result2 = await future1;
        await future2;
        io:println("Sync return value: " + result1);
        io:println("Async return value: " + result2);
        io:println("State completed");
    }

    r2(endpoint conn, http:Request req) {
        blockFunction = false;
        http:Response res = new;
        var response = conn->respond(res);
    }
}
