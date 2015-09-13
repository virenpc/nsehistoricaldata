package com.smartstream.conditions.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smartstream.conditions.util.ExpressionDetailedValidator.ValidationMessages;
import com.smartstream.conditions.util.ExpressionFailFastValidator.ValidationException;
import com.viren.conditions.Conjunction;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;
import com.viren.conditions.Operator;
import com.viren.conditions.Visitor;
import com.viren.conditions.VisitorElement;
import com.viren.conditions.Conjunction.Type;

/**
 * Utility class for some commonly used actions on an expression or condition.
 * <p>
 * <b>Info:</b> Most of the operations use a visitor implementation to achieve their needs.
 * If you need to change the behavior of one of these methods just check if there is a concrete
 * visitor implementation you can extend or reuse.
 * </p>
 * 
 * @author brandstetter
 */
public final class ExpressionUtil {

    private ExpressionUtil() {
        throw new IllegalStateException("Instantiation of this class is not allowed!");
    }

    /**
     * Takes the given expression or condition and builds a readable string representation out of it.
     * 
     * <p>
     * This method uses the {@link ExpressionFormatter} visitor implementation to format the expression/condition.
     * If you want to have a different format either write your own visitor or extend the {@link ExpressionFormatter}.
     * But be careful to call either {@link #checkForCycle(Connectable)} or {@link #hasCycle(Connectable)} before you
     * call the {@link VisitorElement#visit(Visitor)} method because you receive a StackOverflow if there is a cycle
     * in your condition. 
     * </p>
     * 
     * @param connectable the expression or condition to format
     * @return a more readable string representation of the given expression or condition
     * @throws IllegalArgumentException if there is a cycle in given expression or condition
     */
    public static String formatExpression(Connectable connectable){
        if( connectable == null ) return "<NULL>";
        checkForCycle(connectable);
        
        ExpressionFormatter formatter = new ExpressionFormatter();
        connectable.visit(formatter);
        return formatter.toString();
    }
    
