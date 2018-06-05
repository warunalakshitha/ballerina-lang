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

documentation {
    Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
    to provide checkpointing over HTTP requests to support interruptibility.

    F{{serviceUri}} The URL of the remote HTTP endpoint
    F{{config}} The configurations of the client endpoint associated with this HttpActions instance
    F{{httpClient}}  HTTP client for outbound HTTP requests
}
public type InterruptibleClient object {
    //These properties are populated from the init call to the client connector
    public {
        string serviceUri;
        ClientEndpointConfig config;
        CallerActions httpClient;
    }

    documentation {
        Provides the HTTP actions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
    to provide checkpointing over HTTP requests to support interruptibility.

        P{{serviceUri}} Target service url
        P{{config}}  HTTP ClientEndpointConfig to be used for HTTP client invocation
        P{{httpClient}}  HTTP client for outbound HTTP requests
    }
    public new(serviceUri, config, httpClient) {}

    documentation {
        The `post()` function can be used to send HTTP POST requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function post(string path, Request? request = ()) returns Response|error;

    documentation {
        The `head()` function can be used to send HTTP HEAD requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function head(string path, Request? request = ()) returns Response|error;

    documentation {
        The `put()` function can be used to send HTTP PUT requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function put(string path, Request? request = ()) returns Response|error;

    documentation {
		Invokes an HTTP call with the specified HTTP verb.

        P{{httpVerb}} HTTP verb value
        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function execute(string httpVerb, string path, Request request)
                               returns Response|error;

    documentation {
        The `patch()` function can be used to send HTTP PATCH requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function patch(string path, Request? request = ()) returns Response|error;

    documentation {
        The `delete()` function can be used to send HTTP DELETE requests to HTTP endpoints.

        P{{path}} Resource path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function delete(string path, Request? request = ()) returns Response|error;

    documentation {
        The `get()` function can be used to send HTTP GET requests to HTTP endpoints.

        P{{path}} Request path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function get(string path, Request? request = ()) returns Response|error;

    documentation {
        The `options()` function can be used to send HTTP OPTIONS requests to HTTP endpoints.

        P{{path}} Request path
        P{{request}} An HTTP outbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function options(string path, Request? request = ()) returns Response|error;

    documentation {
        The `forward()` function can be used to invoke an HTTP call with inbound request's HTTP verb

        P{{path}} Request path
        P{{request}} An HTTP inbound request message
        R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
    }
    public function forward(string path, Request request) returns Response|error;

    documentation {
        Submits an HTTP request to a service with the specified HTTP verb.
        The `submit()` function does not give out a `Response` as the result,
        rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.

        P{{httpVerb}} The HTTP verb value
        P{{path}} The resource path
        P{{request}} An HTTP outbound request message
        R{{}} An `HttpFuture` that represents an asynchronous service invocation, or an `error` if the submission fails
    }
    public function submit(string httpVerb, string path, Request request)
                               returns HttpFuture|error;

    documentation {
        Retrieves the `Response` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` related to a previous asynchronous invocation
        R{{}} An HTTP response message, or an `error` if the invocation fails
    }
    public function getResponse(HttpFuture httpFuture) returns Response|error;

    documentation {
        Checks whether a `PushPromise` exists for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} A `boolean` that represents whether a `PushPromise` exists
    }
    public function hasPromise(HttpFuture httpFuture) returns (boolean);

    documentation {
        Retrieves the next available `PushPromise` for a previously submitted request.

        P{{httpFuture}} The `HttpFuture` relates to a previous asynchronous invocation
        R{{}} An HTTP Push Promise message, or an `error` if the invocation fails
    }
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|error;

    documentation {
        Retrieves the promised server push `Response` message.

        P{{promise}} The related `PushPromise`
        R{{}} A promised HTTP `Response` message, or an `error` if the invocation fails
    }
    public function getPromisedResponse(PushPromise promise) returns Response|error;

    documentation {
        Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
        response using the rejected promise.

        P{{promise}} The Push Promise to be rejected
    }
    public function rejectPromise(PushPromise promise);
};

public function InterruptibleClient::post(string path, Request? request = ()) returns Response|error {
    io:println("Checkpointing before post..........");
    Request req = request ?: new;
    match self.httpClient.post(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            io:println("Checkpointing after post..........");
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::head(string path, Request? request = ()) returns Response|error {
    io:println("Checkpointing before head..........");
    Request req = request ?: new;
    match self.httpClient.head(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            io:println("Checkpointing after head..........");
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::put(string path, Request? request = ()) returns Response|error {
    io:println("Checkpointing before put..........");
    Request req = request ?: new;
    match self.httpClient.put(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            io:println("Checkpointing after put..........");
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::forward(string path, Request request) returns Response|error {
    match self.httpClient.forward(path, request) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::execute(string httpVerb, string path, Request request) returns Response|error {
    match self.httpClient.execute(httpVerb, path, request) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::patch(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    match self.httpClient.patch(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::delete(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    match self.httpClient.delete(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::get(string path, Request? request = ()) returns Response|error {
    io:println("Checkpointing before get..........");
    Request req = request ?: new;
    match self.httpClient.get(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            io:println("Checkpointing after get..........");
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::options(string path, Request? request = ()) returns Response|error {
    Request req = request ?: new;
    match self.httpClient.options(path, request = req) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::submit(string httpVerb, string path, Request request) returns HttpFuture|error {
    return self.httpClient.submit(httpVerb, path, request);
}

public function InterruptibleClient::getResponse(HttpFuture httpFuture) returns Response|error {
    match self.httpClient.getResponse(httpFuture) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::hasPromise(HttpFuture httpFuture) returns boolean {
    return self.httpClient.hasPromise(httpFuture);
}

public function InterruptibleClient::getNextPromise(HttpFuture httpFuture) returns PushPromise|error {
    return self.httpClient.getNextPromise(httpFuture);
}

public function InterruptibleClient::getPromisedResponse(PushPromise promise) returns Response|error {
    match self.httpClient.getPromisedResponse(promise) {
        Response inboundResponse => {
            var payload = inboundResponse.getBinaryPayload();
            return inboundResponse;
        }
        error err => return err;
    }
}

public function InterruptibleClient::rejectPromise(PushPromise promise) {
    return self.httpClient.rejectPromise(promise);
}

