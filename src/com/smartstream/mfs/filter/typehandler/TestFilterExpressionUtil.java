package com.smartstream.mfs.filter.typehandler;

import org.junit.Assert;
import org.junit.Test;

import com.smartstream.conditions.builders.ExpressionStart;
import com.smartstream.conditions.util.ExpressionUtil;
import com.smartstream.filter.Attribute;
import com.smartstream.filter.Condition;
import com.smartstream.filter.Filter;
import com.smartstream.filter.FilterFactory;
import com.smartstream.filter.Operator;
import com.smartstream.filter.externalization.json.JsonExternalization;
import com.smartstream.filter.util.FilterToExpressionUtil;
import com.viren.conditions.Conjunction;
import com.viren.conditions.Expression;

public class TestFilterExpressionUtil {
    @Test
    public void testNullFilterToExpr() {
        Expression e = FilterToExpressionUtil.filterToExpression(null);
        Assert.assertNull(e);
    }

    @Test
    public void testEmptyFilterToExpr() {
        Filter f = FilterFactory.eINSTANCE.createFilter();
        Expression e = FilterToExpressionUtil.filterToExpression(f);
        Assert.assertNull(e);
    }

    @Test
    public void testFilterToExpr() throws Exception {
        Filter f = FilterFactory.eINSTANCE.createFilter();
        f.setConjunction(com.smartstream.filter.Conjunction.OR);

        Condition condEq1 = FilterFactory.eINSTANCE.createCondition(Operator.EQUALS, "1");
        Attribute attrIdEq1 = FilterFactory.eINSTANCE.createAttribute("name", condEq1);
        f.getAttributeFilters().add(attrIdEq1);

        Condition condEqSam = FilterFactory.eINSTANCE.createCondition(Operator.EQUALS, "Sam");
        Attribute attrNameEqSam = FilterFactory.eINSTANCE.createAttribute("name", condEqSam);
        f.getAttributeFilters().add(attrNameEqSam);

        Expression e = FilterToExpressionUtil.filterToExpression(f);
        Assert.assertNotNull(e);

        Expression idExpr = ExpressionStart.attribute("name", String.class).eq("1").toExpression();
        Expression nameExpr = ExpressionStart.attribute("name",  String.class).eq("Sam").toExpression();

        Expression expected = ExpressionUtil.copy(idExpr);
        expected.setConjunction(Conjunction.Type.OR, nameExpr);

        String jsonStringE = JsonExternalization.toJsonString(e);
        String jsonStringExpected = JsonExternalization.toJsonString(expected);
        Assert.assertEquals(jsonStringExpected, jsonStringE);
        Assert.assertTrue(ExpressionUtil.equal(expected, e));
    }
}
