import ballerina/http as httpAlias;

service serviceName on new http:Listener(8080) {
    @
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}