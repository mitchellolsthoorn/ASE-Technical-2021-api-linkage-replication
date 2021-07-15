package org.evomaster.core.search.modellearning.learning

import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.modellearning.linkageTreeFOSLearning
import org.evomaster.core.search.modellearning.getIthSubsetOfFOS
import org.evomaster.core.search.modellearning.score.BaseScoring
import kotlin.math.floor
import kotlin.math.log10

/**
 * This class implements the GOMEA Bayesian Network learning class.
 * It extends the BNLearning class.
 * It is based on the following article:
 * https://dl.acm.org/doi/10.1145/3205455.3205502
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
class GOMEA(
        scoringFunction: BaseScoring,
        randomness: Randomness,
        mVariables: Int,
        rValues: Int,
        populationSize: Int,
        maxParents: Int
): BaseLearning(
        scoringFunction,
        randomness,
        mVariables,
        rValues,
        populationSize,
        maxParents
) {

    private val maxOptimalMixingTries = 500

    override fun search(maxGenerations: Int, data: Array<IntArray>): BuildingBlockModel {
        var population = createDiversePopulation(populationSize, data)

        var generation = 0

        // Amount of generations without progress
        var tnis = 0

        val bestSolutions = mutableListOf<Pair<IntArray, Double>>()
        bestSolutions.add(getBest(population))

        var bestSolution = bestSolutions[0]

        while (generation < maxGenerations) {
            val solutionLength = mVariables * (mVariables - 1) / 2

            val fos = linkageTreeFOSLearning(solutionLength, population, randomness)

            val newPopulation = Array(populationSize) {
                genepoolOptimalMixing(population[it], data, population, tnis, bestSolution, fos)
            }

            population = newPopulation

            generation += 1

            // Get best etc
            bestSolutions.add(getBest(population))

            // Check for progress
            if (bestSolutions[generation].second > bestSolutions[generation - 1].second) {
                if (bestSolutions[generation].second > bestSolution.second) {
                    bestSolution = bestSolutions[generation]
                }
                tnis = 0
            } else {
                tnis += 1
            }

            var allSame = true
            val first = population[0].first
            for (p in population) {
                if (!first.contentEquals(p.first)) {
                    allSame = false
                    break
                }
            }

            if (allSame) {
                break
            }
        }

        return BuildingBlockModel(createParentMatrix(bestSolution.first))
    }


    /**
     * Taken from pseudocode in https://dl.acm.org/doi/abs/10.1145/3205455.3205502
     *
     * GenepoolOptimalMixing (GOM)
     *
     * @param solution the solution to mix
     * @param data the dataset to evaluate new solution on
     * @param population the current population
     * @param tnis the amount of generations without improvement
     * @param xelitist the best solution so far
     * @param fos the Family Of Subsets to use for mixing
     *
     * @return the newly mixed solution
     */
    fun genepoolOptimalMixing(
            solution: Pair<IntArray, Double>,
            data: Array<IntArray>,
            population: Array<Pair<IntArray, Double>>,
            tnis: Int,
            xelitist: Pair<IntArray, Double>,
            fos: MutableList<MutableList<Int>>
    ): Pair<IntArray, Double> {
        var o = solution.first.clone()
        var b = solution.first.clone()

        var fo = solution.second
        var fb = solution.second

        var changed = false

        val solutionIndex = population.indexOf(solution)

        var turn = 0

        for (i in randomness.shuffle(IntRange(0, fos.size - 1))) {
            if (turn > maxOptimalMixingTries) {
                break
            }

            turn += 1


            var donorIndex = randomness.nextInt(populationSize - 2)

            if (donorIndex >= solutionIndex) {
                donorIndex += 1
            }

            val donor = population[donorIndex]

            o = copyValues(o, donor.first, fos[i].toIntArray())

            if (!compareSolutions(o, b)) {
                fo = evaluateFitness(o, data)

                if (fo >= fb) {
                    b = o
                    fb = fo
                    changed = true
                } else {
                    o = b
                    fo = fb
                }
            }
        }

        if (!changed || tnis > 1 + floor(log10(populationSize.toDouble()))) {
            changed = false
            for (i in randomness.shuffle(IntRange(0, fos.size - 1))) {
                o = copyValues(o, xelitist.first, getIthSubsetOfFOS(i))

                if (!compareSolutions(o, b)) {
                    fo = evaluateFitness(o, data)

                    if (fo > fb) {
                        b = o
                        fb = fo
                        changed = true
                    } else {
                        o = b
                        fo = fb
                    }
                }
                if (changed) {
                    break
                }
            }
        }

        if (!changed) {
            o = xelitist.first
            fo = xelitist.second
        }
        return Pair(o, fo)
    }
}