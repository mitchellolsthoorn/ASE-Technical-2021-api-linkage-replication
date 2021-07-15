package org.evomaster.core.search.modellearning.learning

import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.modellearning.score.*

enum class CrossoverModel {
    NONE,
    GOMEA,
    AC;

    companion object {
        fun getModel(enumValue: CrossoverModel,
                     scoreFunction: BaseScoring,
                     randomness: Randomness,
                     mVariables: Int,
                     rValues: Int,
                     popSize: Int,
                     maxParents: Int
        ): BaseLearning? {
            return when (enumValue) {
                NONE -> {
                    null
                }
                GOMEA -> {
                    GOMEA(scoreFunction, randomness, mVariables, rValues, popSize, maxParents)
                }
                AC -> {
                    AC(scoreFunction, randomness, mVariables, rValues, popSize, maxParents)
                }
                else -> {
                    throw Exception("Invalid enum value. Something went terribly wrong here.")
                }
            }
        }
    }
}
