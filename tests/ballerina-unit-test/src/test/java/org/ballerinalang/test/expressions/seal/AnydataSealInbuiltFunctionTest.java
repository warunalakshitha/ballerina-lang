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
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANYDATA
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for sealing Anydata type variables.
 *
 * @since 0.983.0
 */
public class AnydataSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/anydata-seal-expr-test.bal");
    }


    //----------------------------- Anydata Seal Test cases ------------------------------------------------------

    @Test
    public void testSealAnydataToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToJSON");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "3");
        Assert.assertEquals(anydataValue.getType().getClass(), BJSONType.class);
    }

    @Test
    public void testSealAnydataToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testSealAnydataToJSONV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToJSONV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 5);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testSealAnydataToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToXML");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anydataValue.getType().getClass(), BXMLType.class);
    }

    @Test
    public void testSealAnydataToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
    }

    @Test
    public void testSealAnydataToRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Teacher");
        Assert.assertEquals(mapValue1.getType().getName(), "Teacher");
    }

    @Test
    public void testSealAnydataToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataToTuple");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.stringValue(), "Mohan");
        Assert.assertEquals(tupleValue1.getType().getClass(), BStringType.class);

        Assert.assertEquals(tupleValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue2.getType().getName(), "Teacher");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).stringValue(), "25");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).getType().getName(), "int");

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testSealAnydataMapToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataMapToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getName(), "int");

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BStringType.class);

    }

    @Test
    public void testSealAnydataMapToAnyMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealAnydataMapToAnyMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue.get("name").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue.get("age").getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue.get("status").getType().getClass(), BAnyType.class);

    }
}

