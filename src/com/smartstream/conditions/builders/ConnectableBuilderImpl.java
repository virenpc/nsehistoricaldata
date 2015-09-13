package com.smartstream.conditions.builders;

import java.util.Objects;

import com.viren.conditions.AttributeData;
import com.viren.conditions.Expression;
import com.viren.conditions.Conjunction.Type;

abstract class ConnectableBuilderImpl implements ConnectableBuilder {
    
    protected final ExpressionBuilderImpl rootBuilder;
    protected final Type conjunctionType;
    
    private final boolean negate;
    
    ConnectableBuilderImpl(ExpressionBuilderImpl rootBuilder, Type conjunctionType, boolean negate){
        this.rootBuilder = Objects.requireNonNull(rootBuilder, "ExpressionBuilder is required!");
        this.conjunctionType = conjunctionType;
        this.negate = negate;
    }

    @SuppressWarnings("deprecation") // NOPMD: it's OK to use the ExpressionFinalizer#toExpression() method here, because it does exactly what we want
    @Override
    public ExpressionBuilder group(ExpressionBuilder group) {
        return group(group.toExpression());
    }

    @Override
    public ExpressionBuilder group(Expression group) {
        rootBuilder.handleItem(conjunctionType, group.setNegate(negate));
        return rootBuilder;
    }
    
    /**
     * {@inheritDoc}
     * @throws NullPointerException if either the name or the type is null
     */
    @Override
    public <T> ConditionBuilder<T> attribute(String name, Class<T> type) {
        return new ConditionBuilderImpl<>(rootBuilder, name, type, conjunctionType, negate);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException if attributeDatais null
     */
    @Override
    public <T> ConditionBuilder<T> attribute(AttributeData<T> attributeData){
        return new ConditionBuilderImpl<>(rootBuilder, attributeData.getName(), attributeData.getType(), conjunctionType, negate);
    }

}
