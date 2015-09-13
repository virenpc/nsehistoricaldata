package com.smartstream.mfs.filter.typehandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.ibatis.type.DateTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class DateParamTypeHandler extends AbstractDelegateTypeHandler<Date> {
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    public DateParamTypeHandler() {
        super(new DateTypeHandler());
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (parameter instanceof String) {
            Date dateParameter;
            try {
                dateParameter = dateFormat.parse((String) parameter);
                super.setNonNullParameter(ps, i, dateParameter, jdbcType);
            } catch (ParseException e) {
                throw new IllegalArgumentException(
                        MessageFormat
                                .format("Parameter \"{0}\" is supposed to match format \"{1}\"",
                                        parameter, dateFormat.toPattern()),e);
            }
        } else {
            super.setNonNullParameter(ps, i, (Date) parameter, jdbcType);
        }
    }
    
    @Override
    protected JdbcType getDefaultJdbcType() {
        return JdbcType.TIMESTAMP;
    }
}
