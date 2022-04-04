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
package org.evosuite.runtime.vnet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Used to resolve host names
 *
 * @author arcuri
 */
public class DNS {

    /**
     * The actual IP address does not really matter, as long
     * as it is in the valid format
     */
    private static final String MASK = "200.42.42.";

    /**
     * Used to create unique new mappings
     */
    private final AtomicInteger counter;


    /**
     * Key -> a host name to resolve
     * <p>
     * Value -> the IP address for the given host
     */
    private final Map<String, String> resolved;


    public DNS() {
        counter = new AtomicInteger(0);
        resolved = new ConcurrentHashMap<>();
    }

    /**
     * Get the IP address for the given host name (eg www.evosuite.org)
     *
     * @param host
     * @return {@code null} if the host was not resolved
     */
    public synchronized String resolve(String host) {

        //check if already resolved
        String addr = resolved.get(host);
        if (addr != null) {
            return addr;
        }

        //check if IP numeric
        if (EvoIPAddressUtil.textToNumericFormatV4(host) != null) {
            return host;
        }

        if (counter.get() > 255) {
            /*
             * In theory, we could mock as many address resolutions
             * as we want. But, if addresses are resolved in a loop
             * based on an external string, we might end up in
             * troubles. so just simulate a failure
             */
            return null;
        }

        //need to create a new mapping

        addr = MASK + counter.getAndIncrement();
        resolved.put(host, addr);

        return addr;
    }
}
