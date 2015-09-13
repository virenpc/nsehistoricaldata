package com.smartstream.conditions.builders;

import static com.smartstream.conditions.builders.CommonTestExpressionObjects.TEST_DATE;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.assertEqual;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createComplexExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createComplexExpression2;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createComplexExpressionWithUnary;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createConditionNegation;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createExpressionNegation;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createGroupExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createGroupExpressionNegation;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createNestedGroupsExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createNestedGroupsWithBetweenAndDateExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleAndConjunction;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleLikeExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleNotLikeExpression;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleObject;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSimpleOrConjunction;
import static com.smartstream.conditions.builders.CommonTestExpressionObjects.createSubExpressions;
import static com.smartstream.conditions.builders.ExpressionStart.attribute;
import static com.smartstream.conditions.builders.ExpressionStart.group;
import static com.smartstream.conditions.builders.ExpressionStart.not;

import java.util.Date;

import org.junit.Test;

import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;

public class FluentAPITest {
    @Test
    public void testSimpleObject(){
        // firstName = "Test1"
        Connectable simpleObjectByBuilder = 
        		group(
        	            attribute("name", String.class).eq("Test")
        	            .and().group(
        	                    attribute("viewID", String.class).eq("com.smartstream.mac.Role")
        	                    .or().attribute("viewID", String.class).eq("com.smartstream.mac.User")
        	                    .and().not().group(
        	                            attribute("ownedByAccessAreaID", long.class).in(12L, 1L, 9L)
        	                            .and().attribute("valid", boolean.class).eq(true)
        	                    )
        	                    .or().group(
        	                            attribute("lastModifiedDate", Date.class).ge(TEST_DATE)
        	                            .and().attribute("viewCount", int.class).between(4, 99)
        	                    )
        	             )
        	             .or().attribute("ownedByUserID", int.class).eq(42)
        	        ).build();
        
        System.out.println(simpleObjectByBuilder);
//        assertEqual(simpleObjectByBuilder, createSimpleObject());
    }
    
    @Test
    public void testSimpleAndConjunction(){
        // firstName = "Test1" and lastName = "Test2"
        Connectable simpleAndConjunctionByBuilder = 
            attribute("firstName", String.class).eq("Test1")
            .and().attribute("lastName", String.class).eq("Test2")
        .build();
        
        assertEqual(simpleAndConjunctionByBuilder, createSimpleAndConjunction());
    }
    
    @Test
    public void testSimpleOrConjunction(){
        // firstName = "Test1" or firstName = "Test2"
        Connectable simpleOrConjunctionByBuilder = 
            attribute("firstName", String.class).eq("Test1")
            .or().attribute("firstName", String.class).eq("Test2")
        .build();
        
        assertEqual(simpleOrConjunctionByBuilder, createSimpleOrConjunction());
    }
    
    @Test
    public void testGroupExpression(){
        // firstName = 'Herbert' or firstName = 'Hubert' and (lastName = 'Blub' and married in ('wald', 'traut'))
        Connectable groupExpressionByBuilder = 
            attribute("firstName", String.class).eq("Herbert")
            .or().attribute("firstName", String.class).eq("Hubert")
            .and().group(
                        attribute("lastName", String.class).eq("Blub")
                        .and().attribute("married", String.class).in("Wald", "traut")
                   )
        .build();
        
        assertEqual(groupExpressionByBuilder, createGroupExpression());
    }
    
    @Test
    public void testConditionNegation(){
        // NOT firstName = "Test1"
        Connectable conditionNegationByBuilder =
            not().attribute("firstName", String.class).eq("Test1")
        .build();
        
        assertEqual(conditionNegationByBuilder, createConditionNegation());
    }
    
    @Test
    public void testExpressionNegation(){
        // NOT ( firstName = "Test1" )
        Connectable expressionNegationByBuilder = 
            not().group(attribute("firstName", String.class).eq("Test1"))
        .build();
        
        assertEqual(expressionNegationByBuilder, createExpressionNegation());
    }
    
