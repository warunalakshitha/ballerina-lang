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

type Person object {
    string name;
    int age;
};

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
        future<int> f1 = start sum(10, 20);
        future f2 = start testWorkers();
        runtime:checkpoint();
        io:println("f1 is done: "+ f1.isDone());
        io:println("f2 is done: "+ f2.isDone());
        io:println("Waiting until function unblock...");
        while (blockFunction){

        }
        int x = await f1;
        await f2;
        io:println("f1 return value: " + x);
        io:println("f1 is done: "+ f1.isDone());
        io:println("f2 is done: "+ f2.isDone());
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


function testWorkers() {
    worker w1 {
        Person p = new;
        p.name = "worker 1";
        p.age = 20;
        runtime:checkpoint();
        while (blockFunction){
        }
        io:println("Worker 1 ended with parameter name : " + p.name);
        return;
    }
    worker w2 {
        io:println("Worker 2 Started...");
        Person p = new;
        p.name = "worker 2";
        p.age = 20;
        io:println("Worker 2 ended with parameter name : " + p.name);
    }
}



