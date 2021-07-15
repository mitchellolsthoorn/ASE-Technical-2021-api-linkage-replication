package org.evomaster.core.search.algorithms

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.netflix.governator.guice.LifecycleInjector
import org.evomaster.core.BaseModule
import org.evomaster.core.EMConfig
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.algorithms.onemax.OneMaxIndividual
import org.evomaster.core.search.algorithms.onemax.OneMaxModule
import org.evomaster.core.search.algorithms.onemax.OneMaxSampler
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.StandardMutator
import org.evomaster.core.search.tracer.ArchiveMutationTrackService
import org.evomaster.core.search.tracer.TraceableElementCopyFilter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MioAlgorithmOnTrackOneMaxTest {


    private lateinit var config: EMConfig
    private lateinit var mio: MioAlgorithm<OneMaxIndividual>
    private lateinit var tracker : ArchiveMutationTrackService

    private fun init(args: Array<String>){

        val injector: Injector = LifecycleInjector.builder()
                .withModules(* arrayOf<Module>(OneMaxModule(), BaseModule(args)))
                .build().createInjector()

        mio = injector.getInstance(Key.get(
                object : TypeLiteral<MioAlgorithm<OneMaxIndividual>>() {}))

        config = injector.getInstance(EMConfig::class.java)

        tracker = injector.getInstance(ArchiveMutationTrackService::class.java)

        val randomness = injector.getInstance(Randomness::class.java)
        randomness.updateSeed(42)

        val sampler = injector.getInstance(OneMaxSampler::class.java)
        val n = 20
        sampler.n = n

    }


    @Test
    fun testIndividualWithTrack(){

        val args = arrayOf(
                "--stoppingCriterion",
                "FITNESS_EVALUATIONS",
                "--enableTrackIndividual",
                "true",
                "--enableTrackEvaluatedIndividual",
                "false"
        )
        init(args)
        assert(tracker.exists(TraceableElementCopyFilter.NONE.name))
        assert(tracker.exists(TraceableElementCopyFilter.WITH_TRACK.name))
        assert(tracker.exists(TraceableElementCopyFilter.DEEP_TRACK.name))

        val solution = mio.search()

        solution.individuals.forEach { s->
            assertNull(s.getTracking())
            assertNotNull(s.individual.getTracking())
            s.individual.getTracking()?.apply {
                forEachIndexed { index, t ->
                    if(index == 0)
                        assert(t.trackOperator!!.operatorTag().contains(OneMaxSampler::class.java.simpleName))
                    else
                        assert(t.trackOperator!!.operatorTag().contains("Mutator"))
                }
            }
        }
    }

    @Test
    fun testEvaluatedIndividualWithTrack(){

        val args = arrayOf(
                "--stoppingCriterion",
                "FITNESS_EVALUATIONS",
                "--enableTrackIndividual",
                "false",
                "--enableTrackEvaluatedIndividual",
                "true",
                "--maxLengthOfTraces",
                "-1"
        )
        init(args)

        assert(tracker.exists(TraceableElementCopyFilter.NONE.name))
        assert(tracker.exists(TraceableElementCopyFilter.WITH_TRACK.name))
        assert(tracker.exists(TraceableElementCopyFilter.DEEP_TRACK.name))
        assert(tracker.exists(EvaluatedIndividual.ONLY_INDIVIDUAL))

        val solution = mio.search()

        solution.individuals.forEach {  s->
            assertNull(s.individual.getTracking())
            /**
             * [s] might be null when the individual is never mutated
             */
            if(s.getTracking() == null){
                assertNotNull(s.individual.trackOperator != null)
                assert(s.individual.trackOperator!!.operatorTag().contains(OneMaxSampler::class.java.simpleName))
            }
            s.getTracking()?.forEachIndexed{ index, t->
                assertNotNull(t.trackOperator)
                if(index == 0)
                    assert(t.trackOperator!!.operatorTag().contains(OneMaxSampler::class.java.simpleName))
                else
                    assertEquals(StandardMutator::class.java.simpleName, t.trackOperator!!.operatorTag())
            }
        }
    }

    @Test
    fun testEvaluatedIndividualWithSpecifiedLengthOfTrack(){
        val maxLengthOfTraces = 5
        val args = arrayOf(
                "--stoppingCriterion",
                "FITNESS_EVALUATIONS",
                "--enableTrackIndividual",
                "false",
                "--enableTrackEvaluatedIndividual",
                "true",
                "--maxLengthOfTraces",
                maxLengthOfTraces.toString()
        )
        init(args)

        assert(tracker.exists(TraceableElementCopyFilter.NONE.name))
        assert(tracker.exists(TraceableElementCopyFilter.WITH_TRACK.name))
        assert(tracker.exists(TraceableElementCopyFilter.DEEP_TRACK.name))
        assert(tracker.exists(EvaluatedIndividual.ONLY_INDIVIDUAL))

        val solution = mio.search()

        assert(solution.individuals.count { it.getTracking() != null } > 0)

        solution.individuals.forEach {  s->

            assertNull(s.individual.getTracking())
            /**
             * [s] might be null when the individual is never mutated
             */
            if(s.getTracking() == null){
                assertNotNull(s.individual.trackOperator != null)
                assert(s.individual.trackOperator!!.operatorTag().contains(OneMaxSampler::class.java.simpleName))
            }

            s.getTracking()?.forEachIndexed{ index, t->
                assertNotNull(t.trackOperator)
                assert(index < maxLengthOfTraces)
                assertEquals(StandardMutator::class.java.simpleName, t.trackOperator!!.operatorTag())
            }
        }
    }

    @Test
    fun testEvaluatedIndividualWithTrackImpact(){

        val args = arrayOf(
                "--stoppingCriterion",
                "FITNESS_EVALUATIONS",
                "--enableTrackIndividual",
                "false",
                "--enableTrackEvaluatedIndividual",
                "true",
                "--probOfArchiveMutation",
                "0.5",
                "--maxLengthOfTraces",
                "-1"
        )
        init(args)

        assert(tracker.exists(TraceableElementCopyFilter.NONE.name))
        assert(tracker.exists(TraceableElementCopyFilter.WITH_TRACK.name))
        assert(tracker.exists(TraceableElementCopyFilter.DEEP_TRACK.name))
        assert(tracker.exists(EvaluatedIndividual.ONLY_INDIVIDUAL))
        assert(tracker.exists(EvaluatedIndividual.WITH_TRACK_WITH_IMPACT))

        val solution = mio.search()

        solution.individuals.forEach {  s->
            assertNull(s.individual.getTracking())
            assertNotNull(s.getImpactOfGenes().isNotEmpty())
        }
    }

    @Test
    fun testTrackWithoutTrack(){

        val args = arrayOf(
                "--stoppingCriterion",
                "FITNESS_EVALUATIONS",
                "--enableTrackIndividual",
                "false",
                "--enableTrackEvaluatedIndividual",
                "false"
        )
        init(args)
        assert(tracker.exists(TraceableElementCopyFilter.NONE.name))
        assert(tracker.exists(TraceableElementCopyFilter.WITH_TRACK.name))
        assert(tracker.exists(TraceableElementCopyFilter.DEEP_TRACK.name))

        val solution = mio.search()

        solution.individuals.forEach { s->
            assertNull(s.getTracking())
            assertNull(s.individual.getTracking())
        }
    }
}
