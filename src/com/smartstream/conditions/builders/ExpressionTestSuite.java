package com.smartstream.conditions.builders;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ArityIsArgumentCountValidTest.class, ExpressionObjectTest.class, ExpressionSerializationTest.class, ExpressionUtilityTest.class, FluentAPITest.class})
public class ExpressionTestSuite {

}
