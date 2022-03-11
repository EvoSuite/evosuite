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
package org.evosuite.runtime.util;

import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Andrea Arcuri on 07/10/15.
 */
public class AtMostOnceLogger {

    /**
     * Keep tracks of messages that should be log only once.
     * Note: yes, this is a static field, but has no impact on test generation, so not a big deal
     */
    private static final Map<Logger, Set<String>> atMostOnceLogs = new ConcurrentHashMap<>();


    private static synchronized void logAtMostOnce(Logger logger, String message, boolean error) {
        Inputs.checkNull(logger, message);

        Set<String> previous = atMostOnceLogs.get(logger);
        if (previous == null) {
            previous = new LinkedHashSet<>();
            atMostOnceLogs.put(logger, previous);
        }

        if (!previous.contains(message)) {
            previous.add(message);

            if (error) {
                logger.error(message);
            } else {
                logger.warn(message);
            }
        }
    }

    public static void warn(Logger logger, String message) {
        logAtMostOnce(logger, message, false);
    }

    public static void error(Logger logger, String message) {
        logAtMostOnce(logger, message, true);
    }
}
