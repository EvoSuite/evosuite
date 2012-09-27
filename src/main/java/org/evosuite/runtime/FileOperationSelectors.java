/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime;

/**
 * a set of common FileOperationSelectors
 * 
 * @see FileOperationSelector
 * @author Daniel Muth
 * 
 */
final class FileOperationSelectors {
	
	public static final FileOperationSelector SELECT_ALL = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return true;
		}

	};

	public static final FileOperationSelector FILE_CONTENT_MODIFICATION = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return method.equals("setFileContent");
		}
	};

	public static final FileOperationSelector FOLDER_CONTENT_MODIFICATION = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return (method.equals("fillFolder") || method.equals("deepDelete"));
		}
	};

	public static final FileOperationSelector CREATION_AND_DELETION = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return (method.equals("createFile") || method.equals("createFolder") || method.equals("deepDelete"));
		}
	};

	public static final FileOperationSelector PERMISSION_MODIFICATION = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return method.contains("Permission");
		}
	};

	public static final FileOperationSelector PARENT_CREATION_AND_DELETION = new FileOperationSelector() {

		@Override
		public boolean select(String method) {
			return (method.equals("createParent") || method.equals("deepDeleteParent"));
		}
	};

}
