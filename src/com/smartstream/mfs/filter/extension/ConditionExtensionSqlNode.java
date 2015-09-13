package com.smartstream.mfs.filter.extension;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;

import com.smartstream.conditions.mapped.ExpressionMapper;
import com.smartstream.conditions.mapped.MappedExpression;
import com.smartstream.conditions.util.ExpressionUtil;
import com.viren.conditions.Connectable;
import com.viren.conditions.Operator;
import com.viren.conditions.Visitor;
import com.viren.conditions.Conjunction.Type;

/**
 * Specialized {@link SqlNode} which can handle the new Expression/Condition API.
 * 
 * @author brandstetter
 */
public final class ConditionExtensionSqlNode implements SqlNode {

    /** Default name for the parameter which contains the expression or condition. */
    public static final String DEFAULT_PARAMETER_NAME = "expression";
    
    /** Parameter name which will be used to lookup the {@link DynamicContext} for an expression or condition. */
    private final String parameterName;

    private final String precedingOperator;

    private final boolean isMandatory;
    
    /**
     * Create a new instance of this class in which the default parameter name (={@value #DEFAULT_PARAMETER_NAME}) will be used.
     */
    public ConditionExtensionSqlNode(){
        this(null, null);
    }
    
    /**
     * Create a new instance of this class.
     * @param parameterName the parameter name which will be used to lookup the {@link DynamicContext} for an expression or condition (if null or "" is specified the default (={@value #DEFAULT_PARAMETER_NAME}) one will be used)
     */
    public ConditionExtensionSqlNode(String parameterName, String precedingOperator){
        if ((parameterName == null) || parameterName.isEmpty()) {
            this.parameterName = DEFAULT_PARAMETER_NAME;
            this.isMandatory = false;
        } else if (parameterName.endsWith("!")) {
            this.parameterName = parameterName.substring(0, parameterName.length() -1); 
            this.isMandatory = true;
        } else if (parameterName.endsWith("?")) {
            this.parameterName = parameterName.substring(0, parameterName.length() -1); 
            this.isMandatory = false;
        } else {
            this.parameterName = parameterName;
            this.isMandatory = false;
        }
        this.precedingOperator = precedingOperator;
   }

    /**
     * Checks if an expression or condition parameter is available in the given context and if so it will build the SQL statement for it.
     * <p>
     * Additionally it will also create the correct bindings for the values so prepared statements can be used.
     * </p>
     * 
     * <p>
     * {@inheritDoc}
     * </p>
     * 
     * @return true if an expression or condition was processed, false otherwise 
     * @throws IllegalArgumentException if the found expression or condition isn't valid (see: {@link ExpressionUtil#validate(Connectable)})
     */
    @Override
    public boolean apply(DynamicContext context) {
        MappedExpression me = extractParameter(context);
        
        if( me != null ) {
            Connectable exprOrCondition = me.getExpression();

            // validate the specified parameter (cycle check + correctly specified conditions)
            ExpressionUtil.validate(exprOrCondition);

            if ((precedingOperator != null && !precedingOperator.isEmpty())) {
                context.appendSql(" " + precedingOperator + " ");
            }

            exprOrCondition.visit(new MyBatisSQLVisitor(context, me.getMapper(), parameterName));
            return true;
        }
        
        return false;
    }
    
    /**
     * Helper method to extract the expression or condition from the given {@link DynamicContext}.
     * @param context the context to lookup for an expression or condition
     * @return the found expression or condition; null if there was no value specified
     * @throws ClassCastException if the lookup returned a value which isn't either an expression or condition
     */
    private MappedExpression extractParameter( DynamicContext context ){
        // normally mybatis puts all caller-provided parameters into a nested "_parameter" object.
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) context.getBindings().get("_parameter");

        // Actually *remove* the parameter if it is there, so the caller can verify the param has been used.
        Object me = ( parameters != null ) ? parameters.remove(parameterName) : null;
        
        if( me == null ){
            // check direct reference in context
            me = context.getBindings().remove(parameterName);
        }

