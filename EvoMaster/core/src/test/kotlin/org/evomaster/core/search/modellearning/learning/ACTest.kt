package org.evomaster.core.search.modellearning.learning

import com.google.inject.Injector
import com.google.inject.Module
import com.netflix.governator.guice.LifecycleInjector
import org.evomaster.core.BaseModule
import org.evomaster.core.search.algorithms.onemax.OneMaxModule
import org.evomaster.core.search.modellearning.score.BDeuScore
import org.evomaster.core.search.modellearning.score.BaseScoring
import org.evomaster.core.search.service.Randomness
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock


internal class ACTest: BaseLearningTest() {

    lateinit var scoringFunction: BaseScoring

    val injector: Injector = LifecycleInjector.builder()
            .withModules(* arrayOf<Module>(OneMaxModule(), BaseModule()))
            .build().createInjector()

    @BeforeEach
    fun init() {
        val mVariables = 5
        val rValues = 2
        scoringFunction = mock<BaseScoring>(BaseScoring::class.java)

        val randomness = injector.getInstance(Randomness::class.java)
        randomness.updateSeed(42)

        learning = AC(
                scoringFunction,
                randomness,
                mVariables,
                rValues
        )
    }

    @Test
    fun search() {
    }
}