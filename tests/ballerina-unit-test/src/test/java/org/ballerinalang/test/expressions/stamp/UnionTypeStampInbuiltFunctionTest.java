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
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for stamping Union type variables.
 *
 * @since 0.983.0
 */
public class UnionTypeStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/union-stamp-expr-test.bal");
    }


    //----------------------------- Union Type Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampUnionToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(employee0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(employee0.getType().getName(), "Employee");

        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampUnionToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToJSON");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(employee0.getType().getClass(), BJSONType.class);

        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToXML");
        BValue xmlValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((xmlValue).getType().getClass(), BXMLType.class);
    }

    @Test
    public void testStampUnionToIntMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToIntMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testStampUnionToConstraintMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToConstraintMap");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 2);

        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BRecordType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getName(), "Employee");

        Assert.assertEquals(employee0.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("age")).getType().getClass(),
                BAnydataType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("school")).getType().getClass(),
                BAnydataType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("batch")).getType().getClass(),
                BStringType.class);


        Assert.assertEquals(employee0.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("age")).getType().getClass(),
                BAnydataType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("school")).getType().getClass(),
                BAnydataType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("batch")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampUnionToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToAnydata");
        BValue stampedValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(stampedValue.getType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampUnionToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToTuple");
        BValue stampedValue0 = results[0];
        BValue stampedValue1 = results[1];

        Assert.assertEquals(stampedValue0.stringValue(), "mohan");
        Assert.assertEquals(stampedValue0.getType().getClass(), BStringType.class);

        Assert.assertEquals(stampedValue1.stringValue(), "LK2014");
        Assert.assertEquals(stampedValue1.getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToAnydataV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampUnionToAnydataV2");
        BValue stampedValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(stampedValue.getType().getClass(), BAnydataType.class);
    }
}