    @Test
    public void testGroupExpressionNegation(){
        // firstName = 'Herbert' or firstName = 'Hubert' and NOT (lastName = 'Blub' and married in ('wald', 'traut'))
        Connectable groupExpressionNegationByBuilder =
            attribute("firstName", String.class).eq("Herbert")
            .or().attribute("firstName", String.class).eq("Hubert")
            .and().not().group(
                            attribute("lastName", String.class).eq("Blub")
                            .and().attribute("married", String.class).in("wald", "traut")
                         )
        .build();
        
        assertEqual(groupExpressionNegationByBuilder, createGroupExpressionNegation());
    }
    
    @Test
    public void testComplexExpression(){
        /* 
        firstName = 'Herbert' 
            OR firstName = 'Hubert' 
            AND (
                lastName = 'Blub'
                AND married in ('wald', 'traut')
                OR sex = 'm'
            )
            OR human = true
            AND not age < 18
       */
        Connectable complexExpressionByBuilder =
            attribute("firstName", String.class).eq("Herbert")
            .or().attribute("firstName", String.class).eq("Hubert")
            .and().group(
                       attribute("lastName", String.class).eq("Blub")
                       .and().attribute("married", String.class).in("wald", "traut")
                       .or().attribute("sex", char.class).eq('m')
                   )
            .or().attribute("human", boolean.class).eq(true)
            .and().not().attribute("age", int.class).lt(18)     
        .build();
        
        assertEqual(complexExpressionByBuilder, createComplexExpression());
    }
    
    @Test
    public void testComplexExpression2(){
        /* 
            name = 'Test'
            AND viewID = 'Bla'
            OR viewID = 'Blub' 
            AND NOT (
                    ownedByAccessAreaID in (12, 1, 9)
                    AND valid = true
            )
            OR ownedByUserID = 42
        */
        Connectable complexExpression2ByBuilder =
            attribute("name", String.class).eq("Test")
            .and().attribute("viewID", String.class).eq("Bla")
            .or().attribute("viewID", String.class).eq("Blub")
            .and().not().group(
                            attribute("ownedByAccessAreaID", long.class).in(12l, 1l, 9l)
                            .and().attribute("valid", boolean.class).eq(true)
                         )
            .or().attribute("ownedByUserID", long.class).eq(42l)
            
        .build();
        
        assertEqual(complexExpression2ByBuilder, createComplexExpression2());
    }
    
    @Test
    public void testNestedGroupsExpression(){
        /* 
        name = 'Test'
             AND (
                 viewID = 'Bla'
                 OR viewID = 'Blub' 
                 AND NOT (
                         ownedByAccessAreaID in (12, 1, 9)
                         AND valid = true
                 )
             )
             OR ownedByUserID = 42
        */
        Connectable nestedGroupsExpressionByBuilder = 
            attribute("name", String.class).eq("Test")
            .and().group(
                    attribute("viewID", String.class).eq("Bla")
                    .or().attribute("viewID", String.class).eq("Blub")
                    .and().not().group(
                            attribute("ownedByAccessAreaID", long.class).in(12l, 1l, 9l)
                            .and().attribute("valid", boolean.class).eq(true)
                    )
            ).or().attribute("ownedByUserID", int.class).eq(42)
                
        .build();
        
        assertEqual(nestedGroupsExpressionByBuilder, createNestedGroupsExpression());
    }
    
