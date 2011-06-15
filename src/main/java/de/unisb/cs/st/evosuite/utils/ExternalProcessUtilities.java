package de.unisb.cs.st.evosuite.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import de.unisb.cs.st.evosuite.Properties;

/*
 * this could should be used by the external process that run the test case
 */

public class ExternalProcessUtilities {
	protected static Logger logger = Logger.getLogger(ExternalProcessUtilities.class);

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
			System.out.println("not possible to connect to main process: " + e);
			e.printStackTrace();
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
			logger.debug("error in receiving message", e);
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
		XStream xstream = new XStream();
		//System.out.println(xstream.toXML(population_data));
		try {
			out.writeObject(message);
			out.flush();
			out.writeObject(xstream.toXML(population_data));
			out.flush();
		} catch (Exception e) {
			logger.debug("error in sending messages");
		}

		//main process will kill this one, but we can exit here just to be sure
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		System.exit(0);
	}
}
