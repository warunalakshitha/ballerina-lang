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

//-----------------------Tuple Type Seal -------------------------------------------------------------------

function sealTupleValueV1() returns (string, Teacher) {
    (string, Teacher) tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    (string, Teacher) returnValue = tupleValue.seal((string, Teacher));
    return returnValue;
}

function sealTupleValueV2() returns (string, Employee) {
    (string, Teacher) tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    (string, Employee) returnValue = tupleValue.seal((string, Employee));
    return returnValue;
}

function sealTupleToAny() returns any {
    (string, Teacher) tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    any anyValue = tupleValue.seal(any);
    return anyValue;
}

function sealTupleToAnydata() returns anydata {
    (string, Teacher) tupleValue = ("Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" });

    anydata anydataValue = tupleValue.seal(anydata);
    return anydataValue;
}
