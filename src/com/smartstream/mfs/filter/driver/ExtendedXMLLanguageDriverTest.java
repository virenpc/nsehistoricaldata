package com.smartstream.mfs.filter.driver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;

import com.smartstream.conditions.builders.ExpressionStart;
import com.smartstream.conditions.mapped.ExpressionMapper;
import com.smartstream.conditions.mapped.MappedExpression;
import com.viren.conditions.Expression;

public class ExtendedXMLLanguageDriverTest {

    @Test
    public void testSmartstreamConditionNoObjectAtStart() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO <where>##__SmartStream__Condition:permissionConstraints__##</where>"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        Object parameterObject = null;
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = normalize(boundSql.getSql());
        Assert.assertEquals("SELECT * from FOO", sql);
    }

    @Test
    public void testSmartstreamConditionNoObjectFollowingOtherCondition() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO where 1=1 and ##__SmartStream__Condition:permissionConstraints__##"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        Object parameterObject = null;
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = normalize(boundSql.getSql());
        Assert.assertEquals("SELECT * from FOO where 1=1", sql);
    }

    @Test
    public void testSmartstreamConditionNoObjectFollowingLinefeed() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        // The nested where clause will ensure that the string passed to the buildReplacementNode method starts with "and".
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO"
                + "<where>"
                + "<if test='foo != null'>blah</if>"
                + "\nand ##__SmartStream__Condition:permissionConstraints__##"
                + "</where>"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        Object parameterObject = null;
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = normalize(boundSql.getSql());
        Assert.assertEquals("SELECT * from FOO", sql);
    }

    @Test
    public void testSmartstreamConditionMandatoryObjectMissing() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO where 1=1 and ##__SmartStream__Condition:permissionConstraints!__##"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        try {
            Object parameterObject = null;
            ms.getBoundSql(parameterObject);
            Assert.fail("Missing parameter not detected");
        } catch(IllegalStateException e) {
            Assert.assertEquals("Missing mandatory parameter [permissionConstraints]", e.getMessage());
        }
    }

    @Test
    public void testSmartstreamConditionMultipleInstances() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO <where>"
                + "##__SmartStream__Condition:p1__##"
                + " and ##__SmartStream__Condition:p2__##"
                + " and 1=1"
                + " or ##__SmartStream__Condition:p3__##"
                + " or 2=2"
                + "</where>"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        
        Expression p1 = ExpressionStart.attribute("a1", Long.class).eq(1L).toExpression(); 
        Expression p3 = ExpressionStart.attribute("a3", Long.class).eq(3L).toExpression();
        
        ExpressionMapper identityExpressionMapper = new IdentityExpressionMapper();
        Map<String, Object> parameterObject = new HashMap<>();
        parameterObject.put("p1", new MappedExpression(identityExpressionMapper, p1));
        parameterObject.put("p3", new MappedExpression(identityExpressionMapper, p3));

        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = normalize(boundSql.getSql());
        Assert.assertEquals("SELECT * from FOO WHERE ( a1 = ? ) and 1=1 or ( a3 = ? ) or 2=2", sql.trim());
    }


    @Test
    public void testExpressionMapper() {
        Configuration config = new Configuration();
        config.setDefaultScriptingLanguage(ExtendedXMLLanguageDriver.class);

        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "\n"
                + "\n<mapper namespace=\"com.smartstream.filter\">"
                + "\n<select id=\"q1\">"
                + "\nSELECT * from FOO <where>"
                + "##__SmartStream__Condition:p1__##"
                + " and ##__SmartStream__Condition:p2__##"
                + " and 1=1"
                + " or ##__SmartStream__Condition:p3__##"
                + " or 2=2"
                + "</where>"
                + "\n</select>"
                + "\n</mapper>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        String resource = "input1"; // name of file (or other source) that the inputStream points to

        Map<String, XNode> sqlFragments = null;
        XMLMapperBuilder builder = new XMLMapperBuilder(is, config, resource, sqlFragments);
        builder.parse();

        MappedStatement ms = builder.getConfiguration().getMappedStatement("q1");
        
        Expression p1 = ExpressionStart.attribute("a1", Long.class).eq(1L).toExpression(); 
        Expression p3 = ExpressionStart.attribute("a3", Long.class).eq(3L).toExpression();
        
        ExpressionMapper expressionMapper = new ExpressionMapper() {

            @Override
            public String map(String attrName) {
                if ("a1".equals(attrName)) {
                    return "col1";
                }
                return attrName;
            }
            
        };
        
        Map<String, Object> parameterObject = new HashMap<>();
        parameterObject.put("p1", new MappedExpression(expressionMapper, p1));
        parameterObject.put("p3", new MappedExpression(expressionMapper, p3));

        BoundSql boundSql = ms.getBoundSql(parameterObject);
        String sql = normalize(boundSql.getSql());
        Assert.assertEquals("SELECT * from FOO WHERE ( col1 = ? ) and 1=1 or ( a3 = ? ) or 2=2", sql.trim());
    }

    // replace multiple whitespace chars with just one
    private String normalize(String s) {
        String s2 = s.replaceAll("\\s+", " ");
        return s2;
    }
    
    private static class IdentityExpressionMapper implements ExpressionMapper {
        @Override
        public String map(String attrName) {
            return attrName;
        }
    }
}
