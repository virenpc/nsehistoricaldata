package com.smartstream.conditions.builders;

import java.util.Objects;

import com.viren.conditions.BinaryCondition;
import com.viren.conditions.Condition;
import com.viren.conditions.Operator;
import com.viren.conditions.PolyadicCondition;
import com.viren.conditions.UnaryCondition;
import com.viren.conditions.Conjunction.Type;

final class ConditionBuilderImpl<T> implements ConditionBuilder<T> {
    
    private final ExpressionBuilderImpl rootBuilder;
    
    private final String attributeName;
    private final Class<T> type;
    private final boolean negate;
    private final Type conjunctionType;
    
    ConditionBuilderImpl(ExpressionBuilderImpl rootBuilder, String attributeName, Class<T> type, Type conjunctionType) {
        this(rootBuilder, attributeName, type, conjunctionType, false);
    }
    
    ConditionBuilderImpl(ExpressionBuilderImpl rootBuilder, String attributeName, Class<T> type, Type conjunctionType, boolean negate) {
        this.rootBuilder = Objects.requireNonNull(rootBuilder, "ExpressionBuilder is required!");
        this.attributeName = Objects.requireNonNull(attributeName, "No attribute name given!");
        this.type = Objects.requireNonNull(type, "No attribute type given!");
        this.conjunctionType = conjunctionType;
        this.negate = negate;
    }

    @Override
    public ExpressionBuilder isNull() {
        return conjunct(createUnaryCondition(Operator.IS_NULL));
    }

    @Override
    public ExpressionBuilder isNotNull() {
        return conjunct(createUnaryCondition(Operator.IS_NOT_NULL));
    }

    @Override
    public ExpressionBuilder eq(T value) {
        return conjunct(createBinaryCondition(Operator.EQUALS, value));
    }

    @Override
    public ExpressionBuilder ne(T value) {
        return conjunct(createBinaryCondition(Operator.NOT_EQUALS, value));
    }

    @Override
    public ExpressionBuilder gt(T value) {
        return conjunct(createBinaryCondition(Operator.GREATER_THAN, value));
    }

    @Override
    public ExpressionBuilder ge(T value) {
        return conjunct(createBinaryCondition(Operator.GREATER_THAN_OR_EQUALS, value));
    }

    @Override
    public ExpressionBuilder lt(T value) {
        return conjunct(createBinaryCondition(Operator.LESS_THAN, value));
    }

    @Override
    public ExpressionBuilder le(T value) {
        return conjunct(createBinaryCondition(Operator.LESS_THAN_OR_EQUALS, value));
    }

    @Override
    @SafeVarargs
    public final ExpressionBuilder in(T... values) {
        return conjunct(createPolyadicCondition(Operator.IN, values));
    }

    @Override
    @SafeVarargs
    public final ExpressionBuilder notIn(T... values) {
        return conjunct(createPolyadicCondition(Operator.NOT_IN, values));
    }

    @Override
    public final ExpressionBuilder like(T value) {
        return conjunct(createBinaryCondition(Operator.LIKE, value));
    }

    @Override
    public final ExpressionBuilder notLike(T value) {
        return conjunct(createBinaryCondition(Operator.NOT_LIKE, value));
    }

    @Override
    public ExpressionBuilder between(T from, T to) {
        return conjunct(createPolyadicCondition(Operator.BETWEEN, from, to));
    }
    
    private ExpressionBuilder conjunct(Condition<T> condition){
        rootBuilder.handleItem(conjunctionType, condition);
        return rootBuilder;
    }
    
    private UnaryCondition<T> createUnaryCondition(Operator unaryOperator){
        UnaryCondition<T> unaryCon = new UnaryCondition<>(type, attributeName, unaryOperator);
        unaryCon.setNegate(negate);
        return unaryCon;
    }
    
    private BinaryCondition<T> createBinaryCondition(Operator binaryOperator, T value){
        BinaryCondition<T> binaryCon = new BinaryCondition<>(type, attributeName, binaryOperator, value);
        binaryCon.setNegate(negate);
        return binaryCon;
    }
    
    @SafeVarargs
    private final PolyadicCondition<T> createPolyadicCondition(Operator polyadicOperator, T... values){
        PolyadicCondition<T> polyadicCon = new PolyadicCondition<>(type, attributeName, polyadicOperator, values);
        polyadicCon.setNegate(negate);
        return polyadicCon;
    }
}
