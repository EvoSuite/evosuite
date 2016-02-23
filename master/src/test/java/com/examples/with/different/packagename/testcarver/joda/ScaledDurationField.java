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
package com.examples.with.different.packagename.testcarver.joda;

/**
 * Created by jmr on 11/11/2015.
 * Minimised example from Joda Time.
 */
public class ScaledDurationField extends DecoratedDurationField {

	private final int iScalar;

	public ScaledDurationField(DurationField field, int scalar) {
		super(field);

		if (scalar == 0 || scalar == 1) {
			throw new IllegalArgumentException("The scalar must not be 0 or 1");
		}
		iScalar = scalar;
	}
}
