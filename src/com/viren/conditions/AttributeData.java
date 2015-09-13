package com.viren.conditions;

/**
 * This interface ties together an attribute name and its class, 
 * this avoids potential errors caused by having to pass them as separate arguments
 * 
 * @author tindall
 *
 * @param <T>
 */
public interface AttributeData<T> {

	String getName();
	
	Class<T> getType();
}
