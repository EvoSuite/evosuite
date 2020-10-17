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
package org.ant_project_example;

public class CharacterCounter {

  private int numLetters;

  private int numDigits;

  private int other;

  public CharacterCounter() {
    this.numLetters = 0;
    this.numDigits = 0;
    this.other = 0;
  }

  public void processString(String str) {
    for (char c : str.toCharArray()) {
      if ('A' <= c && 'Z' >= c) {
        this.numLetters += 2; /* FAULT */
      } else if ('a' <= c && 'z' >= c) {
        this.numLetters += 1;
      } else if ('0' <= c && '9' >= c) {
        this.numDigits += 1;
      } else {
        this.other += 1;
      }
    }
  }

  public int getNumLetters() {
    return this.numLetters;
  }

  public int getNumDigits() {
    return this.numDigits;
  }

  public int getNumOtherCharacters() {
    return this.other;
  }

}
