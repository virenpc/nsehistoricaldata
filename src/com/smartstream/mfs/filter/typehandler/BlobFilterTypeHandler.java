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

import com.smartstream.emf.serialization.IEMFSerializer;
import com.smartstream.emf.serialization.SerializationFactory;
import com.smartstream.emf.serialization.SerializationFactory.StdSerializerTypes;
import com.smartstream.filter.Filter;

public class BlobFilterTypeHandler extends BaseTypeHandler<Filter> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Filter filter, JdbcType jdbcType) throws SQLException {
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
    public Filter getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toFilter( rs.getBlob(columnName) );
    }
    
    @Override
    public Filter getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toFilter( rs.getBlob(columnIndex) );
    }

    @Override
    public Filter getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toFilter( cs.getBlob(columnIndex) );
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
