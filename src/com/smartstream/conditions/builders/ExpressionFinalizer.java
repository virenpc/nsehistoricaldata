package com.smartstream.conditions.builders;

import com.viren.conditions.Expression;

/**
 * Interface which marks the end of Expression.
 * 
 * @deprecated use {@link Finalizer} instead
 */
@Deprecated
public interface ExpressionFinalizer {
    /**
     * This method will create a new Expression object which wraps the Connectable created in the concrete builder during the Fluent-API build.
     * @return a newly created Expression
     * @deprecated This will only allow you to create an expression (conditions are wrapped in an {@link Expression})
     *             for a replacement see: {@link Finalizer} and {@link Finalizer#build()}
     */
    @Deprecated
    Expression toExpression();
}
