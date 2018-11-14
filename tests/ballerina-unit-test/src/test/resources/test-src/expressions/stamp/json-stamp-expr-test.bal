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

//----------------------------JSON Stamp -------------------------------------------------------------

function stampJSONToAnydata() returns anydata {

    json jsonValue = 3;
    anydata anydataValue = anydata.stamp(jsonValue);

    return anydataValue;
}

function stampJSONToAnydataV2() returns anydata {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    anydata anyValue = anydata.stamp(jsonValue);

    return anyValue;
}

function stampJSONToRecord() returns Employee|error {

    json employee = { name: "John", status: "single", batch: "LK2014" };
    Employee|error employeeValue = Employee.stamp(employee);

    return employeeValue;
}

function stampJSONToRecordV2() returns Employee|error {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    Employee|error employeeValue = Employee.stamp(employee);

    return employeeValue;
}

function stampJSONToJSON() returns json {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json jsonValue = json.stamp(employee);

    return jsonValue;
}

function stampJSONToMap() returns map<anydata>|error {

    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<anydata>|error mapValue = map<anydata>.stamp(employee);

    return mapValue;
}

function stampJSONToMapV2() returns map<anydata>|error {

    json teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
        emp : { name: "John", status: "single", batch: "LK2014"} };
    map<anydata>|error mapValue = map<anydata>.stamp(teacher);

    return mapValue;
}

function stampConstraintJSONToAnydata() returns anydata {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    anydata anydataValue = anydata.stamp(student);

    return anydataValue;
}

function stampConstraintJSONToJSON() returns json {

    json<Student> student = { name: "John" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json jsonValue  = json.stamp(student);

    return jsonValue;
}

function stampConstraintJSONToConstraintJSON() returns json<Person>|error {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    json<Person>|error jsonValue = json<Person>.stamp(student);

    return jsonValue;
}

function stampConstraintJSONToConstraintMapV2() returns map|error {

    json<Student> student = { name: "Jon" };
    student.status = "Single";
    student.batch = "LK2014";
    student.school = "Hindu College";

    map<anydata>|error mapValue = map<anydata>.stamp(student);

    return mapValue;
}

function stampJSONArrayToConstraintArray() returns Student []|error{

    json employeeArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
                            { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    Student []|error studentArray = Student[].stamp(employeeArray);

    return studentArray;
}

function stampJSONArrayToAnyTypeArray() returns anydata []|error{

    json jsonArray =  [1, false, "foo", { first: "John", last: "Pala" }];
    anydata[]|error anydataArray = anydata[].stamp(jsonArray);

    return anydataArray;
}

function stampJSONToAnydataV3() returns anydata {

    json jsonValue = { name: "John", status: "single", batch: "LK2014" };
    anydata anydataValue = anydata.stamp(jsonValue);

    return anydataValue;
}

//----------------------------- Negative Test cases ---------------------------------------------------------------

function stampJSONToRecordNegative() returns Student|error {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    Student|error student = Student.stamp(employee);

    return student;

}

function stampJSONToMapNegative() returns map<string>|error {

    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string>|error mapValue = map<string>.stamp(employee);

    return mapValue;

}