    @Test
    public void testNestedGroupsWithBetweenAndDateExpression(){
        /* 
        name = 'Test'
             AND (
                 viewID = 'Bla'
                 OR viewID = 'Blub' 
                 AND NOT (
                         ownedByAccessAreaID in (12, 1, 9)
                         AND valid = true
                 )
                 OR (
                     lastModifiedDate >= <<[testDate]>>
                     AND viewCount BETWEEN 4 AND 99
                 )
             )
             OR ownedByUserID = 42
        */
        Connectable nestedGroupsWithBetweenAndDateExpressionByBuilder =
            attribute("name", String.class).eq("Test")
            .and().group(
                    attribute("viewID", String.class).eq("Bla")
                    .or().attribute("viewID", String.class).eq("Blub")
                    .and().not().group(
                            attribute("ownedByAccessAreaID", long.class).in(12L, 1L, 9L)
                            .and().attribute("valid", boolean.class).eq(true)
                    )
                    .or().group(
                            attribute("lastModifiedDate", Date.class).ge(TEST_DATE)
                            .and().attribute("viewCount", int.class).between(4, 99)
                    )
             )
             .or().attribute("ownedByUserID", int.class).eq(42)
        .build();
        
        assertEqual(nestedGroupsWithBetweenAndDateExpressionByBuilder, createNestedGroupsWithBetweenAndDateExpression());
    }
    
    @Test
    public void testComplexExpressionWithUnary(){
        /*
          firstName = 'Herbert' 
           OR firstName = 'Hubert' 
          AND (
              lastName = 'Blub'
              AND married in ('wald', 'traut')
               OR sex = 'm'
              AND birthday IS NULL
          )
           OR human = true
          AND not age < 18
         */
        Connectable complexExpressionWithUnaryByBuilder =
            attribute("firstName", String.class).eq("Herbert")
            .or().attribute("firstName", String.class).eq("Hubert")
            .and().group(
                    attribute("lastName", String.class).eq("Blub")
                    .and().attribute("married", String.class).in("wald", "traut")
                    .or().attribute("sex", char.class).eq('m')
                    .and().attribute("birthday", Date.class).isNull()
            )
            .or().attribute("human", boolean.class).eq(true)
            .and().not().attribute("age", int.class).lt(18)
        .build();
        
        assertEqual(complexExpressionWithUnaryByBuilder, createComplexExpressionWithUnary());
    }
    
    @Test
    public void testSimpleLikeExpression(){
        /*
           name like 'lweFilter%'
         */
        Connectable simpeLikeExpression = attribute("name", String.class).like("lweFilter%").build();
        
        assertEqual( simpeLikeExpression, createSimpleLikeExpression() );
    }
    
    @Test
    public void testSimpleNotLikeExpression(){
        /*
           name not like 'lweFilter%'
         */
        Connectable simpeNotLikeExpression = attribute("name", String.class).notLike("lweFilter%").build();
        
        assertEqual( simpeNotLikeExpression, createSimpleNotLikeExpression() );
    }
    
    @Test
    public void testSubExpressions(){
        /*
          ( accessAreaId == 1 && ( ( a == 1 ) ) ) || 
          ( accessAreaId == 2 && ( 
               ( a == 1 || (
                   b == 2 || 
                   c == 3 
               ) ) || 
               ( d == 4 ) || 
               ( e == -1 ) ) 
          ) || 
          ( accessAreaId == 3 )
         */
        
                                    // ( accessAreaId == 1 && ( ( a == 1 ) ) ) ||
        Connectable subExpressions = group(attribute("accessAreaId", Long.class).eq(1l).and().group(group(attribute("a", Long.class).eq(1l)))).or()
                                    // ( accessAreaId == 2 && (
                                   .group(attribute("accessAreaId", Long.class).eq(2l).and().group(
                                           // ( a == 1 || (
                                           group(attribute("a", Long.class).eq(1l).or().group(
                                                   // b == 2 || 
                                                   attribute("b", Long.class).eq(2l).or()
                                                   // c == 3 
                                                  .attribute("c", Long.class).eq(3l)
                                           // ) ) || 
                                           )).or()
                                           // ( d == 4 ) || 
                                           .group(attribute("d", Long.class).eq(4l)).or()
                                           // ( e == -1 ) ) 
                                           .group(attribute("e", Long.class).eq(-1l)))
                                    // ) || 
                                    ).or()
                                    // ( accessAreaId == 3 )
                                    .group(attribute("accessAreaId", Long.class).eq(3l))
                
                .build();
        assertEqual( subExpressions, createSubExpressions() );
    }
}
