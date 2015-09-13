package com.smartstream.conditions.builders;

import com.viren.conditions.Conjunction.Type;

final class NotBuilderImpl extends ConnectableBuilderImpl implements NotBuilder {

    NotBuilderImpl(ExpressionBuilderImpl rootBuilder, Type conjunctionType) {
        super(rootBuilder, conjunctionType, true);
    }

}
