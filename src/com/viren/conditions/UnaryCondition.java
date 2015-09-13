package com.viren.conditions;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EnumSet;

import com.viren.conditions.Operator.Arity;

/**
 * This class represents a unary condition.
 * 
 * <p>
 * This Condition can be used for all Operators with the arity {@link Arity#UNARY}.
 * Examples for unary conditions are:
 * 
 * <ul>
 *  <li>the "null" operator: {@code ACCESS_AREA_ID is null}</li>
 *  <li>the "not null" operator: {@code ACCESS_AREA_ID is not null}</li>
 * </ul>
 * 
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
public final class UnaryCondition<T> extends Condition<T> implements Serializable {

    /** The serial version UID for this class. */
    private static final long serialVersionUID = 3367181997415828916L;

    /**
     * Create an empty UnaryCondtion.
     * @param type the attribute type
     * @throws NullPointerException if the given type is null
     */
    public UnaryCondition(Class<T> type) {
        this( type, null, null );
    }
    
    /**
     * Create an UnaryCondtion with an attribute name.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @throws NullPointerException if the given type is null
     */
    public UnaryCondition(Class<T> type, String attributeName) {
        this( type, attributeName, null );
    }
    
    /**
     * Create an UnaryCondtion with an attribute name and operator.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @param operator the binary operator of this condition
     * @throws NullPointerException if the given type is null
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#UNARY}
     */
    public UnaryCondition(Class<T> type, String attributeName, Operator operator) {
        super(type, attributeName);
        
        setOperator(operator);
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#UNARY}
     */
    @Override
    public final void setOperator(Operator operator) {
        setOperatorWithArityCheck(operator, EnumSet.of(Arity.UNARY));
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Calls the following {@link Visitor} methods in the specified order.
     * <ol>
     *  <li>{@link Visitor#visitUnaryCondition(boolean, String, Class, Operator)}</li>
     *  <li>{@link #visitConjunction(Visitor)}</li>
     * </ol>
     * </p>
     */
    @Override
    public void visit(Visitor visitor) {
        visitor.visitUnaryCondition(isNegate(), getAttributeName(), getType(), getOperator());
        visitConjunction(visitor);
    }
    
    @Override
    public String toString() {
        return appendConjunction(null).toString();
    }
    
    // ---- object serialization proxy pattern ----
    private Object writeReplace(){
        return new UnaryCondtionSerializationProxy(this);
    }
    
    private static final class UnaryCondtionSerializationProxy implements Serializable {
        /** The serial version UID for this class. */
        private static final long serialVersionUID = 4603166378516193353L;
        
        private final boolean negate;
        private final String attributeName;
        private final Operator operator;
        private final Class<?> type;
        private final Conjunction conjunction;
        
        public UnaryCondtionSerializationProxy( UnaryCondition<?> target ){
            negate = target.isNegate();
            attributeName = target.getAttributeName();
            operator = target.getOperator();
            type = target.getType();
            conjunction = target.getConjunction();
        }
        
        @SuppressWarnings("unchecked")
        private Object readResolve(){
            @SuppressWarnings("rawtypes") UnaryCondition con = new UnaryCondition(type, attributeName, operator);
            con.setNegate(negate);
            con.setConjunction(conjunction);
            return con;
        }
    }
    
    private void readObject(ObjectInputStream ois) throws InvalidObjectException{
        // just for saftey's sake to prevent evil attackers
        throw new InvalidObjectException("Proxy required!");
    }
}
