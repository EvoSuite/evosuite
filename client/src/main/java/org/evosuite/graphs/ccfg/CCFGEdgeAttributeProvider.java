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
package org.evosuite.graphs.ccfg;

import org.jgrapht.ext.ComponentAttributeProvider;

import java.util.HashMap;
import java.util.Map;

public class CCFGEdgeAttributeProvider implements ComponentAttributeProvider<CCFGEdge> {


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getComponentAttributes(CCFGEdge edge) {
        Map<String, String> r = new HashMap<>();
        if (edge instanceof CCFGFrameEdge) {
            r.put("style", "dotted");
        } else if (edge instanceof CCFGMethodCallEdge) {
            r.put("style", "bold");
        }
        return r;
    }

}
