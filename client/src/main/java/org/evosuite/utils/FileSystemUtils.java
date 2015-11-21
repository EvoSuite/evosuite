/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import org.evosuite.runtime.util.Inputs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to cover some limitations of Apache IO FileUtils
 *
 * Created by Andrea Arcuri on 07/06/15.
 */
public class FileSystemUtils {


    public static List<File> getRecursivelyAllFiles(File folder, String suffix) throws IllegalArgumentException{
        Inputs.checkNull(folder, suffix);
        List<File> buffer = new ArrayList<>();
        _recursiveAllFiles(folder, suffix, buffer);
        return buffer;
    }

    private static void _recursiveAllFiles(File folder, String suffix, List<File> buffer){
        if(! folder.exists()){
            throw new IllegalArgumentException("Folder does not exist: "+folder.getAbsolutePath());
        }
        if(! folder.isDirectory()){
            throw new IllegalArgumentException("File is not a folder: "+folder.getAbsolutePath());
        }

        for(File file : folder.listFiles()){
            if(file.isDirectory()){
                _recursiveAllFiles(file, suffix, buffer);
            } else {
                if(file.getName().endsWith(suffix)){
                    buffer.add(file);
                }
            }
        }
    }

    /**
     * Method similar to FileUtils.copyDirectory, but with overwrite
     *
     * @param srcDir
     * @param destDir
     * @throws IllegalArgumentException
     */
    public static void copyDirectoryAndOverwriteFilesIfNeeded(File srcDir, File destDir) throws IllegalArgumentException, IOException {
        if(srcDir==null || destDir==null){
            throw new IllegalArgumentException("Null inputs");
        }
        if(!srcDir.exists()){
            throw new IllegalArgumentException("Source folder does not exist: "+srcDir.getAbsolutePath());
        }

        recursiveCopy(srcDir,destDir);
    }

    private static void recursiveCopy(File src, File dest) throws IOException{

        if(src.isDirectory()){

            //the destination might not exist. if so, let's create it
            if(!dest.exists()){
                dest.mkdirs();
            }

            //iterate over the children
            for (String file : src.list()) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive call
                recursiveCopy(srcFile, destFile);
            }

        }else{

            boolean sameTime = src.lastModified() == dest.lastModified();
            if(sameTime){
                //file was not modified, so no need to copy over
                return;
            }

            try(InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);) {

                byte[] buffer = new byte[2048];

                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            //as it is a copy, make sure to get same time stamp as src, otherwise it ll be always copied over
            dest.setLastModified(src.lastModified());
        }

    }
}
