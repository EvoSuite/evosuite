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
package com.examples.with.different.packagename;

import java.io.File;

public class Person {

    private final String name;
    private final String address;
    private final int age;
    private final File profile;
    private final String email;

    public Person(String name, String address, int age, File profile, String email){
        this.name = name;
        this.address = address;
        this.age = age;
        this.profile = profile;
        this.email = email;

        checkExtension();
        //validateEmail();
    }

    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public int getAge(){
        return age;
    }

    public File getProfile(){
        return profile;
    }

    public String getEmail(){
        return email;
    }

    private void validateEmail() {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    private void checkExtension(){
        if(!profile.getName().endsWith(".pdf")){
            throw new IllegalArgumentException("Invalid file extension. Please provide a JPG file.");
        }
    }
}
