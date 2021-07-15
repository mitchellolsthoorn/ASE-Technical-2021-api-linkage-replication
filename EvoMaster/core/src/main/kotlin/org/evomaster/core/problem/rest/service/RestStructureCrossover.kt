package org.evomaster.core.problem.rest.service

import com.google.inject.Inject
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.problem.rest.RestAction
import org.evomaster.core.problem.rest.RestCallAction
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.problem.rest.SampleType
import org.evomaster.core.search.Action
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.Individual
import org.evomaster.core.search.service.crossover.StructureCrossover
import org.evomaster.core.search.service.mutator.MutatedGeneSpecification
import org.evomaster.core.search.modellearning.BuildingBlockModel
import kotlin.math.min

class RestStructureCrossover: StructureCrossover() {

    @Inject
    private lateinit var sampler: RestSampler

    private fun modelBasedCrossover(parent: RestIndividual, donor: RestIndividual, model: BuildingBlockModel): RestIndividual {
        // add all action from the parent
        val childActions = mutableListOf<RestAction>()
        for (action in parent.seeActions())
            childActions.add(action.copy() as RestAction)

        // add actions from the donor
        val totalActions = sampler.seeAvailableActions()
        val encodedDonor = encodeIndividual(totalActions, donor)

        var blocks = model.buildingBlocks()
        blocks = randomness.shuffle(blocks) as MutableList<MutableSet<Int>>

        var done = false
        for (block in blocks){
            for (gene in block) {
                if (encodedDonor[gene] == 1) {
                    done = true

                    for (action in donor.seeActions()){
                        if (action.getName() == totalActions[gene].getName()) {
                            childActions.add(childActions.size, action.copy() as RestAction)
                        }
                    }
                }
            }

            if (done)
                break
        }

        if (!done) {
            LoggingUtil.getInfoLogger().error("HERE")
            return onePointCrossover(parent, donor)
        }

        childActions.forEach{
            (it as RestCallAction).locationId = null
        }

        return RestIndividual(
                childActions,
                SampleType.RANDOM,
                mutableListOf(),
                if(config.enableTrackEvaluatedIndividual || config.enableTrackIndividual) this else null,
                if(config.enableTrackIndividual) mutableListOf() else null)
    }

    private fun encodeIndividual(totalActions: List<Action>, chromosome: RestIndividual): IntArray{
        var encoded = IntArray(sampler.numberOfDistinctActions()) { 0 }

        for (i in totalActions.indices) {
            for (action in chromosome.seeActions()) {
                if (totalActions[i].getName() == action.getName()) {
                    encoded[i] = 1
                }
            }
        }
        return encoded
    }

    private fun onePointCrossover(parentA: RestIndividual, parentB: RestIndividual): RestIndividual {
        val actionsPA = parentA.seeActions()
        val actionsPB = parentB.seeActions()

        val cutPoint = randomness.nextInt(
                min(actionsPA.size,
                        actionsPB.size)
        )
        val childActions = mutableListOf<RestAction>()

        for (i in 0 until cutPoint){
            childActions.add(actionsPA[i].copy() as RestAction)
        }

        for (i in cutPoint until actionsPB.size){
            childActions.add(actionsPB[i].copy() as RestAction)
        }

        childActions.forEach{
            (it as RestCallAction).locationId = null
        }

        return RestIndividual(
                childActions,
                SampleType.RANDOM,
                mutableListOf(),
                if(config.enableTrackEvaluatedIndividual || config.enableTrackIndividual) this else null,
                if(config.enableTrackIndividual) mutableListOf() else null)
    }

    override fun crossoverStructure(parentA: Individual, parentB: Individual, model: BuildingBlockModel?, mutatedGenes: MutatedGeneSpecification?): RestIndividual {
        if (parentA !is RestIndividual || parentB !is RestIndividual) {
            throw IllegalArgumentException("Invalid individual type")
        }

        if (model != null) {
            return modelBasedCrossover(parentA, parentB, model)
        }

        return onePointCrossover(parentA, parentB)
    }

    override fun addInitializingActions(individual: EvaluatedIndividual<*>, mutatedGenes: MutatedGeneSpecification?) {

        if (!config.shouldGenerateSqlData()) {
            return
        }

        val ind = individual.individual as? RestIndividual
                ?: throw IllegalArgumentException("Invalid individual type")

        val fw = individual.fitness.getViewOfAggregatedFailedWhere()
                //TODO likely to remove/change once we ll support VIEWs
                .filter { sampler.canInsertInto(it.key) }

        if (fw.isEmpty()) {
            return
        }

        if(ind.dbInitialization.isEmpty()
                || ! ind.dbInitialization.any { it.representExistingData }) {
            //add existing data only once
            ind.dbInitialization.addAll(0, sampler.existingSqlData)
            mutatedGenes?.addedInitializationGenes?.addAll( sampler.existingSqlData.flatMap { it.seeGenes() })
        }

        val max = config.maxSqlInitActionsPerMissingData

        var missing = findMissing(fw, ind)

        while (!missing.isEmpty()) {

            val first = missing.entries.first()

            val k = randomness.nextInt(1, max)

            (0 until k).forEach {
                val insertions = sampler.sampleSqlInsertion(first.key, first.value)
                /*
                    New action should be before existing one, but still after the
                    initializing ones
                 */
                val position = sampler.existingSqlData.size
                ind.dbInitialization.addAll(position, insertions)
                mutatedGenes?.addedInitializationGenes?.addAll(insertions.flatMap { it.seeGenes() })
            }

            /*
                When we miss A and B, and we add for A, it can still happen that
                then B is covered as well. For example, if A has a non-null
                foreign key to B, then generating an action for A would also
                imply generating an action for B as well.
                So, we need to recompute "missing" each time
             */
            missing = findMissing(fw, ind)
        }

        if (config.generateSqlDataWithDSE) {
            //TODO DSE could be plugged in here
        }

        ind.repairInitializationActions(randomness)
    }

    private fun findMissing(fw: Map<String, Set<String>>, ind: RestIndividual): Map<String, Set<String>> {

        return fw.filter { e ->
            //shouldn't have already an action adding such SQL data
            ind.dbInitialization.none { a ->
                a.table.name.equals(e.key, ignoreCase = true) && e.value.all { c ->
                    // either the selected column is already in existing action
                    (c != "*" && a.selectedColumns.any { x ->
                        x.name.equals(c, ignoreCase = true)
                    }) // or we want all, and existing action has all columns
                            || (c == "*" && a.table.columns.map { it.name.toLowerCase() }
                            .containsAll(a.selectedColumns.map { it.name.toLowerCase() }))
                }
            }
        }
    }
}