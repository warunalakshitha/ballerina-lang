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
import ballerina/grpc;
import ballerina/io;
import ballerina/runtime;

int total = 0;
function testUnaryNonBlockingClient() returns int {
    // Client endpoint configuration
    endpoint HelloWorldClient helloWorldEp {
        url:"http://localhost:9100"
    };
    // Executing unary non-blocking call registering server message listener.
    error? result = helloWorldEp->hello("WSO2", HelloWorldMessageListener);
    match result {
        error payloadError => {
            io:println("Error occured while sending event " + payloadError.reason());
            return total;
        }
        () => {
            io:println("Connected successfully");
        }
    }

    int wait = 0;
    while(total < 2) {
        runtime:sleep(1000);
        io:println("msg count: " + total);
        if (wait > 10) {
            break;
        }
        wait += 1;
    }
    io:println("Client got response successfully.");
    io:println("responses count: " + total);
    return total;
}

// Server Message Listener.
service<grpc:Service> HelloWorldMessageListener {

    // Resource registered to receive server messages
    onMessage(string message) {
        io:println("Response received from server: " + message);
        total = total + 1;
    }

    // Resource registered to receive server error messages
    onError(error err) {
        io:println("Error reported from server: " + err.reason());
    }

    // Resource registered to receive server completed message.
    onComplete() {
        io:println("Server Complete Sending Response.");
        total = total + 1;
    }
}

public type HelloWorldBlockingStub object {

    public grpc:Client clientEndpoint;
    public grpc:Stub stub;


    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function hello(string req, grpc:Headers? headers = ()) returns ((string, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.HelloWorld100/hello", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (<string>result, resHeaders);
            }
        }
    }

    function testInt(int req, grpc:Headers? headers = ()) returns ((int, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.HelloWorld100/testInt", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <int>result, resHeaders);
            }
        }
    }

    function testFloat(float req, grpc:Headers? headers = ()) returns ((float, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.HelloWorld100/testFloat", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <float>result, resHeaders);
            }
        }
    }

    function testBoolean(boolean req, grpc:Headers? headers = ()) returns ((boolean, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.HelloWorld100/testBoolean", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <boolean>result, resHeaders);
            }
        }
    }

    function testStruct(Request req, grpc:Headers? headers = ()) returns ((Response, grpc:Headers)|error) {
        var unionResp = self.stub.blockingExecute("grpcservices.HelloWorld100/testStruct", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                any result;
                grpc:Headers resHeaders;
                (result, resHeaders) = payload;
                return (check <Response>result, resHeaders);
            }
        }
    }
};


public type HelloWorldStub object {

    public grpc:Client clientEndpoint;
    public grpc:Stub stub;


    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function hello(string req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/hello", req, listener, headers = headers);
    }

    function testInt(int req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testInt", req, listener, headers = headers);
    }

    function testFloat(float req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testFloat", req, listener, headers = headers);
    }

    function testBoolean(boolean req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testBoolean", req, listener, headers = headers);
    }

    function testStruct(Request req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        return self.stub.nonBlockingExecute("grpcservices.HelloWorld100/testStruct", req, listener, headers = headers);
    }
};


public type HelloWorldBlockingClient object {

    public grpc:Client client;
    public HelloWorldBlockingStub stub;


    public function init(grpc:ClientEndpointConfig con) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(con);
        self.client = c;
        // initialize service stub.
        HelloWorldBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldBlockingStub) {
        return self.stub;
    }
};


public type HelloWorldClient object {

    public grpc:Client client;
    public HelloWorldStub stub;


    public function init(grpc:ClientEndpointConfig con) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(con);
        self.client = c;
        // initialize service stub.
        HelloWorldStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions() returns (HelloWorldStub) {
        return self.stub;
    }
};

@final string DESCRIPTOR_KEY = "grpcservices.HelloWorld100.proto";
map descriptorMap =
{
    "grpcservices.HelloWorld100.proto":"0A1348656C6C6F576F726C643130302E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F1A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F22490A075265717565737412120A046E616D6518012001280952046E616D6512180A076D65737361676518022001280952076D65737361676512100A036167651803200128035203616765221E0A08526573706F6E736512120A047265737018012001280952047265737032F3030A0D48656C6C6F576F726C6431303012430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512430A0774657374496E74121B2E676F6F676C652E70726F746F6275662E496E74363456616C75651A1B2E676F6F676C652E70726F746F6275662E496E74363456616C756512450A0974657374466C6F6174121B2E676F6F676C652E70726F746F6275662E466C6F617456616C75651A1B2E676F6F676C652E70726F746F6275662E466C6F617456616C756512450A0B74657374426F6F6C65616E121A2E676F6F676C652E70726F746F6275662E426F6F6C56616C75651A1A2E676F6F676C652E70726F746F6275662E426F6F6C56616C7565123B0A0A7465737453747275637412152E6772706373657276696365732E526571756573741A162E6772706373657276696365732E526573706F6E736512450A0D746573744E6F5265717565737412162E676F6F676C652E70726F746F6275662E456D7074791A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C756512460A0E746573744E6F526573706F6E7365121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A162E676F6F676C652E70726F746F6275662E456D707479620670726F746F33",
    
    "google.protobuf.google/protobuf/wrappers.proto":
    "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33",

    "google.protobuf.google/protobuf/empty.proto":
    "0A1B676F6F676C652F70726F746F6275662F656D7074792E70726F746F120F676F6F676C652E70726F746F62756622070A05456D70747942760A13636F6D2E676F6F676C652E70726F746F627566420A456D70747950726F746F50015A276769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F656D707479F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};

type Request record {
    string name;
    string message;
    int age;
};

type Response record {
    string resp;
};