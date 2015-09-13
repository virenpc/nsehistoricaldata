package com.smartstream.conditions.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.viren.conditions.Operator;
import com.viren.conditions.Visitor;
import com.viren.conditions.Conjunction.Type;

/**
 * Basic implementation for validation visitors which can handle conditionless expressions.
 * 
 * @author brandstetter
 */
public abstract class AbstractExpressionValidator implements Visitor {
    /**
     * Simple state object to check if an expression contains a condition.
     * 
     * @author brandstetter
     */
    protected static final class ExpressionState {
        /** Marker if the condition has an expression. */
        private boolean hasCondition;
        
        /**
         * Marks that the expression contains a condition.
         */
        public void markHasCondition(){
            hasCondition = true;
        }
        
        /**
         * Returns if the expression has a condition.
         * @return if the expression has a condition
         */
        public boolean hasCondition(){
            return hasCondition;
        }
    }
    
    /** Stack for {@link ExpressionState} objects to mark if the current expression has a condition. */
    private final Deque<ExpressionState> expressionStack = new LinkedList<>();
    
    protected AbstractExpressionValidator(){
        expressionStack.push(new ExpressionState()); // adding a null object to the stack if the validator is first called on a condition instead of an expression
    }
    
    @Override
    public final void startExpression(boolean negate) {
        expressionStack.peek().markHasCondition();   // expressions can also host expressions so a mark the previous one
        expressionStack.push(new ExpressionState()); // expression begins put a new state object on the stack which will be set if we hit a condition!
    }
    
    @Override
    public final void endExpression() {
        checkExpression( expressionStack.pop() ); // expression did end so remove the state object from the stack and check it's state
    }
    
    @Override
    public void conjunct(Type conjunctionType) {   
        // NOPMD: no op implementation
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Marks a possible open expression as having a condition and call {@link #basicConditionCheck(String, Operator)}.
     * </p>
     */
    @Override
    public final void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator) {
        expressionStack.peek().markHasCondition();
        basicConditionCheck(attributeName, operator);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Marks a possible open expression as having a condition and call {@link #basicConditionCheck(String, Operator)}
     * and {@link #checkValue(boolean, String, Class, Operator, Object)}.
     * </p>
     */
    @Override
    public final void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
        expressionStack.peek().markHasCondition();
        basicConditionCheck(attributeName, operator);
        checkValue(negate, attributeName, type, operator, value);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Marks a possible open expression as having a condition and call {@link #basicConditionCheck(String, Operator)}
     * and {@link #checkValues(boolean, String, Class, Operator, List)}.
     * </p>
     */
    @Override
    public final void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
        expressionStack.peek().markHasCondition();
        basicConditionCheck(attributeName, operator);
        checkValues(negate, attributeName, type, operator, values);
    }
    
    /**
     * Method which will be called after an expression has ended.
     * @param state object containing the information if the ended expression had a condition specified or not
     */
    protected abstract void checkExpression(ExpressionState state);
    
    /**
     * This method will be called on every kind of condition and marks the basic check which will be required on every condition.
     * @param attributeName the name of the attribute
     * @param operator the used operator
     */
    protected abstract void basicConditionCheck(String attributeName, Operator operator);
    
    /**
     * This method will additionally be called on binary conditions to check the value.
     * @param negate the condition should be negated
     * @param attributeName the name of the attribute
     * @param type the type of the attribute
     * @param operator the operator to use in this condition
     * @param value the value to check in the condition
     */
    protected abstract void checkValue(boolean negate, String attributeName, Class<?> type, Operator operator, Object value);
    
    /**
     * This method will additionally be called on polyadic conditions to check the values.
     * @param negate the condition should be negated
     * @param attributeName the name of the attribute
     * @param type the type of the attribute
     * @param operator the operator to use in this condition
     * @param values the list of values to check in the condition
     */
    protected abstract void checkValues(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> value);
    
}
