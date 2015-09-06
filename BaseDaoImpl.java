package com.smartstream.mfs.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.eclipse.emf.ecore.EObject;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartstream.bo.model.ControllableObject;
import com.smartstream.core.database.config.TransactionTracker;
import com.smartstream.mfs.api.exception.DataAccessException;
import com.smartstream.mfs.api.exception.DataLimitExceededException;
import com.smartstream.mfs.api.exception.DataModifiedByAnotherUserException;
import com.smartstream.mfs.api.exception.DataRemovedByAnotherUserException;
import com.smartstream.mfs.api.exception.ServerFrameworkRuntimeException;
import com.smartstream.mfs.api.model.Definable;
import com.smartstream.mfs.dao.audit.Auditor;
import com.smartstream.mfs.dao.audit.DeltaGenerator;
import com.smartstream.sequencer.interfaces.SequenceProvider;
import com.smartstream.sequencer.interfaces.SpecializedSequenceProvider;
import com.smartstream.session.api.ISessionService;

/**
 * Base class for mybatis dao implementations.
 * <p>
 * This class performs auditing of changes when configured with an auditor service. This class also supports
 * "optimistic locking" for persistent objects.
 * </p>
 * <p>
 * This class does NOT perform any access-control checks at all; if a dao is callable by a "user", then the dao
 * class should subclass BasePermissionConstraintsDaoImpl rather than this class.
 * </p>
 * <p>
 * Provides standard mybatis statement naming conventions as follows:
 * <ol>
 *   <li>&lt;TypeName&gt;_query performs all mybatis SQL queries
 *   <li>&lt;TypeName&gt;_insert performs the insert statement for the type in question.
 *   <li>&lt;TypeName&gt;_update performs the update statement for the type in question
 *   <li>&lt;TypeName&gt;_delete performs the update statement for the type in question.
 * </ol>
 * </p>
 */
public abstract class BaseDaoImpl extends SqlSessionDaoSupport {
    private static final String UNCHECKED = "unchecked";

	/** Constant that never matches the key of a valid object. */
    public static final Long NO_ID = Long.valueOf(-1);

    /**
     * Constant that never matches any valid BusinessObjectType.
     *
     * This can be returned by the getMacObjectType() method of a Context object, but in that case the
     * Context.getAccessAreaId(obj) instance should also return NO_ID.
     */
    public static final String NO_OBJECT_TYPE = "NO_SUCH_TYPE";

    //name of the parameter in the map which holds the migration id (UUID)
    private static final String MIGRATION_ID_PARAMETER = "insertUuid";

    // Naming convention for finding mybatis sql definitions
    private static final String SQL_SUFFIX_QUERY = "_query";
    private static final String SQL_SUFFIX_INSERT = "_insert";
    private static final String SQL_SUFFIX_UPDATE = "_update";
    private static final String SQL_SUFFIX_DELETE = "_delete";

    // Constant strings used as keys in the params object passed to mybatis queries
    private static final String PARAM_OBJECT = "object";
    private static final String PARAM_ID = "id";
    private static final String PARAM_IDS = "id";
    private static final String PARAM_OLD_VERSION = "oldVersion";
    private static final String PARAM_NEW_VERSION = "newVersion";
    private static final String PARAM_SESSIONID = "sessionId";
    private static final String PARAM_CURR_USER_ID = "currentUserID";

    // Limit all queries for this dao to never return more than this number of rows.
    // See comments on getDefaultRowBounds
    private static final String QUERY_LIMIT_KEY = "alldbqueries.limit.dflt";
    private static final int QUERY_LIMIT_DFLT = 1000;

    // Constraint that can be passed to the queryObject method to use the dao-wide default row limit.
    public static final int QUERY_LIMIT_USE_DFLT = -1;

    private static final NullComparator SAFE_EQUALS = new NullComparator();
    
    // =======================================================================================

    /** The mybatis namespace in which SQL statements are looked for by default. */
    protected final String namespace;

    private SequenceProvider<String> sequenceProvider;

    /** Service that records persistence operations in the audit trail. */
    private Auditor auditor;

    private ISessionService sessionService;


    // =======================================================================================

    private final Logger log; //NOPMD we want the logger to log at the level of it's base class.

    private final Map<String, SpecializedSequenceProvider<String>> idGeneratorsByTypeName;

    /** Object which sets the default limit on the max number of rows a query can return from the database. */
    private RowBounds rowBounds;

    /** track classes which we should insert with migrationIds. */
    private final Set<Class<?>> migrationIdSupport;

    // =======================================================================================

    /** Constructor. */
    public BaseDaoImpl(String namespace) {
        this.namespace = namespace;
        log = LoggerFactory.getLogger(getClass());
        idGeneratorsByTypeName = new HashMap<String, SpecializedSequenceProvider<String>>();
        migrationIdSupport = new HashSet<Class<?>>();
        rowBounds = getDefaultRowBounds(namespace); // see also setMaxRows
    }

    public void setSequenceProvider(SequenceProvider<String> idGeneratorFactory) {
        this.sequenceProvider = idGeneratorFactory;
    }

    public void setSessionService(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    public ISessionService getSessionService() {
        return sessionService;
    }

    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }

    /** Return the object that handles creation of "audit records" for inserts/updates/deletes. */
    public Auditor getAuditor() {
        return auditor;
    }

    /**
     * Simple utility method that returns a system property holding an integer safely (ie handling/ignoring
     * any exceptions).
     */
    private int getSystemProperty(String key, int dflt) {
        try {
            String val = System.getProperty(key);
            if (val != null) {
                return Integer.valueOf(val);
            }
        } catch(Exception e) {
            // Don't log this at error level, as it may occur repeatedly...really what is wanted here is
            // some kind of "logOnceOnly" operation..
            log.debug(
                String.format(
                    "Unable to access system property [%s] : setting default for queryLimit to %d",
                    key, dflt));
        }
        return dflt;
    }

