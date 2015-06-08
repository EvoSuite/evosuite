package org.evosuite.utils;

import java.io.*;

/**
 * Class used to cover some limitations of Apache IO FileUtils
 *
 * Created by Andrea Arcuri on 07/06/15.
 */
public class FileSystemUtils {

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
