package com.viren.conditions;

/**
 * Interface which marks an element which can be negated.
 * 
 * @author brandstetter
 */
public interface Negatable {
    /**
     * Mark the element either as negated or not.
     * @param negate true to negate the element; false otherwise
     * @return the this reference to use it in builder like style
     */
    Negatable setNegate(boolean negate);
    
    /**
     * Returns if the element is negated or not.
     * @return true if the element is negated; false otherwise
     */
    boolean isNegate();
}
