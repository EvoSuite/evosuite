package org.evosuite.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 07/06/15.
 */
public class FileSystemUtilsTest {

    @Test
    public void testCopyDirectoryAndOverwriteFilesIfNeeded() throws Exception {

        File tmpRoot = File.createTempFile("foo"+System.currentTimeMillis(),"");
        tmpRoot.delete();
        tmpRoot.mkdirs();
        tmpRoot.deleteOnExit();

        File src = new File(tmpRoot,"src");
        src.mkdirs();
        src.deleteOnExit();
        File a = new File(src,"a.txt");
        a.deleteOnExit();
        a.createNewFile();
        File b = new File(src,"b.txt");
        b.deleteOnExit();
        b.createNewFile();
        File folder = new File(src,"folder");
        folder.mkdirs();
        folder.deleteOnExit();
        File c = new File(folder,"c.txt");
        c.deleteOnExit();
        c.createNewFile();

        File dest = new File(tmpRoot,"dest");
        File destA = new File(dest,"a.txt");
        File destB = new File(dest,"b.txt");
        File destC = new File(new File(dest,"folder"),"c.txt");
        Assert.assertFalse(destA.exists());
        Assert.assertFalse(destB.exists());
        Assert.assertFalse(destC.exists());


        FileSystemUtils.copyDirectoryAndOverwriteFilesIfNeeded(src, dest);

        Assert.assertTrue(destA.exists());
        Assert.assertTrue(destB.exists());
        Assert.assertTrue(destC.exists());
        Assert.assertEquals(a.lastModified(), destA.lastModified());
        Assert.assertEquals(b.lastModified(), destB.lastModified());
        Assert.assertEquals(c.lastModified(), destC.lastModified());

        Thread.sleep(50); // be sure time stamps ll be different

        c.delete();
        c.createNewFile();
        c.deleteOnExit();

        FileWriter out = new FileWriter(c);
        String line = "foo bar";
        out.write(line+"\n");
        out.close();

        Assert.assertNotEquals(c.lastModified(), destC.lastModified());

        FileSystemUtils.copyDirectoryAndOverwriteFilesIfNeeded(src, dest);

        Assert.assertEquals(c.lastModified(), destC.lastModified());
        Scanner in = new Scanner(new FileReader(destC));
        String read = in.nextLine();
        in.close();
        Assert.assertEquals(line,read);
    }
}