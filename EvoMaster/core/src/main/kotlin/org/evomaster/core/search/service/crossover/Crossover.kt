package org.evomaster.core.search.service.crossover

import com.google.inject.Inject
import org.evomaster.core.EMConfig
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.Individual
import org.evomaster.core.search.service.*
import org.evomaster.core.search.service.mutator.MutatedGeneSpecification
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.tracer.TrackOperator

/**
 * This class describes the abstract Crossover Operator.
 *
 * @author Dimitri Stallenberg
 *
 * @param T the type of individuals
 */
abstract class Crossover<T>: TrackOperator where T: Individual {

    @Inject
    protected lateinit var randomness: Randomness

    @Inject
    protected lateinit var ff: FitnessFunction<T>

    @Inject
    protected lateinit var time: SearchTimeController

    @Inject
    protected lateinit var apc: AdaptiveParameterControl

    @Inject
    protected lateinit var structureCrossover: StructureCrossover

    @Inject
    protected lateinit var config: EMConfig

    /**
     * Performs the crossover operation using parentA and parentB to create a child individual.
     *
     * @param parentA
     * @param parentB
     * @param model is used to do crossover using a model
     * @param mutatedGenes is used to record what genes are mutated within [crossover], which can be further used to analyze impacts of genes.
     *
     * @return a mutated copy
     */
    abstract fun crossover(parentA: EvaluatedIndividual<T>, parentB: EvaluatedIndividual<T>, model: BuildingBlockModel? = null, mutatedGenes: MutatedGeneSpecification? = null): T

    /**
     * @return whether do a structure mutation
     */
    abstract fun doesStructureMutation(parentA : T, parentB : T) : Boolean

    open fun update(child : EvaluatedIndividual<T>, mutatedGenes: MutatedGeneSpecification?){}

    /**
     * Exccutes the crossover function and
     */
    fun crossoverAndSave(parentA: EvaluatedIndividual<T>, parentB: EvaluatedIndividual<T>, archive: Archive<T>, model: BuildingBlockModel? = null)
            : EvaluatedIndividual<T>? {

        structureCrossover.addInitializingActions(parentA,null)
        structureCrossover.addInitializingActions(parentB,null)

        val child = crossover(parentA, parentB, model)

        return ff.calculateCoverage(child)
                ?.also { archive.addIfNeeded(it) }
    }

    fun crossover(parentA: EvaluatedIndividual<T>, parentB: EvaluatedIndividual<T>, archive: Archive<T>, model: BuildingBlockModel? = null)
            : EvaluatedIndividual<T>? {

        structureCrossover.addInitializingActions(parentA, parentA.mutatedGeneSpecification)
        structureCrossover.addInitializingActions(parentB, parentB.mutatedGeneSpecification)

        val child = crossover(parentA, parentB, model)

        return EvaluatedIndividual(
                parentA.fitness.copy(),
                child,
                mutableListOf(),
                child.trackOperator,
                mutableListOf(),
                parentA.getUndoTracking(),
                mutableMapOf()
        )
    }
}