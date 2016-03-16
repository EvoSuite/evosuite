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
package org.evosuite.testcarver.configuration;

import java.util.Properties;

public final class Configuration extends Properties
{
	private static final long serialVersionUID = 7207421643238351948L;

	public static final Configuration INSTANCE = new Configuration();
	
	public  static final String MODIFIED_BIN_LOC     = "de.unisb.cs.st.testcarver.modified_bin_loc";
	private static final String DEF_MODIFIED_BIN_LOC = "target/modified_bin";
	
	public  static final String GEN_TESTS_LOC     = "de.unisb.cs.st.testcarver.gen_tests_loc";
	private static final String DEF_GEN_TESTS_LOC = "target/carved_tests";
	
	public static final String  OBS_CLASSES     = "de.uni.sb.observed_classes";
	
	private Configuration()
	{
		// some configuration params might be passed via system properties
		super(System.getProperties());
				
		// FIXME does not work properly
		this.putIfAbsent(MODIFIED_BIN_LOC, DEF_MODIFIED_BIN_LOC);
		this.putIfAbsent(GEN_TESTS_LOC,    DEF_GEN_TESTS_LOC);
//		this.putIfAbsent(OBS_CLASSES,      DEF_OBS_CLASSES);
	}
	
	private void putIfAbsent(final String key, final String value)
	{
		if(! super.containsKey(key))
		{
			super.setProperty(key, value);
		}
	}
	
	/**
	 *  FIXME: logger should not be set in the code!!!
	 *  If really needed, should go to pom.xml file in
	 *  the starting script generation
	 */
	@Deprecated
	public void initLogger()
	{
		// init log4j
		System.getProperties().put("log4j.configuration", "config/log4j.properties");
	}
}
