package com.smartstream.conditions.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility method the check datatypes of values.
 * 
 * @author brandstetter
 */
public abstract class DataTypeUtil {
    
    private DataTypeUtil(){
        throw new IllegalArgumentException("Instantiation not allowed!");
    }
    
    /**
     * Simple helper enumeration to have mapping of basic primitives and their corresponding wrapper objects.
     * 
     * @author brandstetter
     */
    private static enum PrimitiveType {
        /** Maps boolean to Boolean. */
        BOOLEAN(boolean.class, Boolean.class),
        /** Maps byte to Byte. */
        BYTE(byte.class, Byte.class),
        /** Maps short to Short. */
        SHORT(short.class, Short.class),
        /** Maps int to Integer. */
        INT(int.class, Integer.class),
        /** Maps long to Long. */
        LONG(long.class, Long.class),
        /** Maps char to Character. */
        CHAR(char.class, Character.class),
        /** Maps float to Float. */
        FLOAT(float.class, Float.class),
        /** Maps double to Double. */
        DOUBLE(double.class, Double.class);
        
        private final Class<?> primitiveType, wrapperType;
        
        /**
         * Create an enum entry.
         * @param primitiveType the primitive datatype
         * @param wrapperType the corresponding wrapper class
         * @throws IllegalArgumentException if the primitiveType isn't primitive or the wrapperType is primitive too
         */
        private PrimitiveType(Class<?> primitiveType, Class<?> wrapperType){
            this.primitiveType = Objects.requireNonNull(primitiveType);
            this.wrapperType = Objects.requireNonNull(wrapperType);
            
            if( !primitiveType.isPrimitive() ){
                throw new IllegalArgumentException(primitiveType + " is not a primitive type!");
            }
            
            if( wrapperType.isPrimitive() ){
                throw new IllegalArgumentException(wrapperType + " is a primitive type should be a wrapper!");
            }
        }
        
        /**
         * Searches the enumeration for a wrapper class of the given type. 
         * @param pType the primitive data type
         * @return the corresponding wrapper class or null if it can't find one.
         */
        public static Class<?> findWrapper(Class<?> pType){
            for( PrimitiveType type : PrimitiveType.values() ){
                if( type.primitiveType.equals(pType) ){
                    return type.wrapperType;
                }
            }
            
            return null;
        }
    }
    
    /**
     * A simple type-safe list implementation which is backed by an ArrayList.
     * 
     * <p>
     * Every insert into the list will check if the given value is an instance of the specified type. The
     * type-check is done via {@link DataTypeUtil#isInstanceOf(Class, Object)}. Every insertion of an
     * invalid data-type will result in an {@link IllegalArgumentException}.
     * </p> 
     * 
     * @author brandstetter
     *
     * @param <T>
     */
    public static class TypeSafeList<T> extends AbstractList<T>{
        
        /** The data type every value must have. */
        private final Class<T> type;
        
        /** List implementation which will hold the data. */
        private final List<T> data;
        
        /**
         * Creates an instance of this type-safe list.
         * @throws NullPointerException if the given type is null
         */
        public TypeSafeList(Class<T> type){
            this.type = Objects.requireNonNull(type, "No type given!");
            this.data = new ArrayList<>();
        }

        @Override
        public T get(int index) {
            return data.get(index);
        }

        @Override
        public int size() {
            return data.size();
        }
        
        // -- override for modifiable lists --
        /**
         * <p>
         * This implementation will check if the of the element is assignment compatible and if so it will be added.
         * </p>
         * 
         * @throws IllegalArgumentException if the given element is not assignment compatible with the specified type
         */
        @Override
        public T set(int index, T element) {
            return data.set(index, checkType(element));
        }
        
        
        /**
         * <p>
         * This implementation will check if the of the element is assignment compatible and if so it will be added.
         * </p>
         * 
         * @throws IllegalArgumentException if the given element is not assignment compatible with the specified type
         */
        @Override
        public void add(int index, T element) {
            data.add(index, checkType(element));
        }
        
        @Override
        public T remove(int index) {
            return data.remove(index);
        }

        /**
         * Checks if the given element is assignment compatible.
         * @param element the element to check
         * @return {@code element} if not {@code null}
         * @throws IllegalArgumentException if the given element is not assignment compatible with the specified type
         */
        private T checkType(T element){
            if( element != null && !DataTypeUtil.isInstanceOf(type, element) ){
                throw new IllegalArgumentException("The given value '" + element + "' is not an instance of " + type);
            }
            
            return element;
        }
        
    }
    
    /**
     * Utility method for checking the {@code instanceof} the type with the object.
     * This method also handles the primitive datatypes and their wrapper objects,
     * which is <b>NOT</b> done inside {@link Class#isInstance(Object)}.
     * @param type the expected type
     * @param value the value to check
     * @return true if the given object is an instance of the given type; false otherwise
     */
    public static boolean isInstanceOf(Class<?> type, Object value){
        if( type.isPrimitive() ){ // is a primitive - wrapper check required?
            Class<?> wrapperCls = PrimitiveType.findWrapper(type);
            if( wrapperCls != null ){
                return wrapperCls.isInstance(value);
            }
        }
        
        return type.isInstance(value);
    }
}
