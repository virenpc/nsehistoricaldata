package com.smartstream.mfs.filter.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * type handler which accepts parameters of type 'Object' and delegates
 * to a type specific handler.
 *  
 * @author sinclair
 * @author rosenauer
 *
 * @param <T>
 */
public abstract class AbstractDelegateTypeHandler<T> extends BaseTypeHandler<Object> {
    
    protected final BaseTypeHandler<T> delegate;
    
    public AbstractDelegateTypeHandler(BaseTypeHandler<T> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        super.setParameter(ps, i, parameter, jdbcType == null || jdbcType == JdbcType.OTHER ? getDefaultJdbcType() : jdbcType);
    }

    @SuppressWarnings("unchecked")  // we need to cast to the delegate type here, see class comment
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        delegate.setNonNullParameter(ps, i, (T)parameter, jdbcType);
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return delegate.getNullableResult(rs, columnName);
    }
    
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return delegate.getNullableResult(rs, columnIndex);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return delegate.getNullableResult(cs, columnIndex);
    }
    
    /**
     * Specify the default JdbcType for the concrete {@link TypeHandler}.
     * <p>
     * This will normally be used if null should be set as a parameter for the prepared statement.
     * </p> 
     * @return the JdbcType for the concrete {@link TypeHandler}
     */
    protected abstract JdbcType getDefaultJdbcType();
    
}
