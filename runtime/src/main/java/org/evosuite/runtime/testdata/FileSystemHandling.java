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
package org.evosuite.runtime.testdata;

import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VirtualFileSystem;

/**
 * This class is used create files as test data
 * in the test cases.
 *
 * <p>
 * The methods in this class are the main ones that are going
 * to be used in the generated JUnit files to manipulate
 * the virtual file system.
 * Note: if SUT takes as input a {@code File}, then it can happen
 * that mock {@code java.io} objects manipulating the VFS will appear in the test
 * cases.
 *
 * @author arcuri
 */
public class FileSystemHandling {

    /**
     * Append a string to the given file.
     * If the file does not exist, it will be created.
     *
     * @return
     */
    public static boolean appendStringToFile(EvoSuiteFile file, String value) {

        if (file == null || value == null) {
            return false;
        }

        return appendDataToFile(file, value.getBytes());
    }

    /**
     * Append a string to the given file, and then move cursor
     * to the next line.
     * If the file does not exist, it will be created.
     *
     * @return
     */
    public static boolean appendLineToFile(EvoSuiteFile file, String line) {

        if (file == null || line == null) {
            return false;
        }

        return appendStringToFile(file, line + "\n");
    }


    /**
     * Append a byte array to the given file.
     * If the file does not exist, it will be created.
     *
     * @param data
     * @return
     */
    public static boolean appendDataToFile(EvoSuiteFile file, byte[] data) {

        if (file == null || data == null) {
            return false;
        }

        FSObject target = VirtualFileSystem.getInstance().findFSObject(file.getPath());
        //can we write to it?
        if (target != null && (target.isFolder() || !target.isWritePermission())) {
            return false;
        }

        if (target == null) {
            //if it does not exist, let's create it
            boolean created = VirtualFileSystem.getInstance().createFile(file.getPath());
            if (!created) {
                return false;
            }
            target = VirtualFileSystem.getInstance().findFSObject(file.getPath());
            assert target != null;
        }

        VFile vf = (VFile) target;
        vf.writeBytes(data, 0, data.length);

        return true;
    }


    public static boolean createFolder(EvoSuiteFile file) {

        if (file == null) {
            return false;
        }

        return VirtualFileSystem.getInstance().createFolder(file.getPath());
    }

    /**
     * Set read/write/execute permissions to the given file
     *
     * @param file
     * @param isReadable
     * @param isWritable
     * @param isExecutable
     * @return
     */
    public static boolean setPermissions(EvoSuiteFile file, boolean isReadable, boolean isWritable, boolean isExecutable) {
        if (file == null) {
            return false;
        }
        FSObject target = VirtualFileSystem.getInstance().findFSObject(file.getPath());
        if (target == null) {
            return false;
        }

        target.setExecutePermission(isReadable);
        target.setWritePermission(isWritable);
        target.setExecutePermission(isExecutable);
        return true;
    }

    /**
     * All operations on the given {@code file} will throw an IOException if that
     * appears in their method signature
     *
     * @param file
     * @return
     */
    public static boolean shouldThrowIOException(EvoSuiteFile file) {
        if (file == null) {
            return false;
        }
        return VirtualFileSystem.getInstance().setShouldThrowIOException(file);
    }

    /**
     * All operations in the entire VFS will throw an IOException if that
     * appears in their method signature
     */
    public static boolean shouldAllThrowIOExceptions() {
        return VirtualFileSystem.getInstance().setShouldAllThrowIOExceptions();
    }
}
