package com.smartstream.conditions.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.viren.conditions.Operator;

/**
 * Visitor for validate an expression or condition which collects all found errors/problems.
 * 
 * <p>
 * Any found error/problem will be categorized in either error or warning and will be added to a
 * list of messages which can be analyzed afterwards. For a fast not so chatty validation have a
 * look at {@link ExpressionFailFastValidator}.
 * </p>
 * 
 * <p>
 * This is just a basic detailed validator if you want to have a different validation rule feel free
 * to extend this validator.
 * </p>
 * 
 * <i>Info:</i> Not thread-safe!
 * 
 * @author brandstetter
 */
public class ExpressionDetailedValidator extends AbstractExpressionValidator {
    
    /**
     * Minimum information every category has to provide and common base for the categories.
     * 
     * @author brandstetter
     */
    protected static interface ValidationCategory {
        /**
         * Returns a key which can be used on the UI as a NLS key.
         * @return a key for NLS
         */
        public String getNLSKey();
        
        /**
         * Returns true if the category is an error category.
         * @return true if it's an error category false otherwise
         */
        public boolean isError();
    }
    
    /**
     * The "warning" category which means that it probably could be an error depending on the context.
     * 
     * <p>
     * All NLS keys are prefixed with {@code "com.smartstream.conditions.util.validationWarning_"}.
     * </p>
     * 
     * @author brandstetter
     */
    public static enum ValidationWarning implements ValidationCategory {
        /** The condition has no value. */
        CONDITION_NO_VALUE( "condition_value_missing" )
        ;
        private final String nlsKey;
        
        private ValidationWarning( String nlsKey ){
            this.nlsKey = "com.smartstream.conditions.util.validationWarning_" + Objects.requireNonNull(nlsKey);
        }
        
        @Override
        public String getNLSKey() {
            return nlsKey;
        }
        
        @Override
        public boolean isError() {
            return false;
        }
    }
    
    /**
     * The "error" category which means that this is an error regardless of the context.
     * 
     * <p>
     * All NLS keys are prefixed with {@code "com.smartstream.conditions.util.validationError_"}.
     * </p>
     * 
     * @author brandstetter
     */
    public static enum ValidationError implements ValidationCategory {
        /** The given expression is conditionless. */
        EXPRESSION_CONDITIONLESS("conditionless_expression"),
        
        /** The condition has no attribute name specified. */
        CONDITION_NO_ATTRIBUTENAME("condition_attributename_missing"),
        
        /** The condition has no operator specified. */
        CONDITION_NO_OPERATOR("condition_operator_missing"),
        
        /** The type specified in the condition and the provided objecttype to not match.
         * @deprecated The DataType can't mismatch the value anymore, because they are type-safe now!
         */
        @Deprecated
        CONDITION_TYPE_AND_VALUE_MISMATCH("condition_type_and_value_do_not_match"),
        
        /** The amount of values inside the condition doesn't match the range of the specified arity (see: {@link Operator.Arity#isArgumentsCountValid(List)}). */
        CONDITION_AMOUNT_OF_VALUES_NOT_IN_RANGE("condition_amount_of_values_not_in_range")
        ;
        private final String nlsKey;
        
        private ValidationError( String nlsKey ){
            this.nlsKey = "com.smartstream.conditions.util.validationError_" + Objects.requireNonNull(nlsKey);
        }
        
        @Override
        public String getNLSKey() {
            return nlsKey;
        }
        
        @Override
        public boolean isError() {
            return true;
        }
    }
    
    /**
     * This object represents one error/warning message.
     * 
     * @author brandstetter
     */
    public static class ValidationMessage {
        /** The specific error. */
        private final ValidationCategory status;
        
        /** Additional values available during the error detection, which can used to improve the error output. */
        private final List<Object> additionalValidationInformation;
        
        /**
         * Creates an instance of this class.
         * @param status the error/warning which happened
         * @param additionalValidationInformation useful values available during the error detection
         * @throws NullPointerException if the given status is null
         */
        public ValidationMessage(ValidationCategory status, Object... additionalValidationInformation){
            this.status = Objects.requireNonNull(status);
            this.additionalValidationInformation = Collections.unmodifiableList(Arrays.asList(additionalValidationInformation));
        }
        
        /**
         * Return the NLS key of the specified status.
         * @return the NLS key of the status
         */
        public String getNLSKey(){
            return status.getNLSKey();
        }
        
        /**
         * Checks if it is an error message.
         * @return true if the message was caused by an error
         */
        public boolean isError(){
            return status.isError();
        }
        
        /**
         * Checks if it is a warning message
         * @return true if the message was caused by a problem which can be an error in a specific context
         */
        public boolean isWarning(){
            return !status.isError();
        }
        
        /**
         * Returns the corresponding error.
         * @return the corresponding error if the message is an error message; null otherwise
         */
        public ValidationError getError(){
            return ( status.isError() ) ? (ValidationError) status : null;
        }
        
        /**
         * Returns the corresponding warning.
         * @return the corresponding warning if the message is an warning message; null otherwise
         */
        public ValidationWarning getWarning(){
            return ( !status.isError() ) ? (ValidationWarning) status : null;
        }
        
        /**
         * Returns an unmodifiable list useful values available during the error detection.
         * This list can be used to improve the output. 
         * @return useful values available during the error detection
         */
        public List<Object> getAdditionalValidationInformation(){
            return additionalValidationInformation;
        }
        
        @Override
        public String toString() {
            return ( status.isError() ? "ERROR - " : "WARNING - " ) + status + ": " + additionalValidationInformation;
        }
    }
    
