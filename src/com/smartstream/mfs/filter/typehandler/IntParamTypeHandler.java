package com.smartstream.mfs.filter.typehandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class IntParamTypeHandler extends AbstractDelegateTypeHandler<Integer> {

	public IntParamTypeHandler() {
        super(new IntegerTypeHandler());
    }

    @Override
	public void setNonNullParameter(PreparedStatement ps, int i,
			Object parameter, JdbcType jdbcType) throws SQLException {
		if (parameter instanceof String) {
			Integer intParameter;
			try {
				intParameter = Integer.parseInt((String) parameter);
				super.setNonNullParameter(ps, i, intParameter, jdbcType);
			} catch (Exception e) {
				throw new IllegalArgumentException(MessageFormat.format("Error parsing parameter \"{0}\" to a Integer value", parameter),e);
			}
		} else {
			super.setNonNullParameter(ps, i, parameter, jdbcType);
		}
	}

    @Override
    protected JdbcType getDefaultJdbcType() {
        return JdbcType.INTEGER;
    }
}
