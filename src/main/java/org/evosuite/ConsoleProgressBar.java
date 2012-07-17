/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>
 * ConsoleProgressBar class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ConsoleProgressBar {

	/**
	 * <p>
	 * startProgressBar
	 * </p>
	 * 
	 * @return a {@link java.lang.Thread} object.
	 */
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
					int phase = 0;
					while (percent != -1 && !isInterrupted()) {
						percent = in.readInt();
						phase = in.readInt(); // phase
						in.readInt(); // phases
						coverage = in.readInt();
						in.readObject(); // phase name
						if (percent != -1 && phase <= 1) {
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
