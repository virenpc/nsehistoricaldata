package com.smartstream.mfs.filter.driver;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Utility class for replacing the placeholders inside of the SqlNodes. 
 * 
 * @author brandstetter
 */
final class ExtensionUtil {
	private static final int CACHE_SIZE_LIMIT = 100;

	/**
	 * The map should have topmost a few entries. Limiting the size is only to be sure.
	 */
	private final static LoadingCache<FirstInsecureCacheKey,Optional<Field>> FIRST_INSECURE_CACHE = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE_LIMIT).build( new CacheLoader<FirstInsecureCacheKey,Optional<Field>>() {
		@Override
		public Optional<Field> load(FirstInsecureCacheKey key) throws Exception { //NOPMD: throws exception, since the interface has this
	        for( Field f : key.getToSearch().getDeclaredFields()){
	            if(f.getType().equals(key.getFieldType())){
	                return Optional.of(f);
	            }
	        }
			return Optional.absent();
		}
	});
    
    /**
     * Sole constructor.
     * @throws IllegalStateException because this class shouldn't be instantiated
     */
    private ExtensionUtil(){
        throw new IllegalStateException("Instantiation not allowed!");
    }
    
    /** The constant value which marks the begin of a placeholder. */
    static final String PLACEHOLDER_BEGIN = "##__SmartStream__";
    
    /** The constant value which marks the end of a placeholder. */
    static final String PLACEHOLDER_END = "__##";
    
    /** The length of the {@link #PLACEHOLDER_BEGIN} to skip the method calls on the constant string. */
    static final int PLACEHOLDER_BEGIN_OFFSET = PLACEHOLDER_BEGIN.length();
    
    /** The length of the {@link #PLACEHOLDER_END} to skip the method calls on the constant string. */
    static final int PLACEHOLDER_END_OFFSET = PLACEHOLDER_END.length();
    
    /** Field containing the rootSqlNode inside a {@link DynamicSqlSource}, which contains the placeholder. */
    static final Field DYNAMIC_SQL_SOURCE_ROOT_SQL_NODE = findFirst(DynamicSqlSource.class, SqlNode.class);
    
    /**
     * Find the first field which has the appropriate type.
     * @param toSearch the class to search through
     * @param fieldType the type of the field this method is looking for
     * @return the first field of the given class which has the appropriate type
     * @throws IllegalStateException if the given class doesn't contain a field with the appropriate type
     * @throws NullPointerException if the given class is {@code null}
     */
    static Field findFirst(Class<?> toSearch, Class<?> fieldType){
        Field back = findFirstInsecure(toSearch, fieldType);
        if( back == null ) throw new IllegalStateException("can't find a field of type: " + fieldType + " in class " + toSearch);
        return back;
    }
    
    /**
     * Model object to be key in the cache for the {@link ExtensionUtil#findFirstInsecure(Class, Class)} method.
     * @author liptak
     *
     */
    private static final class FirstInsecureCacheKey {
    	private Class<?> toSearch;
    	private Class<?> fieldType;
    	
		public FirstInsecureCacheKey(Class<?> toSearch, Class<?> fieldType) {
			super();
			this.toSearch = toSearch;
			this.fieldType = fieldType;
		}
		
		public Class<?> getToSearch() {
			return toSearch;
		}
		public Class<?> getFieldType() {
			return fieldType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fieldType == null) ? 0 : fieldType.hashCode());
			result = prime * result
					+ ((toSearch == null) ? 0 : toSearch.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FirstInsecureCacheKey other = (FirstInsecureCacheKey) obj;
			if (fieldType == null) {
				if (other.fieldType != null)
					return false;
			} else if (!fieldType.equals(other.fieldType))
				return false;
			if (toSearch == null) {
				if (other.toSearch != null)
					return false;
			} else if (!toSearch.equals(other.toSearch))
				return false;
			return true;
		}
    }
    
    /**
     * Find the first field which has the appropriate type. Profiling has shown, that this method is called frequently, so the result is cached to avoid felix classloader security checks.
     * @param toSearch the class to search through
     * @param fieldType the type of the field this method is looking for
     * @return the first field of the given class which has the appropriate type, or {@code null} if it doesn't find one
     * @throws RuntimeException if the execution of the cache item initialization was not successful and ExecutionException was thrown
     */
    static Field findFirstInsecure(Class<?> toSearch, Class<?> fieldType){
        try {
			return FIRST_INSECURE_CACHE.get(new FirstInsecureCacheKey(toSearch, fieldType)).orNull();
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause()); //NOPMD: The cause cannot be null
		}        
    }
    
    /**
     * Helper method to set the given value to a field in the given object (node).
     * <p>
     * This method will to the action in a privileged call and also wraps all caught-exception into an {@link IllegalStateException}.
     * </p>
     * 
     * @param parentNode the object on which to set the value
     * @param field the field inside the object which should be set
     * @param value the value to set
     * @throws IllegalStateException if the field can't be set
     * @throws NullPointerException if either the parentNode or the field is null
     */
    static void setFieldValue(Object parentNode, Field field, SqlNode value){
        try {
            AccessController.doPrivileged(new SetFieldAction(field, parentNode, value));  // parentNode can be of type DynamicSqlSource or SqlNode
        } catch (PrivilegedActionException e) {
            throw new IllegalStateException("Can't set field '" + field + "' of parent node '" + parentNode + "'!", e.getCause());
        }
    }
    
    /**
     * Helper method to get the value of a field from a given {@link SqlNode}. 
     * <p>
     * This method will to the action in a privileged call and also wraps all caught-exception into an {@link IllegalStateException}.
     * </p>
     * 
     * @param sqlNode the node from which the value should be read
     * @param field the field which should be read
     * @return the read value
     * @throws IllegalStateException if the field can't be read
     * @throws NullPointerException if either the sqlNode or the field is null
     */
    static <T> T getFieldValue(SqlNode sqlNode, Field field){
        try {
            return (T) AccessController.doPrivileged(new GetFieldAction<T>(field, sqlNode));
        } catch (PrivilegedActionException e) {
            throw new IllegalStateException("Can't get field '" + field + "' of SqlNode '" + sqlNode + "'!", e.getCause());
        }
    }
    
    /**
     * Helper method to get the root {@link SqlNode} from the given {@link DynamicSqlSource}. 
     * <p>
     * This method will to the action in a privileged call and also wraps all caught-exception into an {@link IllegalStateException}.
     * </p>
     * 
     * @param sqlSource the {@link DynamicSqlSource} from which the root node should be extracted
     * @return the root {@link SqlNode} contained inside the given {@link DynamicSqlSource}
     * @throws IllegalStateException if the root {@link SqlNode} can't be read
     * @throws NullPointerException if given sqlSource is null
     */
    static SqlNode getRootSqlNodeOfDynamicSqlSource(DynamicSqlSource sqlSource){
        try {
            return AccessController.doPrivileged(new GetRootSqlNodeOfDynamicSqlSource(sqlSource));
        } catch (PrivilegedActionException e) {
            throw new IllegalStateException("Can't get rootSqlNode of DynamicSqlSource!", e.getCause());
        }
    }
    
    /**
     * Helper method to get the text from a given {@link TextSqlNode}. 
     * <p>
     * This method will to the action in a privileged call and also wraps all caught-exception into an {@link IllegalStateException}.
     * </p>
     * 
     * @param textSqlNode the {@link TextSqlNode} from which the text should be extracted
     * @return text of the given {@link TextSqlNode}
     * @throws IllegalStateException if the text can't be read
     * @throws NullPointerException if the given textSqlNode is null
     */
    static String getTextOfTextSqlNode(TextSqlNode textSqlNode){
        try {
            return AccessController.doPrivileged(new GetTextOfTextSqlNode(textSqlNode));
        } catch (PrivilegedActionException e) {
            throw new IllegalStateException("Can't get text of TextSqlNode!", e.getCause());
        }
    }
    
    /**
     * Generic privileged action which reads a field from an object.
     * <p>
     * This action will also be able to access private fields if the permissions are set.
     * </p>
     * 
     * @author brandstetter
     *
     * @param <T> the type of the returned object
     */
    private static class GetFieldAction<T> implements PrivilegedExceptionAction<T>{
        
        /** The field to read. */
        private final Field field;
        
        /** The object from which the field should be read. */
        private final Object object;
        
        /**
         * Creates an instance of this class
         * @param field the field to read
         * @param object the object from which to read the field
         * @throws NullPointerException if either the field or the object is null
         */
        public GetFieldAction(Field field, Object object){
            this.field = Objects.requireNonNull(field, "No field to read given!");
            this.object = Objects.requireNonNull(object, "No object given for extracting the field value!");
        }

        @SuppressWarnings("unchecked")
        @Override
        public T run() throws IllegalArgumentException, IllegalAccessException {
        	synchronized (field) {
	            boolean accessible = field.isAccessible();
	            try{
	                if( !accessible ){
	                    field.setAccessible(true); // make accessible if required
	                }
	                return (T) field.get(object);
	            } finally {
	                if( !accessible ){
	                    field.setAccessible(accessible); // restore the accessibility if it was changed
	                }
	            }
        	}
        }
    }
    
    /**
     * Action for getting the root {@link SqlNode} out of the {@link DynamicSqlSource}.
     * 
     * @author brandstetter
     */
    private static class GetRootSqlNodeOfDynamicSqlSource extends GetFieldAction<SqlNode> {
        
        public GetRootSqlNodeOfDynamicSqlSource(DynamicSqlSource dynamicSqlSoure) {
            super(DYNAMIC_SQL_SOURCE_ROOT_SQL_NODE, dynamicSqlSoure);
        }
        
    }
    
    /**
     * Action for getting the text out of a {@link TextSqlNode}.
     * 
     * @author brandstetter
     */
    private static class GetTextOfTextSqlNode extends GetFieldAction<String> {
        
        /** Field containing the text of a TextSqlNode which probably holds the placeholder token. (The TextSqlNode is an End-Node.) */
        private static final Field TEXT_OF_TEXT_SQL_NODE = findFirst(TextSqlNode.class, String.class);

        public GetTextOfTextSqlNode(TextSqlNode textSqlNode) {
            super(TEXT_OF_TEXT_SQL_NODE, textSqlNode);
        }
        
    }
    
    /**
     * Generic privileged action which sets a field of an object.
     * <p>
     * This action will also be able to access private fields if the permissions are set.
     * </p>
     * 
     * @author brandstetter
     */
    private static class SetFieldAction implements PrivilegedExceptionAction<Void>{
        /** The field to set. */
        private final Field field;
        
        /** The object to set the value to. */
        private final Object object;
        
        /** The value to set. */
        private final Object value;
        
        /**
         * Creates an instance of this class
         * @param field the field to set
         * @param object the object to set the value to
         * @param value the value to set
         * @throws NullPointerException if either the field or the object to set the value to is null
         */
        public SetFieldAction(Field field, Object object, Object value){
            this.field = Objects.requireNonNull(field, "No field to read given!");
            this.object = Objects.requireNonNull(object, "No object given for extracting the field value!");
            this.value = value;
        }

        @Override
        public Void run() throws IllegalArgumentException, IllegalAccessException {
        	synchronized (field) {				
        		boolean accessible = field.isAccessible();
        		try{
        			if( !accessible ){
        				field.setAccessible(true); // make accessible if required
        			}
        			field.set(object, value);
        		} finally {
        			if( !accessible ){
        				field.setAccessible(accessible); // restore the accessibility if it was changed
        			}
        		}
        		return null;
			}
        }
    }
}
