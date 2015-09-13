package com.smartstream.conditions.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.viren.conditions.Operator;
import com.viren.conditions.Visitor;
import com.viren.conditions.Conjunction.Type;

/**
 * Visitor for formatting an expression or condition.
 * 
 * <p>
 * This is just a basic formatter if you want to have a different format feel free
 * to extend this formatter.
 * </p>
 * 
 * <i>Info:</i> Not thread-safe!
 * 
 * @author brandstetter
 */
public class ExpressionFormatter implements Visitor {
    
    /** Holds the the formatted output. Every method in this class will to this builder to append something to the final output. */
    protected final StringBuilder builder = new StringBuilder();
    
    /** Some common formats for values. (already exiting: date and string-under-quotes formatter) */
    protected final Map<Class<?>, ValueFormatter> formatter = new HashMap<>();

    public ExpressionFormatter() {
        formatter.put(Date.class, new ValueFormatter(){
            private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy - HH:mm"); // also not thread-safe!
            
            @Override
            public void formatValue(Object toFormat, StringBuilder builder) {
                String formattedDate;
                try{
                    formattedDate = formatter.format(toFormat);
                } catch ( IllegalArgumentException iae ){
                    // should rarely happen if you use builder and check via ExpressionUtil.validate() but it could
                    formattedDate = iae.getMessage();
                }
                
                builder.append('/').append(formattedDate).append('/');
            }}
        );
        
        formatter.put(String.class, new ValueFormatter(){
            @Override
            public void formatValue(Object value, StringBuilder toAppend) {
                toAppend.append('"').append(value).append('"');
            }
        });
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This will open an expression with a "(". Depending on the given negate parameter
     * it will also prepend the negate string representation (see: {@link #appendNegate(boolean)}).
     * </p>
     */
    @Override
    public void startExpression(boolean negate) {
        if(builder.length() != 0){ // do not append a ' ' on the beginning of the string
            builder.append(' ');
        }
        appendNegate(negate);
        builder.append('(');
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will close the expression with a " )".
     * </p>
     */
    @Override
    public void endExpression() {
        builder.append(" )");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Adds one of the following string representations
     * <ul>
     *  <li>{@link Type#AND} - " &&"</li>
     *  <li>{@link Type#OR} - " ||"</li>
     *  <li>otherwise - {@link Type#toString()}</li>
     * </ul>
     * </p>
     */
    @Override
    public void conjunct(Type conjunctionType) {
        switch (conjunctionType) {
            case AND:
                builder.append(" &&");
                break;
            case OR:
                builder.append(" ||");
                break;

            default:
                builder.append( ' ' ).append(conjunctionType);
                break;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only calls the {@link #appendConditionBasic(boolean, String, Operator)} method, because an UnaryCondition
     * doesn't have any additional values.
     * </p>
     */
    @Override
    public void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator) {
        appendConditionBasic(negate, attributeName, operator);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calls the {@link #appendConditionBasic(boolean, String, Operator)} method for formatting the basics and afterwards
     * it will format the given value (see: {@link #appendFormattedValue(Class, Object)}. 
     * </p>
     */
    @Override
    public void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
        appendConditionBasic(negate, attributeName, operator);
        appendFormattedValue(type, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calls the {@link #appendConditionBasic(boolean, String, Operator)} method for formatting the basics and afterwards
     * it will format the list of given values (see: {@link #appendFormattedValue(Class, Object)}. Depending on the operator
     * the result will differ which means {@link Operator#BETWEEN} will result in {@code "between **formattedvalue** and **formattedvalue**"}
     * and everything else will only be {@code "( **formattedvalue**,**formattedvalue**, ... )"}.  
     * </p>
     */
    @Override
    public void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
        appendConditionBasic(negate, attributeName, operator);
        
        if( values != null ){
            if( operator != null && operator.equals(Operator.BETWEEN) ){
                Object fromValue = ( values.size() >= 1 ) ? values.get(0) : null;
                Object toValue = ( values.size() >= 2 ) ? values.get(1) : null;
                
                appendFormattedValue(type, fromValue);
                builder.append(" and");
                appendFormattedValue(type, toValue);
            } else {
                builder.append(" (");
                
                for(Object value : values){
                    appendFormattedValue(type, value);
                    builder.append(',');
                }
                
                if( !values.isEmpty() ){
                    builder.setLength( builder.length() - 1 ); // remove the trailing ','
                }
                
                builder.append(" )");
            }
        } else {
            builder.append(' ');
            appendFormattedNull(type);
        }
    }
    
    /**
     * Appends the the basic parameter every condition has.
     * @param negate determines if the condition should be negated or not (see: {@link #appendNegate(boolean)})
     * @param attributeName the name of the attribute
     * @param operator the condition operator (see: {@link #appendOperator(Operator)})
     */
    protected void appendConditionBasic(boolean negate, String attributeName, Operator operator){
        builder.append(' ');
        appendNegate(negate);
        builder.append(attributeName);
        appendOperator(operator);
    }

    /**
     * If the given value is {@code true} it will append a "!" to mark the following expression or condition to be negated.
     * @param negate determines if the expression or condition should be negated
     */
    protected void appendNegate(boolean negate){
        if( negate ){
            builder.append('!');
        }
    }
    
    /**
     * Appends a string representation of the operator to the output.
     * <ul>
     * <li>{@code null} - ""</li>
     * <li>{@link Operator#EQUALS} - "=="</li>
     * <li>{@link Operator#NOT_EQUALS} - "!="</li>
     * <li>{@link Operator#GREATER_THAN} - ">"</li>
     * <li>{@link Operator#GREATER_THAN_OR_EQUALS} - ">="</li>
     * <li>{@link Operator#LESS_THAN} - "<"</li>
     * <li>{@link Operator#LESS_THAN_OR_EQUALS} - ">="</li>
     * <li>{@link Operator#BETWEEN} - "between" (the "and" will be added during append of the values; see: {@link #visitPolyadicCondition(boolean, String, Class, Operator, List)})</li>
     * <li>{@link Operator#IN} - "in"</li>
     * <li>{@link Operator#NOT_IN} - "not in"</li>
     * <li>{@link Operator#LIKE} - "like"</li>
     * <li>{@link Operator#NOT_LIKE} - "not like"</li>
     * <li>{@link Operator#IS_NULL} - "isNull"</li>
     * <li>{@link Operator#IS_NOT_NULL} - "isNotNull"</li>
     * <li>{@ default} - {@link Operator#toString()}</li>
     * </ul>
     * @param operator the operator to append
     */
    protected void appendOperator(Operator operator){
        if( operator != null ){
            builder.append(' ');
            
            switch (operator) {
                case BETWEEN:
                    builder.append("between");
                    break;
                    
                case EQUALS:
                    builder.append("==");
                    break;
                    
                case NOT_EQUALS:
                    builder.append("!=");
                    break;
                    
                case GREATER_THAN:
                    builder.append('>');
                    break;
                    
                case GREATER_THAN_OR_EQUALS:
                    builder.append(">=");
                    break;
                    
                case LESS_THAN:
                    builder.append('<');
                    break;
                    
                case LESS_THAN_OR_EQUALS:
                    builder.append("<=");
                    break;
                    
                case IN:
                    builder.append("in");
                    break;
                    
                case NOT_IN:
                    builder.append("not in");
                    break;
                    
                case LIKE:
                    builder.append("like");
                    break;
                    
                case NOT_LIKE:
                    builder.append("not like");
                    break;
                    
                case IS_NULL:
                    builder.append("isNull");
                    break;
                    
                case IS_NOT_NULL:
                    builder.append("isNotNull");
                    break;
                    
                default:
                    builder.append(operator);
                    break;
            }
        }
    }

    /**
     * Appends the formatted value to the output.
     * <p>
     * This method searches the {@link #formatter} map for any registered special formatter otherwise it will add the default toString() representation.
     * {@code null} values will be formatted via {@link #appendFormattedNull(Class)}.
     * </p>
     * <p>
     * <b style="color: red;">Caution:</b> The given type can differ from the real object type if the condition isn't valid.
     * </p>
     * @param type the type of the object (specified in the condition)
     * @param value the real value.
     */
    protected void appendFormattedValue(Class<?> type, Object value){
        builder.append(' ');
        ValueFormatter valueFormatter = formatter.get(type);
        if( valueFormatter != null ){
            valueFormatter.formatValue(value, builder);
        } else {
            if( value == null ){
                appendFormattedNull(type);
            } else {
                builder.append(value);
            }
        }
    }
    
    /**
     * Appends the a null representation of the class to the output.
     * <p>
     * This implementation will add {@code "<Class#getSimpleName():NULL>"}.
     * </p>
     * @param type
     */
    protected void appendFormattedNull(Class<?> type){
        builder.append('<').append(type.getSimpleName()).append(":NULL>");
    }
    
    /**
     * Get the final output after the visit operations complete.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    public String toString() {
        return builder.toString();
    }
    
    /**
     * Simple value formatter interface which uses the StringBuilder to append it's formatted value.
     * (Reason: {@link Format} can only append to StringBuffer but not StringBuilder)
     * 
     * @author brandstetter
     *
     */
    public static interface ValueFormatter{
        /**
         * Take the given not null value and append the formatted output to the toAppend StringBuilder.
         * <p>
         * <b style="color: red;">Caution:</b> The type you used to register your {@link ValueFormatter} can differ from the real object type if the condition isn't valid.
         * </p>
         * @param value the not {@code null} value to format 
         * @param toAppend the builder to append the output to.
         */
        void formatValue(Object value, StringBuilder toAppend);
    }
}
