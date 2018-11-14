/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping JSON type variables.
 *
 * @since 0.983.0
 */
public class JSONStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/json-stamp-expr-test.bal");
    }


    //----------------------------- JSON Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampJSONToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydata");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "3");
        Assert.assertEquals(anydataValue.getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToAnydataV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV2");
        Assert.assertEquals(results.length, 5);
    }

    @Test
    public void testStampJSONToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecord");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
    }

    @Test
    public void testStampJSONToRecordV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecordV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BAnydataType.class);

    }

    @Test
    public void testStampJSONToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
    }

    @Test
    public void testStampJSONToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMap");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "John");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BAnydataType.class);

        Assert.assertEquals(((BMap) mapValue0.get("emp")).size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampConstraintJSONToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampConstraintJSONToAnydata");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BAnydataType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampConstraintJSONToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampConstraintJSONToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampConstraintJSONToConstraintJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampConstraintJSONToConstraintJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals(((BJSONType) mapValue0.getType()).getConstrainedType().getName(), "Person");
        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampConstraintJSONToConstraintMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampConstraintJSONToConstraintMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BAnydataType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BAnydataType.class);

    }


    @Test
    public void testStampJSONArrayToConstraintArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayToConstraintArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Student");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue1.getType().getName(), "Student");

        Assert.assertEquals((mapValue1.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampJSONArrayToAnydataTypeArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayToAnyTypeArray");
        Assert.assertEquals(results.length, 4);
        Assert.assertEquals(results[0].stringValue(), "1");
        Assert.assertEquals(results[0].getType().getClass(), BAnydataType.class);
        Assert.assertEquals(results[1].stringValue(), "false");
        Assert.assertEquals(results[1].getType().getClass(), BAnydataType.class);
        Assert.assertEquals(results[2].stringValue(), "foo");
        Assert.assertEquals(results[2].getType().getClass(), BAnydataType.class);
        Assert.assertEquals(((LinkedHashMap) ((BMap) results[3]).getMap()).size(), 2);
        Assert.assertEquals(results[3].getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToAnydataV3() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV3");
        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(results[0].getType().getClass(), BAnydataType.class);
    }

    //----------------------------------- Negative Test cases ----------------------------------------------------

    @Test
    public void testStampJSONToRecordNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecordNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BError) error).getReason(), "incompatible stamp operation: 'json' value " +
                "cannot be stamped as 'Student'");
    }

    @Test
    public void testStampJSONToMapNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMapNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BError) error).getReason(), "incompatible stamp operation: 'json' value " +
                "cannot be stamped as 'map<string>'");
    }
}
