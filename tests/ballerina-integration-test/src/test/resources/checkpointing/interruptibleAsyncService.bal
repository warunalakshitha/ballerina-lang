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
        // native sync blocking
        string result1 = testString1.toUpper();
        // native sync non blocking
        runtime:sleep(1);
        // native async blocking
        future<string> future1 =  start  testString1.toUpper();
        // native async non blocking
        future future2 =  start runtime:sleep(1);


        string word= "future";
        io:println("before f3 ");
       
        io:println("future1 called");
        runtime:checkpoint();
        future<string> future2 =  start  word.toUpper();
        io:println("future2 called");
        runtime:checkpoint();
        io:println("checkpointed and waiting..");
        runtime:sleep(5000);
        await future1;
        string result2 = await future2;
        io:println("Async future2" + result2 );
        
        
        while (blockFunction){

        }
       
        string result2 = await future1;
        await future2;
        io:println("State completed");

    }

    r2(endpoint conn, http:Request req) {
        blockFunction = false;
        http:Response res = new;
        var response = conn->respond(res);
    }
}

function sum(int a, int b) returns int {
    while (blockFunction){
    }
    return a + b;
}

