/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Gordon Fraser
 * 
 */
public class ConsoleProgressBar {

	public static Thread startProgressBar() {
		Thread progressPrinter = new Thread() {

			public void printProgressBar(int percent, int coverage) {
				StringBuilder bar = new StringBuilder("[Progress:");

				/*
				for (int i = 0; i < 50; i++) {
					if (i < (percent / 2)) {
						bar.append("=");
					} else if (i == (percent / 2)) {
						bar.append(">");
					} else {
						bar.append(" ");
					}
				}
				bar.append("]   " + percent + "%  [Coverage: " + coverage + "%]");
				System.out.print("\r" + bar.toString());
				*/

				for (int i = 0; i < 30; i++) {
					if (i < (int) (percent * 0.30)) {
						bar.append("=");
					} else if (i == (int) (percent * 0.30)) {
						bar.append(">");
					} else {
						bar.append(" ");
					}
				}

				bar.append(Math.min(100, percent) + "%] [Cov:");

				for (int i = 0; i < 35; i++) {
					if (i < (int) (coverage * 0.35)) {
						bar.append("=");
					} else if (i == (int) (coverage * 0.35)) {
						bar.append(">");
					} else {
						bar.append(" ");
					}
				}

				bar.append(coverage + "%]");

				System.out.print("\r" + bar.toString());

			}

			@Override
			public void run() {
				try {

					ServerSocket serverSocket = new ServerSocket(
					        Properties.PROGRESS_STATUS_PORT);
					Socket connection = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(
					        connection.getInputStream());

					int percent = 0;
					int coverage = 0;
					while (percent != -1 && !isInterrupted()) {
						percent = in.readInt();
						coverage = in.readInt();
						if (percent != -1) {
							printProgressBar(percent, coverage);
						}
					}

				} catch (Exception e) {
					//System.err.println("Exception while reading output of client process "
					//        + e);
				}

			}

			/* (non-Javadoc)
			 * @see java.lang.Thread#interrupt()
			 */
			@Override
			public void interrupt() {
				System.out.print("\n");
				super.interrupt();
			}
		};
		progressPrinter.start();
		return progressPrinter;
	}

}
