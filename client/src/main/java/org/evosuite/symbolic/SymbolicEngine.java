/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic;

import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General class for Symbolic Engines.
 *
 * TODO: kind of empty for now.
 * Strongly based on:
 *     "A Survey of Symbolic Execution Techniques, ROBERTO BALDONI, EMILIO COPPA, DANIELE CONO D’ELIA, CAMIL DEMETRESCU,
 *     and IRENE FINOCCHI, Sapienza University of Rome" (Agregar bien esta cita)
 *
 * @author Ignacio Lebrero
 */
public abstract class SymbolicEngine {
    private static Logger logger = LoggerFactory.getLogger(SymbolicEngine.class);

	/** Instrumenting class loader */
	protected final ConcolicInstrumentingClassLoader instrumentingClassLoader;

    public SymbolicEngine(ConcolicInstrumentingClassLoader instrumentingClassLoader) {
        this.instrumentingClassLoader = instrumentingClassLoader;
    }
}