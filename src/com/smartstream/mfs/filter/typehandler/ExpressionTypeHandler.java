package com.smartstream.mfs.filter.typehandler;

import java.io.IOException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.smartstream.filter.externalization.json.JsonExternalization;
import com.viren.conditions.Condition;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;

/**
 * Typehandler for {@link Expression} or {@link Condition} it will save the JSON representation of those object in a BLOB field.
 * 
 * @author brandstetter
 */
public class ExpressionTypeHandler extends BaseTypeHandler<Connectable> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Connectable parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setBlob(i, JsonExternalization.toJsonAsStream(parameter));
        } catch (IOException e) {
            throw new SQLException("Can't convert expression/condition to JSON. (reason: " + e.getMessage() + ")", e);
        }
    }

    @Override
    public Connectable getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return blobToExpression(rs.getBlob(columnName));
    }

    @Override
    public Connectable getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return blobToExpression(rs.getBlob(columnIndex));
    }

    @Override
    public Connectable getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return blobToExpression(cs.getBlob(columnIndex));
    }
    
    /**
     * Helper method to read the JSON data from the blob object and build an Expression/Condition out of it.
     * @param value the blob from the DB
     * @return the Expression/Condition stored in the blob or null if the given blob was null
     * @throws SQLException if the stored data in the blob couldn't be parsed to an Expression/Condition
     */
    private static Connectable blobToExpression(Blob value) throws SQLException {
        if( value == null ) return null;
        
        try {
            return JsonExternalization.jsonToExpression(value.getBinaryStream());
        } catch (IOException e) {
            throw new SQLException("Can't convert JSON representation to expression/condition. (reason: " + e.getMessage() + ")", e);
        }
    }

}
