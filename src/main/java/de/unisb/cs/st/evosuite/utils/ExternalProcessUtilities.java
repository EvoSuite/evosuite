package de.unisb.cs.st.evosuite.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;

/*
 * this could should be used by the external process that run the test case
 */

public class ExternalProcessUtilities {
	protected static Logger logger = LoggerFactory.getLogger(ExternalProcessUtilities.class);

	protected int port;
	protected Socket connection;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;

	public ExternalProcessUtilities() {
		this.port = Properties.PROCESS_COMMUNICATION_PORT;
	}

	public boolean connectToMainProcess() {

		try {
			connection = new Socket("127.0.0.1", port);
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
		} catch (Exception e) {
			logger.error("not possible to connect to main process", e);
			return false;
		}

		return true;
	}

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

	public void askForRestart(Object population_data) {
		sendFinalMessage(Messages.NEED_RESTART, population_data);
	}

	public void informSearchIsFinished(Object population_data) {
		sendFinalMessage(Messages.FINISHED_COMPUTATION, population_data);
	}

	public void sendFinalMessage(String message, Object population_data) {
		try {
			out.writeObject(message);
			out.flush();
			// FIXXME: Currently not working
			// out.writeObject(population_data);
			out.writeObject(null);
			out.flush();
		} catch (Exception e) {
			logger.error("error in sending messages", e);
		}

		//main process will kill this one, but we can exit here just to be sure
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.warn("Thread interrupted while waiting for results from client process",
			            e);
		}

		System.exit(0);
	}
}
