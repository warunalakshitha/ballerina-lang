/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * {@code BLangAccessExpression} represents an chained access expression.
 * eg: field access, index-based access.
 * 
 * @since 0.970.0
 */
public abstract class BLangAccessExpression extends BLangValueExpression {

    // BLangNodes
    public BLangExpression expr;

    // Parser Flags and Data
    public boolean optionalFieldAccess = false;

    // Semantic Data
    public boolean errorSafeNavigation = false;
    public boolean nilSafeNavigation = false;
    public BType originalType;
    public boolean leafNode;
}
