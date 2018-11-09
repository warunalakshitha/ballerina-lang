type Employee record {
    string name;
    int age;
    float salary;
};

type Student record {
    string name;
    int age;
    string batch;
    !...
};

type Person record {
    string name;
    int age;
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

type TeacherObj object {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function sealRecordToXML() returns xml {

    Employee employeeRecord = { name: "Raja", age: 25, salary: 20000 };

    xml xmlValue = employeeRecord.seal(xml);
    return xmlValue;
}

function sealOpenRecordToClosedRecord() returns Employee {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Employee employee = teacher.seal(Employee);

    return employee;
}

function sealClosedRecordToClosedRecord() returns Student {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    Student student = person.seal(Student);

    return student;
}

function sealRecordToObject() returns TeacherObj {

    Teacher teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    TeacherObj returnValue = teacher.seal(TeacherObj);

    return returnValue;
}

function sealClosedRecordToMap() returns map<string> {

    Person person = { name: "Raja", age: 25, batch: "LK2014", school: "Hindu College" };
    map<string> mapValue = person.seal(map<string>);

    return mapValue;
}

function sealRecordToArray() returns string[] {
    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };
    string[] stringArray = e1.seal(string[]);

    return stringArray;
}

function sealRecordToTuple() returns (string, string) {

    Employee e1 = { name: "Raja", status: "single", batch: "LK2014" };
    (string, string) tupleValue = e1.seal((string, string));

    return tupleValue;
}

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

function sealExtendedRecordToAnydata() returns anydata {
    Address addressObj = new Address();
    ExtendedEmployee employee = { name: "Raja", status: "single", batch: "LK2014", address:addressObj};
    anydata anydataValue = employee.seal(anydata);

    return anydataValue;
}
