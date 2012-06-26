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
package org.evosuite.symbolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.Reporter;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;

/**
 * This reporter stays silents
 * 
 * @author Jan Malburg
 * 
 */
public class SilentReporter extends Reporter {

	public SilentReporter(Config conf, JPF jpf) {
		super(conf, jpf);
		//this.publishers = new ArrayList<Publisher>();
		this.publishers = new Publisher[0];
	}

	@Override
	public void searchStarted(Search search) {
	}

	@Override
	public void stateAdvanced(Search search) {
	}

	@Override
	public void searchConstraintHit(Search search) {
	}

	@Override
	public void searchFinished(Search search) {
	}

	@Override
	public boolean hasToReportTrace() {
		return false;
	}

	@Override
	public boolean hasToReportOutput() {
		return false;
	}

}
