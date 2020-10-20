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
package org.evosuite.coverage.aes.method;

public class AESPublicMethodCoverageSuiteFitness extends AESMethodCoverageSuiteFitness {

	private static final long serialVersionUID = -3281621600808603551L;

	public AESPublicMethodCoverageSuiteFitness(Metric metric) {
		super(metric);
	}

	public AESPublicMethodCoverageSuiteFitness() {
		this(Metric.AES);
	}
	
	protected AESMethodCoverageFactory getFactory() {
		return new AESMethodCoverageFactory().setPublicFilter(true);
	}
}
