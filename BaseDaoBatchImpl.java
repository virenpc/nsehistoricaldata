package com.smartstream.mfs.dao;

import com.smartstream.mfs.api.exception.DataAccessException;
import com.smartstream.mfs.api.model.Definable;

/**
 * A subclass of BaseDaoImpl that adds some methods which bypass security-checks and auditing.
 * <p>
 * This class should only be used by *internal trusted* code, never by anything that is directly invoked from a client app.
 * </p>
 */
@SuppressWarnings("deprecation")
public abstract class BaseDaoBatchImpl extends BaseDaoImpl {
    protected BaseDaoBatchImpl(String ns) {
        super(ns);
    }

    // Methods which are safe for "server-side" code to use, but not save for use by code handling client requests.
    //
    // Currently the actual implementations are in the BaseDaoImpl as some "client dao" classes use these unsafe methods.
    // The implementations on the BaseDaoImpl are therefore marked as deprecated (but are still present) and the methods
    // here are *not* deprecated and delegate to the base implementation.
    //
    // TODO: move implementations of these methods from BaseDaoImpl to here, as soon as MMI client daos stop using them! 

    protected void saveObject(Definable<Long> domainObject) throws DataAccessException {
    	super.saveObject(domainObject);
    }
    
    protected void saveObject(String ns, Definable<Long> domainObject) throws DataAccessException {
    	super.saveObject(ns, domainObject);
    }

    protected void insertObject(Object domainObject) throws DataAccessException {
    	super.insertObject(domainObject);
    }

    protected void insertObject(String ns, Object domainObject) throws DataAccessException {
    	super.insertObject(ns, domainObject);
    }

    protected void insertData(Class<?> clazz, Object data) throws DataAccessException {
    	super.insertData(clazz,  data);
    }

    protected int updateObject(Object domainObject) throws DataAccessException {
    	return super.updateObject(domainObject);
    }

    protected int updateObject(String ns, Object domainObject) throws DataAccessException {
    	return super.updateObject(ns, domainObject);
    }

    protected void updateData(Class<?> clazz, Object data) throws DataAccessException {
    	super.updateData(clazz, data);
    }

    protected int deleteObject(Object object) throws DataAccessException {
    	return super.deleteObject(object);
    }

    protected void insert(String typeName, final Object domainObject) {
    	super.insert(typeName, domainObject);
    }

    protected int update(String typeName, Object object) {
    	return super.update(typeName, object);
    }

    protected int delete(Class<?> clazz, String operationSuffix, Object object) {
    	return super.delete(clazz, operationSuffix, object);
    }

    protected int delete(String typeName, String operationSuffix, Object object) {
    	return super.delete(typeName, operationSuffix, object);
    }
}
