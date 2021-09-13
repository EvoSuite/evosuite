/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;

import org.evosuite.Properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Construction of a grid and the neighbourhood models
 *
 * @author Nasser Albunian
 */
public class Neighbourhood<T extends Chromosome<T>> implements NeighborModels<T>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The population size
     **/
    private final int population_size;

    /**
     * Position values of different neighbourhood based on the direction
     **/
    private int _L, _R, _N, _S, _W, _E, _NE, _NW, _SE, _SW, _NN, _SS, _EE, _WW;

    /**
     * An array that represents the grid
     **/
    int[][] neighbour;

    /**
     * Number of chromosomes per one row of a grid
     **/
    int columns;

    /**
     * Collection of cells will be returned by different models of neighbourhood
     */
    private final List<T> chromosomes = new ArrayList<>();

    public Neighbourhood(int populationSize) {

        population_size = populationSize;

        neighbour = new int[population_size][0];

        columns = (int) Math.sqrt(population_size);

        constructNeighbour();
    }

    /**
     * Construct the grid and define positions of neighbours for each individual
     */
    public void constructNeighbour() {

        for (int i = 0; i < population_size; i++) {
            neighbour[i] = new int[8];
        }

        for (int i = 0; i < population_size; i++) {

            //~~~~ NORTH ~~~~//
            if (i > columns - 1) {
                neighbour[i][Positions.N.ordinal()] = i - columns;
            } else {
                int mod = population_size % columns;
                if (mod != 0) {
                    int thisPosition = ((i - columns + population_size) % population_size);
                    if (i == 0) {
                        neighbour[i][Positions.N.ordinal()] = population_size - (mod);
                    } else {
                        if (mod > 1) {
                            if (i >= mod) {
                                neighbour[i][Positions.N.ordinal()] = thisPosition - mod;
                            } else {
                                neighbour[i][Positions.N.ordinal()] = thisPosition + 1;
                            }
                        } else {
                            neighbour[i][Positions.N.ordinal()] = thisPosition - 1;
                        }
                    }
                } else {
                    neighbour[i][Positions.N.ordinal()] = (i - columns + population_size) % population_size;
                }
            }

            //~~~~ SOUTH ~~~~//
            int thisPosition = (i + columns) % population_size;
            if (population_size % columns != 0 && i + columns >= population_size) {
                neighbour[i][Positions.S.ordinal()] = i % columns;
            } else {
                neighbour[i][Positions.S.ordinal()] = thisPosition;
            }

            //~~~~ EAST ~~~~//
            if ((i + 1) % columns == 0) {
                neighbour[i][Positions.E.ordinal()] = i - (columns - 1);
            } else {
                if (population_size % columns != 0 && i == population_size - 1) {
                    neighbour[i][Positions.E.ordinal()] = (i % columns) + 1;
                } else {
                    neighbour[i][Positions.E.ordinal()] = i + 1;
                }
            }

            //~~~~ WEST ~~~~//
            if (i % columns == 0) {
                int westPosition = i + (columns - 1);
                if (westPosition >= population_size) {
                    neighbour[i][Positions.W.ordinal()] = neighbour[i][Positions.E.ordinal()];
                } else {
                    neighbour[i][Positions.W.ordinal()] = westPosition;
                }
            } else {
                neighbour[i][Positions.W.ordinal()] = i - 1;
            }
        }

        //~~~~ NW, SW, NE, SE ~~~~//
        for (int i = 0; i < population_size; i++) {
            neighbour[i][Positions.NW.ordinal()] = neighbour[neighbour[i][Positions.N.ordinal()]][Positions.W.ordinal()];

            neighbour[i][Positions.SW.ordinal()] = neighbour[neighbour[i][Positions.S.ordinal()]][Positions.W.ordinal()];

            neighbour[i][Positions.NE.ordinal()] = neighbour[neighbour[i][Positions.N.ordinal()]][Positions.E.ordinal()];

            neighbour[i][Positions.SE.ordinal()] = neighbour[neighbour[i][Positions.S.ordinal()]][Positions.E.ordinal()];
        }

    }

    /**
     * Retrieve neighbours of a chromosome according to the ring topology (i.e. 1D)
     *
     * @param collection The current collection of chromosomes
     * @param position   The position of a chromosome which its neighbours will be retrieved
     * @return collection of neighbours
     */
    public List<T> ringTopology(List<T> collection, int position) {

        if (position - 1 < 0) {
            _L = collection.size() - 1;
        } else {
            _L = position - 1;
        }

        if (position + 1 > collection.size() - 1) {
            _R = 0;
        } else {
            _R = position + 1;
        }

        chromosomes.add(collection.get(_L));
        chromosomes.add(collection.get(position));
        chromosomes.add(collection.get(_R));

        return chromosomes;
    }

    /**
     * Retrieve neighbours of a chromosome according to the linear five model (i.e. L5)
     *
     * @param collection The current collection of chromosomes
     * @param position   The position of a chromosome which its neighbours will be retrieved
     * @return collection of neighbours
     */
    public List<T> linearFive(List<T> collection, int position) {
        _N = neighbour[position][Positions.N.ordinal()];
        _S = neighbour[position][Positions.S.ordinal()];
        _E = neighbour[position][Positions.E.ordinal()];
        _W = neighbour[position][Positions.W.ordinal()];

        chromosomes.add(collection.get(_N));
        chromosomes.add(collection.get(_S));
        chromosomes.add(collection.get(_E));
        chromosomes.add(collection.get(_W));
        chromosomes.add(collection.get(position));

        return chromosomes;
    }

    /**
     * Retrieve neighbours of a chromosome according to the compact nine model (i.e. C9)
     *
     * @param collection The current collection of chromosomes
     * @param position   The position of a chromosome which its neighbours will be retrieved
     * @return collection of neighbours
     */
    public List<T> compactNine(List<T> collection, int position) {

        _N = neighbour[position][Positions.N.ordinal()];
        _S = neighbour[position][Positions.S.ordinal()];
        _E = neighbour[position][Positions.E.ordinal()];
        _W = neighbour[position][Positions.W.ordinal()];
        _NW = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.W.ordinal()];
        _SW = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.W.ordinal()];
        _NE = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.E.ordinal()];
        _SE = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.E.ordinal()];

        chromosomes.add(collection.get(_N));
        chromosomes.add(collection.get(_S));
        chromosomes.add(collection.get(_E));
        chromosomes.add(collection.get(_W));
        chromosomes.add(collection.get(_NW));
        chromosomes.add(collection.get(_SW));
        chromosomes.add(collection.get(_NE));
        chromosomes.add(collection.get(_SE));
        chromosomes.add(collection.get(position));

        return chromosomes;
    }

    /**
     * Retrieve neighbours of a chromosome according to the linear compact thirteen (i.e. C13)
     *
     * @param collection The current collection of chromosomes
     * @param position   The position of a chromosome which its neighbours will be retrieved
     * @return collection of neighbours
     */
    public List<T> compactThirteen(List<T> collection, int position) {
        _N = neighbour[position][Positions.N.ordinal()];
        _S = neighbour[position][Positions.S.ordinal()];
        _E = neighbour[position][Positions.E.ordinal()];
        _W = neighbour[position][Positions.W.ordinal()];
        _NW = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.W.ordinal()];
        _SW = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.W.ordinal()];
        _NE = neighbour[neighbour[position][Positions.N.ordinal()]][Positions.E.ordinal()];
        _SE = neighbour[neighbour[position][Positions.S.ordinal()]][Positions.E.ordinal()];
        _NN = neighbour[_N][Positions.N.ordinal()];
        _SS = neighbour[_S][Positions.S.ordinal()];
        _EE = neighbour[_E][Positions.E.ordinal()];
        _WW = neighbour[_W][Positions.W.ordinal()];

        chromosomes.add(collection.get(_N));
        chromosomes.add(collection.get(_S));
        chromosomes.add(collection.get(_E));
        chromosomes.add(collection.get(_W));
        chromosomes.add(collection.get(_NW));
        chromosomes.add(collection.get(_SW));
        chromosomes.add(collection.get(_NE));
        chromosomes.add(collection.get(_SE));
        chromosomes.add(collection.get(_NN));
        chromosomes.add(collection.get(_SS));
        chromosomes.add(collection.get(_EE));
        chromosomes.add(collection.get(_WW));
        chromosomes.add(collection.get(position));

        return chromosomes;
    }

    /**
     * Retrieve neighbours of a chromosome
     *
     * @param current_pop The current population
     * @param chromosome  The chromosome which its neighbours will be retrieved
     * @return neighbours as a collection
     */
    public List<T> getNeighbors(List<T> current_pop, int chromosome) {

        switch (Properties.MODEL) {
            case ONE_DIMENSION:
                return this.ringTopology(current_pop, chromosome);
            case LINEAR_FIVE:
                return this.linearFive(current_pop, chromosome);
            case COMPACT_NINE:
                return this.compactNine(current_pop, chromosome);
            case COMPACT_THIRTEEN:
                return this.compactThirteen(current_pop, chromosome);
            default:
                return this.linearFive(current_pop, chromosome);
        }
    }

}
