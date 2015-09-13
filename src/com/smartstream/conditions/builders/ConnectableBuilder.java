package com.smartstream.conditions.builders;

import com.viren.conditions.AttributeData;

public interface ConnectableBuilder {
	
    <T> ConditionBuilder<T> attribute(String name, Class<T> type);
    
    <T> ConditionBuilder<T> attribute(AttributeData<T> attributeData);
    
    ExpressionBuilder group( ExpressionBuilder group );
    
    ExpressionBuilder group( com.viren.conditions.Expression group );
}
