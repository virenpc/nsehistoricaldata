package com.smartstream.conditions.util;

import java.util.List;

import com.viren.conditions.Operator;
import com.viren.conditions.Operator.Arity;

/**
 * Visitor for validate an expression or condition which fails on the first found error/problem.
 * A fail will throw a {@link ValidationException} which indicates that the expression or condition
 * isn't valid.
 * 
 * <p>
 * This is just a basic fast validator if you want to have a different validation rule feel free
 * to extend this validator. For a detailed validation of all errors/problems have a
 * look at {@link ExpressionDetailedValidator}.
 * </p>
 * 
 * <i>Info:</i> Not thread-safe!
 * 
 * @author brandstetter
 */
public class ExpressionFailFastValidator extends AbstractExpressionValidator {
    
    /**
     * Exception which indicates that the expression or condition isn't valid.
     * 
     * @author brandstetter
     */
    public static class ValidationException extends RuntimeException {
        private static final long serialVersionUID = 3632330641316340169L;

        public ValidationException(String message) {
            super(message);
        }
    }

    /** Flag indicating that null condition values are not raising an exception. */
    private final boolean allowNullValues;
    
    /**
     * Create an instance of this class.
     * @param allowNullValues indicates that null condition values should not raise an exception; will also disable arity argument count checks (see: {@link Arity#isArgumentsCountValid(List)})
     */
    public ExpressionFailFastValidator(boolean allowNullValues){
        this.allowNullValues = allowNullValues;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if the expression has a condition.
     * </p>
     * 
     * @throws ValidationException if the expression doesn't had a condition
     */
    @Override
    protected void checkExpression(ExpressionState state) {
        if( !state.hasCondition() ){
            throw new ValidationException("Expression doesn't have a condition!");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Depending on the {@link #allowNullValues} setting this method checks for a {@code null} value and also
     * if the given value is an instance of the given type.
     * </p>
     * 
     * @throws ValidationException if one of the following condition is satisfied:
     * <ul>
     *  <li>the value is null and {@link #allowNullValues} is false</li>
     * </ul>
     */
    @Override
    protected void checkValue(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
        if( !allowNullValues && value == null ){
            throw new ValidationException(String.format("No value for condition '%s %s %s' given!", (negate ? "!" : ""), attributeName, operator));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * If {@code null} values are not allowed this method will check if the given list of values is null or empty and if the
     * parameter count of the operator arity is valid (see: {@link Arity#isArgumentsCountValid(List)}).
     * <br />
     * <b style="color: red;">Caution:</b> This method doesn't check the content of the list and if those values are null or of the wrong type.
     * To achieve this you have to extends this class and overwrite this method!
     * </p>
     * 
     * @throws ValidationException if null values are not allowed and the given list is null or empty or {@link Arity#isArgumentsCountValid(List) return false
     */
    @Override
    protected void checkValues(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
        if( !allowNullValues ){
            if ( values == null || values.isEmpty() ){
                throw new ValidationException(String.format("No values for condition '%s%s %s' given!", (negate ? "! " : ""), attributeName, operator));
            }
            
            if( !operator.getArity().isArgumentsCountValid(values) ){
                Arity arity = operator.getArity();
                String maxValue = ( arity.getMaxArgumentCount() == Arity.INFINITE ) ? "INFINITE" : String.valueOf( arity.getMaxArgumentCount() );
                
                throw new ValidationException(String.format("The amount of values for the arity %s in condition '%s%s %s' isn't valid! (%d <= %d <= %s)", operator.getArity(), (negate ? "! " : ""), attributeName, operator, arity.getMinArgumentCount(), values.size(), maxValue));
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if an attribute name and an operator is given.
     * </p>
     * 
     * @throws ValidationException will be thrown if
     * <ul>
     *  <li>the given attribute name is null or empty</li>
     *  <li>the given operator is null</li>
     * </ul>
     */
    @Override
    protected void basicConditionCheck(String attributeName, Operator operator) {
        if( attributeName == null || attributeName.isEmpty() ){  // trim() not called because we don't want to be do clever and we don't want to create an additional string; can be overwritten if somebody needs it 
            throw new ValidationException("No attribute name in condition given!");
        }
        
        if( operator == null ){
            throw new ValidationException("No operator in condition with attribute name '" + attributeName + "' given!");
        }
    }

}
