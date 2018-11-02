
function sealAnyToJSON() returns json {

    any anyValue = 3;
    anyValue.seal(json);

    return anyValue;
}

