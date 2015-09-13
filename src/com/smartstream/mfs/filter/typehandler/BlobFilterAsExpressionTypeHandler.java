package com.smartstream.mfs.filter.typehandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartstream.emf.serialization.IEMFSerializer;
import com.smartstream.emf.serialization.SerializationFactory;
import com.smartstream.emf.serialization.SerializationFactory.StdSerializerTypes;
import com.smartstream.filter.Filter;
import com.smartstream.filter.util.ExpressionToFilterUtil;
import com.smartstream.filter.util.FilterToExpressionUtil;
import com.viren.conditions.Expression;

/**
 * A TEMPORARY handler class which can read an EMF filter object out of the database, and then
 * convert it to a POJO Expression object.
 */
public class BlobFilterAsExpressionTypeHandler extends BaseTypeHandler<Expression> {
    private static final Logger log = LoggerFactory.getLogger(BlobFilterAsExpressionTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Expression expr, JdbcType jdbcType) throws SQLException {
        Filter filter = ExpressionToFilterUtil.expressionToFilter(expr);
        IEMFSerializer serializer = SerializationFactory.get(StdSerializerTypes.BINARY) ;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            serializer.serialize(filter, baos, null);
        } catch (IOException e) {
            throw new SQLException("Can't serialize Filter!", e);
        }
        ps.setBlob(i, new ByteArrayInputStream(baos.toByteArray()));
    }

    @Override
    public Expression getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return filterToExpression(toFilter(rs.getBlob(columnName)));
    }
    
    @Override
    public Expression getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return filterToExpression(toFilter(rs.getBlob(columnIndex)));
    }

    @Override
    public Expression getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return filterToExpression(toFilter(cs.getBlob(columnIndex)));
    }

    private Expression filterToExpression(Filter f) {
        try {
            return FilterToExpressionUtil.filterToExpression(f);
        } catch(IllegalArgumentException | UnsupportedOperationException e) {
            // Non-standard exception handling; most callers won't care about the cause - and this
            // is a temporary class that will soon be removed...
            log.debug("Unable to map filter to Expression; cause is " + e.getMessage(), e);
            log.warn("Unable to map filter to Expression; see MOR-3513");
            return null;
        }
    }

    private static Filter toFilter(Blob value) throws SQLException{
        if(value == null) return null;
        
        IEMFSerializer serializer = SerializationFactory.get(StdSerializerTypes.BINARY);
        try {
            return (Filter) serializer.deserialize(value.getBinaryStream(), null);
        } catch (IOException e) {
            throw new SQLException("Can't deserialize the stored filter!", e);
        }
    }
}
