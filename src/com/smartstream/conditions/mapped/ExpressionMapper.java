package com.smartstream.conditions.mapped;

/**
 * Interface for objects capable of mapping an "attribute name" within a com.smartstream.conditions.Condition
 * to some other form.
 * <p>
 * The primary use-case is to map a java property name to an SQL column name.
 * </p>
 */
public interface ExpressionMapper {
    String map(String attrName);
}
