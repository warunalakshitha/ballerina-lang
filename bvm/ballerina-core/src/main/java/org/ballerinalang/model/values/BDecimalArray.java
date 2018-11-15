/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The {@code BDecimalArray} represents a decimal array in Ballerina.
 *
 * @since 0.985.0
 */
public class BDecimalArray extends BNewArray {

    private BigDecimal[] values;

    public BDecimalArray(BigDecimal[] values) {
        this.values = values;
        this.size = values.length;
        super.arrayType = new BArrayType(BTypes.typeDecimal);
    }

    public BDecimalArray() {
        values = (BigDecimal[]) newArrayInstance(BigDecimal.class);
        super.arrayType = new BArrayType(BTypes.typeDecimal);
    }

    public BDecimalArray(int size) {
        if (size != -1) {
            this.size = maxArraySize = size;
        }
        values = (BigDecimal[]) newArrayInstance(BigDecimal.class);
        super.arrayType = new BArrayType(BTypes.typeDecimal, size);
    }

    public void add(long index, BigDecimal value) {
        prepareForAdd(index, values.length);
        values[(int) index] = value;
    }

    public BigDecimal get(long index) {
        rangeCheckForGet(index, size);
        return values[(int) index];
    }

    @Override
    public BType getType() {
        return arrayType;
    }

    @Override
    public void grow(int newLength) {
        values = Arrays.copyOf(values, newLength);
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        BDecimalArray decimalArray = new BDecimalArray(Arrays.copyOf(values, values.length));
        decimalArray.size = size;
        refs.put(this, decimalArray);
        return decimalArray;
    }

    @Override
    public String stringValue() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) {
            sj.add(values[i].toString());
        }
        return sj.toString();
    }

    @Override
    public BValue getBValue(long index) {
        return new BDecimal(get(index));
    }
}
