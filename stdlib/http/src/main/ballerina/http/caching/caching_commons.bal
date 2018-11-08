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


// Cache-control directives
# Forces the cache to validate a cached response with the origin server before serving.
@final public string NO_CACHE = "no-cache";

# Instructs the cache to not store a response in non-volatile storage.
@final public string NO_STORE = "no-store";

# Instructs intermediaries not to transform the payload.
@final public string NO_TRANSFORM = "no-transform";

# When used in requests, `max-age` implies that clients are not willing to accept responses whose age is greater
# than `max-age`. When used in responses, the response is to be considered stale after the specified
# number of seconds.
@final public string MAX_AGE = "max-age";


// Request only cache-control directives
# Indicates that the client is willing to accept responses which have exceeded their freshness lifetime by no more
# than the specified number of seconds.
@final public string MAX_STALE = "max-stale";

# Indicates that the client is only accepting responses whose freshness lifetime >= current age + min-fresh.
@final public string MIN_FRESH = "min-fresh";

# Indicates that the client is only willing to accept a cached response. A cached response is served subject to
# other constraints posed by the request.
@final public string ONLY_IF_CACHED = "only-if-cached";


// Response only cache-control directives
# Indicates that once the response has become stale, it should not be reused for subsequent requests without
# validating with the origin server.
@final public string MUST_REVALIDATE = "must-revalidate";

# Indicates that any cache may store the response.
@final public string PUBLIC = "public";

# Indicates that the response is intended for a single user and should not be stored by shared caches.
@final public string PRIVATE = "private";

# Has the same semantics as `must-revalidate`, except that this does not apply to private caches.
@final public string PROXY_REVALIDATE = "proxy-revalidate";

# In shared caches, `s-maxage` overrides the `max-age` or `expires` header field.
@final public string S_MAX_AGE = "s-maxage";

// Other constants
# Setting this as the `max-stale` directives indicates that the `max-stale` directive does not specify a limit.
@final public int MAX_STALE_ANY_AGE = 9223372036854775807;

# Configures cache control directives for a `Request`.
#
# + noCache - Sets the `no-cache` directive
# + noStore - Sets the `no-store` directive
# + noTransform - Sets the `no-transform` directive
# + onlyIfCached - Sets the `only-if-cached` directive
# + maxAge - Sets the `max-age` directive
# + maxStale - Sets the `max-stale` directive
# + minFresh - Sets the `min-fresh` directive
public type RequestCacheControl object {

    public boolean noCache = false;
    public boolean noStore = false;
    public boolean noTransform = false;
    public boolean onlyIfCached = false;
    public int maxAge = -1;
    public int maxStale = -1;
    public int minFresh = -1;

    # Builds the cache control directives string from the current `RequestCacheControl` configurations.
    #
    # + return - The cache control directives string to be used in the `cache-control` header
    public function buildCacheControlDirectives () returns string {
        string[] directives = [];
        int i = 0;

        if (noCache) {
            directives[i] = NO_CACHE;
            i = i + 1;
        }

        if (noStore) {
            directives[i] = NO_STORE;
            i = i + 1;
        }

        if (noTransform) {
            directives[i] = NO_TRANSFORM;
            i = i + 1;
        }

        if (onlyIfCached) {
            directives[i] = ONLY_IF_CACHED;
            i = i + 1;
        }

        if (maxAge >= 0) {
            directives[i] = MAX_AGE + "=" + maxAge;
            i = i + 1;
        }

        if (maxStale == MAX_STALE_ANY_AGE) {
            directives[i] = MAX_STALE;
            i = i + 1;
        } else if (maxStale >= 0) {
            directives[i] = MAX_STALE + "=" + maxStale;
            i = i + 1;
        }

        if (minFresh >= 0) {
            directives[i] = MIN_FRESH + "=" + minFresh;
            i = i + 1;
        }

        return buildCommaSeparatedString(directives);
    }
};

