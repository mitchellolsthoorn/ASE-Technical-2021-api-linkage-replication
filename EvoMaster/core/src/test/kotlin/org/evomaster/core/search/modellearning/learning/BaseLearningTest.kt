package org.evomaster.core.search.modellearning.learning

import org.junit.jupiter.api.Test

internal abstract class BaseLearningTest {

    lateinit var learning: BaseLearning

    @Test
    fun createPopulation() {
        val size = 10

        val data = arrayOf(
                intArrayOf(0, 1, 1, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 1, 1, 1),
                intArrayOf(0, 1, 0, 0)
        )

        val pop = learning.createPopulation(size, data)

        assert(pop.size == size)
    }

    @Test
    fun createDiversePopulation() {
        val size = 10

        val data = arrayOf(
                intArrayOf(0, 1, 1, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 1, 1, 1),
                intArrayOf(0, 1, 0, 0)
        )

        val pop = learning.createDiversePopulation(size, data)

        assert(pop.size == size)
    }

    @Test
    fun createDiversePopulationSmallPop() {
        val size = 3

        val data = arrayOf(
                intArrayOf(0, 1, 1, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 1, 1, 1),
                intArrayOf(0, 1, 0, 0)
        )

        val pop = learning.createDiversePopulation(size, data)

        assert(pop.size == size)
    }

    @Test
    fun evaluateFitness() {
    }

    @Test
    fun getBest() {
    }

    @Test
    fun copyValues() {
    }

    @Test
    fun dagRepairOperator() {
    }

    @Test
    fun curbAmountOfParents() {
    }

    @Test
    fun createParentMatrix() {
    }

    @Test
    fun extractSolution() {
    }

    @Test
    fun compareSolutions() {
    }

    @Test
    fun getRandomness() {
    }

    @Test
    fun getMVariables() {
    }

    @Test
    fun getRValues() {
    }

    @Test
    fun getPopulationSize() {
    }
}