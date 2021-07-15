package org.evomaster.core.problem.rest.service

import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.service.crossover.Crossover
import org.evomaster.core.search.service.mutator.MutatedGeneSpecification
import org.evomaster.core.search.modellearning.BuildingBlockModel

class RestResourceCrossover : Crossover<RestIndividual>() {
    override fun crossover(parentA: EvaluatedIndividual<RestIndividual>, parentB: EvaluatedIndividual<RestIndividual>, model: BuildingBlockModel?, mutatedGenes: MutatedGeneSpecification?): RestIndividual {
        return structureCrossover.crossoverStructure(parentA.individual, parentB.individual, model, mutatedGenes)
    }

    override fun doesStructureMutation(parentA: RestIndividual, parentB: RestIndividual): Boolean {
        return true
    }
}