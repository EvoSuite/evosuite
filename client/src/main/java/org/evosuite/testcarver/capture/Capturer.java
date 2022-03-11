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
package org.evosuite.testcarver.capture;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.evosuite.TimeController;
import org.evosuite.testcarver.exception.CapturerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Capturer {
    private static CaptureLog currentLog;
    private static boolean isCaptureStarted = false;
    private static final boolean isShutdownHookAdded = false;
    private static final ArrayList<CaptureLog> logs = new ArrayList<>();

    public static final String DEFAULT_SAVE_LOC = "captured.log";

    private static final ArrayList<String[]> classesToBeObserved = new ArrayList<>();

    private static final transient Logger logger = LoggerFactory.getLogger(Capturer.class);

    /*
     * TODO this needs refactoring.
     */
    @Deprecated
    private static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("shutting down...");
                Capturer.stopCapture();
                Capturer.postProcess();
                logger.info("shut down");
            }
        }));
    }

    @Deprecated
    public static void postProcess() {
		/*
		if(! Capturer.isCapturing())
		{
			if(! logs.isEmpty())
			{
				try
				{
		//					   LOG.info("Saving captured log to {}", DEFAULT_SAVE_LOC);
		//					   final File targetFile = new File(DEFAULT_SAVE_LOC);
		//					   Capturer.save(new FileOutputStream(targetFile));

					   PostProcessor.init();

					   final ArrayList<String>     pkgNames    = new ArrayList<String>();
					   final ArrayList<Class<?>[]> obsClasses = new ArrayList<Class<?>[]>();

					   int searchIndex;
					   for(String[] classNames : Capturer.classesToBeObserved)
					   {
						   searchIndex = classNames[0].lastIndexOf('.');
						   if(searchIndex > -1)
						   {
							   pkgNames.add(classNames[0].substring(0, searchIndex));  
						   }
						   else
						   {
							   pkgNames.add("");
						   }

						   final Class<?> [] clazzes = new Class<?>[classNames.length];
						   for(int j = 0; j < classNames.length; j++)
						   {
							   clazzes[j] = Class.forName(classNames[j]);
						   }
						   obsClasses.add(clazzes);
					   }


					   PostProcessor.process(logs, pkgNames, obsClasses);

					   Capturer.clear();
				}
				catch(final Exception e)
				{
					logger.error("an error occurred while post proccessin", e);
				}
			}
		}
		 */
    }

    public static void save(final OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("given OutputStream must not be null");
        }

        final XStream xstream = new XStream();
        xstream.toXML(logs, out);
        out.close();
    }

    @SuppressWarnings("unchecked")
    public static void load(final InputStream in) {
        if (in == null) {
            throw new NullPointerException("given InputStream must not be null");
        }

        final XStream xstream = new XStream(new StaxDriver());
        logs.addAll((ArrayList<CaptureLog>) xstream.fromXML(in));
    }

    public static void clear() {
        currentLog = null;
        logs.clear();
        classesToBeObserved.clear();
        isCaptureStarted = false;

        FieldRegistry.clear();
    }

    public static void startCapture() {
        logger.info("Starting Capturer...");

        if (isCaptureStarted) {
            throw new IllegalStateException("Capture has already been started");
        }

        currentLog = new CaptureLog();
        isCaptureStarted = true;

        FieldRegistry.restoreForegoingGETSTATIC();

        logger.info("Capturer has been started successfully");

    }

    public static void startCapture(final String classesToBeObservedString) {
        if (classesToBeObservedString == null) {
            final String msg = "no arguments specified";
            logger.error(msg);
            throw new CapturerException(msg);
        }

        final ArrayList<String> args = new ArrayList<>(
                Arrays.asList(classesToBeObservedString.split("\\s+")));
        if (args.isEmpty()) {
            final String msg = "no class to be observed specified";
            logger.error(msg);
            throw new CapturerException(msg);
        }

        // start Capturer if not active yet
        // NOTE: Stopping the capture and saving the corresponding logs is handled in the ShutdownHook
        //       which is automatically initialized in the Capturer
        Capturer.startCapture(args);
    }

    public static void startCapture(final List<String> classesToBeObserved) {
        logger.info("Starting Capturer...");

        if (isCaptureStarted) {
            throw new IllegalStateException("Capture has already been started");
        }

		/*
		 * TODO need refactoring
		 * 
		if(! isShutdownHookAdded)
		{
			initShutdownHook();
			isShutdownHookAdded = true;
		}
		 */
        currentLog = new CaptureLog();
        isCaptureStarted = true;

        final int size = classesToBeObserved.size();
        final String[] clazzes = new String[size];
        for (int i = 0; i < size; i++) {
            clazzes[i] = classesToBeObserved.get(i);
        }
        Capturer.classesToBeObserved.add(clazzes);

        FieldRegistry.restoreForegoingGETSTATIC();

        logger.info("Capturer has been started successfully");
    }

    public static CaptureLog stopCapture() {
        logger.info("Stopping Capturer...");

        if (isCaptureStarted) {
            isCaptureStarted = false;

            logs.add(currentLog);

            final CaptureLog log = currentLog;
            currentLog = null;

            logger.info("Capturer has been stopped successfully");

            FieldRegistry.clear();
            logger.debug("Done");
            return log;
        }

        logger.debug("Done");
        return null;
    }

    public static boolean isCapturing() {
        return isCaptureStarted;
    }

    public static void setCapturing(final boolean isCapturing) {
        Capturer.isCaptureStarted = isCapturing;
    }

    public static void capture(final int captureId, final Object receiver,
                               final String methodName, final String methodDesc, final Object[] methodParams) {
        try {
            if (isCapturing()) {
                //(currentLog) {
                setCapturing(false);

                if (logger.isDebugEnabled()) {
                    logger.debug("Method call captured:  captureId={} receiver={} type={} method={} methodDesc={} params={}",
                            captureId, System.identityHashCode(receiver),
                            receiver.getClass().getName(), methodName,
                            methodDesc, Arrays.toString(methodParams));
                }

                currentLog.log(captureId, receiver, methodName, methodDesc, methodParams);
                if (TimeController.getInstance().isThereStillTimeInThisPhase())
                    setCapturing(true);
                //}
            }
        } catch (Throwable t) {
            // TODO: Handle properly?
            logger.debug(t.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<CaptureLog> getCaptureLogs() {
        return (List<CaptureLog>) logs.clone();
    }

    public static void enable(final int captureId, final Object receiver,
                              final Object returnValue) {
        try {
            if (isCapturing()) {
                //(currentLog) {
                setCapturing(false);

                if (logger.isDebugEnabled()) {
                    logger.debug("enabled: capturedId={}", captureId);
                    //logger.debug("enabled: capturedId={} receiver={} returnValue={} returnValueOID={}",
                    //            new Object[] { captureId,
                    //                   System.identityHashCode(receiver), System.identityHashCode(returnValue),
                    //                  System.identityHashCode(returnValue) });
                }

                currentLog.logEnd(captureId, receiver, returnValue);
                setCapturing(true);
                //}
            }
        } catch (Throwable t) {
            // TODO: Handle properly
            logger.debug(t.toString());

        }
    }
}
