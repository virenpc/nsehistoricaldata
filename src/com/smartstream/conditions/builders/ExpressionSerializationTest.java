package com.smartstream.conditions.builders;

import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createListOfInvalidExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createListOfManualExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.equal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.smartstream.conditions.util.ExpressionUtil;
import com.viren.conditions.Connectable;

/**
 * Test for different serialization mechanism of a expression or condition.
 * 
 * @author brandstetter
 */
public class ExpressionSerializationTest {

    public ExpressionSerializationTest() {
    }
    
    @Test
    public void testExpressionSimpleSerailization() throws ClassNotFoundException, IOException{
        for( Connectable validManualExpr : createListOfManualExpression() ){
            doSerializationTest(validManualExpr);
        }
        
        for( Connectable invalidManualExpr : createListOfInvalidExpression() ){
            doSerializationTest(invalidManualExpr);
        }
    }
    
    private void doSerializationTest(Connectable con) throws ClassNotFoundException, IOException{
        Connectable serializedCon;
        
        if(!equal(con, serializedCon = deserialize(serialize(con)))){
            Assert.fail("serailization of '" + ExpressionUtil.formatExpression(con) + "' failed! (results: " + ExpressionUtil.formatExpression(serializedCon) + ")");
        }
    }
    
    private byte[] serialize(Connectable con) throws IOException {
        try( ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos) ) {
            oos.writeObject(con);
            return baos.toByteArray();
        }
    }
    
    private Connectable deserialize(byte[] con) throws IOException, ClassNotFoundException {
        try( ByteArrayInputStream bais = new ByteArrayInputStream(con);
             ObjectInputStream ois = new ObjectInputStream(bais) ) {
            return (Connectable) ois.readObject();
        }
    }

}
