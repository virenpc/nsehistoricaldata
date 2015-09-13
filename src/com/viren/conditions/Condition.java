package com.viren.conditions;

import java.util.EnumSet;
import java.util.Objects;

import com.viren.conditions.Conjunction.Type;

/**
 * Abstract basic object for all concrete conditions.
 * 
 * <p>
 * This object handles all common elements a condition is made of.
 * <br />
 * <b style="color: red;">Caution:</b> It is not intended to extend this class by somebody else!
 * </p>
 * 
 * @see <a href="http://confluence/display/Morph/MFF+%28Filter+Framework%29">The Filter Framework confluence page.</a>
 * 
 * <i>Info:</i> Not thread-safe!
 * 
 * @author brandstetter
 *
 * @param <T> The attribute type of this condition requires.
 */
public abstract class Condition<T> implements Connectable, Negatable, VisitorElement {
    
    /** Holds a conjunction to an other expression or condition. */
    private Conjunction conjunction;
    
    /** The attribute type this condition handles. */
    private final Class<T> type;
    
    /** The attribute name of the condition. */
    private String attributeName;
    
    /** The operator of the condition. */
    private Operator operator;
    
    /** Marks the condition as negated or not. */
    private boolean negate;
    
    /**
     * Sole constructor.
     * @param type the attribute type this condition will handle
     * @param attributeName the name of the attribute (can be {@code null})
     * @throws NullPointerException if the given type is null
     */
    protected Condition(Class<T> type, String attributeName){
        this.type = Objects.requireNonNull(type, "No attribute type given!");
        this.attributeName = attributeName;
    }

    @Override
    public Conjunction getConjunction() {
        return conjunction;
    }

    @Override
    public Condition<T> setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
        return this;
    }
    
    @Override
    public Condition<T> setConjunction(Type conjunctionType, Connectable nextItem) {
        return setConjunction( new Conjunction(conjunctionType, nextItem) );
    }

    /**
     * Returns the name of the attribute the concrete condition will handle.
     * @return the attribute name of the condition
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Set the attribute name this condition will handle.
     * @param attributeName the attribute name of the condition
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Returns the operator of this condition. 
     * @return the operator of this condition
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Specify the operator of this condition.
     * @param operator the operator of this condition
     */
    public abstract void setOperator(Operator operator);
    
    /**
     * Specify the operator of this condition but check if the operator has the expected arity of the concrete condition.
     * 
     * <p>
     * This method should be used by the concrete implementations of the {@link #setOperator(Operator)}.
     * </p>
     * 
     * @param operator the operator to set
     * @param expected the arity this operator should have
     * @throws IllegalArgumentException if the operator doesn't have one of the given arity
     */
    protected final void setOperatorWithArityCheck(Operator operator, EnumSet<Operator.Arity> expected){
        this.operator = checkArity(operator, expected);
    }

    /**
     * Returns the object type this condition should handle.
     * @return the object type this condition should handle
     */
    public final Class<T> getType() {
        return type;
    }
    
    @Override
    public Condition<T> setNegate(boolean negate) {
        this.negate = negate;
        return this;
    }

    @Override
    public boolean isNegate() {
        return negate;
    }
    
    /**
     * Checks if the given operator has one of the specified arity.
     * @param operator the operator to check
     * @param expected the arity the given operator should have
     * @return the given operator if it has a valid arity (or null if the given operator is null)
     * @throws IllegalArgumentException if the operator doesn't have one of the given arity
     */
    protected static Operator checkArity(Operator operator, EnumSet<Operator.Arity> expected){
        if( operator != null && !expected.contains(operator.getArity())) {
            throw new IllegalArgumentException(operator + " is not of expected arity '" + expected + "'!");
        }
        return operator;
    }
    
    /**
     * Common method to visit the conjunction if it's specified.
     * <p>
     * The {@link Conjunction#visit(Visitor)} will only be called if the specified {@link #conjunction} isn't {@code null}.
     * </p>
     * @param visitor a concrete visitor implementation
     */
    protected final void visitConjunction(Visitor visitor){
        if( conjunction != null ){
            conjunction.visit(visitor);
        }
    }

    /**
     * Helper method to build the basic toString output every condition has.
     * @return a newly created StringBuilder which contains the basic toString() information
     */
    protected final StringBuilder buildBasicToString(){
        StringBuilder basics = new StringBuilder();
        
        if(negate){
            basics.append("NOT ");
        }
        
        basics.append(attributeName).append(" (").append(type).append(") ").append(operator);
        
        return basics;
    }

    /**
     * Helper method to append the conjunction type information to given StringBuilder, or if none is given to a new one created with {@link #buildBasicToString()}.
     * <p>
     * This method will only append the conjunction type and not the full conjunction, because this could lead to big problems if you have cycle references in
     * your expression. Additional if the expression is very long and complex it could take very long to build the toString() representation and maybe you end up
     * in a stack-overflow.
     * </p>
     * @param toAppendParam a StringBuilder to which this information should be added (@code null} is allowed) 
     * @return the given StringBuilder or a new one created with {@link #buildBasicToString()} if no StringBuilder was given (but both will have the information appended)
     */
    protected final StringBuilder appendConjunction(StringBuilder toAppendParam){
    	StringBuilder toAppend = toAppendParam;
        if( toAppend == null ){
            toAppend = buildBasicToString();
        }
        
        if( conjunction != null ){
            // never add the conjunction#getNextItem() information to the StringBuilder (hint: cyclic references)
            // if you want to have a full string representation of your expression use com.smartstream.conditions.util.ExpressionUtil.formatExpression(Connectable)
            toAppend.append(' ').append(conjunction.getConjunctionType()).append(" ...");
        }
        
        return toAppend;
    }
}
