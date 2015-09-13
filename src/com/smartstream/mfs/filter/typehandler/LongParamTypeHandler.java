package com.smartstream.mfs.filter.typehandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LongTypeHandler;

public class LongParamTypeHandler extends AbstractDelegateTypeHandler<Long> {

	public LongParamTypeHandler() {
        super(new LongTypeHandler());
    }

    public void setNonNullParameter(PreparedStatement ps, int i,
			Object parameter, JdbcType jdbcType) throws SQLException {
		if (parameter instanceof String) {
			Long longParameter;
			try {
				longParameter = Long.parseLong((String) parameter);
				super.setNonNullParameter(ps, i, longParameter, jdbcType);
			} catch (Exception e) {
	            throw new IllegalArgumentException(MessageFormat.format("Error processing parameter \"{0}\" expected value of type Long", parameter),e); 
			}
		} 
		else {
	        super.setNonNullParameter(ps, i, parameter, jdbcType);
		}
	}
    
    @Override
    protected JdbcType getDefaultJdbcType() {
        return JdbcType.NUMERIC;
    }

}
