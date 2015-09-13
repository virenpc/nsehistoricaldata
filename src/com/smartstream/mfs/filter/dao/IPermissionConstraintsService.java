package com.smartstream.mfs.filter.dao;

import com.viren.conditions.Expression;

/**
 * Fetch list of constraints that restrict read access to business objects of a certain type.
 */
public interface IPermissionConstraintsService {
    
	/**
     * Fetch list of constraints the restrict read access for the current server session. Every access area where
     * the current user has read access to a certain object type may be further restricted by 1 to n constraints.
     * 
     * @param accessAreaIdAttribute the name of the attribute that holds the access area id  
     * @param objectType the primary key of the business object type (table MAC_OBJECT_TYPE) that is associated to
     *        the business objects.
     * @return An expression which only matches objects the caller is allowed to see.
     */
	public Expression getPermissionConstraints(String accessAreaIdAttribute, String objectType);
	
	/**
	 * Consumers of this service also need the session id for restricting read access.
	 * <p>
	 * This method is deprecated; previously checking of access-areas for select statements was done by joining
	 * against the MAC_DATA_ACCESS table using the sessionId as the join-column. However the getPermissionConstraints
	 * method now returns an expression that correctly checks access-areas itself. It is therefore no longer
	 * necessary to join against MAC_DATA_ACCESS, and therefore this method is not needed - just expand the
	 * permissionConstraints expression into the where-clause of the SQL.
	 * </p> 
	 * 
	 * @return the session id of the current user
	 */
	@Deprecated // See comments above. 
	public String getSessionId();
}
