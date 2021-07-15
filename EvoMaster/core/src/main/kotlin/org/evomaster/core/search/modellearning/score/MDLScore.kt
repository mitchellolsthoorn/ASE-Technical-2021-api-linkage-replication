package org.evomaster.core.search.modellearning.score

import kotlin.math.log10
import kotlin.math.log2

/**
 * This class implements the Minimum description length scoring function.
 * It extends the BaseScoring class.
 *
 * @author Dimitri Stallenberg
 *
 * @param mVariables the amount of variables
 * @param rValues the amount of values each variable can take
 */
class MDLScore(mVariables: Int, rValues: Int): BaseScoring(mVariables, rValues) {
    override fun score(parentMatrix: Array<IntArray>, data: Array<IntArray>): Double {
        var score = 0.0

        val configsQ = calcConfigsQ(parentMatrix)


        for (i in 0 until mVariables) {

            for (j in 0 until configsQ[i]) {
                var Nij = 0
                for (k in 0 until rValues) {
                    val Nijk = calculateNijk(i, j, k, parentMatrix, data)
                    Nij += Nijk
                }

                for (k in 0 until rValues) {
                    val Nijk = calculateNijk(i, j, k, parentMatrix, data)

                    score += Nijk * log10((Nijk.toDouble() + 1.0) / (Nij.toDouble() + 1.0))

                }
            }

            score += (log2(data.size.toDouble()) / 2) * (rValues - 1) * configsQ[i]
        }

        return score
    }


}