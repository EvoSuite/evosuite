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
package org.evosuite.symbolic.solver.smt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class SmtQuery {

    static Logger logger = LoggerFactory.getLogger(SmtQuery.class);

    private final List<SmtConstantDeclaration> constantDeclarations = new ArrayList<>();

    private final List<SmtFunctionDeclaration> functionDeclarations = new ArrayList<>();

    private final List<SmtFunctionDefinition> functionDefinitions = new ArrayList<>();

    private final List<SmtAssertion> assertions = new ArrayList<>();

    private final Map<String, String> options = new HashMap<>();

    private String smtLogic;

    public SmtQuery() {

    }

    public void addConstantDeclaration(SmtConstantDeclaration constDecl) {
        this.constantDeclarations.add(constDecl);
    }

    public void addFunctionDefinition(SmtFunctionDefinition funcDef) {
        this.functionDefinitions.add(funcDef);
    }

    public void addFunctionDeclaration(SmtFunctionDeclaration funcDecl) {
        this.functionDeclarations.add(funcDecl);
    }

    public void addAssertion(SmtAssertion smtAssert) {
        this.assertions.add(smtAssert);
    }

    public List<SmtAssertion> getAssertions() {
        return assertions;
    }

    public List<SmtConstantDeclaration> getConstantDeclarations() {
        return constantDeclarations;
    }

    public List<SmtFunctionDefinition> getFunctionDefinitions() {
        return this.functionDefinitions;
    }

    public List<SmtFunctionDeclaration> getFunctionDeclarations() {
        return this.functionDeclarations;
    }

    public void setLogic(String smtLogic) {
        this.smtLogic = smtLogic;
    }

    public void addOption(String optionName, String optionValue) {
        this.options.put(optionName, optionValue);
    }

    public boolean hasLogic() {
        return smtLogic != null;
    }

    public String getLogic() {
        return smtLogic;
    }

    public Set<String> getOptions() {
        return this.options.keySet();
    }

    public String getOptionValue(String optionName) {
        return this.options.get(optionName);
    }

    public String toString() {
        SmtQueryPrinter printer = new SmtQueryPrinter();
        String str = printer.print(this);
        return str;
    }

}
