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

package org.evosuite;

import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.utils.Listener;

/**
 * <p>
 * ConsoleProgressBar class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class ConsoleProgressBar implements Listener<ClientStateInformation> {

    private static final long serialVersionUID = 8930332599188240933L;

    /**
     * <p>
     * startProgressBar
     * </p>
     */
    public static void startProgressBar() {
        MasterServices.getInstance().getMasterNode().addListener(new ConsoleProgressBar());
    }

    @Override
    public void receiveEvent(ClientStateInformation event) {
        if (event.getState() != ClientState.SEARCH) {
            return;
        }

        int percent = event.getProgress();
        int coverage = event.getCoverage();


        StringBuilder bar = new StringBuilder("[Progress:");

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

        System.out.print("\r" + bar);

    }

}
