package com.smartstream.conditions.builders;

import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createComplexCycle;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createComplexExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidConditionNoAttributename;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidConditionWrongValueAmount;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidCondtionNoAttributenameAndOrOperator;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidCondtionNoOperator;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidCondtionNoValue;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidCondtionNoValues;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidNestedEmptyExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidNestedExpressionEmptyAndInvalidOne;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidNestedExpressionInvalidOne;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidSimpleEmptyExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createInvalidSimpleExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createListOfCyclicExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createListOfInvalidExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createListOfManualExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createLookingLikeACycle;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleCycle;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.equal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.smartstream.conditions.util.ExpressionDetailedValidator.ValidationError;
import com.smartstream.conditions.util.ExpressionDetailedValidator.ValidationMessage;
import com.smartstream.conditions.util.ExpressionDetailedValidator.ValidationMessages;
import com.smartstream.conditions.util.ExpressionDetailedValidator.ValidationWarning;
import com.smartstream.conditions.util.ExpressionFailFastValidator.ValidationException;
import com.smartstream.conditions.util.ExpressionUtil;
import com.smartstream.conditions.util.ExpressionUtil.ConnectableIterator;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;

public class ExpressionUtilityTest {

    // this is more or less a manual test to see if the output looks good 
//    @Test
//    public void testFormatter(){
//        for(Expression e : createListOfManualExpression()){
//            System.out.println(ExpressionUtil.formatExpression(e));
//        }
//    }
    
    /**
     * Checks for cyclic references in the expression or condition.
     */
    @Test
    public void testCycleCheck (){
        for(Connectable e : createListOfManualExpression()){
            Assert.assertFalse("Element: " + e + " has a cycle, but shouldn't have one!", ExpressionUtil.hasCycle(e));
        }
        
        // now test some cycles
        Assert.assertTrue("SimpleCylce should have a cycle, because it's a test.", ExpressionUtil.hasCycle(createSimpleCycle()));
        Assert.assertTrue("ComplexCylce should have a cycle, because it's a test.", ExpressionUtil.hasCycle(createComplexCycle()));
        Assert.assertFalse("lookingLikeACycle looks like a cycle but hasn't one.", ExpressionUtil.hasCycle(createLookingLikeACycle()));
    }
    
    /**
     * Tests if the copy of the expression works correctly.
     */
    @Test
    public void testCopyExpression(){
        for(Connectable e : createListOfManualExpression()){
            Connectable copy = ExpressionUtil.copy(e);

            if( !equal(e, copy) ){
                Assert.fail(String.format("Copy and original object aren't equal [expected: <%s>] [original: <%s>]", ExpressionUtil.formatExpression(e), ExpressionUtil.formatExpression(copy)));
            }
        }
        
        for(Connectable e : createListOfInvalidExpression()){
            Connectable copy = ExpressionUtil.copy(e);

            if( !equal(e, copy) ){
                Assert.fail(String.format("Copy and original object aren't equal [expected: <%s>] [original: <%s>]", ExpressionUtil.formatExpression(e), ExpressionUtil.formatExpression(copy)));
            }
        }
        
        for(Connectable e : createListOfCyclicExpression()){
            try{
                Connectable copy = ExpressionUtil.copy(e);
                
                Assert.fail("Copy of a cyclic expression should fail!");
    
                // should never reach this point! 
                if( !equal(e, copy) ){
                    Assert.fail(String.format("Copy and original object aren't equal [expected: <%s>] [original: <%s>]", ExpressionUtil.formatExpression(e), ExpressionUtil.formatExpression(copy)));
                }
            } catch (IllegalArgumentException iae){
                // NOPMD: this is OK
            }
        }
    }
    
