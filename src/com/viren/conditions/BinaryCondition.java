package com.viren.conditions;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EnumSet;

import com.smartstream.conditions.util.DataTypeUtil;
import com.viren.conditions.Operator.Arity;

/**
 * This class represents a binary condition.
 * 
 * <p>
 * This Condition can be used for all Operators with the arity {@link Arity#BINARY}.
 * An example for a binary condition would be an "equals" condition: {@code ACCESS_AREA_ID = 12}
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
public final class BinaryCondition<T> extends Condition<T> implements Serializable {
    
    /** The serial version UID for this class. */
    private static final long serialVersionUID = 7801611240722937445L;
    
    /** The value for the condition. */
    private T value;

    /**
     * Create an empty BinaryCondtion.
     * @param type the attribute type
     * @throws NullPointerException if the given type is null
     */
    public BinaryCondition(Class<T> type) {
        this(type, null, null, null);
    }
    
    /**
     * Create an BinaryCondtion with an attribute name.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @throws NullPointerException if the given type is null
     */
    public BinaryCondition(Class<T> type, String attributeName) {
        this(type, attributeName, null, null);
    }
    
    /**
     * Create an BinaryCondtion with an attribute name and operator.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @param operator the binary operator of this condition
     * @throws NullPointerException if the given type is null
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#BINARY}
     */
    public BinaryCondition(Class<T> type, String attributeName, Operator operator) {
        this(type, attributeName, operator, null);
    }
    
    /**
     * Create an BinaryCondtion with an attribute name, operator and value.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @param operator the binary operator of this condition
     * @param value the value for the condition
     * @throws NullPointerException if the given type is null
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#BINARY}
     */
    public BinaryCondition(Class<T> type, String attributeName, Operator operator, T value) {
        super(type, attributeName);
        setOperator(operator);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#BINARY}
     */
    @Override
    public final void setOperator(Operator operator) {
        setOperatorWithArityCheck(operator, EnumSet.of(Arity.BINARY));
    }

    /**
     * Returns the value for the condition.
     * @return the value for the condition
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value for this condition
     * @param value the value for this condition
     * @return the this reference to use it in a builder style
     * @throws IllegalArgumentException if the given value is not an instance of the type ({@link #getType()}) specified in the condition
     */
    public BinaryCondition<T> setValue(T value) {
        if( value != null ) {  // prevent wrong value types
            if( !DataTypeUtil.isInstanceOf(getType(), value) ){
                throw new IllegalArgumentException("The given value '" + value + "' is not an instance of " + getType());
            }
        }
        
        this.value = value;
        return this;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Calls the following {@link Visitor} methods in the specified order.
     * <ol>
     *  <li>{@link Visitor#visitBinaryCondition(boolean, String, Class, Operator, Object)}</li>
     *  <li>{@link #visitConjunction(Visitor)}</li>
     * </ol>
     * </p>
     */
    @Override
    public void visit(Visitor visitor) {
        visitor.visitBinaryCondition(isNegate(), getAttributeName(), getType(), getOperator(), value);
        visitConjunction(visitor);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = buildBasicToString().append(' ').append(value);
        return appendConjunction(sb).toString();
    }
    
    // ---- object serialization proxy pattern ----
    private Object writeReplace(){
        return new BinaryCondtionSerializationProxy(this);
    }
    
    private static final class BinaryCondtionSerializationProxy implements Serializable {
        /** The serial version UID for this class. */
        private static final long serialVersionUID = -4628229755400654398L;
        
        private final boolean negate;
        private final Object value;
        private final String attributeName;
        private final Operator operator;
        private final Class<?> type;
        private final Conjunction conjunction;
        
        public BinaryCondtionSerializationProxy( BinaryCondition<?> target ){
            negate = target.isNegate();
            attributeName = target.getAttributeName();
            operator = target.getOperator();
            type = target.getType();
            value = target.getValue();
            conjunction = target.getConjunction();
        }
        
        private Object readResolve(){
            @SuppressWarnings({ "rawtypes", "unchecked" }) BinaryCondition con = new BinaryCondition(type, attributeName, operator, value);
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
