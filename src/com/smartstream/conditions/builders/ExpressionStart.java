package com.smartstream.conditions.builders;

import com.viren.conditions.AttributeData;



/**
 * This class and it's static methods are the entry point for the fluent API we build to build the Expression and Condition chains.
 * 
 * <p>
 * To build this fluent API and all it's builders we used the following simplified BNF:
 * 
 * <pre>
 * expression ::= condition | alternation | conjunction | negation | '(' expression ')'
 *
 * condition ::= unaryCondition | binaryCondition | polyadicCondition
 * alternation ::= expression 'or' expression
 * conjunction ::= expression 'and' expression
 * negation ::= 'not' expression
 * 
 * unaryCondition ::= attriubteName unaryOperator
 * binaryCondition ::= attriubteName binaryOperator value
 * polyadicCondition ::= attriubteName polyadicOperator valueList
 * 
 * attributeName ::= String
 * 
 * unaryOperator ::= "is null" | "is not null"
 * binaryOperator ::= "==" | "!=" | "<" | "<=" | ">" | ">=" | "~="
 * polyadicOperator ::= "like" | "not like" | "in" | "not in"
 * 
 * value ::= String
 * valueList ::= value | value ',' valueList
 * </pre>
 * </p>
 * <p>
 * To build an expression in the code make a static import of the methods in this class and start either with
 * {@link #attribute(String, Class)} or {@link #not()}. Building the conditions and sub expression will be self-explanatory.
 * If you wan to receive an Expression out of your fluentAPI just call the toExpression() method at the appropriate position.
 * </p>
 * <p>
 * <b>Example:</b><br />
 * To create a filter like this {@code firstName = "Herbert" and lastName = "Mustermann"}
 * you have to  do the following:
 * 
 * <pre>
 * import static com.smartstream.conditions.builders.ExpressionStart.attribute;
 * 
 * ...
 * Expression simpleFilter = 
 *      attribute("firstName", String.class).eq("Herbert")
 *      .and().attribute("lastName", String.class).eq("Mustermann")
 *  .toExpression();
 * ...
 * </pre>
 * </p>
 *  
 * @author brandstetter
 * 
 * @see <a href="http://confluence/display/Morph/MFF+%28Filter+Framework%29">The Filter Framework confluence page.</a>
 */
public final class ExpressionStart{
    private ExpressionStart(){
        throw new IllegalStateException("Instantiation of this class is not allowed!");
    }
    
    /**
     * Entry point to create a condition.
     * @param name the name of the attribute
     * @param type the datatype of the attribute
     * @return a builder which will help you to define a operation on the attribute (eg. equals, greater than, ...)
     * @throws NullPointerException if either the name or the type is null 
     */
    public static <T> ConditionBuilder<T> attribute(String name, Class<T> type){
        return new ConditionBuilderImpl<>(new ExpressionBuilderImpl(), name, type, null);
    }
    
    /**
     * Entry point to create a condition.
     * @param attributeData and attributeData object
     * @return a builder which will help you to define a operation on the attribute (eg. equals, greater than, ...)
     * @throws NullPointerException if either the name or the type is null 
     */
    public static <T> ConditionBuilder<T> attribute(AttributeData<T> attributeData){
        return new ConditionBuilderImpl<>(new ExpressionBuilderImpl(), attributeData.getName(), attributeData.getType(), null);
    }
    
    /**
     * Entry point to create a negated expression or condition.
     * @return a builder which will help you to define an attribute
     */
    public static NotBuilder not(){
        return new NotBuilderImpl(new ExpressionBuilderImpl(), null);
    }
    
    /**
     * Entry point to start an expression
     * @param group the condition which should be wrapped in an Expression
     * @return a builder which will help you to define an attribute
     */
    @SuppressWarnings("deprecation") // NOPMD: it's OK to use the ExpressionFinalizer#toExpression() method here, because it does exactly what we want
    public static ExpressionBuilder group( ExpressionBuilder group ){
        return group(group.toExpression());
    }
    
    /**
     * Entry point to start an expression
     * @param group the expression which should be wrapped in an Expression
     * @return a builder which will help you to define an attribute
     */
    public static ExpressionBuilder group( com.viren.conditions.Expression group ){
        return new ExpressionBuilderImpl(group);
    }
}
