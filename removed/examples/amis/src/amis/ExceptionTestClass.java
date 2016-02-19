/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package amis;

import java.io.*;

/**
 * @author Andre Mis
 * 
 */
public class ExceptionTestClass {

	private int field;
	public int publicField = 0;
	public static boolean staticField = true;
	
	public enum Day {
	    SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
	    THURSDAY, FRIDAY, SATURDAY 
	}

	public Day enexamp(Day test) {
		return test;
	}
	
	public ExceptionTestClass(int anInt) {
		field = anInt;
	}

	public void setField(int val) {
		field = val;
	}

	public int getField() {
		return field;
	}
	
	public int objMethod(Object o) {
		if (o instanceof Integer) {
			return 1;
		}
		return 0;
	}

	public void illegalArgumentThrower(int val) throws Exception {
		if (val < 0)
			throw new IllegalArgumentException("not >=0");
		field = val;
	}

	public void defThenThrow(int val) throws Exception {
		field = val;
		throw new IllegalStateException("error");
	}

	public void useThenThrow() throws Exception {
		if (field % 2 == 0)
			getField();
		throw new IllegalStateException("error");
	}

	public void throwInIf() throws Exception {
		if (field % 2 == 0)
			throw new IllegalStateException("error");

		if (field + 3 < 2) {
			field++;
			throw new Exception("error");
		}
	}

	public void ifTryCatchDef() {
		if (field % 2 == 0)
			try {
				throw new Exception("");
			} catch (Exception e) {
				field = e.toString().length();
			}
	}

	public void ifTryCatchUse() {
		if (field % 2 == 0)
			try {
				throw new Exception("");
			} catch (Exception e) {
				setField(field);
			}
	}

	public void tryIfCatchDef() {
		try {
			if (field % 2 == 0)
				throw new Exception("");
		} catch (Exception e) {
			field = e.toString().length();
		}
	}

	public void tryIfCatchUse() {

		try {
			if (field % 2 == 0)
				throw new Exception("");
		} catch (Exception e) {
			setField(field);
		}
	}

	public void alwaysThrowIllegalState() throws IllegalStateException {
		throw new IllegalStateException("error");
	}

	public void alwaysThrowError() throws Error {
		throw new Error("error");
	}

	public void alwaysThrowException() throws Exception {
		throw new Exception("error");
	}

	public void emptyTryCatch() {
		try {

		} catch (Exception e) {

		}
	}

	public void tryCatchDef() {
		try {
			throw new Exception("");
		} catch (Exception e) {
			field = e.toString().length();
		}
	}

	public void tryAlwaysCatchUse() {
		try {
			throw new Exception("");
		} catch (Exception e) {
			// will always happen
			setField(field);
		}
	}

	public void tryWontCatch() {
		try {
			field = 0;
		} catch (Exception e) {
			// won't happen
		}
	}

	public void tryEmptyCatchEmptyFinally() {
		try {
			field = 0;
		} catch (Exception e) {
			// won't happen
		} finally {

		}
	}

	public void tryMightCatch(int val) {
		try {
			illegalArgumentThrower(val);
		} catch (Exception e) {
			// might happen
		}
	}

	public void tryFinally(int val) {
		try {
			setField(val);
		} finally {
			field--;
		}
	}

	public void tryCatchFinally(int val) {
		try {
			illegalArgumentThrower(val);
		} catch (Exception e) {
			field++;
		} finally {
			field--;
		}
	}

	public void tryCatchEmptyFinally(int val) {
		try {
			illegalArgumentThrower(val);
		} catch (Exception e) {
			field++;
		} finally {
		}
	}

	public void tryEmptyCatchFinally(int val) {
		try {
			illegalArgumentThrower(val);
		} catch (Exception e) {
		} finally {
			field++;
		}
	}

	public void emptyTryCatchFinally(int val) {
		try {
		} catch (Exception e) {
		} finally {
		}
	}

	// CARE: the following method produces unreachable code (return in catch
	// never reachable)

	public int returnInTryCatch(int val) {
		try {
			return val;
		} catch (Exception e) {
			return val;
		}
	}

	// copied from:
	// http://download.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
	static String readFirstLineFromFileWithFinallyBlock(String path)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		try {
			return br.readLine();
		} finally {
			if (br != null)
				br.close();
		}
	}

}
