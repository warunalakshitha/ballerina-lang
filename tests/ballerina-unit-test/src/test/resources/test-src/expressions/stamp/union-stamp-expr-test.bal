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

type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};

//-----------------------Union Type Stamp -------------------------------------------------------------------

function stampUnionToRecord() returns Employee|error  {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    Employee|error  employee = Employee.stamp(unionVar);
    return employee;
}

function stampUnionToJSON() returns json {
    int|float|json unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json jsonValue = json.stamp(unionVar);
    return jsonValue;
}

function stampUnionToXML() returns xml|error  {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    xml|error  xmlValue = xml.stamp(unionVar);
    return xmlValue;
}


function stampUnionToIntMap() returns map<int>|error  {
    int|float|map<int> unionVar = { "a": 1, "b": 2 };

    map<int>|error  mapValue = map<int>.stamp(unionVar);
    return mapValue;
}

function stampUnionToConstraintMap() returns map<Employee>|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Employee>|error  mapValue = map<Employee>.stamp(unionVar);
    return mapValue;
}

function stampUnionToAnydata() returns anydata {

    int|float|string|boolean unionValue = "mohan";
    anydata anydataValue = anydata.stamp(unionValue);

    return anydataValue;
}

function stampUnionToTuple() returns (string, string)|error  {

    int|float|(string, string) unionVar = ("mohan", "LK2014");
    (string, string)|error  tupleValue = (string, string).stamp(unionVar);

    return tupleValue;
}

function stampUnionToAnydataV2() returns anydata {

    int|float|string|boolean unionValue = "mohan";
    anydata anydataValue = anydata.stamp(unionValue);

    return anydataValue;
}
