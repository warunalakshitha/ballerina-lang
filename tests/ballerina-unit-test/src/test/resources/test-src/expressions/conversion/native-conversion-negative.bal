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

type Person record {
    string name;
    int age;
    Person? parent;
    json info;
    map<string> address;
    int[] marks;
    anydata a;
    float score;
    boolean alive;
    !...
};

type Student record {
    string name;
    int age;
    !...
};

function testStructToStruct () returns (Student) {
    Person p = {name:"Supun",
                   age:25,
                   parent:{name:"Parent", age:50},
                   address:{"city":"Kandy", "country":"SriLanka"},
                   info:{status:"single"},
                   marks:[24, 81]
               };
    Student s =  Student.from(p);
    return s;
}

type Info record {
    byte[] infoBlob;
    !...
};

function testStructWithIncompatibleTypeToJson () returns (json) {
    Info info = {};
    json j;
    j = check json.from(info);
    return j;
}
