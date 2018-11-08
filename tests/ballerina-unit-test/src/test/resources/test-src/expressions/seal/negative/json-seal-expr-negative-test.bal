type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};


function sealJSONToXML() returns xml {

    json jsonValue = { name: "Raja", age: 25, salary: 20000 };

    xml xmlValue = jsonValue.seal(xml);
    return xmlValue;
}

function sealJSONToObject() returns EmployeeObj {

    json employee = { name: "John", age: 23 };
    EmployeeObj employeeObj = employee.seal(EmployeeObj);

    return employeeObj;
}

function sealJSONToTuple() returns (string, string) {

    json jsonValue = { name: "Raja", status: "single" };
    (string, string) tupleValue = jsonValue.seal((string, string));

    return tupleValue;
}
