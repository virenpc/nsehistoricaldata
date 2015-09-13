package com.smartstream.conditions.builders;

import com.viren.conditions.Connectable;

/**
 * New Terminator for the Fluent-API.
 * <p>
 * This method marks a possible end of the fluent call. The {@link #build()} method in here
 * should be favored over the {@link ExpressionFinalizer#toExpression()} method because it
 * will also be able to build conditions. The {@link ExpressionFinalizer#toExpression()} always
 * wraps the build condition or expression in another expression which doesn't give you the
 * chance to build a condition like this:
 * 
 * <pre>
 *  // a = 12
 *  attribute("a", int.class).eq(12)
 * </pre>
 * 
 * With {@link ExpressionFinalizer#toExpression()} this would result in:
 * <pre>
 *  ( a = 12 ) // attribute("a", int.class).eq(12).toExpression()
 * </pre>
 * 
 * Which is an Expression holding your condition, but with the {@link #build()} method this
 * will result in
 * <pre>
 *  a = 12 // attribute("a", int.class).eq(12).build()
 * </pre>
 *  
 * Which is only a Condition object and not a Condition object wrapped in an Expression.
 * </p>
 * 
 * <p>
 * If you want it to wrap it in an expression you will have to mention this in your fluent API call:
 * <pre>
 *  group( attribute("a", int.class).eq(12) ).build(); // this will result in: ( a = 12 )
 *
 *  // CAUTION: the #toExpression() call will add an additional Expression around it
 *  group( attribute("a", int.class).eq(12) ).toExpression(); // this will result in: ( ( a = 12 ) )
 * </pre>
 * </p>
 * @author brandstetter
 *
 */
public interface Finalizer {
    /**
     * Creates the correct element depending on the fluent chain you've build. This means
     * either an Expression if you started with ExpressionStart#group() or a Condition otherwise.
     * @return the correct element depending on the fluent chain you've build
     */
    Connectable build();
}
