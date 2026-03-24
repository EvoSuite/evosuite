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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


/**
 * Class used to store the state of a NetworkInterface.
 * Note: we cannot override NetworkInterface, and some of
 * its state is anyway not stored in its Java class (ie
 * it depends on the actual hardware, queried with native code).
 *
 * @author arcuri
 */
public class NetworkInterfaceState {

    private static final Logger logger = LoggerFactory.getLogger(NetworkInterfaceState.class);

    private final static Constructor<NetworkInterface> constructor;
    private final static Field nameField;
    private final static Field indexField;

    static {
        Constructor<NetworkInterface> networkInterfaceConstructor = null;
        Field networkInterfaceNameField = null;
        Field networkInterfaceIndexField = null;
        try {
            networkInterfaceConstructor = NetworkInterface.class.getDeclaredConstructor();
            networkInterfaceConstructor.setAccessible(true);
            networkInterfaceNameField = NetworkInterface.class.getDeclaredField("name");
            networkInterfaceNameField.setAccessible(true);
            networkInterfaceIndexField = NetworkInterface.class.getDeclaredField("index");
            networkInterfaceIndexField.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException | RuntimeException e) {
            logger.warn("Synthetic NetworkInterface creation is not available on this JDK: {}", e.getMessage());
        }
        constructor = networkInterfaceConstructor;
        nameField = networkInterfaceNameField;
        indexField = networkInterfaceIndexField;
    }

    private NetworkInterface ni;
    private final List<InetAddress> localAddresses;
    private final byte[] mac;
    private final int mtu;
    private final boolean loopback;

    public NetworkInterfaceState(
            NetworkInterface networkInterface,
            byte[] mac,
            int mtu,
            boolean loopback,
            InetAddress anAddress) {

        this.mtu = mtu;
        this.loopback = loopback;
        this.mac = mac != null ? mac.clone() : null;
        localAddresses = Collections.singletonList(anAddress);
        ni = networkInterface;
    }

    public NetworkInterfaceState(
            String name,
            int index,
            byte[] mac,
            int mtu,
            boolean loopback,
            InetAddress anAddress) {

        this.mtu = mtu;
        this.loopback = loopback;
        this.mac = mac != null ? mac.clone() : null;
        // for now, we just consider one (IPv4) address per interface
        localAddresses = Collections.singletonList(anAddress);

        if (constructor == null || nameField == null || indexField == null) {
            throw new IllegalStateException("Synthetic NetworkInterface creation is not supported on this JDK");
        }

        try {
            ni = constructor.newInstance();
            nameField.set(ni, name);
            indexField.set(ni, index);
        } catch (IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException | SecurityException e) {
            //shouldn't really happen
            logger.error("Reflection problems: " + e.getMessage());
        }
    }

    public NetworkInterface getNetworkInterface() {
        return ni;
    }


    public List<InetAddress> getLocalAddresses() {
        return localAddresses;
    }

    public byte[] getMacAddr() {
        return mac != null ? mac.clone() : null;
    }

    public int getMTU() {
        return mtu;
    }

    public boolean isLoopback() {
        return loopback;
    }

    /*
     * For following methods we return constants.
     * We could mock them, but not particularly important/useful
     * for testing purposes
     */

    public boolean isVirtual() {
        return false;
    }

    public boolean supportsMulticast() {
        return true;
    }

    public boolean isPointToPoint() {
        return false;
    }


    public boolean isUp() {
        return true;
    }

    public NetworkInterface getParent() {
        return null;
    }
}
