type Student record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type Employee record {
    string name;
    string status;
    string batch;
};

type Person record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type ExtendedEmployee record {
    string name;
    string status;
    string batch;
    Address address;
};

type Address object {
    public int no = 10;
    public string streetName = "Palm Grove";
    public string city = "colombo";
};

//-----------------------Record Seal -------------------------------------------------------------------

function testSealWithOpenRecords() returns Employee {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = t1.seal(Employee);
    return e;
}

function testSealWithOpenRecordsNonAssignable() returns Teacher {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };

    Teacher t = e1.seal(Teacher);
    return t;
}

function testSealClosedRecordWithOpenRecord() returns Employee {
    Person p1 = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee e = p1.seal(Employee);
    return e;
}

function sealRecordToAny() returns any {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = teacher.seal(any);

    return anyValue;
}

function sealRecordToJSON() returns json {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json jsonValue = employee.seal(json);

    return jsonValue;
}

function sealRecordToMap() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map mapValue = employee.seal(map);

    return mapValue;
}

function sealRecordToMapV2() returns map<string> {

    Employee employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = employee.seal(map<string>);

    return mapValue;
}

function sealRecordToMapV3() returns map {

    Employee employee = { name: "John", status: "single", batch: "LK2014" };
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College", emp: employee
    };
    map mapValue = teacher.seal(map);

    return mapValue;
}

function sealRecordToAnydata() returns anydata {
    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = teacher.seal(anydata);

    return anydataValue;
}

function sealExtendedRecordToAny() returns any {
    Address addressObj = new Address();
    ExtendedEmployee employee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    any anyValue = employee.seal(any);

    return anyValue;
}

function sealExtendedRecordToOpenRecord() returns Employee {
    Address addressObj = new Address();
    ExtendedEmployee extendedEmployee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    Employee employee = extendedEmployee.seal(Employee);

    return employee;
}

//-------------------------------- Negative Test cases ------------------------------------------------------------
function sealOpenRecordToMap() returns map<string> {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = teacher.seal(map<string>);

    return mapValue;
}



