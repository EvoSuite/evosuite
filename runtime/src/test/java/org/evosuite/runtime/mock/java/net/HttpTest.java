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
package org.evosuite.runtime.mock.java.net;

import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by arcuri on 11/21/14.
 */
public class HttpTest {

    @Test
    public void testUrlParsingHttp() throws MalformedURLException {

        String location = "http://www.evosuite.org/index.html";
        URL url =  MockURL.URL(location);
        Assert.assertEquals("/index.html",url.getFile());
        Assert.assertEquals("http",url.getProtocol());
    }

    @Test
    public void testHttpNotFound() throws Exception {

        String location = "http://www.evosuite.org/index.html";
        URL url = MockURL.URL(location);
        URLConnection connection = url.openConnection();
        Assert.assertTrue(connection instanceof HttpURLConnection);

        EvoHttpURLConnection evo = (EvoHttpURLConnection) connection;
        evo.connect();

        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, evo.getResponseCode());

        try{
            evo.getInputStream();
            Assert.fail();
        } catch(IOException e){
            //expected
        }
    }

    @Test
    public void testHttpOK() throws Exception {
        VirtualNetwork.getInstance().reset();
        Assert.assertEquals(0 , VirtualNetwork.getInstance().getViewOfRemoteAccessedFiles().size());

        String text = "<html>Hello World!</html>";
        String location = "http://www.evosuite.org/index.html";
        URL url = MockURL.URL(location);

        VirtualNetwork.getInstance().addRemoteTextFile(url.toString() , text);

        URLConnection connection = url.openConnection();
        Assert.assertTrue(connection instanceof HttpURLConnection);

        EvoHttpURLConnection evo = (EvoHttpURLConnection) connection;
        evo.connect();

        Assert.assertEquals(HttpURLConnection.HTTP_OK, evo.getResponseCode());
        Scanner in = new Scanner(evo.getInputStream());
        String result = in.nextLine();
        Assert.assertEquals(text, result);

        Assert.assertEquals(1 , VirtualNetwork.getInstance().getViewOfRemoteAccessedFiles().size());
    }
}
