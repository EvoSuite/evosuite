package org.evosuite.runtime.javaee.javax.transaction;

/*
 * TODO: for now, we just use an empty stub.
 * However, to properly handle rollbacks, we ll need to mock/simulate it properly.
 * Unfortunately, it does not seem so trivial (need to look into JNDI).
 *
 */

import javax.transaction.*;

/**
 *
 * Created by Andrea Arcuri on 15/06/15.
 */
public class EvoUserTransaction implements UserTransaction{
    @Override
    public void begin() throws NotSupportedException, SystemException {
        //TODO
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        //TODO
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        //TODO
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        //TODO
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        //TODO
    }
}
