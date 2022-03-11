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
package org.evosuite.runtime.sandbox;

import org.junit.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.*;
import java.util.logging.LogManager;

public class MSecurityManagerTest {

    private static ExecutorService executor;
    private static MSecurityManager securityManager;

    @BeforeClass
    public static void initClass() {
        executor = Executors.newCachedThreadPool();
        securityManager = new MSecurityManager();
    }

    @AfterClass
    public static void doneWithClass() {
        executor.shutdownNow();
    }

    @Before
    public void initTest() {
        securityManager.apply();
        securityManager.goingToExecuteTestCase();
    }

    @After
    public void doneWithTestCase() {
        securityManager.goingToEndTestCase();
        securityManager.restoreDefaultManager();
    }


    @Test
    public void testSpecifyStreamHandler() throws Exception {
        File tmp = null;
        try {
            tmp = File.createTempFile("testFile_" + System.currentTimeMillis(), "txt");
            tmp.deleteOnExit();

            final String text = "The answer is 42";
            BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
            out.write(text);
            out.flush();
            out.close();

            final String fileName = tmp.getAbsolutePath();

            //check that reading from URL is fine
            Future<?> future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("file:" + fileName);

                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        String input = in.readLine();
                        Assert.assertEquals(text, input);
                        in.close();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

            });
            future.get(1000, TimeUnit.MILLISECONDS);

            //check that writing from URL is forbidden
            future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("file:" + fileName);
                        URLConnection connection = url.openConnection();
                        connection.setDoOutput(true);

                        /*
                         * URL cannot be used to write to a file. if try to do it, a UnknownServiceException
                         * should be thrown
                         */

                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                        out.write(text);
                        out.flush();
                        out.close();
                    } catch (SecurityException se) {
                        throw se;
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

            });
            try {
                future.get(1000, TimeUnit.MILLISECONDS);
                Assert.fail();
            } catch (ExecutionException e) {
                if (!(e.getCause().getCause() instanceof java.net.UnknownServiceException)) {
                    Assert.fail("Cause is " + e.getCause().getCause().getMessage());
                }
            }

        } catch (Exception e) {
            if (tmp != null) {
                tmp.delete();
            }
            throw e;
        }
    }


    @Test
    public void testReadButNotWriteOfFiles() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        File tmp = null;

        final String text = "EvoSuite rock!";

        try {
            //even if securityManager is on, the thread that set it should be able to write files
            tmp = File.createTempFile("foo_" + System.currentTimeMillis(), "tmp");
            tmp.deleteOnExit(); //just in case...

            BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
            out.write(text);
            out.flush();
            out.close();

            final String fileName = tmp.getAbsolutePath();


            //check that reading is fine
            Future<?> future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        File reading = new File(fileName);
                        BufferedReader in = new BufferedReader(new FileReader(reading));
                        String input = in.readLine();
                        Assert.assertEquals(text, input);
                        in.close();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

            });
            future.get(1000, TimeUnit.MILLISECONDS);

            //check that writing is forbidden
            future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
                        out.write(text);
                        out.flush();
                        out.close();
                    } catch (SecurityException se) {
                        throw se;
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }

            });
            try {
                future.get(1000, TimeUnit.MILLISECONDS);
                Assert.fail();
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof SecurityException)) {
                    Assert.fail();
                }
            }

        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    /*
     * Note: this comes from Guava library's Files.createTempDir().
     * Java 6 does not have such method, but should be in Java 7
     */
    public static File createTempDir() {
        final int TEMP_DIR_ATTEMPTS = 10000;
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + TEMP_DIR_ATTEMPTS + " attempts (tried "
                + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }


    @Test
    public void cannotCreateDeleteDirectory() throws InterruptedException, ExecutionException, TimeoutException {

        File dir = createTempDir();
        dir.deleteOnExit();
        Assert.assertTrue(dir.exists());
        dir.delete();
        Thread.sleep(100);
        Assert.assertFalse(dir.exists());

        final File toDelete = createTempDir();
        toDelete.deleteOnExit();
        Assert.assertTrue(toDelete.exists());

        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    createTempDir();
                    Assert.fail("Failed to block creating a new dir");
                } catch (SecurityException e) {
                    //EvoSuite should block creating a new folder
                }

                try {
                    toDelete.delete();
                    Assert.fail("Failed to block deleting an existing dir");
                } catch (SecurityException e) {
                    //EvoSuite should block deleting folder
                }

            }
        });
        future.get(1000, TimeUnit.MILLISECONDS);

        Assert.assertTrue(toDelete.exists());
        toDelete.delete();
    }

    /*
     * System permissions are now forbidden to modify.
     * they are handled in REPLACE_CALLS
     */
    @Ignore
    @Test
    public void testReadAndWriteOfProperties() throws InterruptedException, ExecutionException, TimeoutException {
        final String userDir = System.getProperty("user.dir");
        Assert.assertNotNull(userDir);

        final String rocks = "EvoSuite Rocks!";
        Assert.assertNotSame(rocks, userDir);


        //check that reading is fine
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                String readUserDir = System.getProperty("user.dir");
                Assert.assertEquals(userDir, readUserDir);
                System.setProperty("user.dir", rocks);
            }
        });
        future.get(1000, TimeUnit.MILLISECONDS);

        String modified = System.getProperty("user.dir");
        Assert.assertEquals(rocks, modified);

        //now, "stopping" the test case should re-store value
        try {
            securityManager.goingToEndTestCase();
            modified = System.getProperty("user.dir");
            Assert.assertEquals(userDir, modified);
        } finally {
            securityManager.goingToExecuteTestCase(); //needed
        }
    }

    @Test
    public void testCanLoadSwingStuff() throws InterruptedException, ExecutionException, TimeoutException {

        /*
         * This is needed, as it sets a hook, which will be called in the static
         * initializer of several swing components. So we need to call it
         * here on the main thread
         */
        LogManager.getLogManager();

        /*
         * Note: this test is not particularly robust. Eg, one thing it tests is whether SUT can load
         * the gui native code but that "could" be already loaded (shouldn't be though)
         */
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                     * Note, this JUnit class shouldn't have a static link to AWt (eg "import"), otherwise this
                     * test would be pointless
                     */
                    Class.forName("javax.swing.JFrame");
                } catch (ClassNotFoundException e) {
                    throw new Error(e);
                }
            }
        });
        future.get((long) Math.pow(1000, 1000), TimeUnit.MILLISECONDS);
    }


}
