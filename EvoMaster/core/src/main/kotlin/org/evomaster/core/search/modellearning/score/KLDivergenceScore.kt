package org.evomaster.core.search.modellearning.score

import kotlin.math.log2

/**
 * This class implements the Kullback-Leibler divergence scoring function.
 * It extends the BaseScoring class.
 *
 * This function is not described in articles as far as I know.
 * It is based on the Kullback-Leibler divergence between variables.
 * Whenever 2 variables have a dependency on each-other the KL-divergence is calculated and subtracted from the total score.
 * The best possible KL-divergence score equal 0 and the worst equals 1 and calculates the correlation between variables.
 * To penalize networks with very few connections 0.25 is subtracted for every pair of variables without a connection.
 *
 * @author Dimitri Stallenberg
 *
 * @param mVariables the amount of variables
 * @param rValues the amount of values each variable can take
 */
class KLDivergenceScore(mVariables: Int, rValues: Int): BaseScoring(mVariables, rValues) {

    override fun score(parentMatrix: Array<IntArray>, data: Array<IntArray>): Double {
        var score = 0.0

        for (i in 0 until mVariables) {
            for (j in 0 until mVariables) {
                if (parentMatrix[i][j] == 2) {
                    score -= KLDivergence(i, j, data)
                } else if (parentMatrix[i][j] == 1) {
                    score -= KLDivergence(j, i, data)
                } else if (parentMatrix[i][j] == 0) {
                    score -= 0.25 // (1 - KLDivergence(j, i, data)) //
                }
            }
        }

        return score
    }

    /**
     * Calculates the KL-divergence of 2 variables.
     *
     * @param indexX the index of the first variable
     * @param indexY the index of the second variable
     * @param data the data to evaluate the model on
     *
     * @return the KL-divergence
     */
    private fun KLDivergence(indexX: Int, indexY: Int, data: Array<IntArray>): Double {
        val frequencyX = IntArray(rValues)
        val frequency = Array(rValues) {
            IntArray(rValues)
        }

        for (d in data) {
            frequencyX[d[indexX]] += 1
            frequency[d[indexX]][d[indexY]] += 1
        }

        var Hx = 0.0
        var Hxy = 0.0

        for (x in 0 until rValues) {
            val px = frequencyX[x].toDouble() / data.size

            if (px == 0.0) {
                continue
            }

            for (y in 0 until rValues) {
                val pxy = frequency[x][y].toDouble() / data.size

                if (pxy == 0.0) {
                    continue
                }

                Hxy -= pxy * log2(pxy)
            }
            Hx -= px * log2(px)

        }

        return Hxy - Hx
    }
}