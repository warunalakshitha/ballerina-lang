type Employee record {
    string name;
    string status;
    string batch;
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


function sealAnyToJSON() returns json? {

    any anyValue = 3;
    json? jsonValue = anyValue.seal(json);

    return jsonValue;
}

function sealAnyToRecord() returns Employee? {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;
    Employee? employee = anyValue.seal(Employee);
    return employee;
}


function sealAnyToJSONV2() returns json? {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;

    json? jsonValue = anyValue.seal(json);
    return jsonValue;
}

function sealAnyToXML() returns xml? {

    any anyValue = xml `<book>The Lost World</book>`;

    xml? xmlValue = anyValue.seal(xml);
    return xmlValue;
}

function sealAnyToObject() returns PersonObj? {

    any anyValue = new PersonObj();
    PersonObj? personObj = anyValue.seal(PersonObj);

    return personObj;
}

function sealAnyToMap() returns map<Employee>? {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    any anyValue = teacherMap;
    map<Employee>? mapValue = anyValue.seal(map<Employee>);

    return mapValue;
}

function sealAnyToRecordArray() returns Teacher[]? {

    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    any anyValue = teacherArray;
    Teacher[]? returnValue = anyValue.seal(Teacher[]);

    return returnValue;
}

function sealAnyToTuple() returns (string,Teacher)? {

    (string, Teacher)  tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    any anyValue = tupleValue;
    (string,Teacher)? returnValue = anyValue.seal((string, Teacher));

    return returnValue;
}

function sealAnyToAnydata() returns anydata {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    any anyValue = t1;
    anydata anydataValue = anyValue.seal(anydata);
    return anydataValue;
}

function sealAnyObjectToAnydata() returns anydata {

    any anyValue = new PersonObj();
    anydata anydataValue = anyValue.seal(anydata);

    return anydataValue;
}
