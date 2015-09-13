package com.viren.conditions;

/**
 * This interface marks the element nodes of the visitor pattern.
 * 
 * @author brandstetter
 *
 */
public interface VisitorElement {
    
    /**
     * Calls the appropriate methods on the given visitor.
     * @param visitor a concrete visitor implementation
     */
    void visit(Visitor visitor);
}
