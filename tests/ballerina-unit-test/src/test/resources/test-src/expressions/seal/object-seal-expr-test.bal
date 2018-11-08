
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

//----------------------------Object Seal -------------------------------------------------------------


function sealObjectsV1() returns EmployeeObj {
    PersonObj p = new PersonObj();
    EmployeeObj employee = p.seal(EmployeeObj);

    return employee;
}

function sealObjectsToAny() returns any {
    PersonObj p = new PersonObj();
    any anyValue = p.seal(any);

    return anyValue;
}


