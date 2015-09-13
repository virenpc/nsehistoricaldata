package com.smartstream.mfs.filter.typehandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class BooleanParamTypeHandler extends AbstractDelegateTypeHandler<Boolean> {

	public BooleanParamTypeHandler() {
        super(new BooleanTypeHandler());
    }

    public void setNonNullParameter(PreparedStatement ps, int i,
			Object parameter, JdbcType jdbcType) throws SQLException {
		if (parameter instanceof String) {
			Boolean booleanParameter;
			try {
				booleanParameter = Boolean.parseBoolean((String) parameter);
				super.setNonNullParameter(ps, i, booleanParameter, jdbcType);
			} catch (Exception e) {
				throw new IllegalArgumentException(MessageFormat.format("Error parsing parameter \"{0}\" to a Boolean value", parameter),e);
			}
		} else {
			super.setNonNullParameter(ps, i, parameter, jdbcType);
		}
	}
    
    @Override
    protected JdbcType getDefaultJdbcType() {
        return JdbcType.BIT;
    }

}
