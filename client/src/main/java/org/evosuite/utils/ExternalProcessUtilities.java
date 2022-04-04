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
package org.evosuite.utils;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * this could should be used by the external process that run the test case
 */
public class ExternalProcessUtilities {
    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(ExternalProcessUtilities.class);

    protected Socket connection;
    protected ObjectOutputStream out;
    protected ObjectInputStream in;

    /**
     * <p>
     * Constructor for ExternalProcessUtilities.
     * </p>
     */
    public ExternalProcessUtilities() {
    }

    /**
     * <p>
     * connectToMainProcess
     * </p>
     *
     * @return a boolean.
     */
    public boolean connectToMainProcess() {

        try {
            connection = new Socket("127.0.0.1", Properties.PROCESS_COMMUNICATION_PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (Exception e) {
            logger.error("not possible to connect to main process", e);
            return false;
        }

        return true;
    }

    /**
     * <p>
     * receiveInstruction
     * </p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object receiveInstruction() {
        try {
            String message = (String) in.readObject();
            if (message.equals(Messages.NEW_SEARCH))
                return null;
            else if (message.equals(Messages.CONTINUE_SEARCH)) {
                Object population_data = in.readObject();
                return population_data;
            }
        } catch (Exception e) {
            logger.error("error in receiving message", e);
        }

        throw new RuntimeException("no valid message received");
    }

    /**
     * <p>
     * askForRestart
     * </p>
     *
     * @param population_data a {@link java.lang.Object} object.
     */
    public void askForRestart(Object population_data) {
        sendFinalMessage(Messages.NEED_RESTART, population_data);
    }

    /**
     * <p>
     * informSearchIsFinished
     * </p>
     *
     * @param population_data a {@link java.lang.Object} object.
     */
    public void informSearchIsFinished(Object population_data) {
        sendFinalMessage(Messages.FINISHED_COMPUTATION, population_data);
    }

    /**
     * <p>
     * sendFinalMessage
     * </p>
     *
     * @param message         a {@link java.lang.String} object.
     * @param population_data a {@link java.lang.Object} object.
     */
    public void sendFinalMessage(String message, Object population_data) {
        try {
            out.writeObject(message);
            out.flush();
            if (Properties.SERIALIZE_RESULT)
                out.writeObject(population_data);
            else
                out.writeObject(null);
            out.flush();
        } catch (Exception e) {
            logger.error("error in sending messages", e);
        }

        //main process will kill this one, but we can exit here just to be sure
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.debug("Thread interrupted while waiting after sending results from client to master",
                    e);
        }
    }
}
