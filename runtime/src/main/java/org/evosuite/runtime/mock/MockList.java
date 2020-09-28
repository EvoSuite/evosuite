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
package org.evosuite.runtime.mock;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.runtime.mock.java.io.MockFileInputStream;
import org.evosuite.runtime.mock.java.io.MockFileOutputStream;
import org.evosuite.runtime.mock.java.io.MockFileReader;
import org.evosuite.runtime.mock.java.io.MockFileWriter;
import org.evosuite.runtime.mock.java.io.MockIOException;
import org.evosuite.runtime.mock.java.io.MockPrintStream;
import org.evosuite.runtime.mock.java.io.MockPrintWriter;
import org.evosuite.runtime.mock.java.io.MockRandomAccessFile;
import org.evosuite.runtime.mock.java.lang.*;
import org.evosuite.runtime.mock.java.net.*;
import org.evosuite.runtime.mock.java.security.MockSecureRandom;
import org.evosuite.runtime.mock.java.text.MockDateFormat;
import org.evosuite.runtime.mock.java.text.MockSimpleDateFormat;
import org.evosuite.runtime.mock.java.time.*;
import org.evosuite.runtime.mock.java.time.chrono.*;
import org.evosuite.runtime.mock.java.util.*;
import org.evosuite.runtime.mock.java.util.logging.MockFileHandler;
import org.evosuite.runtime.mock.java.util.logging.MockLogRecord;
import org.evosuite.runtime.mock.java.util.prefs.MockPreferences;
import org.evosuite.runtime.mock.javax.swing.MockDefaultListSelectionModel;
import org.evosuite.runtime.mock.javax.swing.MockJFileChooser;
import org.evosuite.runtime.mock.javax.swing.MockJOptionPane;
import org.evosuite.runtime.mock.javax.swing.MockSpinnerDateModel;
import org.evosuite.runtime.mock.javax.swing.filechooser.MockFileSystemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to handle all the mock objects. When a new mock is defined, it has
 * to be statically added to the source code of this class.
 * 
 * <p>
 * Recall that a "override" mock M of class X has to extend X (ie 'class M
 * extends X'), and have the same constructors with same inputs, and same static
 * methods. Note: cannot use override for constructors and static methods.
 * 
 * @author arcuri
 *
 */
public class MockList {

	private static final Logger logger = LoggerFactory.getLogger(MockList.class);

	/**
	 * Return a list of all mock object classes used in EvoSuite. What is
	 * returned depend on which mock types are going to be used in the search
	 * 
	 * @return a list of Class objects
	 */
	public static List<Class<? extends EvoSuiteMock>> getList() {

		List<Class<? extends EvoSuiteMock>> list = new ArrayList<>();

		if (RuntimeSettings.useVFS) {
			list.add(MockFile.class);
			list.add(MockFileInputStream.class);
			list.add(MockFileOutputStream.class);
			list.add(MockRandomAccessFile.class);
			list.add(MockFileReader.class);
			list.add(MockFileWriter.class);
			list.add(MockPrintStream.class);
			list.add(MockPrintWriter.class);
			list.add(MockFileHandler.class);
			list.add(MockJFileChooser.class); // GUI Stuff?
			list.add(MockFileSystemView.class);
		}

		if (RuntimeSettings.mockJVMNonDeterminism) {

			list.add(MockRuntime.class);
			list.add(MockLogRecord.class);

			// Uses Object.hashCode
			list.add(MockDefaultListSelectionModel.class);

			// CPU time related
			list.add(MockDate.class);
			list.add(MockRandom.class);
			list.add(MockGregorianCalendar.class);
			list.add(MockCalendar.class);
			list.add(MockDateFormat.class);
			list.add(MockSimpleDateFormat.class);
			list.add(MockSpinnerDateModel.class);
			list.add(MockSecureRandom.class);
			list.add(MockUUID.class);
			// MockTimeZone, MockLocale are not actual mocks

			// java.time
			list.add(MockClock.class);
			list.add(MockInstant.class);
			list.add(MockLocalDate.class);
			list.add(MockLocalDateTime.class);
			list.add(MockLocalTime.class);
			list.add(MockMonthDay.class);
			list.add(MockOffsetDateTime.class);
			list.add(MockOffsetTime.class);
			list.add(MockYear.class);
			list.add(MockYearMonth.class);
			list.add(MockZonedDateTime.class);
			list.add(MockHijrahChronology.class);
			list.add(MockHijrahDate.class);
			list.add(MockIsoChronology.class);
			list.add(MockJapaneseChronology.class);
			list.add(MockJapaneseDate.class);
			list.add(MockMinguoDate.class);
			list.add(MockMinguoChronology.class);
			list.add(MockThaiBuddhistChronology.class);
			list.add(MockThaiBuddhistDate.class);

			// preferences
			list.add(MockPreferences.class);

			// thread related
			list.add(MockTimer.class);
			list.add(MockThread.class);

			// exceptions
			list.add(MockIOException.class);
			list.add(MockArithmeticException.class);
			list.add(MockArrayIndexOutOfBoundsException.class);
			list.add(MockError.class);
			list.add(MockException.class);
			list.add(MockIllegalAccessException.class);
			list.add(MockIllegalArgumentException.class);
			list.add(MockIllegalStateException.class);
			list.add(MockNullPointerException.class);
			list.add(MockRuntimeException.class);
			list.add(MockThrowable.class);
		}

		if (RuntimeSettings.useVNET) {
			list.add(MockDatagramSocket.class);
			list.add(MockInetAddress.class);
			list.add(MockInetSocketAddress.class);
			list.add(MockNetworkInterface.class);
			list.add(MockServerSocket.class);
			list.add(MockSocket.class);
			list.add(MockSocketImpl.class);
			list.add(MockURL.class);
			list.add(MockURLStreamHandler.class);
			list.add(MockURI.class);
			// list.add(MockServerSocketChannel.class); //TODO
			// list.add(MockSocketChannel.class); //TODO
		}

		if (RuntimeSettings.mockGUI) {
			// why not including JFileChooser?
			list.add(MockJOptionPane.class);
		}

		return list;
	}

	/**
	 * Check if the given class has been mocked
	 * 
	 * @param originalClass
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static boolean shouldBeMocked(String originalClass) throws IllegalArgumentException {
		return getMockClass(originalClass) != null;
	}

	/**
	 * Check if the given class is among the mock classes
	 * 
	 * @param mockClass
	 * @return
	 */
	public static boolean isAMockClass(String mockClass) {
		if (mockClass == null) {
			return false;
		}

		for (Class<?> mock : getList()) {
			if (mock.getCanonicalName().equals(mockClass)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the mock class for the given target
	 * 
	 * @param originalClass
	 * @return {@code null} if the target is not mocked
	 */
	public static Class<?> getMockClass(String originalClass) throws IllegalArgumentException {
		if (originalClass == null || originalClass.isEmpty()) {
			throw new IllegalArgumentException("Empty className");
		}

		for (Class<? extends EvoSuiteMock> mock : getList()) {

			String name = null;

			if (OverrideMock.class.isAssignableFrom(mock)) {
				Class<?> target = mock.getSuperclass();
				if (target == null) {
					logger.error("Override mock " + mock.getCanonicalName() + " does not have a superclass");
					continue;
				}
				name = target.getCanonicalName();

			} else if (StaticReplacementMock.class.isAssignableFrom(mock)) {
				try {
					StaticReplacementMock m = (StaticReplacementMock) mock.newInstance();
					name = m.getMockedClassName();
				} catch (Exception e) {
					logger.error("Failed to create instance of mock " + mock.getCanonicalName());
					continue;
				}
			} else {
				// should never happen
				logger.error("Cannot handle " + mock);
				continue;
			}

			if (originalClass.equals(name)) {
				return mock;
			}
		}

		return null;
	}
}
