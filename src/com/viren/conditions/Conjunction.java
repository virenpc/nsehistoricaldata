package com.viren.conditions;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class defines a conjunction with an other element (expression or condition).
 * 
 * @author brandstetter
 */
public final class Conjunction implements VisitorElement, Serializable {

    /** The serial version UID for this class. */
    private static final long serialVersionUID = 9079623481684620647L;

    /**
     * An enumeration of the possible conjunction types.
     * 
     * @author brandstetter
     */
    public static enum Type {
        /** An 'and' conjunction. */
        AND(1),
        /** An 'or' conjunction. */
        OR(0);

        private final int precedence;

        Type(int precedence) {
            this.precedence = precedence;
        }
        
        public int getPrecendence() {
            return precedence;
        }
    }
    
    /** The type of the conjunction. */
    private Type conjunctionType;
    
    /** The 2nd element of the conjunction.
     * The first one will be the one on which the {@link Connectable#setConjunction(Conjunction)} or {@link Connectable#setConjunction(Type, Connectable)} method was called.
     */
    private Connectable nextItem;
    
    /**
     * Sole constructor of this class. 
     * @param conjunctionType the type of the conjunction
     * @param nextItem the 2nd element of the conjunction
     * @throws NullPointerException if either conjunctionType or nextItem is null
     */
    public Conjunction(Type conjunctionType, Connectable nextItem){
        this.conjunctionType = Objects.requireNonNull(conjunctionType, "No conjunction type given!");
        this.nextItem = Objects.requireNonNull(nextItem, "Item to connect to not given!");
    }

    /**
     * Returns the type of the conjunction.
     * @return the type of the conjunction
     */
    public Type getConjunctionType() {
        return conjunctionType;
    }

    /**
     * Set the type of the conjunction.
     * @param conjunctionType the type of the conjunction
     * @throws NullPointerException if the given conjunction is null
     */
    public void setConjunctionType(Type conjunctionType) {
        this.conjunctionType = Objects.requireNonNull(conjunctionType, "No conjunction type given!");
    }

    /**
     * Returns the 2nd element (expression or condition) of this conjunction.
     * @return the 2nd element of this conjunction
     */
    public Connectable getNextItem() {
        return nextItem;
    }

    /**
     * Set the 2nd element (expression or condition) of this conjunction
     * @param nextItem the 2nd element of this conjunction
     * @throws NullPointerException if the given element is null
     */
    public void setNextItem(Connectable nextItem) {
        this.nextItem = Objects.requireNonNull(nextItem, "Item to connect to not given!");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This will call the following methods on the visitor:
     * <ol>
     *  <li>{@link Visitor#conjunct(Type)} with the conjunction type of the object</li>
     *  <li>{@link VisitorElement#visit(Visitor)} on the 2nd element configured in the object</li>
     * </ol>
     * </p>
     */
    @Override
    public final void visit(Visitor visitor) {
        visitor.conjunct(conjunctionType);
        nextItem.visit(visitor);
    }
}
