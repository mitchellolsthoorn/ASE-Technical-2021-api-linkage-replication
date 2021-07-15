package org.evomaster.core.search.modellearning.learning

import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.modellearning.score.BaseScoring
import java.util.*
import kotlin.collections.HashMap

/**
 * This class describes the abstract Bayesian Network learning class.
 *
 * @author Dimitri Stallenberg
 *
 * @param scoringFunction the scoring function to use
 * @param randomness the randomness object to use
 * @param populationSize the population size to use
 * @param maxParents the maximum amount of parents a variable is allowed to have
 * @param mVariables the amount of variables the Bayesian Network contains
 * @param rValues the amount of values each variable can take on
 */
abstract class BaseLearning(
        private val scoringFunction: BaseScoring,
        val randomness: Randomness,
        val mVariables: Int,
        val rValues: Int,
        val populationSize: Int = 100,
        private val maxParents: Int = 2
) {

    /**
     * Performs search to find a model that describes the data.
     *
     * @param maxGenerations the maximum amount of generations to search
     * @param data the data to find a model for
     *
     * @return the found optimal solution and its fitness value
     */
    abstract fun search(maxGenerations: Int, data: Array<IntArray>): BuildingBlockModel

    /**
     * Creates a population of random solutions.
     *
     * @param size the size of the population to create
     * @param data the data to evaluate solutions on
     *
     * @return the created population
     */
    fun createPopulation(size: Int, data: Array<IntArray>): Array<Pair<IntArray, Double>> {
        val solutionLength = mVariables * (mVariables - 1) / 2

        val population = MutableList(0) { Pair(IntArray(0), 0.0) }

        for (i in 0 until size) {
            var solution = IntArray(solutionLength) { 0 }

            for (j in 0 until solutionLength) {
                solution[j] = randomness.nextInt(3)
            }

            solution = dagRepairOperator(solution)

            population.add(Pair(solution, evaluateFitness(solution, data)))
        }

        return population.toTypedArray()
    }

    /**
     * Creates a diverse population that contains contains all possible connections atleast once.
     *
     * @param size the size of the population to create
     * @param data the dataset to evaluate solutions on
     *
     * @return the diverse population
     */
    fun createDiversePopulation(size: Int, data: Array<IntArray>): Array<Pair<IntArray, Double>> {
        val solutionLength = mVariables * (mVariables - 1) / 2

        if (size < solutionLength) {
            // Population size is too small to create a diverse population
            return createPopulation(size, data)
        }

        val population = MutableList(0) { Pair(IntArray(0), 0.0) }

        for (i in 0 until solutionLength) {
            val solution = IntArray(solutionLength) { 0 }

            for (j in 0 until solutionLength) {
                solution[j] = 0
                if (i == j) {
                    solution[j] = 2
                }
            }

            population.add(Pair(solution, evaluateFitness(solution, data)))
        }

        // Use regular randomized population for the leftover space
        population.addAll(createPopulation(size - solutionLength, data))

        return population.toTypedArray()
    }


    /**
     * Evaluates the fitness of a given solution on the dataset.
     *
     * @param solution the solution to evaluate
     * @param data the dataset to evaluate the solution on
     *
     * @return the fitness value
     */
    fun evaluateFitness(solution: IntArray, data: Array<IntArray>): Double {
        return scoringFunction.score(createParentMatrix(solution), data)
    }

    /**
     * Find the best solution in the given population.
     *
     * @param population the population to find the best solution of
     *
     * @return the best solution in the population
     */
    fun getBest(population: Array<Pair<IntArray, Double>>): Pair<IntArray, Double> {
        var best = population[0]
        for (solution in population) {
            if (best.second < solution.second) {
                best = solution
            }
        }
        return best
    }

    /**
     * Creates a child of parent x and the parts of donor d specified in the ithSubsetFOS.
     *
     * @param x the original parent
     * @param d the donor parent
     * @param ithSubsetFOS a list of variable indices to take from the donor parent
     *
     * @return the resulting child solution
     */
    fun copyValues(x: IntArray, d: IntArray, ithSubsetFOS: IntArray): IntArray {
        val o = x.clone()

        for (k in ithSubsetFOS) {
            o[k] = d[k]
        }

        return dagRepairOperator(o)
    }

    /**
     * If the solution doesn't result in a DAG this function will repair it by removing the connection that made the graph cyclic.
     * Additionally this function calls the curbAmountOfParents function to reduce the amount of parents.
     * This function should be applied to all newly created solutions.
     *
     * @param solution the solution to check/repair
     *
     * @return the repaired solution
     */
    fun dagRepairOperator(solution: IntArray): IntArray {
        // TODO maybe parentCurbing is better after
        val parentMatrix = curbAmountOfParents(createParentMatrix(solution))

        for (root in 0 until mVariables) {
            val nodesToVisit = Stack<Int>()
            val visited = Array(mVariables) { false }

            nodesToVisit.push(root)

            var currentNode: Int
            while (nodesToVisit.isNotEmpty()) {
                if (visited[nodesToVisit.peek()]) {
                    nodesToVisit.pop()
                    continue
                }

                currentNode = nodesToVisit.pop()
                visited[currentNode] = true

                // Get children
                for (j in 0 until mVariables) {
                    // Check if currentNode is a parent of j
                    if (parentMatrix[currentNode][j] == 2) {
                        if (visited[j]) {
                            // remove connection
                            parentMatrix[currentNode][j] = 0
                            parentMatrix[j][currentNode] = 0
                            continue
                        }

                        nodesToVisit.push(j)
                    }
                }
            }
        }

        return extractSolution(parentMatrix)
    }

    /**
     * Makes sure that the amount of parents per variable stays below the maxParents parameter.
     * If variable X has more parents than allowed this function removes a parent randomly until the amount of parents is equal to maxParents
     *
     * @param parentMatrix a matrix containing the connections between variables
     *
     * @return the modified parentMatrix
     */
    fun curbAmountOfParents(parentMatrix: Array<IntArray>): Array<IntArray> {
        for (i in 0 until mVariables) {
            var parents = 0

            val parentIndices = mutableListOf<Int>()

            for (j in 0 until mVariables) {
                // Check if currentNode is a parent of j
                if (parentMatrix[i][j] == 2) {
                    parents += 1
                    parentIndices.add(j)
                }
            }

            while (parents > maxParents) {
                val j = randomness.choose(parentIndices)
                if (parentMatrix[i][j] == 2) {
                    parentMatrix[i][j] = 0
                    parentMatrix[j][i] = 0
                    parents -= 1
                }
            }

        }

        return parentMatrix
    }

    /**
     * Creates a parentMatrix based on the given solution.
     *
     * @param solution the solution to generate a parentMatrix from
     *
     * @return the generated parentMatrix
     */
    fun createParentMatrix(solution: IntArray): Array<IntArray> {
        // Create a parent matrix from the solution
        val parentMatrix = Array(mVariables) {
            IntArray(mVariables)
        }

        var skipped = 0
        for (i in 0 until mVariables) {
            skipped += i + 1
            for (j in (i + 1) until mVariables) {
                if (solution[i * mVariables + j - skipped] == 2) {
                    parentMatrix[i][j] = 2
                    parentMatrix[j][i] = 1
                } else if (solution[i * mVariables + j - skipped] == 1) {
                    parentMatrix[i][j] = 1
                    parentMatrix[j][i] = 2
                }
            }
        }

        return parentMatrix
    }

    /**
     * Basically the inverse of the createParentMatrix function.
     *
     * @param parentMatrix to extract the solution from
     *
     * @return the solution embedded in the parentMatrix
     */
    fun extractSolution(parentMatrix: Array<IntArray>): IntArray {
        val solution = IntArray((0.5 * mVariables * (mVariables - 1)).toInt())

        var skipped = 0
        for (i in 0 until mVariables) {
            skipped += i + 1
            for (j in (i + 1) until mVariables) {
                solution[i * mVariables + j - skipped] = parentMatrix[i][j]
            }
        }

        return solution
    }

    /**
     * Checks whether 2 separate solutions are equal.
     *
     * @param solutionA the first solution
     * @param solutionB the second solution to compare with
     *
     * @return if solutionA is equal to solutionB
     */
    fun compareSolutions(solutionA: IntArray, solutionB: IntArray): Boolean {
        for (i in 0 until mVariables) {
            if (solutionA[i] != solutionB[i]) {
                return false
            }
        }
        return true
    }
}