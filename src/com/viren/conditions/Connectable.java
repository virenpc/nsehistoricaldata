package com.viren.conditions;

import com.viren.conditions.Conjunction.Type;

/**
 * Defines the contract required to conjunct the different elements (expressions and/or conditions).
 * 
 * @author brandstetter
 */
public interface Connectable extends VisitorElement {
    /**
     * Retrieve the configured conjunction.
     * @return the specified conjunction
     */
    Conjunction getConjunction();
    
    /**
     * Specify a conjunction with an other element (expression or condition)
     * @param conjunction the conjunction type and the element which should be conjuncted
     * @return the this reference to use it in builder like style 
     */
    Connectable setConjunction(Conjunction conjunction);
    
    /**
     * Create a new conjunction with an other element (expression or condition)
     * @param conjunctionType the type of the conjunction
     * @param nextItem the element with which this element should be conjuncted
     * @return the this reference to use it in builder like style 
     */
    Connectable setConjunction(Type conjunctionType, Connectable nextItem);
}
