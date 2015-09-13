package com.smartstream.conditions.util;

import com.viren.conditions.BinaryCondition;
import com.viren.conditions.Condition;
import com.viren.conditions.Conjunction;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;
import com.viren.conditions.PolyadicCondition;

/**
 * Utility class which does the checks in the traditional step by step comparison.
 * 
 * <p>
 * If you want to influence the compare of the expressions or conditions just check
 * {@link ExpressionUtil#equal(Connectable, Connectable, com.smartstream.conditions.util.ExpressionUtil.EqualityHelper)}.
 * </p>
 * 
 * @author brandstetter
 *
 */
public final class EqualUtil {

    private EqualUtil() {
        throw new IllegalStateException("Instantiation of this class is not allowed!");
    }
    
    /**
     * Checks if two expression are equal.
     * <p>
     * <b style="color: red;">Caution:</b> Be sure both expression don't have a cyclic reference!
     * (see: {@link ExpressionUtil#checkForCycle(Connectable)} or {@link ExpressionUtil#hasCycle(Connectable)})
     * </p>
     * @param expr1 the first expression
     * @param expr2 the second expression
     * @return true if they are equal, false otherwise
     */
    public static boolean areEqual(Expression expr1, Expression expr2){
        Boolean basicCheck = basicObjectEqualCheck(expr1, expr2);
        if( basicCheck != null ){
            return basicCheck.booleanValue();
        }
        
        if( expr1.isNegate() == expr2.isNegate() ){
            return areEqual(expr1.getCondition(), expr2.getCondition())   // compare conditions
                   && areEqual(expr1.getConjunction(), expr2.getConjunction()); // compare conjunctions
        }
        
        return false;
    }
    
    /**
     * Checks if two conditions are equal.
     * <p>
     * <b style="color: red;">Caution:</b> Be sure both conditions don't have a cyclic reference!
     * (see: {@link ExpressionUtil#checkForCycle(Connectable)} or {@link ExpressionUtil#hasCycle(Connectable)})
     * </p>
     * @param con1 the first condition
     * @param con2 the second condition
     * @return true if they are equal, false otherwise
     */
    public static boolean areEqual(Condition<?> con1, Condition<?> con2){
        Boolean basicCheck = basicObjectEqualCheck(con1, con2);
        if( basicCheck != null ){
            return basicCheck.booleanValue();
        }
        
        // check basic condition attributes
        if( areEqual(con1.getAttributeName(), con2.getAttributeName()) &&
            areEqual(con1.getOperator(), con2.getOperator()) &&
            areEqual(con1.getType(), con2.getType()) ){
            
            boolean valuesEqual;
            
            if( con1.getOperator() != null ){  // if an operator exists make a switch instead of instanceof checks
                switch( con1.getOperator().getArity() ){
                    case BINARY:
                        valuesEqual = areEqualValues(((BinaryCondition<?>) con1).getValue(), ((BinaryCondition<?>) con2).getValue());
                        break;
                        
                    case TERNARY: // NOPMD: ternary and polyadic are both realized as a PolyadicCondition
                    case POLYADIC:
                        valuesEqual = areEqualValues(((PolyadicCondition<?>) con1).getValues(), ((PolyadicCondition<?>) con2).getValues());
                        break;
                        
                    default:
                        valuesEqual = true; // unary doesn't have any value
                        break;
                }
            } else {
                // no arity to have a fast check make it old school
                if( con1 instanceof BinaryCondition ){
                    valuesEqual = areEqualValues(((BinaryCondition<?>) con1).getValue(), ((BinaryCondition<?>) con2).getValue());
                } else if( con1 instanceof PolyadicCondition ){
                    valuesEqual = areEqualValues(((PolyadicCondition<?>) con1).getValues(), ((PolyadicCondition<?>) con2).getValues());
                } else {
                    valuesEqual = true; // unary doesn't have any value
                }
            }
            
            return valuesEqual && areEqual(con1.getConjunction(), con2.getConjunction());
        }
        
        return false;
    }
    
    /**
     * Checks if two conjunctions are equal.
     * <p>
     * <b style="color: red;">Caution:</b> Be sure both {@link Conjunction#getNextItem()} don't have a cyclic reference!
     * (see: {@link ExpressionUtil#checkForCycle(Connectable)} or {@link ExpressionUtil#hasCycle(Connectable)})
     * </p>
     * @param conj1 the first conjunction
     * @param conj2 the second conjunction
     * @return true if they are equal, false otherwise
     */
    public static boolean areEqual(Conjunction conj1, Conjunction conj2){
        Boolean basicCheck = basicObjectEqualCheck(conj1, conj2);
        if( basicCheck != null ){
            return basicCheck.booleanValue();
        }

        if( areEqual(conj1.getConjunctionType(), conj2.getConjunctionType()) ){  // type equal
            return areEqual(conj1.getNextItem(), conj2.getNextItem()); // next elements equal
        }
        
        return false;
    }
    