    /**
     * Set a default maximum number of rows returned by database queries, to avoid bad queries from
     * consuming too much ram or CPU-time.
     * <p>
     * Some tables in the database may have millions of rows in production. If a user (or a system task)
     * performs a query against such a table without an appropriate filter, then bad things can happen:
     * <ul>
     * <li>large amounts of ram may be used by the database server itself;</li>
     * <li>large amounts of ram may be used as mybatis builds the result object list, and as the results
     * are serialized for streaming back to the caller; </li>
     * <li>large amounts of ram may be used in the RAP webserver or the RCP client;</li>
     * <li>database server performance may be impacted;</li>
     * <li>a connection from the database pool may be held for a long time; </li>
     * <li>heavy network traffic may occur; </li>
     * <li>the user's client application may become unresponsive for a long time.</li>
     * </ul>
     * In the worst case, an OutOfMemoryException might occur in servicemix or the RAP server.
     * </p>
     * <p>
     * This method provides a safety limit to prevent the above from happening; if too many rows are matched
     * in the database, then only the first N rows are retrieved from the db. The performQuery method then
     * detects this and (currently) throws an exception. This is bad for the user, but not as bad as the
     * alternatives above. This limit is *not* meant as an alternative for proper support for "paging" through
     * large sets of data in the client; it is just a last-ditch safety measure.
     * </p>
     * <p>
     * The default value should be larger than any "reasonably usable" result set, but small enough to not
     * trigger system problems. Assuming a row converted to the appropriate java object uses 1KB RAM, then
     * a limit of 1000 would mean a max ram usage for a query of 1MB; this seems reasonable considering that
     * the server is expected to be allocated at least several gigabytes of ram in production. Serializing
     * and transferring 1MB of data across the network is also not a large burden (a few seconds). And a
     * table of 1000 objects in the UI is a *very* large set of data to work with. So 1000 seems a reasonable
     * setting for most DAOs. If a dao manages objects that are large (eg contain embedded BLOB fields holding
     * images or similar) then a lower limit might be appropriate for that dao.
     * </p>
     * <p>
     * The default limit is the constant QUERY_LIMIT_DFLT, but this can be overridden via system properties. The
     * limit is set in the following order:
     * <ul>
     * <li>if system property "{mybatis-ns}.{mybatis-stmt-id}.limit.force=N" is set, then that is used; </li>
     * <li>if the calling code specified a limit, then that is used; </li>
     * <li>if system property "{mybatis-ns}.{mybatis-stmt-id}.limit.dflt=N" is set, then that is used; </li>
     * <li>if the dao's spring config set the maxRows property, then that is used;</li>
     * <li>if the dao's constructor set the maxRows property, then that is used;</li>
     * <li>if system property "{mybatis-ns}.limit.dflt=N" is set, then that is used; </li>
     * <li>if system property "alldbqueries.limit.dflt=N" is set, then that is used; </li>
     * <li>else constant QUERY_LIMIT_DFLT is used;</li.
     * </ul>
     * The last 5 (per-ns, maxRows, alldbqueries, QUERY_LIMIT_DFLT) are effectively per-dao settings; the first three
     * are per-query limits.
     * </p>
     * </p>
     * The intention of the configuration properties is that system admins at the install site can *always* override the limit
     * on a per-statement basis, in case code sets an incorrect limit (too low or too high). Otherwise, the calling code
     * generally knows best how many objects are "reasonable" in a result-set for a specific call. And if the dao query call
     * specifies QUERY_LIMIT_USE_DFLT (which most callers probably will) then there are some sane defaults settable
     * per-statement, per-dao and globally for all DAOs.
     * </p>
     */
    private RowBounds getDefaultRowBounds(String key) {
        int nsQueryLimit = getSystemProperty(key + ".limit.dflt", QUERY_LIMIT_USE_DFLT);
        if (nsQueryLimit != QUERY_LIMIT_USE_DFLT) {
            return new RowBounds(0, nsQueryLimit);
        }

        int allQueryLimit = getSystemProperty(QUERY_LIMIT_KEY, QUERY_LIMIT_DFLT);
        return new RowBounds(0, allQueryLimit);
    }

    /**
     * Used before executing a query statement to determine the maximum number of records to allow.
     */
    protected RowBounds getRowBounds(String mybatisStatementId, int queryLimit) {
        // if a "{stmtid}.limit.force" setting exists, use that
        int forceLimit = getSystemProperty(mybatisStatementId + ".limit.force", QUERY_LIMIT_USE_DFLT);
        if (forceLimit != QUERY_LIMIT_USE_DFLT) {
            return new RowBounds(0, forceLimit);
        }

        // If caller specified a limit, use that
        if (queryLimit != QUERY_LIMIT_USE_DFLT) {
            // Add one to queryLimit to avoid the case where the user wants exactly 1 object, so passes limit of 1. However
            // to detect truncation, we need to ask for *more* than the max.
            return new RowBounds(0, queryLimit + 1);
        }

        // if a "{stmtid}.limit.dflt" setting exists, use that
        int stmtDfltLimit = getSystemProperty(mybatisStatementId + ".limit.dflt", QUERY_LIMIT_USE_DFLT);
        if (stmtDfltLimit != QUERY_LIMIT_USE_DFLT) {
            return new RowBounds(0, stmtDfltLimit);
        }

        // return the default for the dao
        return rowBounds;
    }

    /**
     * Allow spring config to override the maximum number of rows returned by a selectList operation.
     */
    public void setMaxRows(int maxRows) {
        rowBounds = new RowBounds(0, maxRows);
    }

    public Date getTransactionTimestamp() {
        return TransactionTracker.getTransactionStartTimestamp();
    }

    protected void require(String objectName, Object object) {
        if (object == null) {
            throw new IllegalStateException(objectName + " has not been set");
        }
    }

    /**
     * initializes the sequence providers which the dao uses.
     */
    @PostConstruct
    public void init() {
        require("namespace", namespace);
        // sequenceProvider not needed for read-only daos
        
        if (auditor != null) {
            // auditor is optional (for batch daos), but if present then sessionService is required
            require("sessionService", sessionService);
        }

        initSequenceProviders();
        initMigrationIdSupport();

        MyBatisBugFixer.fixDeadlockOnExceptionTranslation(getSqlSession());
    }

    /**
     * extend this method to add implementation specific sequence providers.
     * e.g. addSequenceProvider(MyClass.class, "mySequenceProviderName")
     */
    protected abstract void initSequenceProviders();

    /**
     * extend this method to specify domain objects
     * which should have migration Ids when they are inserted.
     *
     */
    protected void initMigrationIdSupport() {}

    /**
     * registers a sequence provider with name 'sequenceProviderName' for a class.
     * @param clazz the class which will use the sequence provider
     * @param sequenceProviderName the name of the sequence provider
     * @throws DataAccessException if the sequence provider could not be added.
     */
    protected void addSequenceProvider(Class<?> clazz, String sequenceProviderName) {
        addSequenceProvider(clazz.getSimpleName(), sequenceProviderName);
    }

    /**
     * registers a sequence provider with name 'sequenceProviderName' for 'typeName'.
     * @param clazz the class which will use the sequence provider
     * @param sequenceProviderName the name of the sequence provider
     * @throws DataAccessException if the sequence provider could not be added.
     */
    protected void addSequenceProvider(String typeName, String sequenceProviderName) {
        SpecializedSequenceProvider<String> generator = idGeneratorsByTypeName.get(typeName);
        if (generator == null) {
            try {
                generator = sequenceProvider.getSpecializedSequenceProvider(sequenceProviderName);
                idGeneratorsByTypeName.put(typeName, generator);
            }
            catch(SQLException x) {
                throw new DataAccessException("Could not initialize sequence provider with name '" + sequenceProviderName + "'", x);
            }
        }
    }

    /**
     * Adds a type to the set of types with migration id support.
     * This means an #{insertUuid} parameter will be available on insert statements.
     * @param type
     */
    protected void addMigrationIdSupport(Class<?> type) {
        migrationIdSupport.add(type);
    }

