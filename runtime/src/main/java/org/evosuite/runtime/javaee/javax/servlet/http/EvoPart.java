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
package org.evosuite.runtime.javaee.javax.servlet.http;

import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.annotation.EvoSuiteInclude;
import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.util.ByteDataInputStream;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by Andrea Arcuri on 22/05/15.
 */
@EvoSuiteClassExclude
public class EvoPart implements Part {

    private final String name;
    private final String body;
    private String contentType;

    @EvoSuiteInclude
    @Constraints(noNullInputs = true)
    public EvoPart(String name, String body) {
        this.name = name;
        this.body = body;
        contentType = null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteDataInputStream(body);
    }

    @Override
    public String getContentType() {
        TestDataJavaEE.getInstance().accessContentType();
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        //TODO
        return null;
    }

    @Override
    public long getSize() {
        //TODO
        return 0;
    }

    @Override
    public void write(String fileName) throws IOException {
        //TODO

    }

    @Override
    public void delete() throws IOException {
        //TODO
    }

    @Override
    public String getHeader(String name) {
        //TODO
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        //TODO
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        //TODO
        return null;
    }


    // ---------- EvoSuite test methods -------------------


    @EvoSuiteInclude
    @Constraints(atMostOnce = true , excludeOthers = "asTextHtml", dependOnProperties = TestDataJavaEE.HTTP_REQUEST_CONTENT_TYPE)
    public void asTextXml(){
        contentType = EvoHttpServletRequest.TEXT_XML_CONTENT_FORMAT;
    }


    @EvoSuiteInclude
    @Constraints(atMostOnce = true , excludeOthers = "asTextXml", dependOnProperties = TestDataJavaEE.HTTP_REQUEST_CONTENT_TYPE)
    public void asTextHtml(){
        contentType = EvoHttpServletRequest.TEXT_HTML_CONTENT_FORMAT;
    }


}