    /**
     * Checks if the given expression or condition has a cyclic reference.
     * @param connectable the expression or condition to check
     * @return true if a cyclic reference was found; false otherwise
     */
    public static boolean hasCycle(Connectable connectable){
        if( connectable == null ) return false;
        
        Set<Object> elements = new HashSet<>();
        
        ConnectableIterator iter = new ConnectableIterator(connectable);
        
        while( iter.hasNext() ){
            Connectable con = iter.next();
            if( !elements.add( con ) ){
                if( con.getConjunction() != null ){  // if the same object exists at the end of a conjunction it is OK
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the given expression or condition has a cyclic reference.
     * @param connectable the expression or condition to check
     * @throws IllegalArgumentException if a cyclic reference was found
     */
    public static void checkForCycle(Connectable connectable){
        if( hasCycle( connectable ) ){
            throw new IllegalArgumentException("The given element '" + connectable + "' contains a cycle!");
        }
    }
    
    /**
     * Copies the given expression or condition.
     * <p>
     * <b style="color: red;">Caution:</b> This will not copy the values of binary or polyadic conditions.
     * (But this can be achieved with a different {@link ExpressionCopier}.)
     * </p>
     * <p>
     * This method uses the {@link ExpressionCopier} visitor implementation to copy the expressions/conditions.
     * 
     * If you want to have a different copy strategy like skipping some entries or deep copying of specific values you can
     * either write your own visitor or extend the {@link ExpressionCopier}.
     * 
     * But be careful to call either {@link #checkForCycle(Connectable)} or {@link #hasCycle(Connectable)} before you
     * call the {@link VisitorElement#visit(Visitor)} method because you receive a StackOverflow if there is a cycle
     * in your condition. 
     * </p>
     * 
     * @param original the expression or condition to copy
     * @return a copy of the given expression or condition
     * @throws IllegalArgumentException if there is a cycle in given expression or condition
     */
    @SuppressWarnings("unchecked")  // NOPMD: is OK because of the cast after copy
    public static <T extends Connectable> T copy(T original){
        if( original == null ) return null;
        checkForCycle(original);
        
        ExpressionCopier copier = new ExpressionCopier();
        original.visit(copier);
        return (T) copier.getCopy();
    }
    
    /**
     * Entirely validates the given expression or condition.
     * <p>
     * Which means it will <b>not</b> stop validating after it detects an error, it will collect all
     * errors it has found and returns them as the result. (<i>Info:</i> Binary and polyadic conditions
     * without a value will only be warnings not errors.) 
     * </p>
     * 
     * <p>
     * This method uses the {@link ExpressionDetailedValidator} visitor implementation to validate the expressions/conditions.
     * 
     * If you want to have a different validation strategy either write your own visitor or extend the {@link ExpressionDetailedValidator}.
     * But be careful to call either {@link #checkForCycle(Connectable)} or {@link #hasCycle(Connectable)} before you
     * call the {@link VisitorElement#visit(Visitor)} method because you receive a StackOverflow if there is a cycle
     * in your condition. 
     * </p>
     * 
     * @param toValidate the expression or condition to validate
     * @return an object containing all collected validation errors and warnings
     * @throws IllegalArgumentException if there is a cycle in given expression or condition
     */
    public static ValidationMessages detailedValidation(Connectable toValidate){
        if( toValidate == null ) return new ValidationMessages(); // return null object
        checkForCycle(toValidate);
        
        ExpressionDetailedValidator validator = new ExpressionDetailedValidator();
        toValidate.visit(validator);
        return validator.getValidationMessages();
    }
    
    /**
     * Validates the given expression or condition, but throws an exception if it detects an error.
     * <p>
     * (<i>Info:</i> Binary and polyadic conditions without a value will fail and throw an exception.)
     * </p> 
     * 
     * <p>
     * This method uses the {@link ExpressionFailFastValidator} visitor implementation to validate the expressions/conditions.
     * 
     * If you want to have a different validation strategy either write your own visitor or extend the {@link ExpressionFailFastValidator}.
     * But be careful to call either {@link #checkForCycle(Connectable)} or {@link #hasCycle(Connectable)} before you
     * call the {@link VisitorElement#visit(Visitor)} method because you receive a StackOverflow if there is a cycle
     * in your condition. 
     * </p>
     * 
     * @param toValidate the expression or condition to validate
     * @throws IllegalArgumentException if there is a cycle in given expression or condition
     * @throws ValidationException on the first found error
     */
    public static void validate(Connectable toValidate) throws ValidationException {
        validate(toValidate, false);
    }
    
    /**
     * Validates the given expression or condition, but throws an exception if it detects an error.
     * <p>
     * (<i>Info:</i> Depending on the {@code allowNullValues} parameter binary and polyadic conditions without a value will fail and throw an exception.)
     * </p> 
     * 
     * <p>
     * This method uses the {@link ExpressionFailFastValidator} visitor implementation to validate the expressions/conditions.
     * 
     * If you want to have a different validation strategy either write your own visitor or extend the {@link ExpressionFailFastValidator}.
     * But be careful to call either {@link #checkForCycle(Connectable)} or {@link #hasCycle(Connectable)} before you
     * call the {@link VisitorElement#visit(Visitor)} method because you receive a StackOverflow if there is a cycle
     * in your condition. 
     * </p>
     * 
     * @param toValidate the expression or condition to validate
     * @param allowNullValues if true null values in binary and polyadic conditions will not be tread as error
     * @throws IllegalArgumentException if there is a cycle in given expression or condition
     * @throws ValidationException on the first found error
     */
    public static void validate(Connectable toValidate, boolean allowNullValues) throws ValidationException {
        if( toValidate == null ) return;
        checkForCycle(toValidate);
        
        ExpressionFailFastValidator validator = new ExpressionFailFastValidator(allowNullValues);
        toValidate.visit(validator);
    }
    
    /**
     * Tests if the given expressions or conditions are equal.
     * <p>
     * This method will do this equality check with the help of the default {@link EqualityHelper}.
     * If you want to check it in a more traditional way have a look at {@link EqualUtil}.
     * </p>
     * @param connectable1 the first expression or condition of the equality check 
     * @param connectable2 the second expression or condition of the equality check
     * @return true if the elements are equal, false otherwise
     * @throws IllegalArgumentException if there is a cycle in given expressions or conditions
     */
    public static boolean equal(Connectable connectable1, Connectable connectable2){
        return equal(connectable1, connectable2, null);
    }
    
    /**
     * Tests if the given expressions or conditions are equal.
     * 
     * <p>
     * This method will do this equality check with the provided {@link EqualityHelper}.
     * If you want to check it in a more traditional way have a look at {@link EqualUtil}.
     * </p>
     * 
     * @param connectable1 the first expression or condition of the equality check 
     * @param connectable2 the second expression or condition of the equality check
     * @param equlityHelper the provided {@link EqualityHelper} (if you specify {@code null} the default one will be used)
     * @return true if the elements are equal, false otherwise
     * @throws IllegalArgumentException if there is a cycle in given expressions or conditions
     */
    public static boolean equal(Connectable connectable1, Connectable connectable2, EqualityHelper equlityHelper){
        if( connectable1 == connectable2 ) return true; // NOPMD - this is OK because it's a basic object check for null and the same reference the rest of the equality check should be handled in the caller of this method
        if( connectable1 == null || connectable2 == null ) return false;
        if( !connectable1.getClass().equals(connectable2.getClass()) ) return false;  // shortcut if you want to compare to different type of objects
        
        checkForCycle(connectable1);
        checkForCycle(connectable2);
        
        EqualityHelper helper = (equlityHelper == null) ? new EqualityHelper() : equlityHelper;
        List<?> stack1 = helper.doVisit(connectable1);
        List<?> stack2 = helper.doVisit(connectable2);
        
        if( stack1.size() != stack2.size() ) return false;
        return stack1.equals(stack2);
    }
    
    /**
     * Iterates over all expression and conditions in the given expression or conditon.
     * 
     * <p>
     * This iterator will only walk through all expressions and conditions and return those and
     * so it will not return a conjunction!
     * </p>
     * 
     * @author brandstetter
     */
    public static final class ConnectableIterator implements Iterator<Connectable> {
        // Caution on modification: The entire cyclic reference check is based on this iterator implementation
        
        /** Special iterator for expressions which also checks the specified condition. */
        private ExpressionIterator expressionIter = null;  // will be not null if we have to iterate through an expression
        
        /** The element which will be checked for the next conjuncted expression or condition. */
        private Connectable currentElement = null;
        
        /**
         * Create a ConnectableIterator instance.
         * @param start the element to start with (will also be returned during {@link #next()} as long as it's not null)
         */
        public ConnectableIterator(Connectable start){
            this.currentElement = start;
        }

        @Override
        public boolean hasNext() {
            boolean expressionHasNext = false;
            
            if( expressionIter != null ){  // check if we have to iterate over the condition of an expression before we go on inside the conjunction
                expressionHasNext = expressionIter.hasNext();
                if( !expressionHasNext ){
                    expressionIter = null;
                }
            }
            
            return expressionHasNext || currentElement != null;
        }

        @Override
        public Connectable next() {
            Connectable back = null;
            
            if( expressionIter != null ){  // check if we have to return the value of a condition inside of an expression before we can go on with the conjunction
                back = expressionIter.next();                
            }
            
            if( back == null ){
                back = currentElement;
                
                if( currentElement instanceof Expression ){  // if the element is an expression iterate over it's condition and conjunction before we can go on with its conjunction
                    expressionIter = new ExpressionIterator((Expression)currentElement);
                    currentElement = null;
                } else {
                    if( currentElement != null ){
                        Conjunction conj = currentElement.getConjunction();
                        currentElement = ( conj != null ) ? conj.getNextItem() : null; // get next element of the conjunction
                    }
                }
            }
            
            return back;
        }

        /**
         * {@inheritDoc}
         * @throws UnsupportedOperationException this is not allowed
         */
        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Remove not allowed!");
        }
        
    }
    
    /**
     * Internal iterator which iterates over the condition and conjunction of an expression.
     * 
     * @author brandstetter
     *
     */
    private static class ExpressionIterator implements Iterator<Connectable> {
        
        // two iterators to check one for conditions the the other one for conjunctions
        private ConnectableIterator conjcuntionIter, conditionIter;
        
        public ExpressionIterator(Expression expression) {
            if( expression != null ){
                conditionIter = createIter( expression.getCondition() );
                
                Conjunction con = expression.getConjunction();
                if( con != null ){
                    conjcuntionIter = createIter(con.getNextItem());
                }
            }
        }
        
        /**
         * Creates a iterator for the given {@link Connectable}.
         * @param con the element to iterate through
         * @return an iterator for the given element or null if the given element was null
         */
        private static ConnectableIterator createIter(Connectable con){
            return ( con != null ) ? new ConnectableIterator(con) : null;
        }
        
        @Override
        public boolean hasNext() {
            // first iterate over the expression condition
            if( conditionIter != null && conditionIter.hasNext()){
                return true;
            }
            
            // afterwards iterate over the expression conjunction
            if( conjcuntionIter != null && conjcuntionIter.hasNext()){
                return true;
            }
            
            return false;
        }

        @Override
        public Connectable next() {
            Connectable nextElement = null;
            
            // first iterate over the expression condition
            if( conditionIter != null ){
                nextElement = conditionIter.next();
            }
            
            // afterwards iterate over the expression conjunction
            if( nextElement == null && conjcuntionIter != null ){
                nextElement = conjcuntionIter.next();
            }
            
            return nextElement;
        }

        /**
         * {@inheritDoc}
         * @throws UnsupportedOperationException this is not allowed
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not allowed!");
        }
    }
    
    /**
     * Visitor which builds a stack of all visited expression and condition values.
     * <p>
     * To make an equality check you have to build a stack for the orginal object and
     * the object you want to compare. The two stack can afterwards be compared with
     * the normal {@link List#equals(Object)} method.
     * </p>
     * 
     * <p>
     * This is just a basic helper if you want to skip some attributes or change the 
     * equality check of the elements feel free to extend this equality helper and add
     * objects with different equals method to it.
     * </p>
     * 
     * <i>Info:</i> Not thread-safe!
     * 
     * @author brandstetter
     */
    public static class EqualityHelper implements Visitor {
        protected List<Object> stack = new LinkedList<>();
        
        /**
         * Simple markers to store group bounds and negations.
         * 
         * @author brandstetter
         */
        public static enum Marker {
            /** Marks a negate of the expression or condition. */
            NEGATE,
            /** Marks a expression start. */
            EXPRESSION_START,
            /** Marks a expression end. */
            EXPRESSION_END;
        }
        
        /**
         * Puts the {@link Marker#NEGATE} and {@link Marker#EXPRESSION_START} to the stack.
         * {@inheritDoc}
         */
        @Override
        public void startExpression(boolean negate) {
            if( negate ){
                stack.add(Marker.NEGATE);
            }
            stack.add(Marker.EXPRESSION_START);
        }

        /**
         * Puts the {@link Marker#EXPRESSION_END} to the stack.
         * {@inheritDoc}
         */
        @Override
        public void endExpression() {
            stack.add(Marker.EXPRESSION_END);
        }

        /**
         * Puts the given conjunction type to the stack.
         * {@inheritDoc}
         */
        
        @Override
        public void conjunct(Type conjunctionType) {
            stack.add(conjunctionType);
        }

        /**
         * Puts the given parameter to the stack.
         * {@inheritDoc} 
         */
        @Override
        public void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator) {
            markConditionBasics(negate, attributeName, type, operator);            
        }

        /**
         * Puts the given parameter to the stack.
         * {@inheritDoc} 
         */
        @Override
        public void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
            markConditionBasics(negate, attributeName, type, operator);
            stack.add(value);
        }

        /**
         * Puts the given parameter to the stack.
         * {@inheritDoc} 
         */
        @Override
        public void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
            markConditionBasics(negate, attributeName, type, operator);
            stack.add(values);
        }
        
        /**
         * Helper method to put the condition basic to the stack.
         * @param negate 
         * @param attributeName
         * @param type
         * @param operator
         */
        protected void markConditionBasics(boolean negate, String attributeName, Class<?> type, Operator operator){
            if( negate ){
                stack.add(Marker.NEGATE);
            }
            
            stack.add(attributeName);
            stack.add(type);
            stack.add(operator);
        }
        
        /**
         * Builds the stack and returns it.
         * This method will reset the stack by creating a new one a calls the visit method on the
         * given {@link VisitorElement}. That makes an object of this class reusable.
         * @param element the element to visit
         * @return the stack which should be compared with an other one
         */
        public List<Object> doVisit(VisitorElement element){
            stack = new LinkedList<>();
            element.visit(this);
            return stack;
        }
    }
}
