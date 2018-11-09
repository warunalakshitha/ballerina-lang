type Employee record {
    string name;
    int age;
    float salary;
};

type Person record {
    string name;
    int age;
};

function sealStreamTypeVariable() returns stream<Person> {

    stream<Employee> employeeStream;
    Employee e1 = { name: "Raja", age: 25, salary: 20000 };
    Employee e2 = { name: "Mohan", age: 45, salary: 10000 };

    stream<Person> personStream = employeeStream.seal(stream<Person>);
    return personStream;
}

function seaWithInvalidNoOrParameters() returns json {

    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    json returnValue = jsonValue.seal(any, 34);

    return returnValue;
}

function sealStringValueToJson() returns json {
    string value = "mohan";
    json jsonValue = value.seal(json);

    return jsonValue;
}

function sealStringValueToAny() returns any {
    string[] stringArray = ["mohan", "mike"];
    any anyValue = stringArray.seal(any);

    return anyValue;
}

function sealJSONToUnion() returns int|float|json {
    json jsonVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    int|float|json unionValue = jsonVar.seal(int|float|json);
    return unionValue;
}

function sealAnyToString() returns string? {
    any value = "mohan";
    string? stringValue = value.seal(string);

    return stringValue;
}


function sealJSONArrayToPrimitiveTypeArray() returns int []{

    json intArray = [1, 2, 3, 4];
    int [] returnArray = intArray.seal(int []);

    return returnArray;
}
