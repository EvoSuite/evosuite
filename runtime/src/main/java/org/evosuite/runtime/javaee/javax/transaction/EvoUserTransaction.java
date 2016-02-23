/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
