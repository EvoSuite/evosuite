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
package com.examples.with.different.packagename.agent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by arcuri on 12/8/14.
 */
public class GetURL {

    public static URL get(String url) throws MalformedURLException {
        return new URL(url);
    }

    public static URL getFromUri(String uri) throws Exception{
        URI foo = new URI(uri);
        return foo.toURL();
    }
}
