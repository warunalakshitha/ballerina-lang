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

import ballerina/sql;
import ballerina/h2;

type ResultCount record {
    int COUNTVAL;
};

function getTableCount(string tablePrefix) returns (int) {
    endpoint h2:Client testDB {
        name: "TABLEDB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    sql:Parameter p1 = { sqlType: sql:TYPE_VARCHAR, value: tablePrefix };

    int count = 0;
    table dt = check testDB->select("SELECT count(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME
            like ?", ResultCount, p1);
    while (dt.hasNext()) {
        var ret = <ResultCount>dt.getNext();
        if (ret is ResultCount) {
            count = ret.COUNTVAL;
        } else if (ret is error) {
            count = -1;
        }
    }
    testDB.stop();
    return count;
}

function getSessionCount() returns (int) {
    endpoint h2:Client testDB {
        name: "TABLEDB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    };

    int count;
    table dt = check testDB->select("SELECT count(*) as count FROM information_schema.sessions", ResultCount);
    while (dt.hasNext()) {
        var ret = <ResultCount>dt.getNext();
        if (ret is ResultCount) {
            count = ret.COUNTVAL;
        } else if (ret is error) {
            count = -1;
        }
    }
    testDB.stop();
    return count;
}