# Configures cache control directives for a `Response`.
#
# + mustRevalidate - Sets the `must-revalidate` directive
# + noCache - Sets the `no-cache` directive
# + noStore - Sets the `no-store` directive
# + noTransform - Sets the `no-transform` directive
# + isPrivate - Sets the `private` and `public` directives
# + proxyRevalidate - Sets the `proxy-revalidate` directive
# + maxAge - Sets the `max-age` directive
# + sMaxAge - Sets the `s-maxage` directive
# + noCacheFields - Optional fields for the `no-cache` directive. Before sending a listed field in a response, it
#                   must be validated with the origin server.
# + privateFields - Optional fields for the `private` directive. A cache can omit the fields specified and store
#                   the rest of the response.
public type ResponseCacheControl object {

    public boolean mustRevalidate = false;
    public boolean noCache = false;
    public boolean noStore = false;
    public boolean noTransform = false;
    public boolean isPrivate = false;
    public boolean proxyRevalidate = false;
    public int maxAge = -1;
    public int sMaxAge = -1;
    public string[] noCacheFields = [];
    public string[] privateFields = [];

    # Builds the cache control directives string from the current `ResponseCacheControl` configurations.
    #
    # + return - The cache control directives string to be used in the `cache-control` header
    public function buildCacheControlDirectives () returns string {
        string[] directives = [];
        int i = 0;

        if (self.mustRevalidate) {
            directives[i] = MUST_REVALIDATE;
            i = i + 1;
        }

        if (self.noCache) {
            directives[i] = NO_CACHE + appendFields(self.noCacheFields);
            i = i + 1;
        }

        if (self.noStore) {
            directives[i] = NO_STORE;
            i = i + 1;
        }

        if (self.noTransform) {
            directives[i] = NO_TRANSFORM;
            i = i + 1;
        }

        if (self.isPrivate) {
            directives[i] = PRIVATE + appendFields(self.privateFields);
        } else {
            directives[i] = PUBLIC;
        }
        i = i + 1;

        if (self.proxyRevalidate) {
            directives[i] = PROXY_REVALIDATE;
            i = i + 1;
        }

        if (self.maxAge >= 0) {
            directives[i] = MAX_AGE + "=" + self.maxAge;
            i = i + 1;
        }

        if (self.sMaxAge >= 0) {
            directives[i] = S_MAX_AGE + "=" + self.sMaxAge;
            i = i + 1;
        }

        return buildCommaSeparatedString(directives);
    }
};

function Request::parseCacheControlHeader () {
    // If the request doesn't contain a cache-control header, resort to default cache control settings
    if (!self.hasHeader(CACHE_CONTROL)) {
        return;
    }

    RequestCacheControl reqCC = new;
    string cacheControl = self.getHeader(CACHE_CONTROL);
    string[] directives = cacheControl.split(",");

    foreach directive in directives {
        directive = directive.trim();
        if (directive == NO_CACHE) {
            reqCC.noCache = true;
        } else if (directive == NO_STORE) {
            reqCC.noStore = true;
        } else if (directive == NO_TRANSFORM) {
            reqCC.noTransform = true;
        } else if (directive == ONLY_IF_CACHED) {
            reqCC.onlyIfCached = true;
        } else if (directive.hasPrefix(MAX_AGE)) {
            reqCC.maxAge = getExpirationDirectiveValue(directive);
        } else if (directive == MAX_STALE) {
            reqCC.maxStale = MAX_STALE_ANY_AGE;
        } else if (directive.hasPrefix(MAX_STALE)) {
            reqCC.maxStale = getExpirationDirectiveValue(directive);
        } else if (directive.hasPrefix(MIN_FRESH)) {
            reqCC.minFresh = getExpirationDirectiveValue(directive);
        }
        // non-standard directives are ignored
    }

    self.cacheControl = reqCC;
}

function appendFields (string[] fields) returns string {
    if (fields.length() > 0) {
        return "=\"" + buildCommaSeparatedString(fields) + "\"";
    }
    return "";
}

function buildCommaSeparatedString (string[] values) returns string {
    string delimitedValues = values[0];

    int i = 1;
    while (i < values.length()) {
        delimitedValues = delimitedValues + ", " + values[i];
        i = i + 1;
    }

    return delimitedValues;
}

function getExpirationDirectiveValue (string directive) returns int {
    string[] directiveParts = directive.split("=");

    // Disregarding the directive if a value isn't provided
    if (directiveParts.length() != 2) {
        return -1;
    }

    match <int>directiveParts[1] {
        int age => return age;
        error => return -1; // Disregarding the directive if the value cannot be parsed
    }
}

function setRequestCacheControlHeader(Request request) {
    match request.cacheControl {
        RequestCacheControl cacheControl => {
            if (!request.hasHeader(CACHE_CONTROL)) {
                request.setHeader(CACHE_CONTROL, cacheControl.buildCacheControlDirectives());
            }
        }
        () => {}
    }
}

function setResponseCacheControlHeader(Response response) {
    match response.cacheControl {
        ResponseCacheControl cacheControl => {
            if (!response.hasHeader(CACHE_CONTROL)) {
                response.setHeader(CACHE_CONTROL, cacheControl.buildCacheControlDirectives());
            }
        }
        () => {}
    }
}