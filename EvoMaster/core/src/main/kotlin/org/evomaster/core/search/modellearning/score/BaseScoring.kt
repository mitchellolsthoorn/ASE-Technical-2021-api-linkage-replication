package org.evomaster.core.search.modellearning.score

import java.lang.Exception

/**
 * This class describes the abstract BaseScoring class.
 *
 * @author Dimitri Stallenberg
 *
 * @param mVariables the amount of variables
 * @param rValues the amount of values each variable can take
 */
abstract class BaseScoring(val mVariables: Int, val rValues: Int) {

    /**
     * Scores a given solution (parentMatrix) based on the data.
     *
     * @param parentMatrix the solution to score
     * @param data the data to score the solution on
     *
     * @return the fitness value of the solution
     */
    abstract fun score(parentMatrix: Array<IntArray>, data: Array<IntArray>): Double

    /**
     * Calculate the amount of parent configurations per variable.
     *
     * @param parentMatrix the matrix describing the relations between variables
     *
     * @return an array containing the amount of possible parent configurations for each variable
     */
    fun calcConfigsQ(parentMatrix: Array<IntArray>): IntArray {
        val q = IntArray(mVariables);

        for (i in 0 until mVariables) {
            val archs = parentMatrix[i]
            var configurations = 1

            for (arch in archs) {
                if (arch == 2) {
                    // In our case the no. of values per variable is the same for each variable
                    configurations *= rValues
                }
            }
            q[i] = configurations
        }

        return q
    }

    /**
     * Calculates the number of instances in the data where
     * the variable X_varIndex has its k-th value
     * and the parent variables of X_varIndex take their j-th configuration.
     *
     * @param varIndex the index of the variable X
     * @param j the index of the configuration under inspection
     * @param k the index of the value of X under inspection
     * @param parentMatrix the entire parent matrix, i.e. the model/state that is evaluated
     * @param data the data the model is evaluated on
     *
     * @return number of instances
     */
    fun calculateNijk(varIndex: Int, j: Int, k: Int, parentMatrix: Array<IntArray>, data: Array<IntArray>): Int {
        var instances = 0

        val archs = parentMatrix[varIndex]
        val parents = mutableListOf<Int>()

        // Find the parents of the variable at varIndex
        for (archIndex in archs.indices) {
            // Check if the variable at archIndex is a parent of the variable at varIndex
            if (archs[archIndex] == 2) {
                parents.add(archIndex)
            }
        }

        val jthConfig = calcJthConfig(j, parents)

        for (datapoint in data) {
            // Check if the variable X_varIndex has its k-th value.
            // As the values of the variables are encoded as 0 to rValues - 1 and k also goes from 0 to rValues - 1
            // we can simply check if the the variable at varIndex is equal to k.
            if (datapoint[varIndex] != k) {
                continue
            }

            var allParentsCorrect = true

            // Check if the parent variables take their j-th configuration.
            // Since we already calculated the j-th config we can simply check whether the datapoint has the same values for the parents.
            for (parentIndex in 0 until parents.size) {
                val parentIndexInData = parents[parentIndex]

                if (datapoint[parentIndexInData] != jthConfig[parentIndex]) {
                    allParentsCorrect = false
                    break
                }
            }

            if (allParentsCorrect) {
                instances += 1
            }
        }


        return instances
    }

    /**
     * Calculates the j-th configuration the given parents can be in.
     *
     * @param j the index of the wanted configuration
     * @param parents the parents to calculate the j-th config of
     *
     * @return the j-th config
     */
    private fun calcJthConfig(j: Int, parents: MutableList<Int>): IntArray {
        val parentConfig = IntArray(parents.size)
        var configNumber = 0

        while (true) {
            if (configNumber == j) {
                break
            }

            configNumber += 1

            var endReached = true

            for (parentIndex in 0 until parents.size) {
                if (parentConfig[parentIndex] == rValues) {
                    parentConfig[parentIndex] = 0
                } else {
                    parentConfig[parentIndex] += 1
                    endReached = false
                    break
                }
            }

            if (endReached) {
                throw Exception("Something went wrong! Reached final configuration!")
            }
        }

        return parentConfig
    }
}