package org.evomaster.core.search.modellearning.score

/**
 * This class describes the abstract BDScoring class it extends the BaseScoring class.
 * Scoring function in the Bayesian Dirichlet family should extend this class.
 *
 * @author Dimitri Stallenberg
 *
 * @param mVariables the amount of variables
 * @param rValues the amount of values each variable can take
 */
abstract class BDScoring(mVariables: Int, rValues: Int): BaseScoring(mVariables, rValues) {

    /**
     * Calculates the Gamma function for a given x.
     * The Gamma function is bassically the factorial function.
     * However the Gamma function is also defined for non-integer values.
     *
     * @param x the value to calculate the gamma value of.
     *
     * @return the gamma value of x
     */
    fun gamma(x: Double): Double {
        var xx = x
        val p = doubleArrayOf(
                0.99999999999980993,
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.13857109526572012,
                9.9843695780195716e-6,
                1.5056327351493116e-7
        )
        val g = 7
        if (xx < 0.5) return Math.PI / (Math.sin(Math.PI * xx) * gamma(1.0 - xx))
        xx--
        var a = p[0]
        val t = xx + g + 0.5
        for (i in 1 until p.size) a += p[i] / (xx + i)
        return Math.sqrt(2.0 * Math.PI) * Math.pow(t, xx + 0.5) * Math.exp(-t) * a
    }
}