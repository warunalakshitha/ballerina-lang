
//----------------------------XML Seal -------------------------------------------------------------
function sealXMLToAny() returns any {

    xml xmlValue = xml `<book>The Lost World</book>`;

    any anyValue = xmlValue.seal(any);
    return anyValue;
}
