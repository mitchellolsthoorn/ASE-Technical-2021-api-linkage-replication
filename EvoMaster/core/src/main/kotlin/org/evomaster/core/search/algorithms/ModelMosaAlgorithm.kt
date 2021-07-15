package org.evomaster.core.search.algorithms

import org.evomaster.core.EMConfig
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.Individual
import org.evomaster.core.search.Solution
import org.evomaster.core.search.algorithms.diversity.CrowdingDistance
import org.evomaster.core.search.algorithms.diversity.DiversityOperator
import org.evomaster.core.search.gene.DisruptiveGene
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.modellearning.learning.BaseLearning
import org.evomaster.core.search.modellearning.learning.CrossoverModel
import org.evomaster.core.search.modellearning.score.ModelScoringFunction


/**
 * Implementation of ML-based MOSA
 * "Automated Test Case Generation as a Many-Objective Optimisation Problem with Dynamic
 *  Selection of the Targets"
 *
 * @author Dimitri Stallenberg, Annibale Panichella
 */
class ModelMosaAlgorithm<T> : MosaAlgorithm<T>() where T : Individual {

    private var model: BuildingBlockModel? = null

    override val diversityOperator : DiversityOperator<T> = CrowdingDistance<T>()

    override fun getType(): EMConfig.Algorithm {
        return EMConfig.Algorithm.MODELMOSA
    }

    override fun search(): Solution<T> {
        time.startSearch()
        population.clear()

        initPopulation()
        sortPopulation()

        val mVariables = sampler.numberOfDistinctActions()

        val scoreFunction = ModelScoringFunction.getFunction(config.modelScoringFunction, mVariables, 2)
        val module: BaseLearning? = CrossoverModel.getModel(config.crossoverModel, scoreFunction, randomness, mVariables,
                2, config.modelPopulationSize, 2)

        val totalActions = sampler.seeAvailableActions()

        val actionMap = HashMap<Int, String>()

        for (i in totalActions.indices) {
            actionMap.put(i, totalActions[i].getName())
        }

        writeResultHeaders()

        var generation = 0

        while (time.shouldContinueSearch()) {
            //new generation
            if (module != null && generation % config.recalculationInterval == 0) {
                model = module.search(config.modelIterations, encodePopulation())
            }

            var nextPop = generateChildren(config.populationSize)

            population.addAll(nextPop)
            sortPopulation()

            writeResults(generation)
            generation += 1
        }

        return archive.extractSolution()
    }

    protected fun generateChildren(n: Int): MutableList<Data> {
        val nextPop: MutableList<Data> = mutableListOf()

        for (i in 0 until n) {
            var parent = selection(population)

            var child = parent
            if (randomness.nextDouble() < config.crossoverProbability) {
                var donor = selection(population)
                child = getCrossover().crossover(child, donor, archive, model)!!
            }
           child = getMutatator().fastMutateAndSave(this.apc.getNumberOfMutations(), child, archive)!!
           nextPop.add(Data(child))

            if (!time.shouldContinueSearch()) {
                break
            }
        }
        // generate one random solutions
        for (i in 1..2) {
            var ie = sampleIndividual()
            nextPop.add(Data(ie as EvaluatedIndividual))
        }

        return nextPop
    }

    /**
     * Encode the current population.
     *
     * It does this by creating an array of zeros and setting the variable at index i to 1 if the i-th action is present in the individual.
     * Before doing this the population if filtered so that only the first 2 fronts are used.
     *
     * @return an encoded variant of the filtered population
     */
    private fun encodePopulation(): Array<IntArray> {
        val totalActions = sampler.seeAvailableActions()

        var firstFronts = population.map {
            it.ind
        }

        return Array(firstFronts.size) {
            val individual = firstFronts[it].individual
            val actions = individual.seeActions()

            IntArray(sampler.numberOfDistinctActions()) {
                var found = false

                for (action in actions) {
                    if (action.getName() == totalActions[it].getName()) {
                        found = true
                        break
                    }
                }
                if (found) {
                    1
                } else {
                    0
                }
            }
        }
    }

    override fun sampleIndividual(): EvaluatedIndividual<T>? {
        if (sampler.hasSpecialInit() || randomness.nextBoolean()) {
            // If there is still special init set, sample from that
            return ff.calculateCoverage( sampler.smartSample())
                    ?.also {
                        archive.addIfNeeded(it)
                        sampler.feedback(it)
                    }
        } else {
            //note this can still be a smart sample
            return ff.calculateCoverage(sampler.sampleAtRandom())
                    ?.also {
                        archive.addIfNeeded(it)
                        sampler.feedback(it)
                    }
        }
    }

    override fun mosaPreferenceCriterion(notCovered: Set<Int>, list: List<Data>): HashSet<Data> {
        var frontZero: HashSet<Data> = HashSet<Data>()

        var list2 = randomness.shuffle(list)
        notCovered.forEach { t ->
            var chosen = list2[0]
            list2.forEach { data ->
                if (data.ind.fitness.getHeuristic(t) > chosen.ind.fitness.getHeuristic(t)) {
                    // recall: maximization problem
                    chosen = data
                } else if (data.ind.fitness.getHeuristic(t) == chosen.ind.fitness.getHeuristic(t)){
                    if ((data.ind.individual.size() < chosen.ind.individual.size() &&
                                    data.ind.individual.size() > 1)) {
                        // Secondary criterion based on tests lengths
                        chosen = data
                    }
                }
            }
            // MOSA preference criterion: the best for a target gets Rank 0
            chosen.rank = 0
            frontZero.add(chosen)
        }
        return frontZero
    }
}