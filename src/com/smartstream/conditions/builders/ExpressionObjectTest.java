package com.smartstream.conditions.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.viren.conditions.BinaryCondition;
import com.viren.conditions.Operator;
import com.viren.conditions.PolyadicCondition;
import com.viren.conditions.UnaryCondition;
import com.viren.conditions.Operator.Arity;

/**
 * Check of the Arity, Operator and Condition behavior and also the type-safety of the conditions. Which
 * means it check if you can assign an unary operator to a binary condition and so on.
 * @author brandstetter
 *
 */
public class ExpressionObjectTest {
    @Test
    public void testUnaryConditionOperator(){
        createCondions(new ConditonCreator() {
            @Override
            public Object create(Operator op) {
                return new UnaryCondition<>(String.class, "NotImporatnt", op);
            }
        }, Arity.UNARY, 2);
    }
    
    @Test
    public void testBinaryConditionOperator(){
        createCondions(new ConditonCreator() {
            @Override
            public Object create(Operator op) {
                return new BinaryCondition<>(String.class, "NotImporatnt", op, "ReallyNotImportant");
            }
        }, Arity.BINARY, 8);
    }
    
    @Test
    public void testPolyadicConditionOperator(){
        createCondions(new ConditonCreator() {
            @Override
            public Object create(Operator op) {
                return new PolyadicCondition<>(String.class, "NotImporatnt", op, "ReallyNotImportant");
            }
        }, EnumSet.of( Arity.TERNARY, Arity.POLYADIC ), 3);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWrongDatatypeAssignmentBinaryCondition(){
      @SuppressWarnings({ "rawtypes", "unchecked" })
      BinaryCondition<Object> wrongType = new BinaryCondition(String.class, "testWrongtype", Operator.EQUALS);
      wrongType.setValue(Integer.valueOf(12));
      
      // should never reach this line
      throw new IllegalStateException("The binary condition should be uncreateable, and throw an IllegalArgumentExcpetion! (condition: " + wrongType + ')');
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWrongPrimitiveDatatypeAssignmentBinaryCondition(){
        @SuppressWarnings({ "rawtypes", "unchecked" })
        BinaryCondition<Object> wrongType = new BinaryCondition(int.class, "testWrongtype", Operator.EQUALS);
        wrongType.setValue(Double.valueOf(3.14));
        
        // should never reach this line
        throw new IllegalStateException("The binary condition should be uncreateable, and throw an IllegalArgumentExcpetion! (condition: " + wrongType + ')');
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWrongDatatypeAssignmentPolyadicCondition(){
      @SuppressWarnings({ "rawtypes", "unchecked" })
      PolyadicCondition<Object> wrongType = new PolyadicCondition(String.class, "testWrongtype", Operator.IN);
      wrongType.getValues().addAll(Arrays.asList("Test", Integer.valueOf(42), "should be ok"));
     
      // should never reach this line
      throw new IllegalStateException("The polyadic condition should be uncreateable, and throw an IllegalArgumentExcpetion! (condition: " + wrongType + ')');
    }
    
    @Test
    public void testNullValueDatatypeAssignmentPolyadicCondition(){
      @SuppressWarnings({ "rawtypes", "unchecked" })
      PolyadicCondition<Object> containsNull = new PolyadicCondition(int.class, "testNullValue", Operator.IN);
      containsNull.getValues().addAll(Arrays.asList(Integer.valueOf(12), Integer.valueOf(42), null));
      
      Assert.assertEquals(3, containsNull.getValues().size());
    }
    
    @Test
    public void testNullValueDatatypeAssignmentBinaryCondition(){
      @SuppressWarnings({ "rawtypes", "unchecked" })
      BinaryCondition<Object> valueIsNull = new BinaryCondition(int.class, "testNullValue", Operator.EQUALS);
      valueIsNull.setValue(null); // explicit set of null to call the check
      
      Assert.assertNull(valueIsNull.getValue());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWrongPrimitiveDatatypeAssignmentPolyadicCondition(){
      @SuppressWarnings({ "rawtypes", "unchecked" })
      PolyadicCondition<Object> wrongType = new PolyadicCondition(int.class, "testWrongtype", Operator.IN);
      wrongType.getValues().addAll(Arrays.asList(Integer.valueOf(42), Double.valueOf(3.1415)));
     
      // should never reach this line
      throw new IllegalStateException("The polyadic condition should be uncreateable, and throw an IllegalArgumentExcpetion! (condition: " + wrongType + ')');
    }
    
    private void createCondions(ConditonCreator conditionCreator, Arity toCheck, int expectedGoodCreations){
        createCondions(conditionCreator, EnumSet.of( toCheck ), expectedGoodCreations);
    }
    
    private void createCondions(ConditonCreator conditionCreator, EnumSet<Arity> toCheck, int expectedGoodCreations){
        List<Object> allValidConditions = new ArrayList<>();
        boolean expectedException = false;
        
        for( Operator op : Operator.values() ){
            expectedException = !toCheck.contains( op.getArity() );
            
            IllegalArgumentException exception = null;
            Object con = null;
            
            try{
                con = conditionCreator.create(op);
            } catch ( IllegalArgumentException ie ){
                exception = ie;
            }
            
            if( exception == null ){
                if( expectedException ){                    
                    Assert.fail("An IllegalArgumentException was expected, because the arity of the operator " + op + " isn't " + toCheck + "!");
                }
                
                allValidConditions.add( con );
            } else {
                if( !expectedException ){
                    Assert.fail("An IllegalArgumentException was NOT expected, because the arity of the operator " + op + " is " + toCheck + "!");
                }
            }
            
        }
        
        Assert.assertEquals("Only " + expectedGoodCreations + " " + toCheck + " conditions should exsist (check for a new unary operator)", expectedGoodCreations, allValidConditions.size());
    }
    
    private interface ConditonCreator{
        Object create(Operator op);
    }
}
