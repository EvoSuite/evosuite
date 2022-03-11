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
package org.evosuite.rmi;

import org.evosuite.utils.Randomness;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UtilsRMI {

    /**
     * This is a wrapper over UnicastRemoteObject.exportObject,
     * as it does not properly handle anonymous/ephemeral ports.
     * <p>
     * In particular, "exportObject(Remote obj)" returns a RemoteStub, and not a Remote reference.
     *
     * @param obj
     * @return
     * @throws RemoteException
     */
    public static Remote exportObject(Remote obj) throws RemoteException {
        int port = 2000;
        port += Randomness.nextInt(20000);

        final int TRIES = 100;
        for (int i = 0; i < TRIES; i++) {
            try {
                int candidatePort = port + i;
                return UnicastRemoteObject.exportObject(obj, candidatePort);
            } catch (RemoteException e) {
            }
        }

        return UnicastRemoteObject.exportObject(obj, port);
    }

    /**
     * A registry is created on the "localhost".
     * But what is the "localhost"? usually, this is a loopback
     * address 127.0.0.1. But, if there are network interfaces, depending
     * on the operating system the "localhost" can bind to them.
     *
     * <p>
     * So, if we start EvoSuite with a connection on (eg WiFi), losing such connection
     * would mess up EvoSuite. Even worse, if EvoSuite is started from Eclipse, Eclipse
     * will always use the binding it used at it start! In that case, Eclipse would need
     * to be restarted each time connection change.
     *
     * <p>
     * For these reasons, here we try to force to always bind to 127.0.0.1
     */
    public static void ensureRegistryOnLoopbackAddress() {
        /*
         *  Here, it does not seem to be possible in a clean way
         *  to force the biding to 127.0.0.1.
         *  So we need to modify the property used for it
         */
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
    }

}
