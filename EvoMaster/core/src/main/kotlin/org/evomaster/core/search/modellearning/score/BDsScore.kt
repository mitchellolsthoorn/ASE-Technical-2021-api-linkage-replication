package org.evomaster.core.search.modellearning.score

import kotlin.math.ln

/**
 * This class implements the Bayesian Dirichlet sparse scoring function.
 * It extends the BDScoring class.
 * It is based on the following article:
 * https://arxiv.org/abs/1708.00689
 *
 * @author Dimitri Stallenberg
 *
 * @param mVariables the amount of variables
 * @param rValues the amount of values each variable can take
 */
class BDsScore(mVariables: Int, rValues: Int): BDScoring(mVariables, rValues) {

    override fun score(parentMatrix: Array<IntArray>, data: Array<IntArray>): Double {
        val configsQ = calcConfigsQ(parentMatrix)

        var totalScore = 0.0
        for (i in 0 until mVariables) {
            val score = calcPartialScoreG(i, parentMatrix, configsQ[i], data)
            totalScore += score
        }

        return totalScore
    }

    /**
     * Calculate the BDeuScore of the i-th variable.
     *
     * @param i the index of the i-th variable
     * @param parentMatrix the matrix describing the relations between variables
     * @param confQi the amount of parent configurations of the i-th variable
     * @param data the data to score the i-th variable on
     *
     * @return the score of the i-th variable
     */
    private fun calcPartialScoreG(i: Int, parentMatrix: Array<IntArray>, confQi: Int, data: Array<IntArray>): Double {
        var partialScore = 0.0

        val NPrime = 1

        val aijk = NPrime.toDouble() / (confQi * rValues)
        val aij = NPrime.toDouble() / confQi

        val gammaAij = gamma(aij)
        val gammaAijk = gamma(aijk)

        for (j in 0 until confQi) {
            var Nij = 0
            var subScore = 0.0
            for (k in 0 until rValues) {
                val Nijk = calculateNijk(i, j, k, parentMatrix, data)
                subScore += ln(gamma(aijk + Nijk) / gammaAijk)
                Nij += Nijk
            }

            if (Nij <= 0) {
                continue
            }

            partialScore += subScore
            partialScore += ln(gammaAij / gamma(aij + Nij))
        }

        return partialScore
    }
}