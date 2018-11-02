
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
    p.seal(EmployeeObj);

    return p;
}

function sealObjectsToAny() returns any {
    PersonObj p = new PersonObj();
    p.seal(any);

    return p;
}


