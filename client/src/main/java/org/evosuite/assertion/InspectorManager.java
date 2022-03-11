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
package org.evosuite.assertion;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.utils.JdkPureMethodsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class InspectorManager {

    private static InspectorManager instance = null;

    private static final Logger logger = LoggerFactory
            .getLogger(InspectorManager.class);

    private final Map<Class<?>, List<Inspector>> inspectors = new HashMap<>();

    private final Map<String, List<String>> blackList = new HashMap<>();

    private InspectorManager() {
        // TODO: Need to replace this with proper analysis
        // readInspectors();
        initializeBlackList();
    }

    private void initializeBlackList() {
        // These methods will include absolute path names and should not be in assertions
        blackList.put(
                "java.io.File",
                Arrays.asList("getPath", "getAbsolutePath",
                        "getCanonicalPath"));
        blackList.put("java.io.DataOutputStream", Arrays.asList("size"));

        // These methods will contain locale specific strings
        blackList.put("java.util.Date",
                Arrays.asList("getLocaleString"));

        // These methods will include data differing in every run
        blackList.put(
                "java.lang.Thread",
                Arrays.asList("activeCount", "getId", "getName",
                        "getPriority", "toString", "getState", "isAlive"));
        blackList.put(
                "java.lang.ThreadGroup",
                Arrays.asList("activeCount", "activeGroupCount", "getMaxPriority",
                        "isDaemon", "isDestroyed", "toString"));
        blackList.put("java.util.EventObject",
                Arrays.asList("toString"));

        blackList.put(Locale.class.getCanonicalName(),
                Arrays.asList("getDisplay"));
        blackList.put("java.util.Hashtable",
                Arrays.asList("toString"));
        blackList.put("java.util.HashSet",
                Arrays.asList("toString"));
        blackList.put("java.util.HashMap",
                Arrays.asList("toString"));
        blackList.put("java.util.AbstractMap",
                Arrays.asList("toString"));
        blackList.put("java.util.AbstractCollection",
                Arrays.asList("toString"));

        blackList.put("java.util.logging.Logger",
                Arrays.asList("getUseParentHandlers"));

        // AWT identifiers are different with every run
        blackList.put("java.awt.Panel",
                Arrays.asList("toString"));
        blackList.put("java.awt.event.ActionEvent",
                Arrays.asList("toString"));
        // TODO: Figure out how to make AWT/Swing component status deterministic between headless/non-headless
        blackList.put("java.awt.Component",
                Arrays.asList("toString", "isVisible", "isForegroundSet", "isBackgroundSet", "isFontSet", "isCursorSet",
                        "isDisplayable", "isEnabled", "isFocusable", "isFocusOwner", "isFocusTraversable", "isLightweight",
                        "isMaximumSizeSet", "isMinimumSizeSet", "isPreferredSizeSet", "isShowing", "isValid", "isVisible"));
        blackList.put("java.awt.Container",
                Arrays.asList("countComponents", "getComponentCount", "isForegroundSet", "isBackgroundSet", "isFontSet"));
        blackList.put("java.awt.event.MouseWheelEvent",
                Arrays.asList("toString"));
        blackList.put("javax.swing.DefaultListSelectionModel",
                Arrays.asList("toString"));
        blackList.put("javax.swing.JPopupMenu",
                Arrays.asList("isFontSet", "getComponentCount", "isForegroundSet", "isBackgroundSet", "isFontSet"));
        blackList.put("javax.swing.JInternalFrame",
                Arrays.asList("getComponentCount", "countComponents", "isForegroundSet", "isBackgroundSet", "isFontSet"));
        blackList.put("javax.swing.text.StyleContext",
                Arrays.asList("toString"));
        blackList.put("java.rmi.server.ObjID",
                Arrays.asList("toString"));
        blackList.put("java.awt.event.InvocationEvent",
                Arrays.asList("getWhen"));
        blackList.put("java.lang.StringBuffer",
                Arrays.asList("capacity"));
    }

    /**
     * <p>
     * Getter for the field <code>instance</code>.
     * </p>
     *
     * @return a {@link org.evosuite.assertion.InspectorManager} object.
     */
    public static InspectorManager getInstance() {
        if (instance == null) {
            instance = new InspectorManager();
        }
        return instance;
    }

    public static void resetSingleton() {
        instance = null;
    }

    private boolean isInspectorMethod(Method method) {
        if (!Modifier.isPublic(method.getModifiers()))
            return false;

        if (!method.getReturnType().isPrimitive()
                && !method.getReturnType().equals(String.class)
                && !method.getReturnType().isEnum()
                && !ClassUtils.isPrimitiveWrapper(method.getReturnType())) {
            return false;
        }

        if (method.getReturnType().equals(void.class))
            return false;

        if (method.getParameterTypes().length != 0)
            return false;

        if (method.getName().equals("hashCode"))
            return false;

        if (method.getDeclaringClass().equals(Object.class))
            return false;

        if (method.getDeclaringClass().equals(Enum.class))
            return false;

        if (method.isSynthetic())
            return false;

        if (method.isBridge())
            return false;

        if (method.getName().equals("pop"))
            return false;

        if (isBlackListed(method))
            return false;

        if (isImpureJDKMethod(method))
            return false;

        if (isAWTToString(method))
            return false;

        if (Properties.PURE_INSPECTORS) {
            return CheapPurityAnalyzer.getInstance().isPure(method);
        }

        return true;

    }

    private boolean isImpureJDKMethod(Method method) {
        String className = method.getDeclaringClass().getCanonicalName();
        if (!className.startsWith("java."))
            return false;

        return !JdkPureMethodsList.instance.isPureJDKMethod(method);
    }

    private boolean isAWTToString(Method method) {
        String className = method.getDeclaringClass().getCanonicalName();
        if (className.startsWith("javax.") || className.startsWith("java.awt.")) {
            return method.getName().equals("toString");
        }
        return false;
    }

    private boolean isBlackListed(Method method) {
        String className = method.getDeclaringClass().getCanonicalName();
        if (MockList.isAMockClass(className)) {
            className = method.getDeclaringClass().getSuperclass().getCanonicalName();
        }
        if (!blackList.containsKey(className))
            return false;
        String methodName = method.getName();
        return blackList.get(className).contains(methodName);
    }

    private void determineInspectors(Class<?> clazz) {
        if (!TestUsageChecker.canUse(clazz)) {
            inspectors.put(clazz, Collections.emptyList());
        }
        if (!TestUsageChecker.canUse(clazz))
            return;
        List<Inspector> inspectorList = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (isInspectorMethod(method)) { // FIXXME
                logger.debug("Inspector for class " + clazz.getSimpleName()
                        + ": " + method.getName() + " defined in "
                        + method.getDeclaringClass().getCanonicalName());

                inspectorList.add(new Inspector(clazz, method));
            } else {
                logger.debug("Not an inspector: " + method.getName());
            }
        }
        inspectors.put(clazz, inspectorList);
    }

    /**
     * <p>
     * Getter for the field <code>inspectors</code>.
     * </p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a {@link java.util.List} object.
     */
    public List<Inspector> getInspectors(Class<?> clazz) {
        if (!inspectors.containsKey(clazz)) {
            determineInspectors(clazz);
        }
        return inspectors.get(clazz);
    }

    /**
     * <p>
     * removeInspector
     * </p>
     *
     * @param clazz     a {@link java.lang.Class} object.
     * @param inspector a {@link org.evosuite.assertion.Inspector} object.
     */
    public void removeInspector(Class<?> clazz, Inspector inspector) {
        if (inspectors.containsKey(clazz)) {
            inspectors.get(clazz).remove(inspector);
        }
    }
}
