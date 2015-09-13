package com.viren.conditions;

import java.util.List;

import com.viren.conditions.Conjunction.Type;

/**
 * This interface should be implemented by concrete visitors (see: visitor design pattern)
 *  
 * @author brandstetter
 */
public interface Visitor {
    /**
     * Called whenever an expression starts.
     * @param negate specifies if the expression should be negated or not
     */
    void startExpression(boolean negate);
    
    /**
     * Called after all conditions and conjunctions inside of the expression are handled/visited.
     */
    void endExpression();
    
    /**
     * Called when two elements either expression or condition will be conjuncted.
     * @param conjunctionType the type of the conjunction
     */
    void conjunct(Type conjunctionType);
    
    /**
     * Called on an unary condition.
     * @param negate the condition should be negated
     * @param attributeName the name of the attribute
     * @param type the type of the attribute
     * @param operator the operator to use in this condition
     */
    void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator);
    
    /**
     * Called on a binary condition.
     * @param negate the condition should be negated
     * @param attributeName the name of the attribute
     * @param type the type of the attribute
     * @param operator the operator to use in this condition
     * @param value the value to check in the condition
     */
    void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value);
    
    /**
     * Called on a polyadic condition.
     * @param negate the condition should be negated
     * @param attributeName the name of the attribute
     * @param type the type of the attribute
     * @param operator the operator to use in this condition
     * @param values the list of values to check in the condition
     */
    void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values);
}
