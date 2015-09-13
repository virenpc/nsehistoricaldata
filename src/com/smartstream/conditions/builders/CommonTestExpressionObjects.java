package com.smartstream.conditions.builders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import com.smartstream.conditions.util.EqualUtil;
import com.smartstream.conditions.util.ExpressionUtil;
import com.viren.conditions.BinaryCondition;
import com.viren.conditions.Conjunction;
import com.viren.conditions.Connectable;
import com.viren.conditions.Expression;
import com.viren.conditions.Operator;
import com.viren.conditions.PolyadicCondition;
import com.viren.conditions.UnaryCondition;
import com.viren.conditions.Conjunction.Type;

/**
 * Contains only test expression objects for JUnit tests.
 * 
 * @author brandstetter
 */
public abstract class CommonTestExpressionObjects {

    private CommonTestExpressionObjects() {
        throw new IllegalStateException("Don't create it!");
    }
    
    public static final Date TEST_DATE = new Date();
    
    // --------------------------------------------------------------------------------------------------------------------------------------------------------
    // -------------- manually created sample expression test objects -----------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * @return firstName = "Test1"
     */
    public static Connectable createSimpleObject(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test1");
    }
    
    /**
     * @return firstName = "Test1" and lastName = "Test2"
     */
    public static Connectable createSimpleAndConjunction(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test1")
                    .setConjunction(new Conjunction(Type.AND, new BinaryCondition<>(String.class, "lastName", Operator.EQUALS).setValue("Test2")));
    }
    
    /**
     * @return firstName = "Test1" or firstName = "Test2"
     */
    public static Connectable createSimpleOrConjunction(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test1")
                .setConjunction(new Conjunction(Type.OR, new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test2")));
    }
    
    /**
     * @return firstName = 'Herbert' or firstName = 'Hubert' and (lastName = 'Blub' and married in ('wald', 'traut'))
     */
    public static Connectable createGroupExpression(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Herbert")
                    .setConjunction(Type.OR, new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Hubert")
                                            .setConjunction( Type.AND, new Expression(
                                                        new BinaryCondition<>(String.class, "lastName", Operator.EQUALS).setValue("Blub").setConjunction(
                                                                Type.AND, new PolyadicCondition<>(String.class, "married", Operator.IN).values("Wald", "traut"))
                                                        )
                                            )
                                   );
    }
    
    /**
     * @return NOT firstName = "Test1"
     */
    public static Connectable createConditionNegation(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test1").setNegate(true);
    }
    
    /**
     * @return NOT ( firstName = "Test1" )
     */
    public static Connectable createExpressionNegation(){
        return new Expression(
                new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Test1")
            ).setNegate(true);
    }
    
    /**
     * @return firstName = 'Herbert' or firstName = 'Hubert' and NOT (lastName = 'Blub' and married in ('wald', 'traut'))
     */
    public static Connectable createGroupExpressionNegation(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Herbert")
                    .setConjunction(Type.OR, new BinaryCondition<>(String.class, "firstName", Operator.EQUALS).setValue("Hubert")
                                            .setConjunction( Type.AND, new Expression(
                                                        new BinaryCondition<>(String.class, "lastName", Operator.EQUALS).setValue("Blub").setConjunction(
                                                                Type.AND, new PolyadicCondition<>(String.class, "married", Operator.IN).values("wald", "traut"))
                                                        ).setNegate(true)
                                            )
                                   );
    }
    
    /**
     * @return <pre>
     * firstName = 'Herbert' 
     *  OR firstName = 'Hubert' 
     * AND (
     *     lastName = 'Blub'
     *     AND married in ('wald', 'traut')
     *      OR sex = 'm'
     * )
     *  OR human = true
     * AND not age < 18
     * </pre>
     */
    public static Connectable createComplexExpression(){
        return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS, "Herbert")
                    .setConjunction(Type.OR, new BinaryCondition<>(String.class, "firstName", Operator.EQUALS, "Hubert")
                    .setConjunction(Type.AND, new Expression(
                                new BinaryCondition<>(String.class, "lastName", Operator.EQUALS, "Blub")
                                .setConjunction(Type.AND, new PolyadicCondition<>(String.class, "married", Operator.IN, "wald", "traut")
                                .setConjunction(Type.OR, new BinaryCondition<>(char.class, "sex", Operator.EQUALS, 'm')))
                            )
                    .setConjunction(Type.OR, new BinaryCondition<>(boolean.class, "human", Operator.EQUALS, true)
                    .setConjunction(Type.AND, new BinaryCondition<>(int.class, "age", Operator.LESS_THAN, 18).setNegate(true)))) );
    }
    
    /**
     * @return <pre>
     * name = 'Test'
     * AND viewID = 'Bla'
     *  OR viewID = 'Blub' 
     * AND NOT (
     *     ownedByAccessAreaID in (12, 1, 9)
     *     AND valid = true
     * )
     * OR ownedByUserID = 42
     * </pre>
     */ 
    public static Connectable createComplexExpression2(){
        return new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                    .setConjunction(Type.AND, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                    .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                    .setConjunction(Type.AND, new Expression(
                                new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                            ).setNegate(true)
                    .setConjunction(Type.OR, new BinaryCondition<>(long.class, "ownedByUserID", Operator.EQUALS, 42l)))));
    }
    
