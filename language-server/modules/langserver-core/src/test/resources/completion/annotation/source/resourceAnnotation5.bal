import ballerina/http;

service serviceName on new http:Listener(8080) {
    @http:R
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}