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
package org.evosuite.runtime.util;

import org.evosuite.runtime.RuntimeSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by Andrea Arcuri on 10/11/15.
 */
public class JarPathing {

    private static Logger logger = LoggerFactory.getLogger(JarPathing.class);

    private static final String PATHING_JAR_PREFIX = "EvoSuite_pathingJar";

    public static boolean containsAPathingJar(String sequence){
        for(String token : sequence.split(File.pathSeparator)){
            if(isPathingJar(token)){
                return true;
            }
        }
        return false;
    }

    public static boolean isPathingJar(String path) throws IllegalArgumentException {

        if(path.contains(File.pathSeparator)){
            throw new IllegalArgumentException("Multiple elements in path: "+path);
        }

        return path != null && path.contains(PATHING_JAR_PREFIX) && path.endsWith(".jar");
    }

    public static String expandPathingJars(String sequence){
        List<String> list = new ArrayList<>();
        for(String token : sequence.split(File.pathSeparator)){
            if(isPathingJar(token)){
                list.add(extractCPFromPathingJar(token));
            } else {
                list.add(token);
            }
        }
        return String.join(File.pathSeparator, list);
    }

    public static String extractCPFromPathingJar(String pathingJar) throws IllegalArgumentException{
        Inputs.checkNull(pathingJar);
        if(! isPathingJar(pathingJar)){
            throw new IllegalArgumentException("Invalid pathing jar name: "+pathingJar);
        }

        File jar = new File(pathingJar);
        if(! jar.exists()){
            throw new IllegalArgumentException("Pathing jar does not exist: "+pathingJar);
        }
        try (JarInputStream in = new JarInputStream(new FileInputStream(jar))){

            Manifest m = in.getManifest();
            String escapedCP = m.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);

            List<String> list = new ArrayList<>();
            for(String token : escapedCP.split(" ")){
                File file = new File(token.replace("%20"," "));
                if(! file.exists()){
                    //this should never happen, unless bug in EvoSuite
                    throw new IllegalStateException("Pathing jar "+pathingJar+" refers to non-existing entry " + token);
                }
                if(isPathingJar(file.getAbsolutePath())){
                    throw new IllegalArgumentException("Pathing jar "+pathingJar+" contains the pathing jar "+file.getAbsolutePath());
                }
                list.add(file.getAbsolutePath());
            }

            return String.join(File.pathSeparator, list);

        } catch (IOException e) {
            logger.error(e.toString(),e);
            return pathingJar;
        }
    }

    /**
     * Create a jar file in the tmp directory having the given {@code classpath}  in the
     * manifest, and return the path to such jar. This is done to avoid issues in
     * Windows where cannot have too long classpaths
     *
     * @param classpath
     * @return
     */
    public static String createJarPathing(String classpath){

		logger.debug("Going to create jar pathing for: {}", classpath);

        List<String> elements = new ArrayList<>();
        elements.addAll(Arrays.asList(classpath.split(File.pathSeparator)));

        StringBuffer escaped = new StringBuffer();
        while(! elements.isEmpty()){
            String element = elements.remove(0);
            try {

                //be sure the classpath element is absolute
                File file = new File(element);
                element = file.getAbsolutePath();

                if(! file.exists()){
                    logger.warn("Classpath entry does not exist: {}", element);
                    continue;
                }

                if(isPathingJar(element)){
                    elements.addAll(Arrays.asList(extractCPFromPathingJar(element).split(File.pathSeparator)));
                    continue;
                }

				/*
					as the path separator in the manifest is just spaces " ", we need
					to escape the paths to URL to avoid issues in Windows where path might
					have spaces...
				 */

                element = element.replace("\\","/");
                element = element.replace(" ","%20");
                if(!element.startsWith("/")){
                    element = "/" + element;
                }


                if(!element.endsWith(".jar")){
                    //that means it is a folder. we need to make sure it does end with a "/"
                    if(!element.endsWith("/")){
                        element += "/";
                    }
                }


                escaped.append(element+ " ");
                //escaped.append(URLEncoder.encode(element,"UTF-8")+ " ");
            } catch (Exception e) {
                logger.error("Problem when encoding '"+element+"': "+e.toString());
                return classpath;
            }
        }

        String jarLocation = null;
        try {
            File tmp = File.createTempFile(PATHING_JAR_PREFIX,".jar");
            tmp.deleteOnExit();
            jarLocation = tmp.getAbsolutePath();

            Manifest m = new Manifest();
            m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            m.getMainAttributes().put(Attributes.Name.CLASS_PATH, escaped.toString());
            JarOutputStream out = new JarOutputStream(new FileOutputStream(tmp), m);
            out.flush();
            out.close();

            if(! RuntimeSettings.isRunningASystemTest) {
                //the location is likely non-deterministic
                logger.info("Created jar path at {} with CP: {}", jarLocation, escaped.toString());
            }

        } catch (Exception e) {
            logger.error("Cannot create pathing jar: "+e.toString());
            return classpath;
        }

        return jarLocation;
    }
}
