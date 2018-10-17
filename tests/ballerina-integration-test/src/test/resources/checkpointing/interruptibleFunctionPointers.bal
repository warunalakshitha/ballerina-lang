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
        var response = conn->respond(res);
        int result = f1(5, 20);
        io:println("Result final is :" + result);
        io:println("State completed");
    }

    r2(endpoint conn, http:Request req) {
        blockFunction = false;
        http:Response res = new;
        var response = conn->respond(res);
    }
}

function f1(int x, int y) returns int {
    function (int) returns (int) bar1 = option1;
    function (int) returns (int) bar2 = option2;
    if (x < 10) {
        bar1 = option2;
    }
    if (y > 10) {
        bar2 = option1;
    }
    runtime:checkpoint();
    io:println("Waiting on second request");
    while (blockFunction) {

    }
    int resultX = bar1(x);
    int resultY = bar2(y);
    io:println("Result x is :" + resultX);
    io:println("Result y is :" + resultY);
    return resultX + resultY;
}

function option1(int i) returns (int) {
    return i * 4;
}

function option2(int i) returns (int) {
    return i * 2;
}
