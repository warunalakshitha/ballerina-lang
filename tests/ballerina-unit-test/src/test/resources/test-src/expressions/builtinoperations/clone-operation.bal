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
public type Person record {
    int id;
    string name;
    float salary;
    string...
};

type Employee record {
    int id;
    string name;
    float salary;
};

type ConstrainedEmp record {
    int id;
    string name;
    float salary;
    !...
};

public function cloneInt() returns (int, int, int) {
    int a = 10;
    int x = a.clone();
    int y = a.clone();
    a = 12;
    y = 13;
    return (a, x, y);
}

public function cloneFloat() returns (float, float, float) {
    float a = 10.01;
    float x = a.clone();
    float y = a.clone();
    a = 12.01;
    y = 13.01;
    return (a, x, y);
}

public function cloneByte() returns (byte, byte, byte) {
    byte a = 100;
    byte x = a.clone();
    byte y = a.clone();
    a = 234;
    y = 133;
    return (a, x, y);
}


public function cloneBoolean() returns (boolean, boolean, boolean) {
    boolean a = true;
    boolean x = a.clone();
    boolean y = a.clone();
    a = false;
    y = true;
    return (a, x, y);
}

public function cloneString() returns (string, string, string) {
    string a = "AAAA";
    string x = a.clone();
    string y = a.clone();
    a = "BBBB";
    y = "CCCC";
    return (a, x, y);
}

public function cloneXML() returns (xml, xml, xml) {
    xml a = xml `<root><name>Alex</name><id>123</id><age>21</age></root>`;
    xml newName = xml `<name>Charlos</name>`;
    xml newId = xml `<id>5000</id>`;
    xml x = a.clone();
    xml y = a.clone();
    a.removeChildren("name");
    a.appendChildren(newName);
    y.removeChildren("id");
    y.appendChildren(newId);
    return (a, x, y);
}

public function cloneMap() returns (map, map, map) {

    map<map<map<json>>> a;
    map<map<json>> b;
    map<json> c;
    c["xxx"] = {"id": 123, "name": "Alex", "age": 21};
    b["yyy"] = c;
    a["zzz"] = b;

    map<map<map<json>>> x = a.clone();
    map<map<map<json>>> y = a.clone();

    a["zzz"]["yyy"]["xxx"].name = "Charlos";
    y["zzz"]["yyy"]["xxx"].id = 5000;

    return (a, x, y);
}

public function cloneTable() returns (table, table, table) {

    Employee e1 = { id: 1, name: "Jane", salary: 300.50 };
    Employee e2 = { id: 2, name: "Anne", salary: 100.50 };
    Employee e3 = { id: 3, name: "John", salary: 400.50 };

    table<Employee> a = table {
        { key id, name, salary },
        [e1, e2]
    };
    table x = a.clone();
    table y = a.clone();
    _ = a.add(e3);
    _ = y.add(e3);
    return (a, x, y);
}

public function cloneJSON() returns (json, json, json) {
    json a = {"name": "Alex", "age": 21, "id": 123, "otherData":[1, "EE", 12.3]};
    json x = a.clone();
    json y = a.clone();

    a["name"] = "Charlos";
    y["id"] = 5000;

    return (a, x, y);
}

public function cloneJSONArray() returns (json, json, json) {
    json a = [1, "EE", 12.3];
    json x = a.clone();
    json y = a.clone();
    a[0] = 100;
    y[2] = 300.5;
    return (a, x, y);
}

public function cloneIntArray() returns (int[], int[], int[]) {
    int[] a = [1, 2, 3];
    int[] x = a.clone();
    int[] y = a.clone();
    a[0] = 100;
    y[2] = 300;
    return (a, x, y);
}

public function cloneFloatArray() returns (float[], float[], float[]) {
    float[] a = [1.0, 2.0, 3.0];
    float[] x = a.clone();
    float[] y = a.clone();
    a[0] = 100.5;
    y[2] = 300.5;
    return (a, x, y);
}

public function cloneByteArray() returns (byte[], byte[], byte[]) {
    byte[] a = [1, 2, 3];
    byte[] x = a.clone();
    byte[] y = a.clone();
    a[0] = 100;
    y[2] = 234;
    return (a, x, y);
}

public function cloneStringArray() returns (string[], string[], string[]) {
    string[] a = ["A", "B", "C"];
    string[] x = a.clone();
    string[] y = a.clone();
    a[0] = "XX";
    y[2] = "YY";
    return (a, x, y);
}

public function cloneUnionArray() returns (any[], any[], any[]) {
    (int|string|float)[] a = [1, "EE", 12.3];
    (int|string|float)[] x = a.clone();
    (int|string|float)[] y = a.clone();
    a[0] = 100;
    y[2] = 300.5;
    return (a, x, y);
}

public function cloneUnion() returns (any, any, any) {
    int|string|float a = 1;
    int|string|float x = a.clone();
    int|string|float y = a.clone();
    a = 100;
    y = 300.5;
    return (a, x, y);
}

public function cloneConstrainedJSON() returns (json, json, json) {
    json<ConstrainedEmp> a = { id: 1, name: "Jane", salary: 300.50 };
    json<ConstrainedEmp> x = a.clone();
    json<ConstrainedEmp> y = a.clone();
    a.name = "Charlos";
    y.salary = 400.5;
    return (a, x, y);
}

public function cloneNilableInt() returns (any, any, any, any, any, any) {
    int? a = 10;
    int? b = ();
    int? x = a.clone();
    int? y = a.clone();
    int? xx = b.clone();
    int? yy = b.clone();
    a = 4;
    y = 5;
    b = 4;
    yy = 5;
    return (a, x, y, b, xx, yy);
}

public function cloneTuple() returns ((map, int[]), (map, int[]), (map, int[])) {
    map<int> m;
    m["one"] = 100;
    int[] arr = [200];
    (map<int>, int[]) a = (m, arr);
    (map, int[]) x = a.clone();
    (map, int[]) y = a.clone();
    a[1][0] = 400;
    y[1][0] = 500;
    return (a, x, y);
}

public function cloneArrayOfArrays() returns (int[][], int[][], int[][]) {
    int[][] a = [[200]];
    int[][] x = a.clone();
    int[][] y = a.clone();
    a[0][0] = 400;
    y[0][0] = 500;
    return (a, x, y);
}

function getIntValue(int[] val) returns int[] {
    int[] x = val;
    x[0] = x[0] * 2;
    return x;
}

public function cloneReturnValues() returns (int[], int[], int[]) {
    int[] a = [10];
    int[] x = getIntValue(a).clone();
    int[] y = getIntValue(a).clone();
    a[0] = 100;
    y[0] = 1000;
    return (a, x, y);
}

public function cloneAnydataRecord() returns (Person, Person, Person) {
    Person a = {id: 100, name: "Alex", salary: 300.5};
    Person x = a.clone();
    Person y = a.clone();
    a.name = "Charlos";
    y.salary = 400.5;
    return (a, x, y);
}