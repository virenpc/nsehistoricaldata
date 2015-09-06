package com.smartstream.mfs.dao;

import java.sql.SQLException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Utility classes with methods for working around bugs in MyBatis or MyBatis-Spring.
 */
public class MyBatisBugFixer {
    private MyBatisBugFixer() {
        // never used
    }

    /**
     * Workaround for mybatis-spring bug (https://github.com/mybatis/spring/issues/22).
     * <p>
     * Under some unusual conditions, it is possible for mybatis-spring (currently v1.1.1) to
     * deadlock while trying to apply custom mappings from DB-specific SQL exception codes
     * to application-specific exception codes (which Morpheus never does anyway). This hack
     * solves the issue - but must be called by every subclass of SqlSessionDaoSupport once
     * during object initialisation (eg in the @PostConstruct method).
     * </p>
     */
    public static void fixDeadlockOnExceptionTranslation(SqlSession sqlSession) {
        if (sqlSession instanceof SqlSessionTemplate) {
            SqlSessionTemplate sst = (SqlSessionTemplate) sqlSession;

            // Force eager initialisation of the exception translator, to avoid possible deadlocks later.
            //
            // The return value of translateExceptionIfPossible is not needed; just calling it will
            // force the lazy-initialisation logic to run - at a time where the caller is not under
            // heavy load and therefore will not deadlock.
            SQLException baseException = new SQLException();
            PersistenceException wrapperException = new PersistenceException(baseException);
            sst.getPersistenceExceptionTranslator().translateExceptionIfPossible(wrapperException);
        }
    }
}
