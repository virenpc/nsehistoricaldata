package com.smartstream.conditions.mapped;

import com.viren.conditions.Expression;

/**
 * Represents an Expression object paired with another object that can translate the attribute names within
 * the expression to some "target domain" (eg to SQL column names).
 */
public class MappedExpression {
    private final ExpressionMapper mapper;
    private final Expression expression;
    
    public MappedExpression(ExpressionMapper mapper, Expression expression) {
        this.mapper = mapper;
        this.expression = expression;
    }
    
    public ExpressionMapper getMapper() {
        return mapper;
    }
    
    public Expression getExpression() {
        return expression;
    }
}
