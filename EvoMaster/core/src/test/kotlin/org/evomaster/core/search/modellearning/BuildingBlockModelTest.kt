package org.evomaster.core.search.modellearning

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.netflix.governator.guice.LifecycleInjector
import org.evomaster.core.BaseModule
import org.evomaster.core.EMConfig
import org.evomaster.core.search.algorithms.MioAlgorithm
import org.evomaster.core.search.algorithms.onemax.OneMaxIndividual
import org.evomaster.core.search.algorithms.onemax.OneMaxModule
import org.evomaster.core.search.algorithms.onemax.OneMaxSampler
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.tracer.ArchiveMutationTrackService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class BuildingBlockModelTest {

    private lateinit var buildingBlockModel: BuildingBlockModel

    @Test
    fun parentMatrixConstructorContainsAll() {
        val parentMatrix = arrayOf(
                intArrayOf(0, 2, 0),
                intArrayOf(1, 0, 0),
                intArrayOf(0, 0, 0)
        )
        buildingBlockModel = BuildingBlockModel(parentMatrix)

        assert(buildingBlockModel.toString().contains("0"))
        assert(buildingBlockModel.toString().contains("1"))
        assert(buildingBlockModel.toString().contains("2"))
    }

    @Test
    fun parentMatrixConstructorContainsBlock() {
        val parentMatrix = arrayOf(
                intArrayOf(0, 2, 0),
                intArrayOf(1, 0, 0),
                intArrayOf(0, 0, 0)
        )
        buildingBlockModel = BuildingBlockModel(parentMatrix)

        assert(buildingBlockModel.toString().contains("0, 1"))
    }

    @Test
    fun largeParentMatrixConstructorContainsBlock() {
        val parentMatrix = arrayOf(
                intArrayOf(0, 2, 0, 1),
                intArrayOf(1, 0, 2, 0),
                intArrayOf(0, 1, 0, 0),
                intArrayOf(2, 0, 0, 0)
        )
        buildingBlockModel = BuildingBlockModel(parentMatrix)

        println(buildingBlockModel.toString())

        assert(buildingBlockModel.toString().contains("0, 1, 2, 3"))

    }

    @Test
    fun fosConstructorContainsRelevant() {
        val fos = mutableListOf(
                mutableListOf(1, 2, 3),
                mutableListOf(2, 3),
                mutableListOf(1)
        )

        buildingBlockModel = BuildingBlockModel(fos)


        assert(buildingBlockModel.toString().contains("2"))
        assert(buildingBlockModel.toString().contains("3"))
    }

    @Test
    fun fosConstructorContainsBlock() {
        val fos = mutableListOf(
                mutableListOf(1, 2, 3),
                mutableListOf(2, 3),
                mutableListOf(1)
        )

        buildingBlockModel = BuildingBlockModel(fos)

        assert(buildingBlockModel.toString().contains("2, 3"))
    }

    @Test
    fun getRandomBuildingBlockExistingBlock() {
        val fos = mutableListOf(
                mutableListOf(1, 2, 3),
                mutableListOf(2, 3),
                mutableListOf(1)
        )

        buildingBlockModel = BuildingBlockModel(fos)

        val injector: Injector = LifecycleInjector.builder()
                .withModules(* arrayOf<Module>(OneMaxModule(), BaseModule()))
                .build().createInjector()

        val randomness = injector.getInstance(Randomness::class.java)

        val block = buildingBlockModel.getRandomBuildingBlock(randomness)

        assert(block.contains(2))
        assert(block.contains(3))

    }
}