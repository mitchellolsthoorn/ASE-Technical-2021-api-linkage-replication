package org.evomaster.core.search.modellearning.score

enum class ModelScoringFunction {
    BDeu,
    BDs,
    KLD,
    LL,
    MDL;

    companion object {
        fun getFunction(enumValue: ModelScoringFunction, mVariables: Int, rValues: Int): BaseScoring {
            return when (enumValue) {
                BDeu -> {
                    BDeuScore(mVariables, rValues)
                }
                BDs -> {
                    BDsScore(mVariables, rValues)
                }
                KLD -> {
                    KLDivergenceScore(mVariables, rValues)
                }
                LL -> {
                    LLScore(mVariables, rValues)
                }
                MDL -> {
                    MDLScore(mVariables, rValues)
                }
                else -> {
                    throw Exception("Invalid enum value. Something went terribly wrong here.")
                }
            }
        }
    }
}