        if (me == null && isMandatory) {
            throw new IllegalStateException(
                    String.format("Missing mandatory parameter [%s]", parameterName));
        }

        return (MappedExpression) me;
    }
    
    @Override
    public String toString() {
        return ">>" + parameterName + "<<";
    }

    /**
     * A specialized {@link Visitor} implementation which will generate the SQL and bind the values for the prepared statement.
     * 
     * @author brandstetter
     */
    private static final class MyBatisSQLVisitor implements Visitor {
        
        /** The context for building the SQL and binding the values. */
        private final DynamicContext dynamicSQLContext;

        /** Translates from Java property names to SQL column names. */
        private final ExpressionMapper mapper;

        /** The base for creating the binding names of the values. */
        private final String parameterName;
        
        /** A counter to create different binding names for the values. */
        private int valueID = 0;
        
        /**
         * Create an instance of this visitor.
         * 
         * @param dynamicContext context for building the SQL and binding the values
         * @param parameterName the base for creating the binding names of the values
         * @throws NullPointerException if either the dynamicContext or the parameterName is null
         */
        public MyBatisSQLVisitor(DynamicContext dynamicContext, ExpressionMapper mapper, String parameterName){
            this.dynamicSQLContext = Objects.requireNonNull(dynamicContext, "No DynamicContext given!");
            this.mapper = Objects.requireNonNull(mapper, "No mapper given");
            this.parameterName = Objects.requireNonNull(parameterName, "No parameter name given!");
        }

        /**
         * {@inheritDoc}
         * 
         * <p>
         * This will open an expression with a " (". Depending on the given negate parameter
         * it will also prepend a " not" in fornt of the " (".
         * </p>
         */
        @Override
        public void startExpression(boolean negate) {
            appendNegate(negate);
            
            dynamicSQLContext.appendSql(" (");
        }

        /**
         * {@inheritDoc}
         * <p>
         * This will close the expression with a " )".
         * </p>
         */
        @Override
        public void endExpression() {
            dynamicSQLContext.appendSql(" )");
        }

        /**
         * {@inheritDoc}
         * 
         * <p>
         * Depending on the given conjunction type it will append either " and" or " or" to
         * the SQL.
         * </p>
         * 
         * @throws IllegalStateException if an unknown conjuction type was given
         */
        @Override
        public void conjunct(Type conjunctionType) {
            switch (conjunctionType) {
                case AND:
                    dynamicSQLContext.appendSql(" and");
                    break;
                    
                case OR:
                    dynamicSQLContext.appendSql(" or");
                    break;

                default:
                    throw new IllegalStateException("Unkown conjunction type: " + conjunctionType);
            }
        }

        /**
         * {@inheritDoc}
         * 
         * <p>
         * Appends everything required (=negation, attribute name and operator) for an unary condition to the SQL.
         * </p>
         */
        @Override
        public void visitUnaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator) {
            appendConditionBasic(negate, attributeName, operator);
        }

        /**
         * {@inheritDoc}
         * 
         * <p>
         * Appends everything required (=negation, attribute name, operator and value) for a binary condition to the SQL.
         * The value will also be bound to the context so it can be used as a prepared statement value afterwards. 
         * </p>
         */
        @Override
        public void visitBinaryCondition(boolean negate, String attributeName, Class<?> type, Operator operator, Object value) {
            appendConditionBasic(negate, attributeName, operator);
            
            String bindingName = createNewBindingName();
            
            dynamicSQLContext.bind(bindingName, value);
            dynamicSQLContext.appendSql(" #{" + bindingName + ",javaType=" + type.getName() + "}");
        }

        /**
         * {@inheritDoc}
         * 
         * <p>
         * Appends everything required (=negation, attribute name, operator and values) for a polyadic condition to the SQL.
         * The value list will also be bound to the context so they can be used as prepared statement values afterwards. 
         * </p>
         * 
         * <p>
         * The way the values are mentioned in the SQL depends on the operator. If the operator is {@link Operator#BETWEEN}
         * than
         *  <pre>#{bindingName[0]} and #{bindingName[1]}</pre>
         * will be added to the SQL otherwise
         *  <pre>(#{bindingName[0]}, #{bindingName[1]}, #{bindingName[2]}, ...)</pre>
         * will be added.  
         * </p>
         */
        @Override
        public void visitPolyadicCondition(boolean negate, String attributeName, Class<?> type, Operator operator, List<?> values) {
            appendConditionBasic(negate, attributeName, operator);
            
            String bindingName = createNewBindingName();
            dynamicSQLContext.bind(bindingName, values); // bind the entire list to this name
            
            // now we append the values to the sql
            if( operator == Operator.BETWEEN ){
                dynamicSQLContext.appendSql(" #{" + bindingName + "[0],javaType=" + type.getName() + "} and #{" + bindingName + "[1],javaType=" + type.getName() + "}"); // there should be exactly two values, this will be checked by the validate() method inside the apply(DynamicContext) method
            } else {
                StringBuilder valuesList = new StringBuilder(" (");
                
                for( int i = 0, valueCount = values.size(); i < valueCount; i++ ){
                    valuesList.append(" #{").append(bindingName).append('[').append(i).append("],javaType=").append(type.getName()).append("},");
                }
                
                if( valuesList.length() > 2 ){ // if something was added after the first " ("
                    valuesList.setLength(valuesList.length() - 1); // -1 because of the last ',' at the end
                }
                
                valuesList.append( " )" );
                dynamicSQLContext.appendSql(valuesList.toString());
            }
        }
        
        /**
         * Helper method to create a new binding name for a parameter value.
         * @return a newly created parameter name
         */
        private String createNewBindingName(){
            return parameterName + '_' + valueID++;
        }
        
        /**
         * Appends a " not" to the SQL if the given parameter is true.
         * @param negate flag which marks if the condition or expression should be negated
         */
        private void appendNegate(boolean negate){
            if( negate ){
                dynamicSQLContext.appendSql(" not");
            }
        }
        
        /**
         * Appends the basics for any condition to the SQL.
         * 
         * <p>
         * This method will also translate the given operator to the equivalent DB operator.
         * </p>
         * 
         * @param negate flag which marks if the condition should be negated
         * @param attributeName the DB column name
         * @param operator the operator to append
         * @throws IllegalStateException if the operator is unknown
         */
        private void appendConditionBasic(boolean negate, String attributeName, Operator operator){
            appendNegate(negate);
            dynamicSQLContext.appendSql(' ' + mapper.map(attributeName) );
            
            switch( operator ){
                // --- unary operator ---
                case IS_NOT_NULL:
                    dynamicSQLContext.appendSql( " is not null" );
                    break;
                    
                case IS_NULL:
                    dynamicSQLContext.appendSql( " is null" );
                    break;
                    
                 // --- binary operator ---
                case EQUALS:
                    dynamicSQLContext.appendSql( " =" );
                    break;
                    
                case NOT_EQUALS:
                    dynamicSQLContext.appendSql( " <>" );
                    break;
                    
                case GREATER_THAN:
                    dynamicSQLContext.appendSql( " >" );
                    break;
                    
                case GREATER_THAN_OR_EQUALS:
                    dynamicSQLContext.appendSql( " >=" );
                    break;
                    
                case LESS_THAN:
                    dynamicSQLContext.appendSql( " <" );
                    break;
                    
                case LESS_THAN_OR_EQUALS:
                    dynamicSQLContext.appendSql( " <=" );
                    break;
                    
                case LIKE:
                    dynamicSQLContext.appendSql( " like" );
                    break;
                    
                case NOT_LIKE:
                    dynamicSQLContext.appendSql( " not like" );
                    break;
                    
                // --- ternary operator ---
                case BETWEEN:
                    dynamicSQLContext.appendSql( " between" ); // "and" + values are in the visitPolyadic method! 
                    break;
                    
                // --- polyadic operator ---
                case IN:
                    dynamicSQLContext.appendSql( " in" );
                    break;
                    
                case NOT_IN:
                    dynamicSQLContext.appendSql( " not in" );
                    break;
                    
                default:
                    throw new IllegalStateException("Unkown operator: " + operator);
            }
        }
    }
}
