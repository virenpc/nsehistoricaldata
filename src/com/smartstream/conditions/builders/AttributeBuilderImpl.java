package com.smartstream.conditions.builders;

import com.viren.conditions.Conjunction.Type;

final class AttributeBuilderImpl extends ConnectableBuilderImpl implements AttributeBuilder {
    
    AttributeBuilderImpl(ExpressionBuilderImpl rootBuilder, Type conjunctionType){
        super(rootBuilder, conjunctionType, false); // never negated
    }

    @Override
    public NotBuilder not() {
        return new NotBuilderImpl(rootBuilder, conjunctionType);
    }
}