    /**
     * Object containing all errors and warnings found during the validation.
     * 
     * @author brandstetter
     */
    public static final class ValidationMessages implements Iterable<ValidationMessage> {
        /** List of all errors and warnings. */
        private final List<ValidationMessage> allMessages = new LinkedList<>();
        
        /** List containing only errors. */
        private final List<ValidationMessage> errors = new LinkedList<>();
        
        /** List containing only warnings. */
        private final List<ValidationMessage> warnings = new LinkedList<>();
        
        /**
         * Adds an ValidationMessage to the corresponding lists. 
         * @param msg the message to add
         */
        protected final void addMessage(ValidationMessage msg){
            if( msg == null ) return;
            
            allMessages.add(msg);
            
            // also add it to the categorized list
            if( msg.isError() ){
                errors.add(msg);
            } else {
                warnings.add(msg);
            }
        }
        
        /**
         * Returns an unmodifiable list of all messages regardless if error or warning.
         * @return unmodifiable list of all messages
         */
        public final List<ValidationMessage> getAllMessages(){
            return Collections.unmodifiableList(allMessages);
        }
        
        /**
         * Returns an unmodifiable list of all error messages.
         * @return unmodifiable list of all error messages
         */
        public final List<ValidationMessage> getAllErrorMessages(){
            return Collections.unmodifiableList(errors);
        }
        
        /**
         * Returns an unmodifiable list of all warning messages.
         * @return unmodifiable list of all warning messages
         */
        public final List<ValidationMessage> getAllWarningMessages(){
            return Collections.unmodifiableList(warnings);
        }
        
        /**
         * Checks if there are errors.
         * @return true if this object contains error messages
         */
        public boolean hasErrors(){
            return !errors.isEmpty();
        }
        
        /**
         * Checks if there are warnings.
         * @return true if this object contains warning messages
         */
        public boolean hasWarnings(){
            return !warnings.isEmpty();
        }
        
        /**
         * Checks if there are any errors or messages.
         * @return true if this object contains messages
         */
        public boolean hasMessages(){
            return !allMessages.isEmpty();
        }

        /**
         * <p>
         * Iterates over the list of all errors and warnings.
         * </p>
         * 
         * {@inheritDoc}
         */
        @Override
        public Iterator<ValidationMessage> iterator() {
            return allMessages.iterator();
        }
    }
    
    /** Holds all found errors and warnings after the validation. */
    protected final ValidationMessages validationMessages = new ValidationMessages();
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if the expression has a condition and if not it will add the error {@link ValidationError#EXPRESSION_CONDITIONLESS}.
     * </p>
     */
    @Override
    protected void checkExpression(ExpressionState state) {
        if( !state.hasCondition() ){
            validationMessages.addMessage(new ValidationMessage(ValidationError.EXPRESSION_CONDITIONLESS));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This method checks for a {@code null} value.
     * 
     * <ul>
     *  <li>{@link ValidationWarning#CONDITION_NO_VALUE} will be added if the value is null</li>
     *  <li>{@link ValidationError#CONDITION_TYPE_AND_VALUE_MISMATCH} will be added if the type and objecttype do not match</li>
     * </ul>
     * 
     * </p>
     */
    @Override
    protected void checkValue(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
        if( value == null ){
            validationMessages.addMessage( new ValidationMessage(ValidationWarning.CONDITION_NO_VALUE, attributeName, type, operator));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Check if the given list of values is null or empty and if the parameter count of the operator arity is valid (see: {@link Operator.Arity#isArgumentsCountValid(List)}).
     * <br />
     * <b style="color: red;">Caution:</b> This method doesn't check the content of the list and if those values are null or of the wrong type.
     * To achieve this you have to extends this class an overwrite this method!
     * </p>
     * 
     * <ul>
     *  <li>{@link ValidationWarning#CONDITION_NO_VALUE} will be added if the values list is null or empty</li>
     *  <li>{@link ValidationError#CONDITION_AMOUNT_OF_VALUES_NOT_IN_RANGE} will be added if the {@link Operator.Arity#isArgumentsCountValid(List) return false</li>
     * </ul>
     */
    @Override
    protected void checkValues(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
        if( values == null || values.isEmpty() ){
            validationMessages.addMessage( new ValidationMessage(ValidationWarning.CONDITION_NO_VALUE, attributeName, type, operator));
        } else if( operator != null && !operator.getArity().isArgumentsCountValid(values) ) {
            validationMessages.addMessage( new ValidationMessage(ValidationError.CONDITION_AMOUNT_OF_VALUES_NOT_IN_RANGE, attributeName, type, operator) );
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Checks if an attribute name and an operator is given.
     * 
     * <ul>
     *  <li>{@link ValidationError#CONDITION_NO_ATTRIBUTENAME} will be added if the given attribute name is null or empty</li>
     *  <li>{@link ValidationError#CONDITION_NO_OPERATOR} will be added if the given operator is null</li>
     * </ul>
     * 
     * </p>
     */
    @Override
    protected void basicConditionCheck(String attributeName, Operator operator){
        if( attributeName == null || attributeName.isEmpty() ){
            validationMessages.addMessage(new ValidationMessage(ValidationError.CONDITION_NO_ATTRIBUTENAME));
        }
        
        if( operator == null ){
            validationMessages.addMessage(new ValidationMessage(ValidationError.CONDITION_NO_OPERATOR, attributeName));
        }
    }
    
    /**
     * Returns all found errors and warnings during the validation.
     * @return all found errors and warnings during the validation
     */
    public final ValidationMessages getValidationMessages(){
        return validationMessages;
    }

}
