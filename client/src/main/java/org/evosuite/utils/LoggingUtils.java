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
package org.evosuite.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.evosuite.runtime.util.Inputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this class is used to get help on some customization of logging facility
 *
 * @author arcuri
 */
public class LoggingUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);

    /**
     * Constant <code>DEFAULT_OUT</code>
     */
    public static final PrintStream DEFAULT_OUT = System.out;
    /**
     * Constant <code>DEFAULT_ERR</code>
     */
    public static final PrintStream DEFAULT_ERR = System.err;

    public static final String USE_DIFFERENT_LOGGING_XML_PARAMETER = "use_different_logback";

    private static final String EVO_LOGGER = "evo_logger";

    /**
     * Constant <code>latestOut</code>
     */
    protected static PrintStream latestOut = null;
    /**
     * Constant <code>latestErr</code>
     */
    protected static PrintStream latestErr = null;

    /**
     * Constant <code>LOG_TARGET="log.target"</code>
     */
    public static final String LOG_TARGET = "log.target";
    /**
     * Constant <code>LOG_LEVEL="log.level"</code>
     */
    public static final String LOG_LEVEL = "log.level";

    private static volatile boolean alreadyMuted = false;

    private ServerSocket serverSocket;

    private final ExecutorService logConnections = Executors.newSingleThreadExecutor();
    private final ExecutorService logHandler = Executors.newCachedThreadPool();


    /**
     * <p>
     * Constructor for LoggingUtils.
     * </p>
     */
    public LoggingUtils() {
    }

    /**
     * Rather use AtMostOnceLogger directly
     *
     * @param logger
     * @param message
     */
    @Deprecated
    public static void logWarnAtMostOnce(Logger logger, String message) {
        AtMostOnceLogger.warn(logger, message);
    }

    /**
     * Rather use AtMostOnceLogger directly
     *
     * @param logger
     * @param message
     */
    @Deprecated
    public static void logErrorAtMostOnce(Logger logger, String message) {
        AtMostOnceLogger.error(logger, message);
    }

    /**
     * <p>
     * getEvoLogger
     * </p>
     *
     * @return a {@link org.slf4j.Logger} object.
     */
    public static Logger getEvoLogger() {
        return LoggerFactory.getLogger(EVO_LOGGER);
    }

    /**
     * <p>
     * startLogServer
     * </p>
     *
     * @return a boolean.
     */
    public boolean startLogServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(null);

            logConnections.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    while (!isServerClosed()) {
                        final Socket socket = serverSocket.accept();

                        logHandler.submit(new Callable<Void>() {
                            @Override
                            public Void call() {
                                try {
                                    ObjectInputStream ois = new ObjectInputStream(
                                            new BufferedInputStream(
                                                    socket.getInputStream()));
                                    while (socket != null && socket.isConnected()
                                            && !isServerClosed()) {
                                        ILoggingEvent event = (ILoggingEvent) ois.readObject();
                                        /*
                                         * We call the appender regardless of level in the master (ie, if the level was
                                         * set in the client and we receive a log message, then we just print it).
                                         * Note: we use
                                         * the local logger with same name just for formatting reasons
                                         */
                                        ch.qos.logback.classic.Logger remoteLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(event.getLoggerName());
                                        remoteLogger.callAppenders(event);
                                    }
                                } catch (java.net.SocketException se) {
                                    /*
                                     * FIXME: this can happen if client dies or is stopped by master with "destroy" in Windows. It is not a big problem,
                                     * but anyway how we stop clients will need to be refactored.
                                     * It can also happen if client just crash. As we already report such info in ExternalProcessGroupHandler, we do not need to
                                     * log it here
                                     */
                                } catch (java.io.UTFDataFormatException utfe) {
                                    //as above
                                } catch (java.io.EOFException eof) {
                                    //this is normal, do nothing
                                } catch (java.io.InvalidClassException ice) {
                                    /*
                                     * TODO: unclear why it happens... need more investigation
                                     */
                                    logger.error("Error in de-serialized log event: " + ice.getMessage());
                                } catch (Exception e) {
                                    logger.error("Problem in reading loggings", e);
                                }
                                return null;
                            }
                        });
                    }
                    return null;
                }
            });

            return true;
        } catch (Exception e) {
            logger.error("Can't start log server", e);
            return false;
        }
    }

    /**
     * <p>
     * isServerClosed
     * </p>
     *
     * @return a boolean.
     */
    public boolean isServerClosed() {
        return serverSocket == null || serverSocket.isClosed() || !serverSocket.isBound();
    }

    /**
     * <p>
     * getLogServerPort
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLogServerPort() {
        if (isServerClosed()) {
            return null;
        }
        return serverSocket.getLocalPort();
    }

    /**
     * <p>
     * closeLogServer
     * </p>
     */
    public void closeLogServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error in closing log server", e);
            }
            serverSocket = null;
        }
    }

    /**
     * Redirect current System.out and System.err to a buffer
     */
    public static void muteCurrentOutAndErrStream() {
        if (alreadyMuted) {
            return;
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(byteStream);
        latestOut = System.out;
        latestErr = System.err;
        System.setOut(outStream);
        System.setErr(outStream);

        alreadyMuted = true;
    }

    /**
     * Allow again printing to previous streams that were muted
     */
    public static void restorePreviousOutAndErrStream() {
        if (!alreadyMuted) {
            return;
        }
        System.setOut(latestOut);
        System.setErr(latestErr);
        alreadyMuted = false;
    }

    /**
     * Allow again printing to System.out and System.err
     */
    public static void restoreDefaultOutAndErrStream() {
        System.setOut(DEFAULT_OUT);
        System.setErr(DEFAULT_ERR);
    }


    /**
     * If the application is using a SLF4 compliant logging framework, check
     * if it has been configured. If so, keep the logging as it is.
     * On the other hand, if no configuration/framework is used, then mute
     * the default logging (Logback) of the EvoSuite modules.
     */
    public static void setLoggingForJUnit() {

        if (Properties.ENABLE_ASSERTS_FOR_EVOSUITE) {
            //if we are debugging, we don't want to switch off the logging
            return;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (isDefaultLoggingConfiguration(context)) {

            Logger root = LoggerFactory.getLogger(PackageInfo.getEvoSuitePackage());
            if (root != null && root instanceof ch.qos.logback.classic.Logger) {
                ((ch.qos.logback.classic.Logger) root).setLevel(Level.OFF);
            }
        }
    }

    private static boolean isDefaultLoggingConfiguration(LoggerContext context) {
        // TODO: Find better way to find out the default configuration
        return context.getName().equals("default");
    }

    /**
     * Load the EvoSuite xml configuration file for Logback, unless a
     * non-default one is already in use. The file has to be on the classpath.
     */
    public static boolean loadLogbackForEvoSuite() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        boolean isOK = true;

        // Only overrule default configurations
        if (isDefaultLoggingConfiguration(context)) {
            isOK = changeLogbackFile(getLogbackFileName());
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
        return isOK;
    }

    public static boolean changeLogbackFile(String resourceFilePath) {
        Inputs.checkNull(resourceFilePath);
        if (!resourceFilePath.endsWith(".xml")) {
            throw new IllegalArgumentException("Logback file name does not terminate with '.xml'");
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            final String xmlFileName = resourceFilePath;
            InputStream f = null;
            if (LoggingUtils.class.getClassLoader() != null) {
                f = LoggingUtils.class.getClassLoader().getResourceAsStream(xmlFileName);
            } else {
                // If the classloader is null, then that means EvoSuite.class was loaded
                // with the bootstrap classloader, so let's try that as well
                f = ClassLoader.getSystemClassLoader().getResourceAsStream(xmlFileName);
            }
            if (f == null) {
                String msg = xmlFileName + " not found on classpath";
                System.err.println(msg);
                logger.error(msg);
                return false;
            } else {
                context.reset();
                configurator.doConfigure(f);
            }
        } catch (JoranException je) {
            // StatusPrinter will handle this
            return false;
        }

        return true;
    }

    public static String getLogbackFileName() {
        return System.getProperty(USE_DIFFERENT_LOGGING_XML_PARAMETER, "logback-evosuite.xml");
    }

}
