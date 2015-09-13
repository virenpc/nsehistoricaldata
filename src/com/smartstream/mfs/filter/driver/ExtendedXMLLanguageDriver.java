package com.smartstream.mfs.filter.driver;

import static com.smartstream.mfs.filter.driver.ExtensionUtil.DYNAMIC_SQL_SOURCE_ROOT_SQL_NODE;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.PLACEHOLDER_BEGIN;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.PLACEHOLDER_BEGIN_OFFSET;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.PLACEHOLDER_END;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.PLACEHOLDER_END_OFFSET;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.findFirstInsecure;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.getFieldValue;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.getRootSqlNodeOfDynamicSqlSource;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.getTextOfTextSqlNode;
import static com.smartstream.mfs.filter.driver.ExtensionUtil.setFieldValue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.scripting.xmltags.VarDeclSqlNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import com.smartstream.mfs.filter.extension.ConditionExtensionSqlNode;

/**
 * A MyBaits {@link org.apache.ibatis.scripting.LanguageDriver} implementation which extends the default XML based one
 * and inserts our "special" nodes.
 * 
 * <p>
 * Currently only the new Expression/Condition objects are supported but it can be extend and enriched in the future to
 * also add other special SQL treatments.
 * </p>
 * 
 * <p>
 * There are two ways to specify the usage of this language driver:
 * <ul>
 *  <li>register it as the default language driver:
 *      <p>
 *      <b>configuration entry:</b>
 *      <pre>
 *      &lt;configuration&gt;
 *           ...
 *           &lt;settings&gt;
 *              <b>&lt;setting name="defaultScriptingLanguage" value="com.smartstream.mfs.filter.driver.ExtendedXMLLanguageDriver" /&gt;</b>
 *           &lt;/settings&gt;
 *           ...
 *      &lt;/configuration&gt;
 *      </pre>
 *      </p>
 *  </li>
 *  <li>register it as a typeAlias in the mybatis configuration and reference the new alias inside the {@code lang} attribute of the {@code <select>, <insert>, <update>} and {@code <delete>} tags:</li>
 *      <p>
 *      <b>configuration entry:</b>
 *      <pre>
 *      &lt;configuration&gt;
 *          &lt;typeAliases&gt;
 *              ...
 *              <b>&lt;typeAlias alias="EXTENDED_XML" type="com.smartstream.mfs.filter.driver.ExtendedXMLLanguageDriver"/&gt;</b>
 *              ...
 *          &lt;/typeAliases&gt;
 *          ...
 *      &lt;/configuration&gt;
 *      </pre>
 *      </p>
 *      <p>
 *      <b>mapping entry:</b>
 *      <pre>
 *      &lt;mapper namespace="com.smartstream.filterstore"&gt;
 *          &lt;select id="FilterInfo_query" resultMap="FilterInfoMap" <b>lang="EXTENDED_XML"</b>&gt;
 *               select * from MFW_FILTER
 *              ...
 *          &lt;/select&gt;
 *          ...
 *      &lt;/mapper&gt;
 *      </pre>
 *      </p>
 * </ul>
 * 
 * The difference between those two is that the specification of the defaultScriptLanguage will always use the ExtendedXMLLanguageDriver
 * and the one configured with typeAlias will only be used on those {@code select} statements which are marked with the appropriate
 * {@code lang} attribute. <i>The first one with the defaultScriptLanguage configuration is the preferred one, because you don't
 * have to specify {@code lang} attribute all the time.</i>
 * </p>
 * 
 * <p>
 * Currently there is only one extension available and that's the {@code Condition} extension which will be required to handle
 * the new expression and condition API. The {@code Condition} extension will handle the additional extension parameter as follows:
 * <ul>
 *  <li>if the additional extension parameter exists: it will be used as the lookup name inside of the parameter map you hand-over to the SQL</li>
 *  <li>if the additional extension parameter doesn't exist: a default name will be used as the lookup name inside of the parameter map you hand-over to the SQL</li>
 * </ul>
 * </p>
 * 
 * @author brandstetter 
 *
 */
public class ExtendedXMLLanguageDriver extends XMLLanguageDriver {
    private static final Set<String> PRECEDING_OPERATORS = definePrecedingOperators();