    /**
     * @return <pre>
     * name = 'Test'
     * AND (
     *     viewID = 'Bla'
     *      OR viewID = 'Blub' 
     *     AND NOT (
     *         ownedByAccessAreaID in (12, 1, 9)
     *         AND valid = true
     *     )
     * )
     *  OR ownedByUserID = 42
     * </pre>
     */
     public static Connectable createNestedGroupsExpression(){
         return new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                     .setConjunction(Type.AND, new Expression(
                             new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                             .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                             .setConjunction(Type.AND, new Expression(
                                                             new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                             .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                       ).setNegate(true)
                             )))
                     .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)));
     }

     
     /**
      * @return <pre>
      * name = 'Test'
      * AND (
      *     viewID = 'Bla'
      *      OR viewID = 'Blub' 
      *     AND NOT (
      *         ownedByAccessAreaID in (12, 1, 9)
      *         AND valid = true
      *     )
      *      OR (
      *         lastModifiedDate >= <<[testDate]>>
      *         AND viewCount BETWEEN 4 AND 99
      *     )
      * )
      *  OR ownedByUserID = 42
      * </pre>
     */
      public static Connectable createNestedGroupsWithBetweenAndDateExpression(){
          return new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99))
                                                       )
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)));
      }
      
      /**
       * @return <pre>
       * firstName = 'Herbert' 
       *  OR firstName = 'Hubert' 
       * AND (
       *     lastName = 'Blub'
       *     AND married in ('wald', 'traut')
       *      OR sex = 'm'
       *     AND birthday IS NULL
       * )
       *  OR human = true
       * AND not age < 18
       * </pre>
       */
      public static Connectable createComplexExpressionWithUnary(){
          return new BinaryCondition<>(String.class, "firstName", Operator.EQUALS, "Herbert")
                      .setConjunction(Type.OR, new BinaryCondition<>(String.class, "firstName", Operator.EQUALS, "Hubert")
                      .setConjunction(Type.AND, new Expression(
                                  new BinaryCondition<>(String.class, "lastName", Operator.EQUALS, "Blub")
                                  .setConjunction(Type.AND, new PolyadicCondition<>(String.class, "married", Operator.IN, "wald", "traut")
                                  .setConjunction(Type.OR, new BinaryCondition<>(char.class, "sex", Operator.EQUALS, 'm')
                                  .setConjunction(Type.AND, new UnaryCondition<>(Date.class, "birthday", Operator.IS_NULL))))
                              )
                      .setConjunction(Type.OR, new BinaryCondition<>(boolean.class, "human", Operator.EQUALS, true)
                      .setConjunction(Type.AND, new BinaryCondition<>(int.class, "age", Operator.LESS_THAN, 18).setNegate(true)))) );
      }
      
      /**
       * @return <pre>
       * name like 'lweFilter%'
       * </pre>
       */
      public static Connectable createSimpleLikeExpression(){
          return new BinaryCondition<>(String.class, "name", Operator.LIKE, "lweFilter%");
      }
      
      /**
       * @return <pre>
       * name not like 'lweFilter%'
       * </pre>
       */
      public static Connectable createSimpleNotLikeExpression(){
          return new BinaryCondition<>(String.class, "name", Operator.NOT_LIKE, "lweFilter%");
      }
      
      /**
       * @return <pre>
       *   ( accessAreaId == 1 && ( ( a == 1 ) ) ) || 
       *   ( accessAreaId == 2 && ( 
       *        ( a == 1 || (
       *            b == 2 || 
       *            c == 3 
       *        ) ) || 
       *        ( d == 4 ) || 
       *        ( e == -1 ) ) 
       *   ) || 
       *   ( accessAreaId == 3 )
       * </ pre>
       */
      public static Connectable createSubExpressions(){
          // ( accessAreaId == 1 && ( ( a == 1 ) ) )
          Expression partOne = new Expression( new BinaryCondition<>(Long.class, "accessAreaId", Operator.EQUALS, 1L).setConjunction(Type.AND, new Expression(
                                  new Expression(
                                          new BinaryCondition<>(Long.class, "a", Operator.EQUALS, 1L)
                                  ))));
          
          // ( accessAreaId == 2 && ( ( a == 1 || ( b == 2 || c == 3 ) ) || ( d == 4 ) || ( e == -1 ) ) )
          Expression partTwo = new Expression( new BinaryCondition<>(Long.class, "accessAreaId", Operator.EQUALS, 2L).setConjunction(Type.AND, new Expression(
                                   new Expression( new BinaryCondition<>(Long.class, "a", Operator.EQUALS, 1L )
                                                             .setConjunction(Type.OR, new Expression(
                                                                    new BinaryCondition<>(Long.class, "b", Operator.EQUALS, 2L).setConjunction(Type.OR, new BinaryCondition<>(Long.class, "c", Operator.EQUALS, 3L)))))
                                                 .setConjunction(Type.OR, new Expression( new BinaryCondition<>(Long.class, "d", Operator.EQUALS, 4L))
                                                 .setConjunction(Type.OR, new Expression( new BinaryCondition<>(Long.class, "e", Operator.EQUALS, -1L )))))));
          // ( accessAreaId == 3 )
          Expression partThree = new Expression( new BinaryCondition<>(Long.class, "accessAreaId", Operator.EQUALS, 3L) );
          
          partOne.setConjunction(Type.OR, partTwo);
          partTwo.setConjunction(Type.OR, partThree);
          return partOne;
      }
      
      public static List<Connectable> createListOfManualExpression(){
          return Arrays.asList(
                      createSimpleObject(),
                      createSimpleAndConjunction(),
                      createSimpleOrConjunction(),
                      createSimpleLikeExpression(),
                      createSimpleNotLikeExpression(),
                      createGroupExpression(),
                      createConditionNegation(),
                      createExpressionNegation(),
                      createGroupExpressionNegation(),
                      createComplexExpression(),
                      createComplexExpression2(),
                      createNestedGroupsExpression(),
                      createNestedGroupsWithBetweenAndDateExpression(),
                      createComplexExpressionWithUnary(),
                      createSubExpressions()
                  );
      }
      
      // --------------------------------------------------------------------------------------------------------------------------------------------------------
      // -------------- cycle expression test objects -----------------------------------------------------------------------------------------------------------
      // --------------------------------------------------------------------------------------------------------------------------------------------------------
      
      public static Connectable createSimpleCycle(){ 
          BinaryCondition<String> cycleCondition = new BinaryCondition<>(String.class, "CycleMe", Operator.EQUALS, "Oh");
          
          return new Expression(cycleCondition.setConjunction(Type.AND, cycleCondition));
      }
              
      public static Connectable createComplexCycle(){
          BinaryCondition<String> cycleCondition = new BinaryCondition<>(String.class, "CycleMe", Operator.EQUALS, "Oh");
          
          return new Expression(
              new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                  .setConjunction(Type.AND, new Expression(
                          new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                          .setConjunction(Type.AND, cycleCondition
                          .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                          .setConjunction(Type.AND, new Expression(
                                                          new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                          .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                    ).setNegate(true)
                          .setConjunction(Type.OR, new Expression(
                                                      new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                      .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99).setConjunction(Type.OR, cycleCondition))
                                                   )
                          )))))
                  .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
          );
      }
      
      public static Connectable createLookingLikeACycle(){
          BinaryCondition<String> cycleCondition = new BinaryCondition<>(String.class, "CycleMe", Operator.EQUALS, "Oh");
          
          return new Expression(
              new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                  .setConjunction(Type.AND, new Expression(
                          new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                          .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                          .setConjunction(Type.AND, new Expression(
                                                          new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                          .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true).setConjunction(Type.AND, cycleCondition))
                                                    ).setNegate(true)
                          .setConjunction(Type.OR, new Expression(
                                                      new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                      .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99).setConjunction(Type.OR, cycleCondition))
                                                   )
                          ))))
                  .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
          );
      }
      
      public static List<Connectable> createListOfCyclicExpression(){
          return Arrays.asList(
                  createSimpleCycle(),
                  createComplexCycle()
// THIS ONLY LOOKS LIKE ONE:    ,createLookingLikeACycle() // this one only looks like one but isn't one!! 
                  );
      }
      
      
      // --------------------------------------------------------------------------------------------------------------------------------------------------------
      // -------------- invalid expression test objects ---------------------------------------------------------------------------------------------------------
      // --------------------------------------------------------------------------------------------------------------------------------------------------------
      
      public static Connectable createInvalidSimpleEmptyExpression(){
          return new Expression();  // empty expression
      }
      
      public static Connectable createInvalidNestedEmptyExpression(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true).setConjunction(Type.AND, new Expression())) // empty expression
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99)
                                                       )
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
           );
      }
      
      public static Connectable createInvalidNestedExpressionEmptyAndInvalidOne(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true).setConjunction(Type.AND, new Expression()))  // empty expression
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(null, new Conjunction( Type.AND,  // invalid expression
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99))
                                                       ))
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
           );
      }
      
      public static Connectable createInvalidNestedExpressionInvalidOne(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(null, new Conjunction( Type.AND,  // invalid expression
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99))
                                                       ))
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
           );
      }
      
      public static Connectable createInvalidSimpleExpression(){
          return new Expression( null, new Conjunction(Type.AND, new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test") ) );
      }
      
      public static Connectable createInvalidCondtionNoValue(){
          return new Expression(
              new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                  .setConjunction(Type.AND, new Expression(
                          new BinaryCondition<>(String.class, "viewID", Operator.EQUALS)  // no value
                          .setConjunction(Type.AND, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                          .setConjunction(Type.AND, new Expression(
                                                          new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN, 12l, 1l, 9l)
                                                          .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                    ).setNegate(true)
                          .setConjunction(Type.OR, new Expression(
                                                      new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                      .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99)
                                                   )
                          )))))
                  .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS)))  // no value
          );
      }
      
      public static Connectable createInvalidCondtionNoValues(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "bla")
                              .setConjunction(Type.AND, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN) // no values
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN)  // no values
                                                       )
                              )))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
              );
      }
      
      
      public static Connectable createInvalidCondtionNoOperator(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", null, "bla")  // no operator
                              .setConjunction(Type.AND, new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Blub")
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN) // no value
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", null, TEST_DATE)  // no operator
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN) // no value
                                                       )
                              )))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
              );
      }
      
      public static Connectable createInvalidCondtionNoAttributenameAndOrOperator(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, null, null, "bla")  // no name, no operator
                              .setConjunction(Type.AND, new BinaryCondition<>(String.class, "", Operator.EQUALS, "Blub")  // empty == no name
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "ownedByAccessAreaID", Operator.IN)  // no value
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new Expression(
                                                          new BinaryCondition<>(Date.class, "lastModifiedDate", null, TEST_DATE)  // no operator
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN)  // no value
                                                       )
                              )))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
              );
      }
      
      public static Connectable createInvalidConditionNoAttributename(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new BinaryCondition<>(String.class, null, Operator.EQUALS, "Blub") // no name
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "", Operator.IN, 12l, 1l, 9l)  // empty == no name
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99)
                                                       )
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
           );
      }
      
      public static Connectable createInvalidConditionWrongValueAmount(){
          return new Expression(
                  new BinaryCondition<>(String.class, "name", Operator.EQUALS, "Test")
                      .setConjunction(Type.AND, new Expression(
                              new BinaryCondition<>(String.class, "viewID", Operator.EQUALS, "Bla")
                              .setConjunction(Type.OR, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4, 99, 77)  // wrong amount
                              .setConjunction(Type.AND, new Expression(
                                                              new PolyadicCondition<>(long.class, "foo", Operator.IN, 12l, 1l, 9l)
                                                              .setConjunction(Type.AND, new BinaryCondition<>(boolean.class, "valid", Operator.EQUALS, true))
                                                        ).setNegate(true)
                              .setConjunction(Type.OR, new BinaryCondition<>(Date.class, "lastModifiedDate", Operator.GREATER_THAN_OR_EQUALS, TEST_DATE)
                                                          .setConjunction(Type.AND, new PolyadicCondition<>(int.class, "viewCount", Operator.BETWEEN, 4) // wrong amount
                                                       )
                              ))))
                      .setConjunction(Type.OR, new BinaryCondition<>(int.class, "ownedByUserID", Operator.EQUALS, 42)))
           );
      }
      
      public static List<Connectable> createListOfInvalidExpression(){
          return Arrays.asList(
                      createInvalidSimpleEmptyExpression(),
                      createInvalidNestedEmptyExpression(),
                      createInvalidNestedExpressionEmptyAndInvalidOne(),
                      createInvalidNestedExpressionInvalidOne(),
                      createInvalidSimpleExpression(),
                      createInvalidCondtionNoValue(),
                      createInvalidCondtionNoValues(),
                      createInvalidCondtionNoOperator(),
                      createInvalidCondtionNoAttributenameAndOrOperator(),
                      createInvalidConditionNoAttributename(),
                      createInvalidConditionWrongValueAmount()
                  );
      }
      
      public static void assertEqual(Connectable createdByBuilder, Connectable createdTraditional){
          assertEqual("Object created by Builder isn't equal to normally created object", createdByBuilder, createdTraditional);
      }
      
      public static void assertEqual(String msg, Connectable createdByBuilder, Connectable createdTraditional){
          Assert.assertTrue("EqualUtil:::::::: " + msg, EqualUtil.areEqual(createdByBuilder, createdTraditional ) );
          Assert.assertTrue("EqulityHelper:::: " + msg, ExpressionUtil.equal(createdByBuilder, createdTraditional ) );          
      }
      
      public static boolean equal(Connectable createdByBuilder, Connectable createdTraditional){
          boolean resultLegacy = EqualUtil.areEqual(createdByBuilder, createdTraditional );
          boolean resultVisitor = ExpressionUtil.equal(createdByBuilder, createdTraditional ); 
          
          if( resultLegacy != resultVisitor ){
              Assert.fail("EqualUtil and EqulityHelper returned different results!");
          }
          
          return resultVisitor;
      }
}
