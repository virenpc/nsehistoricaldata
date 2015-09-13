package com.viren.conditions;

import java.util.List;
import java.util.Objects;

/**
 * Enumeration of all supported operators and their arity.
 * 
 * @author brandstetter
 */
public enum Operator{
	
    /** The greater than operator (>) which has the {@link Arity#BINARY}. */
    GREATER_THAN,

    /** The greater than or equal operator (>=) which has the {@link Arity#BINARY}. */
    GREATER_THAN_OR_EQUALS,

    /** The less than operator (<) which has the {@link Arity#BINARY}. */
    LESS_THAN,

    /** The less than or equal operator (<=) which has the {@link Arity#BINARY}. */
    LESS_THAN_OR_EQUALS,

    /** The equal operator (=) which has the {@link Arity#BINARY}. */
    EQUALS,
    
    /** The not equal operator (!=) which has the {@link Arity#BINARY}. */
    NOT_EQUALS,

    /** The is null operator which has the {@link Arity#UNARY}. */
    IS_NULL(Arity.UNARY),

    /** The is not null operator which has the {@link Arity#UNARY}. */
    IS_NOT_NULL(Arity.UNARY),

    /** The like operator which has the {@link Arity#BINARY}. */
    LIKE,

    /** The not like operator which has the {@link Arity#BINARY}. */
    NOT_LIKE,

    /** The in operator which has the {@link Arity#POLYADIC}. */
    IN(Arity.POLYADIC),

    /** The not in operator which has the {@link Arity#POLYADIC}. */
    NOT_IN(Arity.POLYADIC),
    
    /** The between operator which has the {@link Arity#TERNARY}. */
    BETWEEN(Arity.TERNARY);

    /** The arity of this operator. */
    private final Arity arity;

    private Operator() {
        this(Arity.BINARY);
    }

    /**
     * Creates an enumeration value with the given arity.
     * @param arity the arity of the operator
     * @throws NullPointerException if the given arity is {@code null}
     */
    private Operator(Arity arity) {
        this.arity = Objects.requireNonNull(arity, "no arity given!");
    }

    /**
     * Return the artiy of this operator.
     * @return the artiy of this operator
     */
    public Arity getArity() {
        return arity;
    }
    
    /**
     * The arity defines the amount of required arguments of an operator.
     * 
     * <p>
     * The amount of required arguments those not include the attriubtename argument.
     * </p>
     * 
     * @author brandstetter
     */
    public static enum Arity {
        /** Operator which only has one argument. */
	    UNARY(0),
	    /** Operator which has two arguments. */
	    BINARY(1),
	    /** Operator which has three arguments. */
	    TERNARY(2),
	    /** Operator which has an infinite amount of arguments. */
	    POLYADIC(1, Arity.INFINITE);
	    
	    public static final int INFINITE = -1;

	    private final int minArgumentCount;
	    private final int maxArgumentCount;
	    
	    /**
	     * Create an arity enumeration entry with an exact amount of additional arguments.
	     * Additional means it doesn't count the first argument because this a required one.
	     * @param argumentCount the exact amount of additional arguments
	     */
	    private Arity(int argumentCount) {
	        if( argumentCount == INFINITE ) throw new IllegalArgumentException("A minimum argument count has to be specified!");
	        if( argumentCount < 0 ) throw new IllegalArgumentException("The minimum argument count can't be less than 0!");
	        
	        this.minArgumentCount = argumentCount;
	        this.maxArgumentCount = argumentCount;
	    }
	    
	    /**
         * Create an arity enumeration entry with has an exact range of additional arguments.
         * Additional means it doesn't count the first argument because this a required one.
         * @param minArgumentCount the minimum amount of additional arguments
         * @param maxArgumentCount the maximum amount of additional arguments (to have an infinite amount of arguments specify {@link #INFINITE})
         * @throws IllegalArgumentException if the minimum amount is less than 0 and if the minimum amount is larger than the maximum
         */
        private Arity(int minArgumentCount, int maxArgumentCount) {
	        if( minArgumentCount < 0 ) throw new IllegalArgumentException("The minimum argument count can't be less than 0!");
	        if( maxArgumentCount != INFINITE && minArgumentCount > maxArgumentCount ) throw new IllegalArgumentException("Mininum argument count can't be larger the the maximum argument count!");
	        
	        this.minArgumentCount = minArgumentCount;
	        this.maxArgumentCount = maxArgumentCount;
	    }

        /**
         * Return the minimal required additional arguments.
         * @return minimal required additional arguments
         */
	    public int getMinArgumentCount() {
	        return minArgumentCount;
	    }
	    
	    /**
         * Return the maximal required additional arguments.
         * @return maximal required additional arguments
         * @see #INFINITE
         */
        public int getMaxArgumentCount() {
            return maxArgumentCount;
        }

        /**
         * Method which checks if the given list of additional arguments meats the arity range condition. 
         * @param additionalArguments the list of additional arguments to check
         * @return true if the amount of additional argument fits the specified artiy range; false otherwise
         */
	    public boolean isArgumentsCountValid(List<?> additionalArguments) {
	        // special treatment of null
	        if( additionalArguments == null ){
	            if( minArgumentCount == 0 ){
	                return true;
	            }
	            return false;
	        }
	        
	        int argumentCount = additionalArguments.size();
	        
	        if( maxArgumentCount == INFINITE ){ // no upper limit
	            return argumentCount >= minArgumentCount;
	        }
	        
	        return (argumentCount >= minArgumentCount) && (argumentCount <= maxArgumentCount);
	    }

	}
}
