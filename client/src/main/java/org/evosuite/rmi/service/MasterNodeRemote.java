/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.rmi.service;

import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.ga.Chromosome;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.statistics.RuntimeVariable;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Master Node view in the client process.
 *
 * @author arcuri
 */
public interface MasterNodeRemote extends Remote {

    String RMI_SERVICE_NAME = "MasterNode";

    /*
     * Note: we need names starting with 'evosuite' here, because those names are accessed
     * through reflections and used in the checks of the sandbox
     */

    void evosuite_registerClientNode(String clientRmiIdentifier) throws RemoteException;

    void evosuite_informChangeOfStateInClient(String clientRmiIdentifier, ClientState state, ClientStateInformation information) throws RemoteException;

    void evosuite_collectStatistics(String clientRmiIdentifier, Chromosome<?> individual) throws RemoteException;

    void evosuite_collectStatistics(String clientRmiIdentifier, RuntimeVariable variable, Object value) throws RemoteException;

    void evosuite_collectTestGenerationResult(String clientRmiIdentifier, List<TestGenerationResult> results) throws RemoteException;

    void evosuite_flushStatisticsForClassChange(String clientRmiIdentifier) throws RemoteException;

    void evosuite_updateProperty(String clientRmiIdentifier, String propertyName, Object value) throws RemoteException, IllegalArgumentException, IllegalAccessException, NoSuchParameterException;

    void evosuite_migrate(String clientRmiIdentifier, Set<? extends Chromosome<?>> migrants) throws RemoteException;

    void evosuite_collectBestSolutions(String clientRmiIdentifier, Set<? extends Chromosome<?>> solutions) throws RemoteException;
}
