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
import ballerina/http;
import ballerina/mime;

endpoint http:Listener serviceEndpoint2 {
    port: 9102
};

endpoint http:Listener serviceEndpoint3 {
    port: 9103
};
endpoint http:Listener httpsEP {
    port:9104,
    secureSocket: {
        keyStore: {
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

endpoint http:Client endPoint1 {
    url: "http://localhost:9103",
    followRedirects: { enabled: true, maxCount: 3 }
};

endpoint http:Client endPoint2 {
    url: "http://localhost:9103",
    followRedirects: { enabled: true, maxCount: 5 }
};

endpoint http:Client endPoint3 {
    url: "http://localhost:9102",
    followRedirects: { enabled: true }
};

endpoint http:Client endPoint4 {
    url: "http://localhost:9103"
};

endpoint http:Client endPoint5 {
    url: "https://localhost:9104",
    followRedirects: { enabled: true }
};

@http:ServiceConfig {
    basePath: "/service1"
}
service<http:Service> testRedirect bind serviceEndpoint3 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    redirectClient(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint1->get("/redirect1");
        http:Response finalResponse = new;
        if (response is http:Response) {
            finalResponse.setPayload(response.resolvedRequestedURI);
            _ = client->respond(finalResponse);
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/maxRedirect"
    }
    maxRedirectClient(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint1->get("/redirect1/round1");
        if (response is http:Response) {
            string value = "";
            if (response.hasHeader(http:LOCATION)) {
                value = response.getHeader(http:LOCATION);
            }
            value = value + ":" + response.resolvedRequestedURI;
            _ = client->respond(untaint value);
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/crossDomain"
    }
    crossDomain(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2->get("/redirect1/round1");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/noRedirect"
    }
    NoRedirect(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint3->get("/redirect2");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithRelativePath"
    }
    qpWithRelativePath(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2->get("/redirect1/qpWithRelativePath");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithAbsolutePath"
    }
    qpWithAbsolutePath(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2->get("/redirect1/qpWithAbsolutePath");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/originalRequestWithQP"
    }
    originalRequestWithQP(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2->get("/redirect1/round4?key=value&lang=ballerina");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test303"
    }
    test303(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint3->post("/redirect2/test303", "Test value!");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/redirectOff"
    }
    redirectOff(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint4->get("/redirect1/round1");
        if (response is http:Response) {
            string value = "";
            if (response.hasHeader(http:LOCATION)) {
                value = response.getHeader(http:LOCATION);
            }
            value = value + ":" + response.resolvedRequestedURI;
            _ = client->respond(untaint value);
        } else if (response is error) {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/httpsRedirect"
    }
    redirectWithHTTPs(endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint5->get("/redirect3");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = client->respond(untaint value);
            } else if (value is error) {
                io:println("Payload error!");
            }
        } else if (response is error) {
            io:println("Connector error!");
        }
    }
}

@http:ServiceConfig {
    basePath: "/redirect1"
}
service<http:Service> redirect1 bind serviceEndpoint3 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    redirect1(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:9102/redirect2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round1"
    }
    round1(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["/redirect1/round2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round2"
    }
    round2(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_USE_PROXY_305, ["/redirect1/round3"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round3"
    }
    round3(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_MULTIPLE_CHOICES_300, ["/redirect1/round4"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round4"
    }
    round4(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["/redirect1/round5"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round5"
    }
    round5(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_FOUND_302, ["http://localhost:9102/redirect2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithRelativePath"
    }
    qpWithRelativePath(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/processQP?key=value&lang=ballerina"
            ]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithAbsolutePath"
    }
    qpWithAbsolutePath(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, [
                "http://localhost:9103/redirect1/processQP?key=value&lang=ballerina"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/processQP"
    }
    processQP(endpoint client, http:Request req) {
        map<string> paramsMap = req.getQueryParams();
        string returnVal = paramsMap.key + ":" + paramsMap.lang;
        _ = client->respond(untaint returnVal);
    }
}

@http:ServiceConfig {
    basePath: "/redirect2"
}
service<http:Service> redirect2 bind serviceEndpoint2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    redirect2(endpoint client, http:Request req) {
        http:Response res = new;
        res.setPayload("hello world");
        _ = client->respond(res);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test303"
    }
    test303(endpoint client, http:Request req) {
        http:Response res = new;
        _ = client->redirect(res, http:REDIRECT_SEE_OTHER_303, ["/redirect2"]);
    }
}

@http:ServiceConfig {
    basePath:"/redirect3"
}

service<http:Service> redirect3 bind httpsEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    firstRedirect (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_SEE_OTHER_303, ["/redirect3/result"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/result"
    }
    finalResult (endpoint caller, http:Request req) {
        _ = caller -> respond("HTTPs Result");
    }
}
