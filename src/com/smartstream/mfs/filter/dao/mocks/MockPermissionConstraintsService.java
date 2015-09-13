package com.smartstream.mfs.filter.dao.mocks;

import com.smartstream.conditions.builders.ExpressionStart;
import com.smartstream.mfs.filter.dao.IPermissionConstraintsService;
import com.viren.conditions.Expression;

public class MockPermissionConstraintsService implements
		IPermissionConstraintsService {

	public MockPermissionConstraintsService() {
	}
	
	@Override
	public Expression getPermissionConstraints(String accessAreaIdAttribute, String objectType) {
		return ExpressionStart.attribute("id", Long.class).isNotNull().toExpression();
	}

	@Override
	public String getSessionId() {
		return "";
	}
}