    /**
     * Checks if two expression or conditions are equal.
     * <p>
     * <b style="color: red;">Caution:</b> Be sure both expression or conditions don't have a cyclic reference!
     * (see: {@link ExpressionUtil#checkForCycle(Connectable)} or {@link ExpressionUtil#hasCycle(Connectable)})
     * </p>
     * @param con1 the first expression or conditions
     * @param con2 the second expression or conditions
     * @return true if they are equal, false otherwise
     */
    public static boolean areEqual(Connectable con1, Connectable con2){
        Boolean basicCheck = basicObjectEqualCheck(con1, con2);
        if( basicCheck != null ){
            return basicCheck.booleanValue();
        }
        
        if( con1 instanceof Expression ){ // first element is a expression so the second one will also be one (this is checked inside the basicObjectEqualCheck() method)
            return areEqual((Expression) con1, (Expression) con2);
        }
        
        return areEqual((Condition<?>) con1, (Condition<?>) con2);
    }
    
    /**
     * Checks if two objects are equal.
     * @param one the first object
     * @param two the second object
     * @return true if they are equal, false otherwise
     */
    private static boolean areEqualValues(Object one, Object two){
        Boolean basicCheck = basicObjectEqualCheck(one, two);
        if( basicCheck != null ){
            return basicCheck.booleanValue();
        }
        
        return one.equals( two );
    }
    
    /**
     * Checks if two Strings are equal.
     * This method doesn't do the {@link #basicObjectEqualCheck(Object, Object)} because we
     * already know the object type.
     * @param str1 the first string
     * @param str2 the second string
     * @return true if they are equal, false otherwise
     */
    private static boolean areEqual(String str1, String str2){
        if( str1 != null ){
            return str1.equals( str2 );
        }
        
        return ( str2 != null ) ? false : true;
    }
    
    /**
     * Checks if two enumeration values are equal.
     * This method doesn't do the {@link #basicObjectEqualCheck(Object, Object)} because we
     * already know the object type.
     * @param enum1 the first enum value
     * @param enum2 the second enum value
     * @return true if they are equal, false otherwise
     */
    private static boolean areEqual( Enum<?> enum1, Enum<?> enum2 ){
        if( enum1 != null ){
            return enum1.equals( enum2 );
        }
        
        return ( enum2 != null ) ? false : true;
    }
    
    /**
     * Checks if two classes values are equal.
     * This method doesn't do the {@link #basicObjectEqualCheck(Object, Object)} because we
     * already know the object type.
     * @param class1 the first class
     * @param class2 the second class
     * @return true if they are equal, false otherwise
     */
    private static boolean areEqual( Class<?> class1, Class<?> class2){
        if( class1 != null ){
            return class1.equals( class2 );
        }
        
        return ( class2 != null ) ? false : true;
    }
    
    /**
     * This method does the basic equality check on the two given objects.
     * 
     * <p>
     * The following checks are done:
     * <ul>
     *   <li>same reference check: {@code one == two (which also includes null == null)}</li>
     *   <li>check if one of the elements is null: {@code one == null || two == null}</li>
     *   <li>check if the class of both elements is equal: {@code one.getClass().equals(two.getClass())}</li>
     * </ul>
     * </p>
     * @param one the first object
     * @param two the second object
     * @return <ul>
     *  <li>{@link Boolean#TRUE} - if reference are the same</li>
     *  <li>{@link Boolean#FALSE} - if only one given object is null</li>
     *  <li>{@link Boolean#FALSE} - if the given objects are not of the same class</li>
     *  <li>{@code null} - if the basic check was OK and you can go on with the object specific checks</li>
     * </ul>
     */
    private static Boolean basicObjectEqualCheck(Object one, Object two){
        if( one == two ) return Boolean.TRUE; // NOPMD - this is OK because it's a basic object check for null and the same reference the rest of the equality check should be handled in the caller of this method
        if( one == null || two == null ) return Boolean.FALSE;
        
        if( !one.getClass().equals(two.getClass()) ) return Boolean.FALSE; // shortcut if you want to compare to different type of objects
        
        return null;
    }
}
