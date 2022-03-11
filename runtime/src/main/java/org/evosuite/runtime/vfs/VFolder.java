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
package org.evosuite.runtime.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A virtual folder
 *
 * @author arcuri
 */
public class VFolder extends FSObject {

    private final List<FSObject> children;

    public VFolder(String path, VFolder parent) {
        super(path, parent);

        children = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean delete() {
        if (children.size() > 0) {
            return false;
        }

        return super.delete();
    }

    public boolean isRoot() {
        return parent == null && path == null;
    }

    public void addChild(FSObject child) {
        children.add(child);
    }

    public boolean removeChild(String name) throws IllegalArgumentException {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }

        for (FSObject element : children) {
            if (name.equals(element.getName())) {
                return children.remove(element);
            }
        }

        return false;
    }

    public boolean hasChild(String name) {
        return getChild(name) != null;
    }

    public String[] getChildrenNames() {
        List<String> list = new ArrayList<>(children.size());
        for (final FSObject child : children) {
            list.add(child.getName());
        }
        return list.toArray(new String[0]);
    }

    public FSObject getChild(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }

        for (final FSObject current : children) {
            if (name.equals(current.getName())) {
                return current;
            }
        }

        return null;
    }
}
