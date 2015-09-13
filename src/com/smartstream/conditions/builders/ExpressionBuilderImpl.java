package com.smartstream.conditions.builders;

import java.util.Objects;

import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;
import com.viren.conditions.Conjunction.Type;

final class ExpressionBuilderImpl implements ExpressionBuilder {
    
    private final Expression expression;
    
    private Connectable currentConnectable;
    private Connectable startElement;
    
    ExpressionBuilderImpl(){
        this( null );
    }
    
    ExpressionBuilderImpl(Expression expression){
        this.expression = expression;
        this.startElement = this.currentConnectable = expression;
    }
    
    final void handleItem(Type conjunctionType, Connectable connectable){
        if( conjunctionType != null ){
            conjunct(conjunctionType, connectable);
        } else {
            if( expression != null ){
                expression.setCondition(connectable);                
            } else if( startElement == null ){
                startElement = connectable;
            }
            
            currentConnectable = connectable;
        }
    }
    
    private void conjunct(Type conjunctionType, Connectable nextItem){
        currentConnectable.setConjunction(conjunctionType, nextItem);
        currentConnectable = nextItem;
    }
    
    @Override
    public AttributeBuilder or() {
        return new AttributeBuilderImpl(this, Type.OR);
    }

    @Override
    public AttributeBuilder and() {
        return new AttributeBuilderImpl(this, Type.AND);
    }
    
    /**
     * {@inheritDoc}
     * @throws NullPointerException if the condition for the new {@link Expression} is null.
     */
    @Override
    public Expression toExpression() {
        return new Expression(Objects.requireNonNull(startElement, "No Condition for the new Expression given!"));
    }
    
    @Override
    public Connectable build() {
        return startElement;
    }
    
}