    /** Initialize the set of words that are passed to extension nodes as "their preceding operator". */
    private static Set<String> definePrecedingOperators() {
        Set<String> s = new HashSet<>();
        s.add("AND");
        s.add("OR");
        return s;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Calls the original {@link XMLLanguageDriver#createSqlSource(Configuration, String, Class)} and substitutes all the defined extensions in the 
     * returned SqlSource with the corresponding SqlNode.
     * </p>
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        return configureExtension( super.createSqlSource(configuration, script, parameterType) );
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Calls the original {@link XMLLanguageDriver#createSqlSource(Configuration, XNode, Class)} and substitutes all the defined extensions in the 
     * returned SqlSource with the corresponding SqlNode.
     * </p>
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        return configureExtension( super.createSqlSource(configuration, script, parameterType) );
    }
    
    /**
     * Convenient method which wraps a call to {@link #replaceExtensionPlaceholder(DynamicSqlSource)} in a more builder style way.
     * @param sqlSource the {@link SqlSource} which could contain palceholders to replace
     * @return the given {@link SqlSource} after all extensions are replaced
     * @throws ClassCastException if the given sqlSource is not of type {@link DynamicSqlSource}
     */
    private SqlSource configureExtension(SqlSource sqlSource){
        DynamicSqlSource rootNode = (DynamicSqlSource) sqlSource; // XMLLanguageDriver always returns a DynamicSqlSource and we need it for the dynamic part of the where clause --> fail early
        
        replaceExtensionPlaceholder(rootNode);
        
        return sqlSource;
    }
    
    /**
     * Will substitute the {@link SqlNode}s inside the given {@link SqlSource} which contain a placeholders with the appropriate {@link SqlNode}.
     * <p>
     * This method will change the nested nodes inside the given {@link DynamicSqlSource} if a replace of a placeholder is required.
     * </p>
     * @param sqlSource the {@link SqlSource} which could contain palceholders that should be replaced
     */
    private void replaceExtensionPlaceholder(DynamicSqlSource sqlSource){
        SqlNode rootSqlNode = getRootSqlNodeOfDynamicSqlSource(sqlSource);
        replacePlaceholdersRecursive(rootSqlNode, sqlSource, DYNAMIC_SQL_SOURCE_ROOT_SQL_NODE);
    }
    
    /**
     * Walks throw all nested SqlNodes in a recursive style and replaces all the placeholders with the appropriate SqlNode.
     * @param toCheck node to check if it is a placeholder or not
     * @param parentNode the node which contains the node to check (required for replace)
     * @param fieldInParentNode the field of the parent node which is the reference to the toCheck node (required for replace)
     * @throws IllegalStateException if one the reflection methods to access the fields fails
     */
    private void replacePlaceholdersRecursive(SqlNode toCheck, Object parentNode, Field fieldInParentNode){
        if( toCheck == null || toCheck instanceof VarDeclSqlNode ) return; // shortcut for empty nodes and end-nodes we are not interested in
        
        if( toCheck instanceof TextSqlNode ){
            SqlNode replacement = buildReplacementNode( getTextOfTextSqlNode((TextSqlNode) toCheck) );
            if( replacement != null ){ // do we have a replacement?
                setFieldValue(parentNode, fieldInParentNode, replacement);
            }
            
            return; // nothing left to check, because TextSqlNode is an end-node
        }
        
        Class<?> classContainingTheContent = toCheck.getClass();
        
        // placeholder node is not possible here! (check was done above)
        if( !(toCheck instanceof TrimSqlNode) ){ // trim nodes are an exception, because TrimSqlNode is the base object for WHERE and SET and they do not hold a list
            Field listContents = findFirstInsecure(toCheck.getClass(), List.class);  // contains the content nodes (see ChooseSqlNode, MixedSqlNode, ...)
            if( listContents != null ){
                List<SqlNode> contents = getFieldValue(toCheck, listContents); // return of SqlNode is known because we had a look at the source code (--> check after every MyBatis update is required!)
                processSqlNodeList(contents);
            }
        } else {
            classContainingTheContent = TrimSqlNode.class; // check the TrimSqlNode class and not the child classes because the value/contents is held in this node and not in the children
        }
        
        Field contents = findFirstInsecure(classContainingTheContent, SqlNode.class);
        if( contents != null ){
            replacePlaceholdersRecursive((SqlNode) getFieldValue(toCheck, contents), toCheck, contents);  // check all nodes which only contain one SqlNode as there content
        }
    }

    /**
     * Extracted from {@link #replacePlaceholdersRecursive(SqlNode, Object, Field)} to reduce cyclomatic complexity.
     * @param contents
     */
	private void processSqlNodeList(List<SqlNode> contents) {
		for(int i = 0; i < contents.size(); i++ ){
		    SqlNode content = contents.get(i);
		    
		    if( content instanceof TextSqlNode ){
		        // replace list item directly
		        SqlNode replacement = buildReplacementNode( getTextOfTextSqlNode((TextSqlNode) content) );
		        if( replacement != null ){ // do we have a replacement?
		            contents.set(i, replacement);
		        }
		    } else {
		        // find possible nested TextSqlNodes and replace them too
		        replacePlaceholdersRecursive(content, null, null); // parent and field aren't required
		    }
		}
	}
    
    /**
     * Takes the given text from a {@link TextSqlNode} and checks if there are placeholders mentioned and if so it will replace them.
     * <p>
     * This method will check for placeholders which have the following format
     * <pre>
     *  ##__SmartStream__&lt;EXTESION_NAME&gt;[:ADDITONAL_EXTENSION_PARAMETER]__##
     * </pre>
     * The {@code <EXTESION_NAME>[:ADDITONAL_EXTENSION_PARAMETER]} part will be split at the ':' if it exists an the
     * values will be used to call the {@link #handleExtension(String, String)} method. This method is responsible for
     * creating the correct {@link SqlNode} for the give {@code <EXTESION_NAME>}.
     * </p>
     * <p>
     * If the parameter-name ends with an exclamation-mark, then an exception is thrown if that parameter is not found when
     * the SQL is generated. Otherwise, the ##..## expression becomes an empty string (and any preceding AND or OR operator
     * is automatically suppressed).
     * </p>
     * <p>
     * <i>Info:</i> This method can handle with multiple and/or different placeholders inside the given text. It can also
     * handle the interruption of placeholders with oder string content. (e.g.: 
     * {@code "##__SmartStream__Conditon:test__## and some other content here ##__SmartStream__Conditon:expr__##"})
     * </p>
     * @param textOfTextSqlNode the text of a {@link TextSqlNode} which could contain a placeholder that should be replaced
     * @return the new {@link SqlNode} which should be used instead of the {@link TextSqlNode} if a placeholder has been replaced, or
     * null if no replace should be done
     */
    private SqlNode buildReplacementNode(String textOfTextSqlNode){
        if( textOfTextSqlNode != null ){
            List<SqlNode> replacements = new LinkedList<>();
            
            String trimmed = textOfTextSqlNode.trim();  // trim to simplify the following logic
            int searchOffset = 0; // start of "unprocessed" area of the original text string
            boolean anyReplacementOccured = false;  // marker to see if any replacement happened at all
            
            while( searchOffset < trimmed.length() ){
                int placeholderIdx = trimmed.indexOf(PLACEHOLDER_BEGIN, searchOffset);
                if( placeholderIdx < 0 ){
                    replacements.add( new TextSqlNode(trimmed.substring(searchOffset) ) ); // everything until the end
                    break;  // exit loop because we can't find any placeholder
                }
                
                int placeholderEnd = trimmed.indexOf(PLACEHOLDER_END, placeholderIdx + PLACEHOLDER_BEGIN_OFFSET);  // start search after the begin placeholder
                if( placeholderEnd < 0 ){
                    replacements.add( new TextSqlNode(trimmed.substring(placeholderIdx) ) ); // everything until the end
                    break; // exit loop because we can't find the end of the placeholder
                }
                
                // -- the extension node itself --
                String extension = trimmed.substring(placeholderIdx + PLACEHOLDER_BEGIN_OFFSET, placeholderEnd);
                int parameterNameIndex = extension.indexOf(':');

                // -- the preceding work (if any)
                int precedingOperatorIdx = findPrecedingOperator(trimmed, placeholderIdx);
                String precedingOperator = trimmed.substring(precedingOperatorIdx, placeholderIdx).trim(); 
                
                SqlNode replacementNode;
                if (parameterNameIndex >= 0) { // are additional extension parameter available
                    String extensionName = extension.substring(0, parameterNameIndex); // part before the ':'
                    String paramName = extension.substring(parameterNameIndex + 1); // part after the ':'
                    replacementNode = handleExtension(extensionName, paramName, precedingOperator); 
                } else {
                    replacementNode = handleExtension(extension, null, precedingOperator);
                }          
                
                if( replacementNode != null ){
                    // Extension-type was recognized and a suitable Node was created
                    if (searchOffset < precedingOperatorIdx) {
                        // We found a placeholder at somewhere other than the start of the string. Therefore create a text-node representing
                        // the non-placeholder-specific text between the start-of-string and the start of the current placeholder.
                        String newBody = trimmed.substring(searchOffset, precedingOperatorIdx);
                        replacements.add( new TextSqlNode( newBody) );
                    }
                    
                    replacements.add( replacementNode );
                    anyReplacementOccured = true;  // mark replacement has happened
                } else {
                    // Create a text-node representing all text from the start of the string to the *end* of the detected (but not recognised)
                    // placeholder. This *leaves* the detected placeholder text in the generated text node for debugging purposes.
                    String newBody = trimmed.substring(searchOffset, placeholderEnd + PLACEHOLDER_END_OFFSET);
                    replacements.add(new TextSqlNode(newBody));
                }
                
                searchOffset = placeholderEnd + PLACEHOLDER_END_OFFSET; // go on with the search at the position directly after the end of this placeholder
            }
            
            if( anyReplacementOccured && !replacements.isEmpty() ){  // do we have a replacement at all?
                return replacements.size() == 1 ? replacements.get(0) : new MixedSqlNode(replacements); // if we result in multiple nodes (TextSqlNodes and "extension"Nodes) we return a MixedSqlNode, but if only one node was created just return this one
            }
        }
        return null; // no replace required (reason: no placeholder found or the extension wasn't handled)
    }

    /**
     * If the word "OR" or "AND" occurs directly before the specified point in the text input, then return the index
     * at which that preceding word starts.
     */
    private int findPrecedingOperator(String text, int at) {
        // Step backwards over whitespace
        int endIndex = at - 1;
        while (endIndex >= 0 && Character.isWhitespace(text.charAt(endIndex))) {
            --endIndex;
        }

        // Step backwards over non-whitespace
        int startIndex = endIndex;
        while (startIndex >= 0 && !Character.isWhitespace(text.charAt(startIndex))) {
            --startIndex;
        }

        String word = text.substring(startIndex+1, endIndex + 1).toUpperCase();
        if (PRECEDING_OPERATORS.contains(word)) {
            return startIndex + 1;
        }
        return at;
    }

    /**
     * Creates a {@link SqlNode} for the given extension.
     * <p>
     * This method will be called whenever a placeholder was found. So this method can be enriched or overwritten if
     * additional extensions are required. If you overwrite this method please don't forget to call the super implementation
     * of this method, because otherwise the {@code Condition} extension wouldn't work.
     * </p>
     * @param extension the name of the found extension
     * @param additionalExtensionParameter additional parameter which can be used to configure the extension 
     * @return a {@link SqlNode} which should replace the found extension; or null if the given extension shouldn't be handled
     */
    protected SqlNode handleExtension(String extension, String additionalExtensionParameter, String precedingOperator){
        switch( extension ){
            case "Condition":
                return new ConditionExtensionSqlNode(additionalExtensionParameter, precedingOperator);
        }
        
        return null;
    }
    
//  Test of the #buildReplacementNode(String) method to check if the logic is correct!
//  I didn't want to test this in a JUnit test because I don't want to extend the visibility of the #buildReplacementNode(String) method!
//    
//    public static void main(String[] args) {
//        ExtendedXMLLanguageDriver tmp = new ExtendedXMLLanguageDriver();
//        String[] tests = {
//            "##__SmartStream___## --> nix",
//            "##__SmartStream____## --> empty extension and null parameter",
//            "##__SmartStream__ __## --> ' ' extension and null parameter",
//            "   ##__SmartStream__Condition__## --> parameter name expression\ntext which should survive: ##__SmartStream__Condition:blub__##",
//            "##__SmartStream__Condition:Test__## --> parameter name Test",
//            "##__SmartStream__Condition:TE_ST__## --> parameter name TE_ST",
//            "##__SmartStream__Condition:__## --> parameter name ",
//            "##__SmartStream__Condition__## --> parameter name null",
//            "nix\n aslfjaslkdfjalöksjfaölksjfölaksdjföalks dvoiasdnvaösdiufasd vciuasdhgfpowjeiufhsalkdf::: ##_",
//            "asdfasfasfasdfasf ##__SmartStream__Condition:MitnDrin__##asdfasdfasdfc advsadcasdvsd",
//            "##__SmartStream__Unknown:asdf__## --> parameter name ",
//            "##__SmartStream__Unknown:asdf__####__SmartStream__Condition:Known__##",
//            "##__SmartStream__Condition!sdflklsdfionnvasdflk asdf\nasdfasdfsdd"
//                "select * from foo where 1=1 and ##__SmartStream__Condition__## and 2=2"
//        };
//        
//        for( String test : tests ){
//            SqlNode node = tmp.buildReplacementNode(test);
//            System.out.printf("'%s'%n'%s'%n%n", test, buildNodeOutput(node));
//        }
//    }
//    
//    private static StringBuilder buildNodeOutput(SqlNode node){
//        return buildNodeOutput( node, new StringBuilder() );
//    }
//    
//    private static StringBuilder buildNodeOutput(SqlNode node, StringBuilder out){
//        if( node instanceof TextSqlNode ){
//            out.append( ExtensionUtil.getTextOfTextSqlNode( (TextSqlNode) node) );
//        } else if ( node instanceof MixedSqlNode ){
//            List<SqlNode> content = getFieldValue(node, ExtensionUtil.findFirst(MixedSqlNode.class, List.class));
//            for( SqlNode n : content ){
//                buildNodeOutput(n, out);
//            }
//        } else {
//            out.append(node);
//        }
//        
//        return out;
//    }
//        
}
