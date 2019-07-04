/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.sorters;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Item Sorter for the Top Level context.
 * 
 * @since 1.0
 */
public class TopLevelContextSorter extends VariableDefContextItemSorter {
    @Override
    public void sortItems(LSContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
    }

    @Override
    void setPriority(CompletionItem completionItem) {
        String detail = completionItem.getDetail();
        switch (detail) {
            case ItemResolverConstants.B_TYPE:
                completionItem.setSortText(Priority.PRIORITY150.toString());
                break;
            case ItemResolverConstants.PACKAGE_TYPE:
                completionItem.setSortText(Priority.PRIORITY140.toString());
                break;
            case ItemResolverConstants.FUNCTION_TYPE:
                completionItem.setSortText(Priority.PRIORITY130.toString());
                break;
            case ItemResolverConstants.KEYWORD_TYPE:
                completionItem.setSortText(Priority.PRIORITY120.toString());
                break;
            case ItemResolverConstants.SNIPPET_TYPE:
                completionItem.setSortText(Priority.PRIORITY110.toString());
                break;
            default:
                completionItem.setSortText(Priority.PRIORITY160.toString());
                break;
        }
    }
}
