package com.smartstream.conditions.builders;

public interface ConditionBuilder<T> {
    ExpressionBuilder isNull();
    ExpressionBuilder isNotNull();
    
    ExpressionBuilder eq( T value );
    ExpressionBuilder ne( T value );
    
    ExpressionBuilder gt( T value );
    ExpressionBuilder ge( T value );
    
    ExpressionBuilder lt( T value );
    ExpressionBuilder le( T value );
    
    ExpressionBuilder in( @SuppressWarnings("unchecked") T... values );
    ExpressionBuilder notIn( @SuppressWarnings("unchecked") T... values );
    
    ExpressionBuilder like( T value );
    ExpressionBuilder notLike( T value );
    
    ExpressionBuilder between( T from, T to );
}