    /**
     * Tests if the ConnectableIterator works correctly.
     */
    @Test
    public void testExpressionIteration(){
        Connectable testExpression = createComplexExpression();
        Iterator<Connectable> connectableIter = new ConnectableIterator(testExpression);
        
        List<Connectable> elementsWhichShouldBeFound = new ArrayList<>();
        elementsWhichShouldBeFound.add( testExpression ); // firstName = "Herbert"
        elementsWhichShouldBeFound.add( testExpression.getConjunction().getNextItem() ); // firstName = "Hubert"
        elementsWhichShouldBeFound.add( testExpression.getConjunction().getNextItem().getConjunction().getNextItem() ); // group
        elementsWhichShouldBeFound.add( ((Expression) testExpression.getConjunction().getNextItem().getConjunction().getNextItem()).getCondition() ); // lastName = "Blub"
        elementsWhichShouldBeFound.add( ((Expression) testExpression.getConjunction().getNextItem().getConjunction().getNextItem()).getCondition().getConjunction().getNextItem() ); // married in ("wald", "traut")
        elementsWhichShouldBeFound.add( ((Expression) testExpression.getConjunction().getNextItem().getConjunction().getNextItem()).getCondition().getConjunction().getNextItem().getConjunction().getNextItem() ); // sex = 'm'
        elementsWhichShouldBeFound.add( testExpression.getConjunction().getNextItem().getConjunction().getNextItem().getConjunction().getNextItem() ); // human = true 
        elementsWhichShouldBeFound.add( testExpression.getConjunction().getNextItem().getConjunction().getNextItem().getConjunction().getNextItem().getConjunction().getNextItem() ); // age < 18
        
        Iterator<Connectable> elementsWhichShouldBeFoundIter = elementsWhichShouldBeFound.iterator();
        
        while(connectableIter.hasNext()){
            Assert.assertEquals(elementsWhichShouldBeFoundIter.next(), connectableIter.next());
        }
        
        Assert.assertFalse("There shouldn't be any elements left in the test connector!", connectableIter.hasNext()); // will never happen because otherwise we haven't been here see while loop
        Assert.assertFalse("Elements are missing!", elementsWhichShouldBeFoundIter.hasNext());
    }
    
