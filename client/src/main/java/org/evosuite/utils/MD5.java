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
package org.evosuite.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * MD5 class.
 * </p>
 *
 * @author José Campos
 */
public class MD5 {

    /**
     * Return the md5-hash of a string
     * based on Heshan Perera @ http://stackoverflow.com/a/10530959/998816
     *
     * @param s
     * @return
     */
    public static String hash(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());

            BigInteger i = new BigInteger(1, m.digest());
            return String.format("%1$032x", i);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return the md5-hash of a file
     *
     * @param s
     * @return
     */
    public static String hash(File f) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
            String content = new String(encoded, Charset.defaultCharset());

            return MD5.hash(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