    /**
     * Return an object that limits the size of the result-set.
     * <p>
     * This is critical for preventing out-of-memory errors or an unresponsive UI when a user performs a
     * query against a large table with an insufficient or non-existent filter.
     * </p>
     */
    protected RowBounds getRowBounds() {
        return rowBounds;
    }

    @Deprecated // use insertDomainObject or updateDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void saveObject(Definable<Long> domainObject) {
        saveObject(namespace, domainObject);
    }

    /**
     * saves the object, performing an insert if the object's ID is null or an update otherwise.
     * If inserting the object and it's ID is null, then a unique ID will be generated for it.
     * @param domainObject the object to save.
     * @throws DataAccessException if the object could not be saved.
     */
    @Deprecated // use insertDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void saveObject(String ns, Definable<Long> domainObject) {
        if (domainObject.getId() != null) {
            updateObject(ns, domainObject);
        } else {
            insertObject(ns, domainObject);
        }
    }

    /**
     * returns the next generated id for a specific type.
     * @param clazz the class of the type.
     * @throws DataAccessException if the next id could not be generated.
     */
    protected Long generateNextIdForType(Class<?> clazz) {
        return generateNextIdForType(clazz.getSimpleName());
    }

    /**
     * returns the next id for a specific type.
     * @param typeName the name of the type in the mybatis configuration.
     * @throws DataAccessException if the next id could not be generated.
     */
    protected Long generateNextIdForType(String typeName) {
        try {
            SpecializedSequenceProvider<String> gen = idGeneratorsByTypeName.get(typeName);
            if (gen == null) {
                throw new DataAccessException("Sequence generator does not exist for type '" + typeName + "'");
            }
            return gen.getNextValue();
        }
        catch(SQLException x) {
            throw new DataAccessException("Could not increment row for sequence provider '" + typeName + "'", x);
        }
    }

    /**
     * Generates a UUID.
     *
     * @return a UUID.
     * @since MOR-2094
     */
    protected UUID generateUUID() {
        return UUID.randomUUID();
    }

    /**
     * performs a query for a single object of type 'clazz'
     * this method also sets the context classloader to the classloader of the 'clazz' parameter.
     * @param clazz
     * @param params
     * @param mustExist
     * @return
     * @throws DataAccessException
     */
    protected <T> T getObjectByQueryCL(final Class<T> clazz, final Object params, final boolean mustExist) {
         return InClassLoader.execute(clazz.getClassLoader(), new InClassLoader.Executer<T, DataAccessException>() {
            @Override
			public T execute() {
                return getObjectByQuery(clazz, params, mustExist);
            }
        });
    }

    /**
     * Performs a query for a single object of type 'clazz'.
     * <p>
     * convenience wrapper for {@link #getObjectByQuery(Class, Object, boolean)}
     * </p>
     * <p>
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     * </p>
     * 
     * @param id the ID of the object.
     * @param mustExist if set to true, an empty result will throw an exception
     * @throws DataAccessException if mustExist is true and there is no result, or if there
     * is more than one result returned from the query, or if there was an sql exception.
     * @return the result object or null if it doesn't exist and mustExist == false.
     */
    protected <T> T getObjectById(String sessionId, Class<T> clazz, Object id, boolean mustExist) {
        @SuppressWarnings(UNCHECKED)
        T obj = (T) getObjectById(sessionId, clazz.getSimpleName(), id, mustExist);
        return obj;
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    protected <T> T getObjectById(Class<T> clazz, Object id, boolean mustExist) {
        @SuppressWarnings(UNCHECKED)
        T obj = (T) getObjectById(null, clazz.getSimpleName(), id, mustExist);
        return obj;
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    protected Object getObjectById(String typeName, Object id, boolean mustExist) {
        return getObjectById(null, typeName, id, mustExist);
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    protected Object getObjectById(String sessionId, String typeName, Object id, boolean mustExist) {
    	Map<String, Object> mybatisParams = toMap(PARAM_SESSIONID, sessionId, PARAM_ID, id);
    	return getObjectById(typeName, mybatisParams, mustExist);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private Object getObjectById(String typeName, Map<String, Object> mybatisParams, boolean mustExist) {
        return getObjectById(typeName, mybatisParams, namespace, mustExist);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private Object getObjectById(String typeName, Map<String, Object> mybatisParams, String ns, boolean mustExist) {
        Object object;
        try {
            Object tmp = getSqlSession().selectOne(buildSQLQueryID(ns, typeName), mybatisParams);
            // Compiler doesn't like assigning the return value of selectOne to a variable whose type is declared
            // elsewhere because the selectOne method is generic based upon the type of value the *returned result is
            // assigned to*. Therefore, tmp must be declared "inline", and var "object" then initialised separately.
            object = tmp;
        } catch (Exception x) {
            throw new DataAccessException("Error getting " + typeName + " by ID", x);
        }
        if (mustExist && object == null) {
            throw new DataAccessException("Could not get " + typeName + " by ID '" + mybatisParams.get(PARAM_ID) + "'");
        }
        return object;
    }

    /**
     * Performs a query for a single object of type 'clazz'.
     * 
     * DO NOT USE THIS METHOD WHEN QUERYING AN OBJECT BY ITS ID - USE queryDomainObject instead!
     * 
     * @param <T> the type that we are querying.
     * @param clazz the type that we are querying.
     * @param params the filter criteria for the query.
     * @param mustExist if set to true, an empty result will throw an exception
     * @throws DataAccessException if mustExist is true and there is no result, or if there
     * is more than one result returned from the query or if there was an sql exception.
     * @return the result object or null if it doesn't exist and mustExist == false.
     */
    protected <T> T getObjectByQuery(Class<T> clazz, Object params, boolean mustExist) {
        return getObjectByQuery(clazz, clazz.getSimpleName(), params, mustExist);
    }

    /**
     * Performs a query for a single object of type 'typeName'.
     * <p>
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     * </p>
     * 
     * @param typeName
     * @param params
     * @param mustExist
     * @return
     * @throws DataAccessException
     */
    protected <T> T getObjectByQuery(Class<T> clazz, String typeName, Object params, boolean mustExist) {
        return getObjectByQuery(namespace, clazz, typeName, params, mustExist);
    }

    /**
     * shorthand for {@link #getObjectByQuery(String, Class, String, Object, boolean)}.
     * <p>
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     * </p>
     *
     * @param ns
     * @param clazz
     * @param typeName
     * @param params
     * @return
     */
    protected <T> T getObjectByQuery(String ns, Class<T> clazz, Object params) {
        return getObjectByQuery(ns, clazz, clazz.getSimpleName(), params, false);
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    protected <T> T getObjectByQuery(String ns, Class<T> clazz, String typeName, Object params, boolean mustExist, int queryLimit) {
        List<T> result = performQuery(ns, typeName, params, mustExist, queryLimit);
        if (result.size() > 1) {
            throw new DataAccessException("Error, too many results for query '" + typeName + "' with params " + params);
        }
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    protected <T> T getObjectByQuery(String ns, Class<T> clazz, String typeName, Object params, boolean mustExist) {
        return getObjectByQuery(ns, clazz, typeName, params, mustExist, QUERY_LIMIT_USE_DFLT);
    }

    /**
     * Performs a query for a list of objects type 'clazz'.
     * <p>
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     * </p>
     * @param <T> the type that we are querying.
     * @param clazz the type that we are querying.
     * @param params the filter criteria for the query.
     * @throws DataAccessException if there is a problem performing the query.
     * @return the list of data or an empty list if there are no results.
     */
    protected <T> List<T> performQuery(Class<T> clazz, Object params, int queryLimit) {
        return performQuery(clazz, params, false, queryLimit);
    }

    /**
     * WARNING: Method queryDomainObject should be used rather than this method, if possible.
     */
    protected <T> List<T> performQuery(Class<T> clazz, Object params) {
        return performQuery(clazz, params, QUERY_LIMIT_USE_DFLT);
    }

    /**
     * performs the insert statement, maps to statement <domainObject.getClass().getSimpleName()>_insert,
     * using the specified namespace (useful when using a common query from another namespace).
     *
     * @param domainObject the data to insert
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use insertDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void insertObject(String ns, Object domainObject) {
        insert(ns, domainObject.getClass().getSimpleName(), domainObject);
    }

    /**
     * performs the insert statement, maps to statement <domainObject.getClass().getSimpleName()>_insert
     * @param domainObject the data to insert
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use insertDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void insertObject(Object domainObject) {
         insert(namespace, domainObject.getClass().getSimpleName(), domainObject);
    }

    /**
     * performs an insert statement, maps to <clazz.getSimpleName()>_insert
     * @param clazz the type of the object
     * @param domainObject the actual object to insert.
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use insertDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void insertData(Class<?> clazz, Object data) {
        insert(namespace, clazz.getSimpleName(), data);
    }

    /**
     * performs the update statement for the type of the object, maps to <domainObject.getClass().getSimpleName()>_update
     * @param domainObject the data to insert
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use updateDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected int updateObject(Object domainObject) {
        return updateObject(namespace, domainObject);
    }

    @Deprecated // use updateDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected int updateObject(String ns, Object domainObject) {
        return update(ns, domainObject.getClass().getSimpleName(), domainObject);
    }

    /**
     * update data maps to statement <clazz.getSimpleName()>_update
     * @param clazz the type of data to update
     * @param data the actual data values
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use updateDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected void updateData(Class<?> clazz, Object data) {
        update(clazz.getSimpleName(), data);
    }

    /**
     * deletes an object from the database, maps to statement <object.getClass().getSimpleName()>_delete
     * @param clazz the type of data to update
     * @param data the actual data values
     * @return the number of deleted records
     * @throws DataAccessException if there was an sql exception.
     */
    @Deprecated // use deleteDomainObject instead (or batch-code should use BaseDaoBatchImpl)
    protected int deleteObject(Object object) {
        return delete(object.getClass().getSimpleName(), SQL_SUFFIX_DELETE, object);
    }

    /**
     * Performs a query for a single object of type 'clazz'.
     *
     * This method also sets the context classloader to the classloader of the 'clazz' parameter.
     */
    protected <T> List<T> performQueryCL(final Class<T> clazz, final Object params, final boolean mustExist, final int queryLimit) {
         return InClassLoader.execute(clazz.getClassLoader(), new InClassLoader.Executer<List<T>, DataAccessException>() {
            @Override
			public List<T> execute() {
                return performQuery(clazz, params, mustExist, queryLimit);
            }
        });
    }

    // Use version that takes an extra "queryLimit" param (which may be QUERY_LIMIT_USE_DFLT).
    protected <T> List<T> performQueryCL(final Class<T> clazz, final Object params, final boolean mustExist) {
        return performQueryCL(clazz, params, mustExist, QUERY_LIMIT_USE_DFLT);
    }

    /**
     * Shorthand for {@link #performQuery(String, String, Object, boolean, queryLimit)}.
     */
    protected <T> List<T> performQuery(String typeName, Object params, boolean mustExist, int queryLimit) {
        return performQuery(namespace, typeName, params, mustExist, queryLimit);
    }

    protected <T> List<T> performQuery(String typeName, Object params, boolean mustExist) {
        return performQuery(typeName, params, mustExist, QUERY_LIMIT_USE_DFLT);
    }

    /**
     * Performs a query, maps to statement <clazz.getSimpleName()>_query
     *
     * @param clazz the type of the data we are querying.
     * @param params the filter criteria for the query.
     * @param mustExist if set to true, an empty result will throw an exception
     * @throws DataAccessException if data must exist and the result is empty, or there was an sql exception.
     * @return
     */
    protected <T> List<T> performQuery(Class<T> clazz, Object params, boolean mustExist, int queryLimit) {
        return performQuery(namespace, clazz.getSimpleName(), params, mustExist, queryLimit);
    }

    protected <T> List<T> performQuery(Class<T> clazz, Object params, boolean mustExist) {
        return performQuery(clazz, params, mustExist, QUERY_LIMIT_USE_DFLT);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected void handleQuery(Class<?> clazz, ResultHandler resultHandler) {
        getSqlSession().select(buildSQLQueryID(clazz), resultHandler);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected void handleQuery(Class<?> clazz, Object params, ResultHandler resultHandler) {
        getSqlSession().select(buildSQLQueryID(clazz), params, resultHandler);
    }

    /**
     * Shorthand for {@link #performQuery(String, String, Object, boolean, queryLimit)}
     */
    protected <T> List<T> performQuery(String ns, Class<T> clazz, Object params, int queryLimit) {
        return performQuery(ns, clazz.getSimpleName(), params, false, queryLimit);
    }

    protected <T> List<T> performQuery(String ns, Class<T> clazz, Object params) {
        return performQuery(ns, clazz, params, QUERY_LIMIT_USE_DFLT);
    }

    /**
     * Performs a query, maps to statement <typeName>_query
     *
     * @param ns the namespace to use, by default this is the namespace defined in the DAO
     * @param typeName the name of the type in the mybatis configuration.
     * @param params the filter criteria for the query.
     * @param mustExist if set to true, an empty result will throw an exception
     * @param queryLimit specifies the max number of rows to return; use constant QUERY_LIMIT_USE_DFLT to
     * use the standard limit for this DAO.
     *
     * @throws DataAccessException if data must exist and the result is empty, or there was an sql exception.
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    protected <T> List<T> performQuery(String ns, String typeName, Object params, boolean mustExist, int queryLimit) {
        return _performQuery(ns, typeName, params, mustExist, queryLimit);
    }

    private <T> List<T> _performQuery(String ns, String typeName, Object params, boolean mustExist, int queryLimit) {

        String mybatisStatementId = buildSQLQueryID(ns, typeName);
        RowBounds bounds = getRowBounds(mybatisStatementId, queryLimit);

        List<T> result;
        try {
            result = getSqlSession().selectList(mybatisStatementId, params, bounds);
        } catch (Exception x) {
            throw new DataAccessException("Error executing query " + mybatisStatementId + " with parameters '" + params + "'", x);
        }

        if (mustExist && (result == null || result.isEmpty())) {
            throw new DataAccessException("Error executing query '" + mybatisStatementId + "' with params " + params
                    + ": must exist but no matches found");
        }

        if ((result != null) && (result.size() >= bounds.getLimit())) {
            // Possibly here we could add a flag to the results to indicate truncation rather than just
            // failing the request - or add a "total results" field to the returned data object which is
            // normally set to results.size, but here (on truncation) we could do a "count" operation to
            // tell the user how many matches there are. The problem with returning partial results is
            // that the UI needs to properly handle this, ie indicate to the user that data has been
            // truncated, and disable sorting of the results (as sorting a partially-complete result-set
            // is very misleading). It is easier to just fail the entire request thus forcing the user
            // to specify a suitable filter instead.
            //
            // Note that the bounds object does not affect the SQL sent to the database; it just causes
            // mybatis to skip the #offset rows of the JDBC ResultSet, and then close the resultset
            // after #limit rows have been turned into objects. This does at least prevent too much
            // excess data being streamed from db server to caller, but does not help the database
            // server as much as a proper "LIMIT" clause in the SQL would.
            //
            // Note that when using a JOIN to populate a collection, reaching a bound may cause
            // the parent object's collection to be only partially populated...
            String msg = String.format("Too many results (more than %d); use a filter", bounds.getLimit()); 
            throw new DataLimitExceededException(msg, null, result);
        }

        return result;
    }

    /**
     * Create the standard selectId for queries with the given class.
     * @param clazz the class for which the query selectId should be created
     * @return the query selectId for the given class
     */
    protected String buildSQLQueryID(Class<?> clazz){
        return buildSQLQueryID(clazz.getSimpleName());
    }

    /**
     * Create the standard selectId for queries with the given typeName and the default namespace.
     * @param typeName the typeName of the query
     * @return the query selectId for the given typeName
     */
    protected String buildSQLQueryID(String typeName){
        return buildSQLQueryID(namespace, typeName);
    }

    protected String buildSQLQueryID(String ns, String typeName){
        return ns + "." + typeName + SQL_SUFFIX_QUERY;
    }

    /**
     *
     * @param clazz the type of the object we are getting an ID for.
     * @param params
     * @param mustExist
     * @return the ID of an item in the database.
     */
    protected Long getIdByQuery(Class<?> clazz, Object params, boolean mustExist) {
        return getIdByQuery(clazz, clazz.getSimpleName(), params,  mustExist);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected Long getIdByQuery(Class<?> clazz, String typeName, Object params, boolean mustExist) {
        Long id;
        try {
             id = (Long) getSqlSession().selectOne(namespace + "." + typeName + "_idquery", params);
        } catch (Exception x) {
            throw new DataAccessException("Error calling statement '" + typeName + "'", x);
        }
        if (id == null && mustExist) {
            throw new DataAccessException("Could not get ID for " + typeName + " with params '" + params + "'");
        }
        return id;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    protected Integer count(String statement, Object params) {
        try {
            return (Integer) getSqlSession().selectOne(namespace + "." + statement, params);
        } catch (Exception x) {
            throw new DataAccessException("Error calling statement '" + statement + "'", x);
        }
    }

    /**
     * shorthand for {@link #insert(String, String, Object)}
     */
    @Deprecated // use insertDomainObject (or batch code use BaseDaoBatchImpl)
    protected void insert(String typeName, final Object domainObject) {
        insert(namespace, typeName, domainObject);
    }

    /**
     * inserts the object to the database, tries to generate a new primary key
     * maps to mybatis statement <typeName>_insert
     * if required and if the object extends IUniqueID
     * @param typeName the name of the type in the mybatis configuration.
     * @param domainObject the object to insert into the database.
     */
    @SuppressWarnings(UNCHECKED)
    @Transactional(propagation = Propagation.REQUIRED)
    @Deprecated // only used by other deprecated methods in this class
    private void insert(String ns, String typeName, final Object domainObject) {
        Object toSave = domainObject;
        boolean assignedNewId = false;
        try {
            if (domainObject instanceof Definable) {
                Definable<Long> object = (Definable<Long>)domainObject;
                if (object.getId() == null) {
                    Long newId = generateNextIdForType(typeName);
                    object.setId(newId);
                    assignedNewId = true;
                }
            }

            /*
             *if the domain object supports migration Ids then
             *dump the object properties in a map and add the migrationId parameter.
             */
            if (supportsMigrationIds(domainObject)) {
                Map<String,Object> map = convertToParameterMap( domainObject );
                setMigrationId(map);
                toSave = map;
            }
            int rows = getSqlSession().insert(ns + "." + typeName + SQL_SUFFIX_INSERT, toSave);
            log.debug("inserted " + rows + " " + typeName + " with params: {}", toSave);
        } catch (Exception x) {
            if (assignedNewId) {
                // Note that this isn't terribly useful. This resets the ID for *this*
                // object when the save fails. However it is likely that the current
                // transaction will get rolled-back, and the ID for objects successfully
                // saved earlier in the transaction cannot be so easily cleared here:-(
                ((Definable<Long>)domainObject).setId(null);
            }
            throw new DataAccessException("Could not insert " + toSave, x);
        }
    }

    /**
     * performs a database update
     * @param typeName the name of the type in the mybatis configuration.
     * @param object the object containing the data to update.
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Deprecated // use updateDomainObject (or batch code use BaseDaoBatchImpl)
    protected int update(String typeName, Object object) {
        return update(namespace, typeName, object);
    }

    private int update(String ns, String typeName, Object object) {
        try {
            int rows = getSqlSession().update(ns + "." + typeName + SQL_SUFFIX_UPDATE, object);
            log.debug("updated " + rows + " " + typeName + " with params: {}", object);
            return rows;
        } catch (Exception x) {
            DataAccessException toThrow = unwrapDataAccessException(x);
            if( toThrow == null ){
                toThrow = new DataAccessException("Could not update " + object, x);
            }
            throw toThrow;
        }
    }

    /**
     * Performs a delete in the database; this maps to {@code <clazz.getSimpleName()>_<statement>}.
     *
     * @param clazz the type of data we are deleting
     * @param operationSuffix the name of the delete statement without the type prefix.
     * @param object the data for the delete filter criteria
     * @return the number of deleted records
     */
    @Deprecated // use deleteDomainObject (or batch code use BaseDaoBatchImpl)
    protected int delete(Class<?> clazz, String operationSuffix, Object object) {
        return delete(clazz.getSimpleName(), operationSuffix, object);
    }

    /**
     * Performs a delete in the database; this maps to mybatis statement {@code <typeName>_<statement>}.
     *
     * @param typeName the name of the type in the mybatis configuration.
     * @param operationSuffix the name of the delete statement without the type prefix.
     * @param object the data for the delete filter criteria
     * @return the number of deleted records
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Deprecated // use deleteDomainObject (or batch code use BaseDaoBatchImpl)
    protected int delete(String typeName, String operationSuffix, Object object) {
        // Note that the "operationSuffix" part is specific to "delete" operations; it allows the mysql to have
        // many different delete statements for the same "type", and for the caller to specify which one it
        // wants, eg "com.foo.bar.SomeType_deleteByX" and "com.foo.bar.SomeType_deleteByY". This is actually a
        // very bad idea as it creates a very type-unsafe coupling between the high-level java code and the
        // mybatis statement definition file; it is bad enough having a naming convention for _query, _insert
        // and _update; having multiple variants makes it worse. However this usage is currently wide-spread
        // throughout the app.
        String queryName;
        if (operationSuffix.startsWith("_")) {
            queryName = namespace + "." + typeName + operationSuffix;
        } else {
            queryName = namespace + "." + typeName + "_" + operationSuffix;
        }

        if (log.isDebugEnabled()) {
            log.debug("deleting " + typeName + ": " + object);
        }

        try {
            // This method is used for deleting single objects or groups of objects (eg all children of a
            // parent object). The number of deleted objects can therefore range from 0 to N, ie there is
            // no way to verify that this delete method worked as expected except by the *caller* verifying
            // the returned value.
            int nObjects = getSqlSession().delete(queryName, object);
            return nObjects;
        } catch (Exception x) {

            DataAccessException toThrow = unwrapDataAccessException(x);
            if( toThrow == null ) {
                toThrow = new DataAccessException(
                    String.format("Could not execute delete statement '%s' with param %s", queryName, object),
                    x);
            }
            throw toThrow;
        }
    }


    @SuppressWarnings(UNCHECKED) //map contains pojo properties, so <String,Object> is safe.
    @Deprecated // not needed when all code is converted to (insert/update/delete)DomainObject methods
    protected Map<String,Object> convertToParameterMap(Object domainObject)
    {
        if (domainObject == null) {
            return null;
        }
        try
        {
            Map<String,Object> map = PropertyUtils.describe(domainObject);
            //use a case insensitive map to improve property resolving in the SQL mapper configurations.
            return new CaseInsensitiveMap(map);
        }
        catch (Exception e)
        {
            throw new ServerFrameworkRuntimeException("Could not convert domain object '" + domainObject + "' to map", e);
        }
    }

    /**
     * Checks the domainObject to see if it supports migrationIds according to
     * the set of classes in the 'migrationIdSupport' set.
     * The domainObject class and it's parent classes are checked to see if they belong in the set.
     * <br/>
     * Interfaces which the domainObject may implement are not checked.
     *
     * @param domainObject the object to check.
     * @return true if the domain object supports migration Ids.
     */
    protected boolean supportsMigrationIds(Object domainObject) {
        Class<?> type = domainObject.getClass();
        while(!type.equals( Object.class )) {
            if (migrationIdSupport.contains(type)) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    protected void setMigrationId(Map<String, Object> map)  {
        map.put(MIGRATION_ID_PARAMETER, generateUUID());
    }

    /**
     * util method to convert an array of contiguous key value pairs to a map
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @param values the array of contiguous key value pairs
     * @return
     */
    @SuppressWarnings(UNCHECKED)
    protected static<K,V> Map<K,V> toMap(Object ...values) {
        Map<K,V> map = new HashMap<K,V>();
        for (int i=0; i<values.length-1; i+=2) {
            map.put((K)values[i], (V)values[i+1]);
        }
        return map;
    }

    private static DataAccessException unwrapDataAccessException(Throwable throwableParameter){
    	Throwable throwable = throwableParameter;
        while(true){
            if( throwable instanceof DataAccessException ){
                return (DataAccessException) throwable;
            } else {
                if( throwable == null ){
                    return null;
                }

                throwable = throwable.getCause();
            }
        }
    }

    private Long getUserIdIfSessionExists() {
        Long id = null; 
        if (sessionService != null) {
            try {
                id = sessionService.getSession().getUserId();
            } catch(IllegalStateException e) {
                // ignore; no session exists
            }
        }
        return id;
    }

    // =============================

    /**
     * Retrieve a single object from the database by (type, id).
     */
    protected final <T extends ControllableObject> T queryDomainObject(DomainObjectQueryContext<T> ctx) {
        return queryDomainObject(ctx, new HashMap<String, Object>());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected <T extends ControllableObject> T queryDomainObject(DomainObjectQueryContext<T> ctx,
            Map<String, Object>mybatisParams) {

        Long id = ctx.getId();
        if ((id != null) && !NO_ID.equals(id)) {
            mybatisParams.put(PARAM_ID, id);
        }

        @SuppressWarnings(UNCHECKED)
        T original = (T) getObjectById(ctx.getQueryTypeId(), mybatisParams, getContextOrDefaultNamespace(ctx), true);

        return original;
    }

    /**
     * Retrieves multiple objects from the database by (type, ids) or (type, filter).
     */
    protected final <T extends ControllableObject> List<T> queryDomainObjects(DomainObjectQueryManyContext<T> ctx) {
        return queryDomainObjects(ctx, new HashMap<String, Object>());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected <T extends ControllableObject> List<T> queryDomainObjects(DomainObjectQueryManyContext<T> ctx,
            Map<String, Object>mybatisParams) {

        List<Long> ids = ctx.getIds();
        mybatisParams.put(PARAM_IDS, ids);
        mybatisParams.put(PARAM_CURR_USER_ID, getUserIdIfSessionExists());

        @SuppressWarnings(UNCHECKED)
        List<T> originals = _performQuery(
                getContextOrDefaultNamespace(ctx),
                ctx.getQueryTypeId(),
                mybatisParams,
                false,
                ctx.getQueryLimit());

        return originals;
    }

    /**
     * Inserts the object specified by the context into the database.
     */
    protected final <T extends ControllableObject> void insertDomainObject(DomainObjectCreateContext<T> ctx) {
        insertDomainObject(ctx, new HashMap<String, Object>());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected <T extends ControllableObject> void insertDomainObject(DomainObjectCreateContext<T> ctx,
            Map<String, Object> mybatisParams) {

        T curr = ctx.getCurrent();
        assertNotEmfProxy(curr);

        // Create mybatis params
        mybatisParams.put(PARAM_ID, curr.getId());
        mybatisParams.put(PARAM_OBJECT, curr);
        if (supportsMigrationIds(curr)) {
            setMigrationId(mybatisParams);
        }
        mybatisParams.put(PARAM_CURR_USER_ID, getUserIdIfSessionExists());

        String typeName = ctx.getQueryTypeId();
        String queryName = getContextOrDefaultNamespace(ctx) + "." + typeName + SQL_SUFFIX_INSERT;

        try {
            // Note: param passed to mybatis is a map. For now, the map contains only one entry,
            // "object" => the object to be saved. It would be possible to just pass that directly
            // and make the SQL statement simpler, but
            // (a) in future it might be necessary to pass more parameters, and
            // (b) it is good to be consistent with update/delete which do pass extra params
            int rows = getSqlSession().insert(queryName, mybatisParams);
            log.debug("inserted " + rows + " " + typeName + " with params: {}", ctx);

            if (rows != 1) {
                // WARNING: this might not work with mybatis "batch" operations...
                throw new DataAccessException("Could not insert " + ctx);
            }

            // And create audit trail in same transaction
            if (ctx.isAudited()) {
                // When object should be audited then there must be a session
                Long userId = sessionService.getSession().getUserId();
                auditor.insert(userId, curr);
            }
        } catch (Exception x) {
            // Give the context the option to (somehow) revert any changes made to id and
            // version properties for the current transaction before we throw an exception.
            ctx.reset();

            DataAccessException toThrow = unwrapDataAccessException(x);
            if (toThrow == null) {
                toThrow = new DataAccessException("Could not insert " + typeName, x);
            }
            throw toThrow;
        }
    }

    /**
     * Updates an existing version of the object specified by the Context to the new state in the context.
     */
    protected final <T extends ControllableObject> void updateDomainObject(DomainObjectUpdateContext<T> ctx) {
        updateDomainObject(ctx, new HashMap<String, Object>());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected <T extends ControllableObject> void updateDomainObject(DomainObjectUpdateContext<T> ctx,
            Map<String, Object> mybatisParams) {

        T original = ctx.getOriginal();
        assertNotEmfProxy(original);

        T curr = ctx.getCurrent();
        assertNotEmfProxy(curr);

        Object originalVersion = ctx.getVersion(original);
        Object oldVersion = ctx.getVersion(curr);
        Object newVersion = ctx.updateVersion(curr);
        if (SAFE_EQUALS.compare(originalVersion,  oldVersion) != 0) {
            // Report race-conditions detected by optimistic-locking early. Because the original should
            // have been read in the same transaction as this update operation, this check *should* be
            // sufficient (depending upon read-isolation property of the transaction perhaps). However
            // the SQL should *also* check via a "where row.version=#{oldVersion}".
            throw new DataModifiedByAnotherUserException();
        }

        mybatisParams.put(PARAM_ID, curr.getId());

        // Add current user id (if any)
        mybatisParams.put(PARAM_CURR_USER_ID, getUserIdIfSessionExists());

        // Add old/new versions for optimistic locking to params
        mybatisParams.put(PARAM_OLD_VERSION, oldVersion);
        mybatisParams.put(PARAM_NEW_VERSION, newVersion); // also available from the object itself

        // Add object itself to params
        mybatisParams.put(PARAM_OBJECT, curr);

        // Compute MyBatis statement name using a standard convention
        String typeName = ctx.getQueryTypeId();
        String queryName = getContextOrDefaultNamespace(ctx) + "." + typeName + SQL_SUFFIX_UPDATE;

        try {
            // Do the actual update..
            int rows = getSqlSession().update(queryName, mybatisParams);
            log.debug("updated " + rows + " " + typeName + " with params: {}", mybatisParams);

            if (rows != 1) {
                // WARNING: this might not work with mybatis "batch" operations...
                //
                // Ideally here we would check the db state more carefully and throw an appropriate exception such as
                // DataRemovedByAnotherUser or DataModifiedByAnotherUser where appropriate. However:
                // - object being deleted will usually have been detected when the caller loaded the original object;
                // - object being modified will usually have been detected by the version-check at the start of
                //   this method.
                String msg = String.format(
                        "Could not update record (zero rows matched) for type %s : query %s : params %s",
                        original.getClass().getName(),
                        queryName,
                        mybatisParams);
                throw new DataAccessException(msg);
            }

            // And create audit trail in same transaction
            if (ctx.isAudited()) {
                Long userId = sessionService.getSession().getUserId();
                auditor.update(userId, original, curr);
            }
        } catch (Exception x) {
            DataAccessException toThrow = unwrapDataAccessException(x);
            if (toThrow == null) {
                toThrow = new DataAccessException("Could not update " + mybatisParams, x);
            }
            throw toThrow;
        }
    }

    /**
     * Deletes the object specified by the context from the database.
     */
    protected final <T extends ControllableObject> void deleteDomainObject(DomainObjectDeleteContext<T> ctx) {
        deleteDomainObject(ctx, new HashMap<String, Object>());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected <T extends ControllableObject> void deleteDomainObject(DomainObjectDeleteContext<T> ctx,
            Map<String, Object> mybatisParams) {

        T curr = ctx.getCurrent();
        assertNotEmfProxy(curr);

        mybatisParams.put(PARAM_ID, curr.getId());

        // Add old/new versions for optimistic locking to params
        Object oldVersion = ctx.getVersion(curr);
        if (oldVersion == null) {
            throw new IllegalArgumentException("internal error: oldVersion cannot be null for object to delete");
        }

        mybatisParams.put(PARAM_OLD_VERSION, oldVersion);

        // Add object itself to params
        mybatisParams.put(PARAM_OBJECT, curr);

        String typeName = ctx.getQueryTypeId();
        String queryNamespace = getContextOrDefaultNamespace(ctx);
        String queryName = queryNamespace + "." + typeName + SQL_SUFFIX_DELETE;

        try {
            int rows = getSqlSession().delete(queryName, mybatisParams);
            log.debug("deleted " + rows + " " + typeName + " with params: {}", ctx);

            if (rows != 1) {
                // WARNING: this might not work with mybatis "batch" operations...

                // To generate a proper error message, we need to see whether the object being deleted
                // actually exists in the database...
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(PARAM_ID, curr.getId());
                T original;

                try {
                    @SuppressWarnings(UNCHECKED)
                    T tmp = (T) getObjectById(typeName, params, queryNamespace, false);
                    original = tmp; // tmp var needed to make @SuppressWarnings work..
                } catch(Exception e) {
                    // Looks like "getObjectById" is not supported for this type; it isn't necessary for objects which are
                    // being deleted so just ignore this error.
                    //
                    // Not supporting getObjectById for a type can happen for "child" types that are only accessed via their parent;
                    // these are typically loaded via a "selectList" rather than selectOne in order to populate a collection member
                    // of the parent. When deleted, they are deleted one-by-one here but nothing ever *loads* them singly - unless
                    // this error occurs.
                    original = null;
                }

                if (original == null) {
                    // Can't find it - either the object has been deleted, or the current
                    // user simply has no access to the object. Guess the first...
                    throw new DataRemovedByAnotherUserException();
                }

                if (!oldVersion.equals(ctx.getVersion(original))) {
                    // Someone modified it
                    throw new DataModifiedByAnotherUserException();
                }

                // Not sure why the delete failed
                throw new DataAccessException("Could not delete " + ctx);
            }

            // And create audit trail in same transaction
            if (ctx.isAudited()) {
                Long userId = sessionService.getSession().getUserId();
                auditor.delete(userId, curr);
            }
        } catch (Exception x) {

            DataAccessException toThrow = unwrapDataAccessException(x);
            if (toThrow == null) {
                toThrow = new DataAccessException("Could not delete " + ctx, x);
            }
            throw toThrow;
        }
    }

    /**
     * Common helper method to retrieve the query namespace from the context, or use the default
     * namespace from this DAO.
     */
    protected String getContextOrDefaultNamespace(DomainObjectCommonContext<?> ctx) {
        return (ctx.getNamespace() == null ? namespace : ctx.getNamespace());
    }

    /**
     * Helper methods for handling update of persistent objects which hold collections of other objects
     * (ie in database, where a 1:N or N:M relation exists and has been translated into object-oriented
     * representation as a parent object with a list of references to other objects).
     * <p>
     * The caller is expected to then iterate over the returned lists:
     * <ul>
     * <li>every object in the "deleted" collection should be deleted from the db (and its children also deleted);</li>
     * <li>every object in the "created" collection should be inserted into the db (and its children also created);</li>
     * <li>every object in the "modified" collection should be updated in the db (and its children recursively checked);</li>
     * <li>every object in the "unmodified" collection should have its children recursively checked.</li>
     * </ul>
     * </p>
     * <p>
     * The {@code shallowComparator} parameter should return equal if the properties of the compared objects are
     * equal, even if they have child objects which are different.
     * </p>
     */
    public <T extends ControllableObject> ChildListState<T> compareChildLists(Collection<T> prev, Collection<T> curr,
            Comparator<T> shallowComparator) {
        ChildListState<T> state = new ChildListState<T>(Math.max(prev.size(), curr.size()));

        // Hmm .. handling EMF objects is tricky. It is not possible to *update* or *create* objects via their EMF proxies.
        // It is possible to *delete* objects even when they are just a proxy, but great care is needed. And at all times,
        // the "id" of a possible object needs to be retrieved via CRUDUriUtil.getId(obj), not obj.getId(). As this is all
        // so tricky, it is best to just not support EMF objects here.

        // All objects in the "prev" list have previously been persisted, so they all have valid and unique ids.
        // This is not true for the "curr" list.
        Map<Long, T> currById = new HashMap<Long, T>();
        for(T o : curr) {
            assertNotEmfProxy(o);
            Long id = o.getId();
            if (id == null) {
                // Possibly this method could take an "IdSetter" object as a parameter, and here invoke
                // "idSetter.initId(o);" to init the id of this object when needed. However for now this
                // is not needed.
                throw new IllegalStateException("Id not set");
            }

            currById.put(id, o);
        }

        for(T o : prev) {
            assertNotEmfProxy(o);
            Long id = o.getId();
            T pair = currById.remove(id);
            if (pair == null) {
                state.deleted.add(o);
            } else if (shallowComparator.compare(o, pair) == 0) {
                state.unmodified.add(o);
            } else {
                state.modified.put(o, pair);
            }
        }

        if (!currById.isEmpty()) {
            // Assume all objects in the "new" set which are not in the "old" set should be inserted.
            //
            // Note in particular that ConfigurableMyBatisCRUDOperation.create(EObject) allocates ids for created child objects
            // automatically. This is needed in order to support the "preCreateConsumers" functionality.
            state.created.addAll(currById.values());
        }

        return state;
    }

    /**
     * Simple helper method to process a property of a modified object, where the property holds a list of other
     * "child" persistent objects.
     * <p>
     * This only handles the simple case where the "child" objects do *not* themselves have children, ie where it is not
     * necessary to recursively check "child" properties of the objects in the state.[deleted|created|modified|unmodified]
     * lists.
     * </p>
     */
    public <T extends ControllableObject> void processSimpleChildState(ChildListState<T> state, BaseDomainObjectContext<T> ctxBase) {
        for(T toDelete : state.deleted) {
            deleteDomainObject(ctxBase.asDeleteContext(toDelete));
        }

        for(T toCreate : state.created) {
            if (toCreate.getId() == null) {
                throw new IllegalStateException("Id not set");
            }
            insertDomainObject(ctxBase.asCreateContext(toCreate));
        }

        for(Map.Entry<T,T> entry : state.modified.entrySet()) {
            T original = entry.getKey();
            T curr = entry.getValue();
            updateDomainObject(ctxBase.asUpdateContext(original, curr));
        }
    }

    /** Refuse to support EMF proxies. */
    protected void assertNotEmfProxy(Object o) {
        if (o instanceof EObject) {
            EObject eo = (EObject) o;
            if (eo.eIsProxy()) {
                throw new IllegalArgumentException("Object is an EMF proxy; this is not supported");
            }
        }
    }

    /**
     * Simple helper method that returns an object which does a "shallow compare" of two objects by
     * determining whether they have any "auditable differences".
     * <p>
     * This method ignores any child objects, ie returns true if the objects are the same even when their
     * children are different.
     * </p>
     * <p>
     * The return object is suitable for passing to the compareChildLists method.
     * </p>
     */
    public <T extends ControllableObject> ShallowComparator<T> getShallowComparator() {
        return new ShallowComparator<T>(getAuditor().getDeltaGenerator());
    }

    // ===================================================================

    /**
     * Simple Comparator implementation that uses reflection to compare all *primitive auditable*
     * properties of two instances.
     */
    public static class ShallowComparator<T extends ControllableObject> implements Comparator<T> {
        private final DeltaGenerator dg;

        public ShallowComparator(DeltaGenerator dg) {
            this.dg = dg;
        }

        @Override
        public int compare(T o1, T o2) {
            // hack - really should return *less*, *equal* or *greater* values. However the only use
            // of this comparator is to check for *equality* so this implementation works in practice..
            if (dg.shallowEquals(o1, o2)) {
                return 0;
            }

            // Return a consistent (though meaningless) ordering
            return System.identityHashCode(o1) - System.identityHashCode(o2);
        }
    }

    // ===================================================================

    /**
     * Simple data-structure to hold the return value of the compareChildLists() method.
     */
    public static class ChildListState<T> {
        private final List<T> unmodified;
        private final List<T> deleted;
        private final List<T> created;
        private final Map<T,T> modified;

        public ChildListState(int maxSize) {
            unmodified = new ArrayList<T>(maxSize);
            deleted = new ArrayList<T>(maxSize);
            created = new ArrayList<T>(maxSize);
            modified = new HashMap<T,T>(maxSize); // TODO: IdentityHashMap
        }

		public List<T> getUnmodified() {
			return unmodified;
		}

		public List<T> getDeleted() {
			return deleted;
		}

		public List<T> getCreated() {
			return created;
		}

		public Map<T, T> getModified() {
			return modified;
		}
    }
}
