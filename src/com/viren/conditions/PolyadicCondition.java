package com.viren.conditions;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.smartstream.conditions.util.DataTypeUtil.TypeSafeList;
import com.viren.conditions.Operator.Arity;

/**
 * This class represents a polyadic condition.
 * 
 * <p>
 * This Condition can be used for all Operators with the arity {@link Arity#POLYADIC} or {@link Arity#TERNARY}.
 * Examples for polyadic conditions are:
 * 
 * <ul>
 *  <li>the "in" operator: {@code ACCESS_AREA_ID in (12, 9, 42)}</li>
 *  <li>the "between" operator: {@code ACCESS_AREA_ID between 12 and 42}</li>
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
public final class PolyadicCondition<T> extends Condition<T> implements Serializable {
    
    /** The serial version UID for this class. */
    private static final long serialVersionUID = 2815127449728471957L;
    
    /** The values for the condition. */
    private List<T> values;

    /**
     * Create an empty PolyadicCondition.
     * @param type the attribute type
     * @throws NullPointerException if the given type is null
     */
    public PolyadicCondition(Class<T> type) {
        this( type, null, null );
    }
    
    /**
     * Create an PolyadicCondition with an attribute name.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @throws NullPointerException if the given type is null
     */
    public PolyadicCondition(Class<T> type, String attributeName) {
        this( type, attributeName, null );
    }
    
    /**
     * Create an PolyadicCondition with an attribute name, operator and values.
     * @param type the attribute type
     * @param attributeName the name of the attribute to check
     * @param operator the binary operator of this condition
     * @param values the values for the condition
     * @throws NullPointerException if the given type is null
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#POLYADIC} or {@link Arity#TERNARY} 
     */
    @SafeVarargs
    public PolyadicCondition(Class<T> type, String attributeName, Operator operator, T... values) {
        super(type, attributeName);
        setOperator(operator);
        
        if (values != null && values.length > 0){
            getValues().addAll(Arrays.asList(values));
        }
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if the arity of the given operator is not of type {@link Arity#POLYADIC} or {@link Arity#TERNARY}
     */
    @Override
    public final void setOperator(Operator operator) {
        setOperatorWithArityCheck(operator, EnumSet.of(Arity.POLYADIC, Arity.TERNARY));
    }
    
    /**
     * Returns the values for the condition.
     * @return the values for the condition (will never return {@code null})
     */
    public List<T> getValues(){
        if( values == null ){
            values = new TypeSafeList<T>( getType() );
        }
        
        return values;
    }
    
    /**
     * Sets the values for this condition
     * @param values the values for this condition
     * @return the this reference to use it in a builder style
     * * @throws IllegalArgumentException if one of the given values is not an instance of the type ({@link #getType()}) specified in the condition
     */
    @SafeVarargs
    public final PolyadicCondition<T> values(T... values){
        List<T> valuesList = getValues();
        
        if( !valuesList.isEmpty() ){
            valuesList.clear();
        }
        
        valuesList.addAll(Arrays.asList(values));
        return this;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Calls the following {@link Visitor} methods in the specified order.
     * <ol>
     *  <li>{@link Visitor#visitPolyadicCondition(boolean, String, Class, Operator, List)}</li>
     *  <li>{@link #visitConjunction(Visitor)}</li>
     * </ol>
     * </p>
     */
    @Override
    public void visit(Visitor visitor) {
        visitor.visitPolyadicCondition(isNegate(), getAttributeName(), getType(), getOperator(), values != null ? Collections.unmodifiableList(values) : null);
        visitConjunction(visitor);
    }

    @Override
    public String toString() {
        StringBuilder sb = buildBasicToString().append(' ').append(values);
        return appendConjunction(sb).toString();
    }
    
    // ---- object serialization proxy pattern ----
    private Object writeReplace(){
        return new PolyadicCondtionSerializationProxy(this);
    }
    
    private static final class PolyadicCondtionSerializationProxy implements Serializable {
        /** The serial version UID for this class. */
        private static final long serialVersionUID = -7617360758914352550L;
        
        private final boolean negate;
        private final Object[] values;
        private final String attributeName;
        private final Operator operator;
        private final Class<?> type;
        private final Conjunction conjunction;
        
        public PolyadicCondtionSerializationProxy( PolyadicCondition<?> target ){
            negate = target.isNegate();
            attributeName = target.getAttributeName();
            operator = target.getOperator();
            type = target.getType();
            values = target.getValues().toArray(); // convert to array just for safety if the list implementation isn't serializable
            conjunction = target.getConjunction();
        }
        
        @SuppressWarnings("unchecked")
        private Object readResolve(){
            @SuppressWarnings("rawtypes") PolyadicCondition con = new PolyadicCondition(type, attributeName, operator);
            if( values != null ){
                con.getValues().addAll(Arrays.asList(values));
            }
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
