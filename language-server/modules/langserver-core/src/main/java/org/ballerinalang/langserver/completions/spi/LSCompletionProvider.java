/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.spi;

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.LSCompletionException;
import org.ballerinalang.langserver.completions.LSCompletionProviderFactory;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Interface for completion item providers.
 *
 * @since 0.995.0
 */
public abstract class LSCompletionProvider {

    protected List<Class> attachmentPoints = new ArrayList<>();

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Language Server Context
     * @return {@link List}     List of calculated Completion Items
     */
    public abstract List<CompletionItem> getCompletions(LSContext context);

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    public List<Class> getAttachmentPoints() {
        return this.attachmentPoints;
    }

    /**
     * Get the Context Provider.
     * Ex: When a given scope is resolved then the context can be resolved by parsing a sub rule or token analyzing
     *
     * @param ctx Language Server Context
     * @return {@link Optional} Context Completion provider
     */
    public Optional<LSCompletionProvider> getContextProvider(LSContext ctx) {
        return Optional.empty();
    }

    /**
     * Populate the completion item list by considering the.
     *
     * @param symbolInfoList list of symbol information
     * @param context        Language server operation context
     * @return {@link List}     list of completion items
     */
    protected List<CompletionItem> getCompletionItemList(List<SymbolInfo> symbolInfoList, LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        symbolInfoList.removeIf(CommonUtil.invalidSymbolsPredicate());
        symbolInfoList.forEach(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.isCustomOperation() ? null : symbolInfo.getScopeEntry().symbol;
            if (CommonUtil.isValidInvokableSymbol(bSymbol) || symbolInfo.isCustomOperation()) {
                completionItems.add(populateBallerinaFunctionCompletionItem(symbolInfo));
            } else if (bSymbol instanceof BConstantSymbol) {
                completionItems.add(this.getBallerinaConstantCompletionItem(symbolInfo, context));
            } else if (!(bSymbol instanceof BInvokableSymbol) && bSymbol instanceof BVarSymbol) {
                String typeName = CommonUtil.getBTypeName(symbolInfo.getScopeEntry().symbol.type, context);
                completionItems.add(
                        BVariableCompletionItemBuilder.build((BVarSymbol) bSymbol, symbolInfo.getSymbolName(), typeName)
                );
            } else if (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol)) {
                // Here skip all the package symbols since the package is added separately
                completionItems.add(
                        BTypeCompletionItemBuilder.build((BTypeSymbol) bSymbol, symbolInfo.getSymbolName()));
            }
        });
        return completionItems;
    }

    /**
     * Populate the completion item list by either list.
     *
     * @param list    Either List of completion items or symbol info
     * @param context LS Operation Context
     * @return {@link List}     Completion Items List
     */
    protected List<CompletionItem> getCompletionItemList(Either<List<CompletionItem>, List<SymbolInfo>> list,
                                                         LSContext context) {
        return list.isLeft() ? list.getLeft() : this.getCompletionItemList(list.getRight(), context);
    }

    /**
     * Get the basic types.
     *
     * @param visibleSymbols List of visible symbols
     * @return {@link List}     List of completion items
     */
    protected List<CompletionItem> getBasicTypes(List<SymbolInfo> visibleSymbols) {
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        List<CompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            if (bSymbol instanceof BTypeSymbol && !(bSymbol instanceof BPackageSymbol)) {
                completionItems.add(
                        BTypeCompletionItemBuilder.build((BTypeSymbol) bSymbol, symbolInfo.getSymbolName()));
            }
        });

        return completionItems;
    }

    /**
     * Get all the types in the Package with given name.
     *
     * @param visibleSymbols Visible Symbols
     * @param pkgName package name
     * @param ctx language server context
     * @return {@link List} list of Type completion items
     */
    protected List<CompletionItem> getTypesInPackage(List<SymbolInfo> visibleSymbols, String pkgName, LSContext ctx) {
        List<SymbolInfo> filteredList = new ArrayList<>();
        Optional<SymbolInfo> pkgSymbolInfo = visibleSymbols.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BPackageSymbol && symbolInfo.getSymbolName().equals(pkgName); 
                })
                .findAny();
        pkgSymbolInfo.ifPresent(symbolInfo -> {
            BSymbol pkgSymbol = symbolInfo.getScopeEntry().symbol;
            pkgSymbol.scope.entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BTypeSymbol) {
                    filteredList.add(new SymbolInfo(name.getValue(), scopeEntry));
                }
            });
        });
        
        return this.getCompletionItemList(filteredList, ctx);
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param context LS Context
     * @return {@link List}     List of populated completion items
     */
    protected List<CompletionItem> addTopLevelItems(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getStaticItem(context, Snippet.KW_IMPORT));
        completionItems.add(getStaticItem(context, Snippet.KW_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_MAIN_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSOCKET));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_WEBSUB));
        completionItems.add(getStaticItem(context, Snippet.DEF_SERVICE_GRPC));
        completionItems.add(getStaticItem(context, Snippet.DEF_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.STMT_NAMESPACE_DECLARATION));
        completionItems.add(getStaticItem(context, Snippet.DEF_OBJECT_SNIPPET));
        completionItems.add(getStaticItem(context, Snippet.DEF_RECORD));
        completionItems.add(getStaticItem(context, Snippet.KW_TYPE));
        completionItems.add(getStaticItem(context, Snippet.KW_PUBLIC));
        completionItems.add(getStaticItem(context, Snippet.KW_FINAL));
        completionItems.add(getStaticItem(context, Snippet.KW_CONST));
        completionItems.add(getStaticItem(context, Snippet.DEF_ERROR));
        completionItems.add(getStaticItem(context, Snippet.KW_LISTENER));
        return completionItems;
    }

    /**
     * Get a static completion Item for the given snippet.
     *
     * @param ctx     Language Server Context
     * @param snippet Snippet to generate the static completion item
     * @return {@link CompletionItem} Generated static completion Item
     */
    protected CompletionItem getStaticItem(LSContext ctx, Snippet snippet) {
        return snippet.get().build(ctx);
    }

    /**
     * Get the completion item for a package import.
     * If the package is already imported, additional text edit for the import statement will not be added.
     *
     * @param ctx LS Operation context
     * @return {@link List}     List of packages completion items
     */
    protected List<CompletionItem> getPackagesCompletionItems(LSContext ctx) {
        // First we include the packages from the imported list.
        List<String> populatedList = new ArrayList<>();
        String relativePath = ctx.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage currentPkg = ctx.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, currentPkg);
        List<CompletionItem> completionItems = CommonUtil.getCurrentFileImports(srcOwnerPkg, ctx).stream()
                .map(pkg -> {
                    PackageID pkgID = pkg.symbol.pkgID;
                    String alias = pkg.alias.value;
                    String orgName = pkgID.orgName.value;
                    String pkgName = pkgID.nameComps.stream()
                            .map(id -> id.value)
                            .collect(Collectors.joining("."));
                    CompletionItem item = new CompletionItem();
                    item.setLabel(alias);
                    item.setInsertText(alias);
                    item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                    item.setKind(CompletionItemKind.Module);
                    populatedList.add(orgName + "/" + pkgName);
                    return item;
                }).collect(Collectors.toList());
        List<BallerinaPackage> packages = LSPackageLoader.getSdkPackages();
        packages.addAll(LSPackageLoader.getHomeRepoPackages());
        String sourceRoot = ctx.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
        String fileUri = ctx.get(DocumentServiceKeys.FILE_URI_KEY);
        packages.addAll(LSPackageLoader.getCurrentProjectImportPackages(currentPkg, sourceRoot, fileUri));
        packages.forEach(ballerinaPackage -> {
            String name = ballerinaPackage.getPackageName();
            String orgName = ballerinaPackage.getOrgName();
            if (!populatedList.contains(orgName + "/" + name)) {
                CompletionItem item = new CompletionItem();
                item.setLabel(ballerinaPackage.getFullPackageNameAlias());
                item.setInsertText(name);
                item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
                item.setKind(CompletionItemKind.Module);
                item.setAdditionalTextEdits(CommonUtil.getAutoImportTextEdits(ctx, orgName, name));
                completionItems.add(item);
            }
        });

        return completionItems;
    }

    /**
     * Get the completion items based on the delimiter token, as an example . and : .
     *
     * @param context Language Server Service Operation Context
     * @return {@link List} Completion Item List
     */
    protected List<CompletionItem> getDelimiterBasedCompletionItems(LSContext context) {
        Either<List<CompletionItem>, List<SymbolInfo>> itemList = SymbolFilters.get(DelimiterBasedContentFilter.class)
                .filterItems(context);
        return this.getCompletionItemList(itemList, context);
    }

    /**
     * Predicate to filter the attached of Self, symbol.
     *
     * @return {@link Predicate} Symbol filter predicate
     */
    protected Predicate<SymbolInfo> attachedOrSelfKeywordFilter() {
        return symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED))
                    || (CommonKeys.SELF_KEYWORD_KEY.equals(bSymbol.getName().getValue())
                    && (bSymbol.owner.flags & Flags.RESOURCE) == Flags.RESOURCE);
        };
    }

    protected Optional<String> getSubRule(List<CommonToken> tokenList) {
        if (tokenList == null || tokenList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tokenList.stream().map(CommonToken::getText).collect(Collectors.joining("")));
    }

    /**
     * Get the provider for the given key.
     *
     * @param providerKey key to get the provider
     * @return {@link LSCompletionProvider} Completion Provider
     */
    protected LSCompletionProvider getProvider(Class providerKey) {
        return LSCompletionProviderFactory.getInstance().getProvider(providerKey);
    }

    protected List<CompletionItem> getCompletionItemsAfterOnKeyword(LSContext ctx) {
        List<SymbolInfo> filtered = this.filterListenerVariables(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<CompletionItem> completionItems = new ArrayList<>(this.getCompletionItemList(filtered, ctx));
        completionItems.add(Snippet.KW_NEW.get().build(ctx));

        return completionItems;
    }

    /**
     * Get the completions for the expression context in a variable definition.
     * Eg: Completion after the EQUAL sign of the variable definition
     * 
     * @param context Language Server context
     * @return {@link List} List of completion Items
     */
    protected List<CompletionItem> getVarDefExpressionCompletions(LSContext context) {
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        List<CompletionItem> completionItems = new ArrayList<>(this.getVarDefCompletions(context));
        List<SymbolInfo> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        int counter = 0;
        StringBuilder subRule = new StringBuilder("function testFunction () {" + CommonUtil.LINE_SEPARATOR + "\t");
        while (counter < lhsTokens.size()) {
            subRule.append(lhsTokens.get(counter).getText());
            if (lhsTokens.get(counter).getType() == BallerinaParser.ASSIGN) {
                subRule.append("0;");
                break;
            }
            counter++;
        }
        subRule.append(CommonUtil.LINE_SEPARATOR).append("}");
        
        try {
            Optional<BLangType> assignmentType = getAssignmentType(subRule.toString(), context);
            if (!assignmentType.isPresent()) {
                return completionItems;
            }
            if (assignmentType.get() instanceof BLangFunctionTypeNode) {
                fillFunctionSnippet((BLangFunctionTypeNode) assignmentType.get(), context, completionItems);
                fillArrowFunctionSnippet((BLangFunctionTypeNode) assignmentType.get(), context, completionItems);
            } else if (assignmentType.get() instanceof BLangUserDefinedType) {
                BLangUserDefinedType type = ((BLangUserDefinedType) assignmentType.get());
                String typeName = type.typeName.value;
                Optional<SymbolInfo> pkgSymbol = this.getPackageSymbolFromAlias(context, type.pkgAlias.value);
                BObjectTypeSymbol objectTypeSymbol;
                if (!pkgSymbol.isPresent()) {
                    // search in the visible symbols
                    Optional<SymbolInfo> objectSymbol = visibleSymbols.stream().filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        return bSymbol instanceof BObjectTypeSymbol && bSymbol.getName().getValue().equals(typeName);
                    }).findAny();
                    if (!objectSymbol.isPresent()
                            || !(objectSymbol.get().getScopeEntry().symbol instanceof BObjectTypeSymbol)) {
                        return completionItems;
                    }
                    objectTypeSymbol = (BObjectTypeSymbol) objectSymbol.get().getScopeEntry().symbol;
                } else {
                    Optional<BSymbol> objectInPackage = getObjectInPackage(pkgSymbol.get().getScopeEntry().symbol,
                            typeName);
                    if (!objectInPackage.isPresent() || !(objectInPackage.get() instanceof BObjectTypeSymbol)) {
                        return completionItems;
                    }
                    objectTypeSymbol = (BObjectTypeSymbol) objectInPackage.get();
                }

                BAttachedFunction initFunction = objectTypeSymbol.initializerFunc;
                CompletionItem newCItem;
                if (initFunction == null) {
                    newCItem = BFunctionCompletionItemBuilder.build(null, "new()", "new();");
                } else {
                    newCItem = BFunctionCompletionItemBuilder.build(initFunction.symbol);
                }
                completionItems.add(newCItem);
            }
        } catch (LSCompletionException ex) {
            // do nothing
        }
        
        return completionItems;
    }

    /**
     * Get the Resource Snippets.
     * 
     * @param ctx Language Server Context
     * @return {@link Optional} Completion List
     */
    protected List<CompletionItem> getResourceSnippets(LSContext ctx) {
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);
        List<CompletionItem> items = new ArrayList<>();
        if (symbolEnvNode instanceof BLangService) {
            BLangService service = (BLangService) symbolEnvNode;
            String owner = service.listenerType.tsymbol.owner.name.value;
            String serviceTypeName = service.listenerType.tsymbol.name.value;
            // Only http, grpc have generic resource templates, others will have generic resource snippet
            switch (owner) {
                case "http":
                    if ("Listener".equals(serviceTypeName)) {
                        items.add(Snippet.DEF_RESOURCE.get().build(ctx));
                        break;
                    } else if ("WebSocketListener".equals(serviceTypeName)) {
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_OPEN.get(), service, items, ctx);
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_TEXT.get(), service, items, ctx);
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_CLOSE.get(), service, items, ctx);
                        break;
                    }
                    return items;
                case "grpc":
                    items.add(Snippet.DEF_RESOURCE_GRPC.get().build(ctx));
                    break;
                case "websub":
                    addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_INTENT.get(), service, items, ctx);
                    addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_NOTIFY.get(), service, items, ctx);
                    break;
                default:
                    return items;
            }
        }
        return items;
    }
    
    protected Optional<SymbolInfo> getPackageSymbolFromAlias(LSContext context, String alias) {
        List<SymbolInfo> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        return visibleSymbols.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BPackageSymbol && alias.equals(symbol.getName().getValue());
                })
                .findAny();
    }
    
    protected Optional<BSymbol> getObjectInPackage(BSymbol pkg, String typeName) {
        if (!(pkg instanceof BPackageSymbol)) {
            return Optional.empty();
        }
        return pkg.scope.entries.values().stream().filter(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            return symbol instanceof BObjectTypeSymbol && symbol.getName().getValue().equals(typeName);
        }).findAny().map(scopeEntry -> scopeEntry.symbol);
    }

    // Private Methods

    /**
     * Populate the Ballerina Function Completion Item.
     *
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo) {
        if (symbolInfo.isCustomOperation()) {
            SymbolInfo.CustomOperationSignature signature =
                    symbolInfo.getCustomOperationSignature();
            return BFunctionCompletionItemBuilder.build(null, signature.getLabel(), signature.getInsertText());
        }
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        if (!(bSymbol instanceof BInvokableSymbol)) {
            return null;
        }
        return BFunctionCompletionItemBuilder.build((BInvokableSymbol) bSymbol);
    }

    /**
     * Get the Ballerina Constant Completion Item.
     *
     * @param symbolInfo symbol information
     * @param context    Language Server Operation Context
     * @return {@link CompletionItem}   completion item
     */
    private CompletionItem getBallerinaConstantCompletionItem(SymbolInfo symbolInfo, LSContext context) {
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        if (!(bSymbol instanceof BConstantSymbol)) {
            return null;
        }

        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(bSymbol.getName().getValue());
        completionItem.setInsertText(bSymbol.getName().getValue());
        completionItem.setDetail(CommonUtil.getBTypeName(((BConstantSymbol) bSymbol).literalType, context));
        completionItem.setDocumentation(ItemResolverConstants.CONSTANT_TYPE);
        completionItem.setKind(CompletionItemKind.Variable);

        return completionItem;
    }

    private List<SymbolInfo> filterListenerVariables(List<SymbolInfo> symbolInfos) {
        return symbolInfos.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BVarSymbol && CommonUtil.isListenerObject(symbol.type.tsymbol);
                })
                .collect(Collectors.toList());
    }
    
    private Optional<BLangType> getAssignmentType(String subRule, LSContext context) throws LSCompletionException {
        Optional<BLangPackage> bLangPackage;
        try {
            bLangPackage = LSCompiler.compileContent(subRule, CompilerPhase.CODE_ANALYZE)
                    .getBLangPackage();
        } catch (LSCompilerException e) {
            throw new LSCompletionException("Error while parsing the sub-rule");
        }

        if (!bLangPackage.isPresent()) {
            return Optional.empty();
        }

        BLangStatement evalStatement = bLangPackage.get().getFunctions().get(0).getBody().stmts.get(0);

        if (!(evalStatement instanceof BLangSimpleVariableDef)) {
            return Optional.empty();
        }

        BLangType typeNode = ((BLangSimpleVariableDef) evalStatement).getVariable().getTypeNode();

        return Optional.of(typeNode);
    }

    private void fillFunctionSnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                     List<CompletionItem> completionItems)
            throws LSCompletionException {

        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        String functionSignature = this.getFunctionSignature(params, returnBLangType);
        String body = this.getAnonFunctionSnippetBody(returnBLangType, params.size());
        String snippet = functionSignature + body;
        String label = this.convertToLabel(functionSignature);
        SnippetBlock snippetBlock = new SnippetBlock(label, snippet, ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(context));
    }

    private String getFunctionSignature(List<BLangVariable> paramTypes, BLangType returnType)
            throws LSCompletionException {
        StringBuilder signature = new StringBuilder("function ");

        signature.append(this.getParamsSnippet(paramTypes, true));
        if (!(returnType.type instanceof BNilType)) {
            signature.append("returns (")
                    .append(this.getTypeName(returnType))
                    .append(") ");
        }

        return signature.toString();
    }

    private void fillArrowFunctionSnippet(BLangFunctionTypeNode functionTypeNode, LSContext context,
                                           List<CompletionItem> completionItems) throws LSCompletionException {
        List<BLangVariable> params = functionTypeNode.getParams();
        BLangType returnBLangType = functionTypeNode.getReturnTypeNode();
        String paramSignature = this.getParamsSnippet(params, false);
        StringBuilder signature = new StringBuilder(paramSignature);

        signature.append(" => ")
                .append("${");
        if (!(returnBLangType.type instanceof BNilType)) {
            signature.append(params.size() + 1)
                    .append(":")
                    .append(CommonUtil.getDefaultValueForType(returnBLangType.type));
        } else {
            signature.append(params.size() + 1);
        }
        signature.append("};");

        String label = "arrow function  " + this.convertToLabel(paramSignature);

        SnippetBlock snippetBlock = new SnippetBlock(label, signature.toString(), ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.SnippetType.SNIPPET);

        // Populate the anonymous function signature completion item
        completionItems.add(snippetBlock.build(context));
    }
    
    private String getAnonFunctionSnippetBody(BLangType returnType, int numberOfParams) {
        StringBuilder body = new StringBuilder();
        if (!(returnType.type instanceof BNilType)) {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t")
                    .append("return ")
                    .append(CommonUtil.getDefaultValueForType(returnType.type))
                    .append(";")
                    .append(CommonUtil.LINE_SEPARATOR);
        } else {
            body.append("{")
                    .append(CommonUtil.LINE_SEPARATOR)
                    .append("\t${")
                    .append(numberOfParams + 1)
                    .append("}")
                    .append(CommonUtil.LINE_SEPARATOR);
        }

        body.append("};");

        return body.toString();
    }

    private String convertToLabel(String signature) {
        return signature
                .replaceAll("(\\$\\{\\d:)([a-zA-Z\\d]*:*[a-zA-Z\\d]*)(\\})", "$2")
                .replaceAll("(\\$\\{\\d\\})", "");
    }

    private String getParamsSnippet(List<BLangVariable> paramTypes, boolean withType) throws LSCompletionException {
        String paramName = "param";
        StringBuilder signature = new StringBuilder("(");
        List<String> params = IntStream.range(0, paramTypes.size())
                .mapToObj(index -> {
                    try {
                        int paramIndex = index + 1;
                        String paramPlaceHolder = "${" + paramIndex + ":" + paramName + paramIndex + "}";
                        if (withType) {
                            paramPlaceHolder = this.getTypeName(paramTypes.get(index).getTypeNode()) + " "
                                    + paramPlaceHolder;
                        }
                        return paramPlaceHolder;
                    } catch (LSCompletionException e) {
                        return "";
                    }
                })
                .collect(Collectors.toList());

        if (params.contains("")) {
            throw new LSCompletionException("Contains invalid parameter type");
        }

        signature.append(String.join(", ", params))
                .append(") ");

        return signature.toString();
    }

    private String getTypeName(BLangType bLangType) throws LSCompletionException {
        if (bLangType instanceof BLangValueType || bLangType instanceof BLangUnionTypeNode) {
            return bLangType.toString();
        } else if (bLangType instanceof BLangUserDefinedType) {
            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) bLangType;
            String pkgAlias = userDefinedType.getPackageAlias().getValue();
            String typeName = userDefinedType.getTypeName().getValue();
            return pkgAlias.isEmpty() ? typeName : (pkgAlias + CommonKeys.PKG_DELIMITER_KEYWORD + typeName);
        } else {
            throw new LSCompletionException("Error identifying the type of anonymous function parameter");
        }
    }

    /**
     * Get variable definition context related completion items. This will extract the completion items analyzing the
     * variable definition context properties.
     *
     * @param context Completion context
     * @return {@link List}     List of resolved completion items
     */
    public List<CompletionItem> getVarDefCompletions(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredList = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        filteredList.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || ((bSymbol instanceof BTypeSymbol)
                    && !(bSymbol instanceof BPackageSymbol))
                    || (bSymbol instanceof BInvokableSymbol
                    && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
        });
        completionItems.addAll(getCompletionItemList(filteredList, context));
        // Add the packages completion items.
        completionItems.addAll(getPackagesCompletionItems(context));
        // Add the check keyword
        CompletionItem checkKeyword = Snippet.KW_CHECK.get().build(context);
        completionItems.add(checkKeyword);
        // Add the wait keyword
        CompletionItem waitKeyword = Snippet.KW_WAIT.get().build(context);
        completionItems.add(waitKeyword);
        // Add the untaint keyword
        CompletionItem untaintKeyword = Snippet.KW_UNTAINT.get().build(context);
        completionItems.add(untaintKeyword);
        // Add But keyword item
        CompletionItem butKeyword = Snippet.EXPR_MATCH.get().build(context);
        completionItems.add(butKeyword);
        // Add the trap expression keyword
        CompletionItem trapExpression = Snippet.STMT_TRAP.get().build(context);
        completionItems.add(trapExpression);

        return completionItems;
    }

    /**
     * Check whether the current context is annotation context.
     *
     * @param context Language server context
     * @return {@link Boolean} whether the cursor is in the annotation context
     */
    protected boolean isAnnotationAttachmentContext(LSContext context) {
        List<Integer> lhsDefaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        /*
        Max token bactrack count is set to 4 in order to support the following
        @moduleName:Rec
         */
        int maxTokenVisitCount = 4;
        int counter = 0;
        while (counter < lhsDefaultTokenTypes.size() && counter < maxTokenVisitCount) {
            Integer token = lhsDefaultTokenTypes.get(counter);
            if (token == BallerinaParser.AT) {
                return true;
            }
            counter++;
        }
        
        return false;
    }

    private void addIfNotExists(SnippetBlock snippet, BLangService service, List<CompletionItem> items, LSContext ctx) {
        boolean found = false;
        for (BLangFunction resource : service.getResources()) {
            if (snippet.getLabel().equals(resource.name.value + " " + ItemResolverConstants.RESOURCE)) {
                found = true;
            }
        }
        if (!found) {
            items.add(snippet.build(ctx));
        }
    }
}
