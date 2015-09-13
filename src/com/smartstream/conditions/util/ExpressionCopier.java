package com.smartstream.conditions.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.viren.conditions.BinaryCondition;
import com.viren.conditions.Condition;
import com.viren.conditions.Conjunction;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;
import com.viren.conditions.Operator;
import com.viren.conditions.PolyadicCondition;
import com.viren.conditions.UnaryCondition;
import com.viren.conditions.Visitor;
import com.viren.conditions.Conjunction.Type;

/**
 * Visitor for copying an expression or condition.
 * 
 * <p>
 * This is just a basic copier if you want to have a different behavior like skipping some
 * attributes or deep copy of condition values feel free to extend this formatter.
 * </p>
 * 
 * <i>Info:</i> Not thread-safe!
 * 
 * @author brandstetter
 */
public class ExpressionCopier implements Visitor {
    
    /** The copy which will be returned after the copy visiting has been done. */
    protected Connectable copy;
    
    /** Reference to the currently used Connectable to build the conjunction chain with {@link Connectable#setConjunction(Type, Connectable)}. */
    private Connectable current;
    
    /** Stack of expression to build groups (to know when to enter an expression or leave it). */
    private Deque<Expression> expressionStack = new LinkedList<>();
    
    /** The type of conjunction which should be used when {@link Connectable#setConjunction(Type, Connectable)} is called. */
    private Type conjunctionType;

    /**
     * Sole constructor.
     */
    public ExpressionCopier() {}

    /**
     * Creates a new {@link Expression} object.
     * This will put the new {@link Expression} object onto a stack to know that the condition
     * of the expression should be filled and not its conjunction. The expression will also be
     * conjuncted with the {@link #current} element.
     * 
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    public void startExpression(boolean negate) {
        Expression newExpression = new Expression().setNegate(negate);
        expressionStack.push(newExpression);
        
        conjunctNew(newExpression);
    }

    /**
     * Finishes the expression.
     * The top expression will be removed from the stack and the reference will be stored in the {@link #current} element.
     * This leads the next visited condition or expression to be conjuncted with this expression.
     * 
     * <p>
     * {@inheritDoc}
     * </p> 
     */
    @Override
    public void endExpression() {
        current = expressionStack.pop();
    }

    /**
     * Keep the given conjunction type for the next {@link #conjunctNew(Connectable)} call.
     * 
     * <p>
     * {@inheritDoc}
     * </p> 
     */
    @Override
    public void conjunct(Type conjunctionType) {
        this.conjunctionType = conjunctionType;
    }

    /**
     * Creates a new UnaryCondition with the given values and conjuncts it with the {@link #current} element.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    public void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator) {
        conjunctNew( new UnaryCondition<>(type, attributeName, operator).setNegate(negate) );
    }

    
    /**
     * Creates a new BinaryCondition with the given values and conjuncts it with the {@link #current} element.
     * <p>
     * <b>Hint:</b> This method will call the {@link #copyValue(Object)} hook method before the value is set to
     * the binary condition. The hook method in this class will only return the given object but if you want
     * to make a deep copy of the object too just overwrite the {@link #copyValue(Object)} method.
     * </p>
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    public void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
        @SuppressWarnings("unchecked") BinaryCondition<Object> con = (BinaryCondition<Object>) new BinaryCondition<>(type, attributeName, operator);
        con.setNegate(negate);
        con.setValue(copyValue(value));
        
        conjunctNew( con );
    }

    /**
     * Creates a new PolyadicCondition with the given values and conjuncts it with the {@link #current} element.
     * <p>
     * <b>Hint:</b> This method will call the {@link #copyValues(List)} hook method before the values are set to
     * the polyadic condition. The hook method in this class will only return the given list but if you want
     * to make a deep copy of the list and objects in it just overwrite the {@link #copyValues(List)} method.
     * </p>
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    public void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
        @SuppressWarnings("unchecked")
        PolyadicCondition<Object> con = (PolyadicCondition<Object>) new PolyadicCondition<>(type, attributeName, operator);
        con.setNegate(negate);
        con.getValues().addAll(copyValues(values));
        
        conjunctNew( con );
    }

    /**
     * Conjuncts the given {@link Connectable} with the current one.
     * This method will be called whenever an expression or condition was created.
     * To build the {@link Conjunction} object it will use the conjunction type
     * stored during the {@link #conjunct(Type)} visit call.
     * @param newElement the element to conjunct with the current one
     */
    protected void conjunctNew(Connectable newElement){
        if( copy == null ){  // the copy process has just begun
            copy = newElement;
        } else {
            if( conjunctionType != null ){ // if the conjunct(Type) method was already called we can conjunct the current element with the new one
                current.setConjunction(conjunctionType, newElement);
                conjunctionType = null;
            } else { // the conjunct(Type) method wasn't called so far so we just entered an expression and we have to fill it's condition instead of it's conjunction which will be done afterwards
                Expression lastExpression = expressionStack.peek();
                
                if(lastExpression.equals(newElement)){  // means that an expression is the condition of an expression
                    // don't push it back to the stack because the stack is just
                    lastExpression = (Expression)current;
                }
                
                lastExpression.setCondition(newElement);
            }
        }
        
        current = newElement;
    }
    
    /**
     * Hook method which will be called whenever a value of a binary condition should be copied.
     * @param value the value to copy
     * @return the value which should be stored in the new condition (this method will only return the given object --> no copy)
     */
    protected Object copyValue(Object value){
        return value;
    }
    
    /**
     * Hook method which will be called whenever a values of a polyadic condition should be copied.
     * This method creates a new list and copies the references of the given value list, but it doesn't make
     * a deep copy of the objects from the value list. 
     * @param values the values to copy
     * @return the values which should be stored in the new condition (should never return {@code null})
     */
    protected List<?> copyValues(List<?> values){
        if( values == null ) return Collections.emptyList();
        return new ArrayList<>(values);
    }
    
    /**
     * Returns the copied {@link Expression} or {@link Condition} after the visitor has finished it's work.
     * @return the copied {@link Expression} or {@link Condition}
     */
    public Connectable getCopy(){
        return copy;
    }
}
