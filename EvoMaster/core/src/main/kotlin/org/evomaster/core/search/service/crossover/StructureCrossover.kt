package org.evomaster.core.search.service.crossover

import com.google.inject.Inject
import org.evomaster.core.EMConfig
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.Individual
import org.evomaster.core.search.service.*
import org.evomaster.core.search.service.mutator.MutatedGeneSpecification
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.tracer.TrackOperator

abstract class StructureCrossover : TrackOperator {

    @Inject
    protected lateinit var config : EMConfig

    @Inject
    protected lateinit var randomness : Randomness

    abstract fun crossoverStructure(parentA: Individual, parentB: Individual, model: BuildingBlockModel?, mutatedGenes: MutatedGeneSpecification?): RestIndividual

    /**
     * Before the main "actions" (e.g, HTTP calls for web services and
     * clicks on browsers), there can be a series of initializing actions
     * to control the environment of the SUT, like for example setting
     * up data in a SQL database.
     * What to setup is often depending on what is executed by the test.
     * But once such init actions are added, the behavior of the test
     * might change.
     */
    abstract fun addInitializingActions(individual: EvaluatedIndividual<*>, mutatedGenes: MutatedGeneSpecification?)
}