    /**
     * Checks the detailed validation mechanism if the expected ValidationErrors/ValidationWarnings arise (also in the correct order).
     */
    @Test
    public void testDetailedValidation(){
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidSimpleEmptyExpression()), ValidationError.EXPRESSION_CONDITIONLESS );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidNestedEmptyExpression()), ValidationError.EXPRESSION_CONDITIONLESS );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidNestedExpressionEmptyAndInvalidOne()), ValidationError.EXPRESSION_CONDITIONLESS, ValidationError.EXPRESSION_CONDITIONLESS );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidNestedExpressionInvalidOne()), ValidationError.EXPRESSION_CONDITIONLESS );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidSimpleExpression()), ValidationError.EXPRESSION_CONDITIONLESS );
        
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidCondtionNoValue()), ValidationWarning.CONDITION_NO_VALUE, ValidationWarning.CONDITION_NO_VALUE );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidCondtionNoValues()), ValidationWarning.CONDITION_NO_VALUE, ValidationWarning.CONDITION_NO_VALUE );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidCondtionNoOperator()), ValidationError.CONDITION_NO_OPERATOR, ValidationWarning.CONDITION_NO_VALUE, ValidationError.CONDITION_NO_OPERATOR, ValidationWarning.CONDITION_NO_VALUE );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidCondtionNoAttributenameAndOrOperator()), ValidationError.CONDITION_NO_ATTRIBUTENAME, ValidationError.CONDITION_NO_OPERATOR, ValidationError.CONDITION_NO_ATTRIBUTENAME, ValidationWarning.CONDITION_NO_VALUE, ValidationError.CONDITION_NO_OPERATOR, ValidationWarning.CONDITION_NO_VALUE );
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidConditionNoAttributename()), ValidationError.CONDITION_NO_ATTRIBUTENAME, ValidationError.CONDITION_NO_ATTRIBUTENAME);
        
        checkValidationMessage( ExpressionUtil.detailedValidation(createInvalidConditionWrongValueAmount()), ValidationError.CONDITION_AMOUNT_OF_VALUES_NOT_IN_RANGE, ValidationError.CONDITION_AMOUNT_OF_VALUES_NOT_IN_RANGE);
        
        for( Connectable validManualExpr : createListOfManualExpression() ){
            checkValidationMessage( ExpressionUtil.detailedValidation(validManualExpr) );
        }
        
        try{
            checkValidationMessage(ExpressionUtil.detailedValidation(createComplexCycle()));
            Assert.fail("Cycle shouldn't start to validate at all!");
        } catch (IllegalArgumentException cyclic){
            // NOPMD: this should fail!
        }
    }
    
    private void checkValidationMessage(ValidationMessages messages, Object... expectedMessages){
        if( messages.getAllMessages().size() != expectedMessages.length ){
            if( messages.getAllMessages().size() < expectedMessages.length ){
                Assert.fail("Not all errors/warnings found!");
            } else {
                Assert.fail("More errors/warnings found than expected!");
            }
        }
        
        int i = 0;
        for( ValidationMessage msg : messages ){
            if( msg.isError() ){
                Assert.assertEquals(expectedMessages[i], msg.getError());
            } else {
                Assert.assertEquals(expectedMessages[i], msg.getWarning());
            }
            
            i++;
        }
    }
    
    /**
     * Checks the fast fail validation if null values do not lead to an exception but the rest does.
     */
    @Test
    public void testFastValidationAllowNullValues(){
        checkFastValidationNullPermitted(createInvalidSimpleEmptyExpression(), true);
        
        checkFastValidationNullPermitted(createInvalidNestedEmptyExpression(), true);
        checkFastValidationNullPermitted(createInvalidNestedExpressionEmptyAndInvalidOne(), true);
        checkFastValidationNullPermitted(createInvalidNestedExpressionInvalidOne(), true);
        checkFastValidationNullPermitted(createInvalidSimpleExpression(), true);
        
        checkFastValidationNullPermitted(createInvalidCondtionNoValue(), false);
        checkFastValidationNullPermitted(createInvalidCondtionNoValues(), false);
        checkFastValidationNullPermitted(createInvalidCondtionNoOperator(), true);
        checkFastValidationNullPermitted(createInvalidCondtionNoAttributenameAndOrOperator(), true);
        checkFastValidationNullPermitted(createInvalidConditionNoAttributename(), true);
        
        checkFastValidationNullPermitted( createInvalidConditionWrongValueAmount(), false );
        
        for( Connectable validManualExpr : createListOfManualExpression() ){
            checkFastValidationNullPermitted( validManualExpr, false );
        }
        
        try{
            checkFastValidationNullPermitted(createComplexCycle(), true);
            Assert.fail("Cycle shouldn't start to validate at all!");
        } catch (IllegalArgumentException cyclic){
            // NOPMD: this should fail!
        }
    }
    
    /**
     * Checks the fast fail validation if they fail correctly.
     */
    @Test
    public void testFastValidation(){
        checkFastValidation(createInvalidSimpleEmptyExpression(), true);
        
        checkFastValidation(createInvalidNestedEmptyExpression(), true);
        checkFastValidation(createInvalidNestedExpressionEmptyAndInvalidOne(), true);
        checkFastValidation(createInvalidNestedExpressionInvalidOne(), true);
        checkFastValidation(createInvalidSimpleExpression(), true);
        
        checkFastValidation(createInvalidCondtionNoValue(), true);
        checkFastValidation(createInvalidCondtionNoValues(), true);
        checkFastValidation(createInvalidCondtionNoOperator(), true);
        checkFastValidation(createInvalidCondtionNoAttributenameAndOrOperator(), true);
        checkFastValidation(createInvalidConditionNoAttributename(), true);
        
        checkFastValidation( createInvalidConditionWrongValueAmount(), true );
        
        for( Connectable validManualExpr : createListOfManualExpression() ){
            checkFastValidation( validManualExpr, false );
        }
        
        try{
            checkFastValidation(createComplexCycle(), true);
            Assert.fail("Cycle shouldn't start to validate at all!");
        } catch (IllegalArgumentException cyclic){
            // NOPMD: this should fail!
        }
    }
    
    private void checkFastValidation(Connectable e, boolean expectException){
        checkFastValidation(e, false, expectException);
    }
    
    private void checkFastValidationNullPermitted(Connectable e, boolean expectException){
        checkFastValidation(e, true, expectException);
    }
    
    private void checkFastValidation(Connectable e, boolean allowNull, boolean expectException ){
        try {
            ExpressionUtil.validate(e, allowNull);
            
            if( expectException ){
                Assert.fail("This should fail: " + ExpressionUtil.formatExpression(e));
            }
        } catch ( ValidationException ve ){
            if( !expectException ){  // exception wasn't expected
                Assert.fail("This wasn't expected: " + ve.getMessage());
            }
        }
    }
    
    /**
     * Checks if all validation methods (detailed, fast and fast with null) return more or less the same result.
     */
    @Test
    public void testValidationEquality(){
        // test fast and test detailed both should result in more or less in the same result
        checkValiationEquality(createInvalidSimpleEmptyExpression());
        
        checkValiationEquality(createInvalidNestedEmptyExpression());
        checkValiationEquality(createInvalidNestedExpressionEmptyAndInvalidOne());
        checkValiationEquality(createInvalidNestedExpressionInvalidOne());
        checkValiationEquality(createInvalidSimpleExpression());
        
        checkValiationEquality(createInvalidCondtionNoValue());
        checkValiationEquality(createInvalidCondtionNoValues());
        checkValiationEquality(createInvalidCondtionNoOperator());
        checkValiationEquality(createInvalidCondtionNoAttributenameAndOrOperator());
        checkValiationEquality(createInvalidConditionNoAttributename());
        
        for( Connectable validManualExpr : createListOfManualExpression() ){
            checkValiationEquality(validManualExpr);
        }
        
        try{
            checkValiationEquality(createComplexCycle());
            Assert.fail("Cycle shouldn't start to validate at all!");
        } catch (IllegalArgumentException cyclic){
            // NOPMD: this should fail!
        }
    }
    
    private void checkValiationEquality(Connectable toCheck){
        ValidationMessages messages = ExpressionUtil.detailedValidation(toCheck);
        String fastValid = fastValid(toCheck);
        String fastValidWithNull = fastValidNullPermitted(toCheck);
        
        if( fastValid == null ){
            if( messages.hasMessages() ){
                Assert.fail( "Fast validation was valid but detailed announces a problem! (msg: " + messages.getAllMessages().get(0) + ")" );
            }
        } else {
            if( !messages.hasMessages() ){
                Assert.fail( "Detailed validation was valid but fast announces a problem! (msg: " + fastValid + ")" );
            }
        }
        
        if( fastValidWithNull == null ){
            if( messages.hasErrors() ){
                Assert.fail( "Fast validation (null allowed) was valid but detailed announces a problem! (msg: " + messages.getAllErrorMessages().get(0) + ")" );
            }
            
            // warnings should be OK for null allowing fast validiaton -- because null values are only warnings in detailed validation
            // but are NOT OK for the other one
            if( messages.hasWarnings() && fastValid == null ){
                Assert.fail( "Null value wasn't caught by Fast validation! (msg: " + messages.getAllWarningMessages().get(0) + ")" );
            }
        } else {
            if( !messages.hasErrors() ){
                Assert.fail( "Fast validation (null allowed) found a null value which wasn't found by detailed validation! (msg: " + fastValidWithNull + ")" );
            }
            
            if( messages.hasErrors() && fastValid == null ){
                Assert.fail( "Error value wasn't found by Fast validation but in the detailed validation! (msg: " + messages.getAllErrorMessages().get(0) + ")" );
            }
        }
    }
    
    private String fastValid(Connectable e){
        return fastValid(e, false);
    }
    
    private String fastValidNullPermitted(Connectable e){
        return fastValid(e, true);
    }
    
    private String fastValid(Connectable e, boolean allowNull ){
        try {
            ExpressionUtil.validate(e, allowNull);
            return null;
        } catch ( ValidationException ve ){
            return ve.getMessage();
        }
    }

}
