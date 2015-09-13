package com.viren.conditions;

import java.io.Serializable;

import com.viren.conditions.Conjunction.Type;

/**
 * An Expression can be seen as group or subgroup of conditions.
 * 
 * <p>
 * Because of the {@link Connectable} interface you can conjunct other expressions or conditions with it.
 * </p>
 * <p>
 * <i>Info:</i> Not thread-safe!
 * </p>
 * <p>
 * The Serializable form of this class is passed between client and server. If instances of this class are
 * persisted long-term (eg in a database) the storage format must NOT simply be the serialized form - no
 * attempt has been made to optimize the expression serialized form for efficiency or long-term maintainability
 * (see "Effective Java", 2nd Edition, chapter 11). At the current time, Expressions persisted in a database are
 * stored in JSON format.
 * </p>
 * 
 * @see <a href="http://confluence/display/Morph/MFF+%28Filter+Framework%29">The Filter Framework confluence page.</a>
 * 
 * @author brandstetter
 */
public final class Expression implements Connectable, Negatable, VisitorElement, Serializable {
    
    /** The serial version UID for this class. */
    private static final long serialVersionUID = 3802226490357977533L;
    
    /** Holds a conjunction to an other expression or condition. */
    private Conjunction conjunction;
    
    /** Holds the condition nested inside of this expression. */
    private Connectable condition;
    
    /** Marks the expression as negated or not. */
    private boolean negate;
    
    public Expression(){}
    
    public Expression(Connectable condition){
        this(condition, null);
    }
    
    public Expression(Connectable condition, Conjunction conjunction){
        this.condition = condition;
        this.conjunction = conjunction;
    }

    @Override
    public Conjunction getConjunction() {
        return conjunction;
    }

    @Override
    public Expression setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
        return this;
    }
    
    @Override
    public Expression setConjunction(Type conjunctionType, Connectable nextItem) {
        this.conjunction = new Conjunction(conjunctionType, nextItem);
        return this;
    }

    /**
     * Return the condition of this expression.
     * @return the condition contained in this expression
     */
    public Connectable getCondition() {
        return condition;
    }

    /**
     * Set a condition to this expression which can be seen as the entry of a subgroup.
     * @param condition the condition of this expression
     */
    public void setCondition(Connectable condition) {
        this.condition = condition;
    }

    @Override
    public Expression setNegate(boolean negate) {
        this.negate = negate;
        return this;
    }

    @Override
    public boolean isNegate() {
        return negate;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if(negate){
            sb.append("NOT ");
        }
        
        sb.append("( ");
        
        if( condition == null ){
            sb.append("<not set>");
        } else {
            sb.append(condition);
        }
        
        sb.append(" )");
        
        if( conjunction != null ){
            // never add the conjunction#getNextItem() information to the StringBuilder (hint: cyclic references)
            // if you want to have a full string representation of your expression use com.smartstream.conditions.util.ExpressionUtil.formatExpression(Connectable)
            sb.append( ' ' ).append(conjunction.getConjunctionType()).append(" ...");
        }
        
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calls the following {@link Visitor} methods in the specified order.
     * <ol>
     *  <li>{@link Visitor#startExpression(boolean)}</li>
     *  <li>if it contains a condition: {@link Condition#visit(Visitor)}</li>
     *  <li>{@link Visitor#endExpression()}</li>
     *  <li>if it contains a conjunction: {@link Conjunction#visit(Visitor)}</li>
     * </ol>
     * </p>
     */
    @Override
    public void visit(Visitor visitor) {
        visitor.startExpression(negate);
        
        if( condition != null ){
            condition.visit(visitor);
        }
        
        visitor.endExpression();
        
        if( conjunction != null ){
            conjunction.visit(visitor);
        }
    }

}
