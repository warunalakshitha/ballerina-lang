/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.anydata;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for `anydata` type.
 *
 * @since 0.985.0
 */
public class AnydataTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/anydata/anydata_test.bal");
    }

    @Test(description = "Test allowed literals for anydata")
    public void testAllowedLiterals() {
        BValue[] returns = BRunUtil.invoke(result, "testLiteralValueAssignment");
        assertEquals(((BInteger) returns[0]).intValue(), 10);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[2]).booleanValue());
        assertEquals(returns[3].stringValue(), "Hello World!");
    }

    @Test(description = "Test allowed types for anydata")
    public void testValueTypesAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypesAssignment");
        assertEquals(((BInteger) returns[0]).intValue(), 10);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[2]).booleanValue());
        assertEquals(returns[3].stringValue(), "Hello World!");
    }

    @Test(description = "Test record types assignment")
    public void testRecordAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordAssignment");

        BMap foo = (BMap) returns[0];
        assertEquals(foo.getType().getName(), "Foo");
        assertEquals(((BInteger) foo.get("a")).intValue(), 20);

        BMap closedFoo = (BMap) returns[1];
        assertEquals(closedFoo.getType().getName(), "ClosedFoo");
        assertEquals(((BInteger) closedFoo.get("ca")).intValue(), 35);
    }

    @Test(description = "Test XML assignment")
    public void testXMLAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLAssignment");

        assertTrue(returns[0] instanceof BXML);
        assertEquals(returns[0].stringValue(), "<book>The Lost World</book>");

        assertTrue(returns[1] instanceof BXML);
        assertEquals(returns[1].stringValue(), "<book>Count of Monte Cristo</book>");
    }

    @Test(description = "Test JSON assignment")
    public void testJSONAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testJSONAssignment");
        assertEquals(returns[0].getType().getTag(), TypeTags.JSON_TAG);
        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
    }

    @Test(description = "Test table assignment")
    public void testTableAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAssignment");
        assertEquals(returns[0].getType().getTag(), TypeTags.TABLE_TAG);
        assertEquals(returns[0].stringValue(), "table<Employee> {index: [], primaryKey: [\"id\"], data: " +
                "[{id:1, name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5}, {id:3, name:\"Jim\", " +
                "salary:330.5}]}");
    }

    @Test(description = "Test map assignment")
    public void testMapAssignment() {
        BRunUtil.invoke(result, "testMapAssignment");
    }

    @Test(description = "Test for maps constrained by anydata")
    public void testConstrainedMaps() {
        BValue[] returns = BRunUtil.invoke(result, "testConstrainedMaps");
        BMap anydataMap = (BMap) returns[0];
        assertEquals(((BInteger) anydataMap.get("int")).intValue(), 1234);
        assertEquals(((BFloat) anydataMap.get("float")).floatValue(), 23.45);
        assertTrue(((BBoolean) anydataMap.get("boolean")).booleanValue());
        assertEquals(anydataMap.get("string").stringValue(), "Hello World");
        assertEquals(((BByte) anydataMap.get("byte")).byteValue(), 10);
        assertEquals(anydataMap.get("xml").stringValue(), "<book>The Lost World</book>");
        assertEquals(anydataMap.get("record").stringValue(), "{a:15}");
        assertEquals(anydataMap.get("map").stringValue(), "{\"foo\":\"foo\", \"bar\":\"bar\"}");
        assertEquals(anydataMap.get("json").stringValue(),
                     "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(anydataMap.get("table").stringValue(), "table<Employee> {index: [], primaryKey: [\"id\"]," +
                " data: [{id:1, name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5}, {id:3, " +
                "name:\"Jim\", salary:330.5}]}");
    }

    @Test(description = "Test array assignment")
    public void testArrayAssignment() {
        BRunUtil.invoke(result, "testArrayAssignment");
    }

    @Test(description = "Test union assignment")
    public void testUnionAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionAssignment");
        assertEquals(returns[0].stringValue(), "hello world!");
        assertEquals(((BInteger) returns[1]).intValue(), 123);
        assertEquals(((BFloat) returns[2]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[3]).booleanValue());
        assertEquals(((BByte) returns[4]).intValue(), 255);
    }

    @Test(description = "Test union assignment for more complex types")
    public void testUnionAssignment2() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionAssignment2");
        assertEquals(returns[0].stringValue(), "hello world!");
        assertEquals(returns[1].stringValue(), "table<Employee> {index: [], primaryKey: [\"id\"], data: [{id:1, " +
                "name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5}, {id:3, name:\"Jim\", " +
                "salary:330.5}]}");
        assertEquals(returns[2].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(returns[3].stringValue(), "<book>The Lost World</book>");
        assertEquals(returns[4].stringValue(), "{a:15}");
        assertEquals(returns[5].stringValue(), "{ca:15}");
        assertEquals(returns[6].stringValue(), "{\"foo\":{a:15}}");
        assertEquals(returns[7].stringValue(), "[\"hello world!\"]");
    }

    @Test(description = "Test tuple assignment")
    public void testTupleAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleAssignment");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[1].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[2].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].stringValue(), "(123, 23.45, true, \"hello world!\", 255)");
        assertEquals(returns[1].stringValue(), "({\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>)");
        assertEquals(returns[2].stringValue(), "([{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>], \"hello world!\")");

        // Verifying nested tuple
        BRefValueArray tuple = (BRefValueArray) returns[3];
        assertEquals(tuple.getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(tuple.stringValue(), "(([{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The Lost " +
                "World</book>], \"hello world!\"), 123, 23.45)");

        BRefValueArray nestedTuple = (BRefValueArray) tuple.get(0);
        assertEquals(nestedTuple.getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(nestedTuple.stringValue(), "([{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>], \"hello world!\")");
    }

    @Test(description = "Test nil assignment")
    public void testNilAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testNilAssignment");
        assertNull(returns[0]);
    }

    @Test(description = "Test finite type assignment")
    public void testFiniteTypeAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAssignment");
        assertEquals(returns[0].stringValue(), "A");
        assertEquals(returns[1].stringValue(), "Z");
        assertEquals(((BInteger) returns[2]).intValue(), 123);
        assertEquals(((BFloat) returns[3]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test anydata array")
    public void testAnydataArray() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataArray", new BValue[]{});
        assertTrue(((BArrayType) returns[0].getType()).getElementType().getTag() == TypeTags.ANYDATA_TAG);
        BRefValueArray adArr = (BRefValueArray) returns[0];

        assertEquals(((BInteger) adArr.get(0)).intValue(), 1234);
        assertEquals(((BFloat) adArr.get(1)).floatValue(), 23.45);
        assertTrue(((BBoolean) adArr.get(2)).booleanValue());
        assertEquals(adArr.get(3).stringValue(), "Hello World!");
        assertEquals(((BByte) adArr.get(4)).byteValue(), 10);
        assertEquals(adArr.get(5).stringValue(), "{a:15}");
        assertEquals(adArr.get(6).stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(adArr.get(7).stringValue(), "<book>The Lost World</book>");
    }

    @Test(description = "Test anydata to value type conversion")
    public void testAnydataToValueTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToValueTypes");
        assertEquals(((BInteger) returns[0]).intValue(), 33);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[2]).booleanValue());
        assertEquals(returns[3].stringValue(), "Hello World!");
    }

    @Test(description = "Test anydata to json conversion")
    public void testAnydataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToJson");
        assertEquals(returns[0].getType().getTag(), TypeTags.JSON_TAG);
        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
    }

    @Test(description = "Test anydata to xml conversion")
    public void testAnydataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToXml");
        assertEquals(returns[0].getType().getTag(), TypeTags.XML_TAG);
        assertEquals(returns[0].stringValue(), "<book>The Lost World</book>");
    }

    @Test(description = "Test anydata to record conversion")
    public void testAnydataToRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToRecord");
        assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[0].stringValue(), "{a:15}");
    }

    @Test(description = "Test anydata to table conversion")
    public void testAnydataToTable() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToTable");
        assertEquals(returns[0].getType().getTag(), TypeTags.TABLE_TAG);
        assertEquals(returns[0].stringValue(), "table<Employee> {index: [], primaryKey: [\"id\"], data: [{id:1, " +
                "name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5}, {id:3, name:\"Jim\", " +
                "salary:330.5}]}");
    }

    @Test(description = "Test anydata to union conversion")
    public void testAnydataToUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToUnion");
        assertEquals(returns[0].getType().getTag(), TypeTags.INT_TAG);
        assertEquals(returns[1].getType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(returns[2].getType().getTag(), TypeTags.STRING_TAG);
        assertEquals(returns[3].getType().getTag(), TypeTags.BOOLEAN_TAG);
        assertEquals(returns[4].getType().getTag(), TypeTags.BYTE_TAG);
        assertEquals(((BInteger) returns[0]).intValue(), 10);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertEquals(returns[2].stringValue(), "hello world!");
        assertTrue(((BBoolean) returns[3]).booleanValue());
        assertEquals(((BByte) returns[4]).intValue(), 255);
    }

    @Test(description = "Test anydata to union conversion for complex types")
    public void testAnydataToUnion2() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToUnion2");
        assertEquals(returns[0].getType().getTag(), TypeTags.JSON_TAG);
        assertEquals(returns[1].getType().getTag(), TypeTags.XML_TAG);
        assertEquals(returns[2].getType().getTag(), TypeTags.TABLE_TAG);
        assertEquals(returns[3].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[4].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[5].getType().getTag(), TypeTags.MAP_TAG);
        assertEquals(returns[6].getType().getTag(), TypeTags.ARRAY_TAG);
        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(returns[1].stringValue(), "<book>The Lost World</book>");
        assertEquals(returns[2].stringValue(), "table<Employee> {index: [], primaryKey: [\"id\"], data: [{id:1, " +
                "name:\"Mary\", salary:300.5}, {id:2, name:\"John\", salary:200.5}, {id:3, name:\"Jim\", " +
                "salary:330.5}]}");
        assertEquals(returns[3].stringValue(), "{a:15}");
        assertEquals(returns[4].stringValue(), "{ca:15}");
        assertEquals(returns[5].stringValue(), "{\"foo\":{a:15}}");
        assertEquals(returns[6].stringValue(), "[{a:15}]");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(), "(int,float,boolean,string,byte)");
        assertEquals(returns[0].stringValue(), "(123, 23.45, true, \"hello world!\", 255)");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple2");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(), "(json,xml)");
        assertEquals(returns[0].stringValue(), "({\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>)");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple3");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(),
                     "((int|float|string|boolean|byte|table|json|xml|ClosedFoo|Foo|map<anydata>|anydata[][]|null," +
                             "string),int,float)");
        assertEquals(returns[0].stringValue(), "(([{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>], \"hello world!\"), 123, 23.45)");
    }

    @Test(description = "Test anydata to nil conversion")
    public void testAnydataToNil() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToNil");
        assertNull(returns[0]);
    }

    @Test(description = "Test anydata to finite type conversion")
    public void testAnydataToFiniteType() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToFiniteType");
        assertEquals(returns[0].stringValue(), "A");
        assertEquals(returns[1].stringValue(), "Z");
        assertEquals(((BInteger) returns[2]).intValue(), 123);
        assertEquals(((BFloat) returns[3]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test(description = "Test type testing on any")
    public void testTypeCheckingOnAny() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTypeCheckingOnAny");

        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        assertEquals(((BArrayType) returns[0].getType()).getElementType().getTag(), TypeTags.ANYDATA_TAG);

        BRefValueArray rets = (BRefValueArray) returns[0];
        assertEquals(((BInteger) rets.get(0)).intValue(), 10);
        assertEquals(((BFloat) rets.get(1)).floatValue(), 23.45);
        assertTrue(((BBoolean) rets.get(2)).booleanValue());
        assertEquals(rets.get(3).stringValue(), "hello world!");
        assertEquals(rets.get(4).stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(rets.get(5).stringValue(), "<book>The Lost World</book>");
        assertEquals(rets.get(6).stringValue(), "{a:15}");
        assertEquals(rets.get(7).stringValue(), "{ca:15}");
    }
}
