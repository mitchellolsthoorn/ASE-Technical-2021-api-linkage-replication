package org.evomaster.core.search.algorithms

import org.evomaster.core.EMConfig
import org.evomaster.core.search.EvaluatedIndividual
import org.evomaster.core.search.Individual
import org.evomaster.core.search.Solution
import org.evomaster.core.search.service.SearchAlgorithm
import org.evomaster.core.logging.LoggingUtil
import org.evomaster.core.search.algorithms.diversity.CrowdingDistance
import org.evomaster.core.search.algorithms.diversity.DiversityOperator
import org.evomaster.core.search.algorithms.diversity.SubvectorDominance
import java.util.ArrayList
import kotlin.math.max


/**
 * Implementation of MOSA from
 * "Automated Test Case Generation as a Many-Objective Optimisation Problem with Dynamic
 *  Selection of the Targets"
 */
open class MosaAlgorithm<T> : SearchAlgorithm<T>() where T : Individual {

    class Data(val ind: EvaluatedIndividual<*>) {

        var rank = -1
        var crowdingDistance = -1.0
    }

    protected var population: MutableList<Data> = mutableListOf()
    protected open val diversityOperator : DiversityOperator<T> = SubvectorDominance<T>()

    override fun getType(): EMConfig.Algorithm {
        return EMConfig.Algorithm.MOSA
    }

    override fun search(): Solution<T> {
        time.startSearch()
        population.clear()

        initPopulation()
        sortPopulation()

        val n = config.populationSize

        writeResultHeaders()

        var generation = 0

        while (time.shouldContinueSearch()) {

            //new generation

            val nextPop: MutableList<Data> = mutableListOf()

            while (nextPop.size < n-2) {

                var child1 = selection(this.population)
                var child2 =  selection(this.population)

                if (randomness.nextDouble() < config.crossoverProbability) {
                    child1 = getCrossover().crossover(child1, child2, archive, null)!!
                    child2 = getCrossover().crossover(child2, child1, archive, null)!!
                }

                getMutatator().mutateAndSave(child1, archive)
                        ?.let{nextPop.add(Data(it))}
                getMutatator().mutateAndSave(child2, archive)
                        ?.let{nextPop.add(Data(it))}

                if (!time.shouldContinueSearch()) {
                    break
                }
            }

            // generate two random solution
            var ie = sampleIndividual()
            nextPop.add(Data(ie as EvaluatedIndividual))
            ie = sampleIndividual()
            nextPop.add(Data(ie as EvaluatedIndividual))

            population.addAll(nextPop)
            sortPopulation()

            writeResults(generation)
            generation += 1
        }

        return archive.extractSolution()
    }


    protected fun sortPopulation() {

        val notCovered = archive.notCoveredTargets()

        if(notCovered.isEmpty()){
            //Trivial problem: everything covered in first population
            return
        }

        val fronts = preferenceSorting(notCovered, population)
        var remain: Int = max(config.populationSize, fronts[0]!!.size)
        var index = 0
        population.clear()

        // Obtain the next front
        var front = fronts[index]

        while (front!=null && remain > 0 && remain >= front.size && front.isNotEmpty()) {
            // Assign crowding distance to individuals
            diversityOperator.assignDiversityScore(notCovered, front)

            // Add the individuals of this front
            for (d in front) {
                population.add(d)
            }

            // Decrement remain
            remain -= front.size

            // Obtain the next front
            index += 1
            if (remain > 0) {
                front = fronts[index]
            } // if
        } // while

        // Remain is less than front(index).size, insert only the best one
        if (remain > 0 && front!=null && front.isNotEmpty()) {
            diversityOperator.assignDiversityScore(notCovered, front)
            var front2 = diversityOperator.sortFront(front)
                    .toMutableList()
            for (k in 0..remain - 1) {
                population.add(front2[k])
            } // for

        } // if

    }

    /*
      See: Preference sorting as discussed in the TSE paper for DynaMOSA
    */
    protected fun preferenceSorting(notCovered: Set<Int>, list: List<Data>): HashMap<Int, List<Data>> {

        val fronts = HashMap<Int, List<Data>>()

        // compute the first front using the Preference Criteria
        val frontZero = mosaPreferenceCriterion(notCovered, list)
        fronts.put(0, ArrayList(frontZero))
        LoggingUtil.getInfoLogger().apply {
            debug("First front size : ${frontZero.size}")
        }

        // compute the remaining non-dominated Fronts
        val remaining_solutions: MutableList<Data> = mutableListOf()
        remaining_solutions.addAll(list)
        remaining_solutions.removeAll(frontZero)

        var selected_solutions = frontZero.size
        var front_index = 1

        while (selected_solutions < config.populationSize && remaining_solutions.isNotEmpty()){
            var front: MutableList<Data> = getNonDominatedFront(notCovered, remaining_solutions)
            fronts.put(front_index, front)
            for (sol in front){
                sol.rank = front_index
            }
            remaining_solutions.removeAll(front)

            selected_solutions += front.size

            front_index += 1

            LoggingUtil.getInfoLogger().apply {
                debug("Selected Solutions : ${selected_solutions}")
            }
        }
        return fronts
    }

    /**
     * It retrieves the front of non-dominated solutions from a list
     */
    protected fun getNonDominatedFront(notCovered: Set<Int>, remaining_sols: List<Data>): MutableList<Data>{
        var front: MutableList<Data> = mutableListOf()
        var isDominated: Boolean

        for (p in remaining_sols) {
            isDominated = false
            val dominatedSolutions = ArrayList<Data>(remaining_sols.size)
            for (best in front) {
                val flag = compare(p, best, notCovered)
                if (flag == -1) {
                    dominatedSolutions.add(best)
                }
                if (flag == +1) {
                    isDominated = true
                }
            }

            if (isDominated)
                continue

            front.removeAll(dominatedSolutions)
            front.add(p)

        }
        return front
    }

    /**
     * Fast routine based on the Dominance Comparator discussed in
     * "Automated Test Case Generation as a Many-Objective Optimisation Problem with Dynamic
     *  Selection of the Targets"
     */
    protected fun compare(x: Data, y: Data, notCovered: Set<Int>): Int {
        var dominatesX = false
        var dominatesY = false

        for (index in 1..notCovered.size) {
            if (x.ind.fitness.getHeuristic(index) > y.ind.fitness.getHeuristic(index))
                dominatesX = true
            if (y.ind.fitness.getHeuristic(index) > x.ind.fitness.getHeuristic(index))
                dominatesY = true

            // if the both do not dominates each other, we don't
            // need to iterate over all the other targets
            if (dominatesX && dominatesY)
                return 0
        }

        if (dominatesX == dominatesY)
            return 0

        else if (dominatesX)
            return -1

        else (dominatesY)
        return +1
    }

    protected open fun mosaPreferenceCriterion(notCovered: Set<Int>, list: List<Data>): HashSet<Data> {
        var frontZero: HashSet<Data> = HashSet<Data>()

        notCovered.forEach { t ->
            var chosen = list[0]
            list.forEach { data ->
                if (data.ind.fitness.getHeuristic(t) > chosen.ind.fitness.getHeuristic(t)) {
                    // recall: maximization problem
                    chosen = data
                } else if ((data.ind.fitness.getHeuristic(t) == chosen.ind.fitness.getHeuristic(t)) &&
                        (data.ind.individual.size() < chosen.ind.individual.size())) {
                        // Secondary criterion based on tests lengths
                        chosen = data
                }
            }
            // MOSA preference criterion: the best for a target gets Rank 0
            chosen.rank = 0
            frontZero.add(chosen)
        }
        return frontZero
    }

    /**
     * Selects an individual from the given population using tournament select based on the rank of the solutions.
     *
     * @param subPopulation the population to select an individual from, this defaults to the entire population
     * @return the selected individual
     */
    protected open fun selection(subPopulation: MutableList<Data> = population): EvaluatedIndividual<T> {
        // the population is not fully sorted
        var min = randomness.nextInt(subPopulation.size)

        (0 until config.tournamentSize-1).forEach {
            val sel = randomness.nextInt(subPopulation.size)
            if (subPopulation[sel].rank < subPopulation[min].rank) {
                min = sel
            } else if (subPopulation[sel].rank == subPopulation[min].rank){
                if (diversityOperator.isBetter(subPopulation[sel], subPopulation[min])) {
                    min = sel
                }
            }
        }

        return (subPopulation[min].ind as EvaluatedIndividual<T>).copy()
    }

    protected fun initPopulation() {

        val n = config.populationSize

        for (i in 1..n) {
            sampleIndividual()?.run { population.add(Data(this)) }

            if (!time.shouldContinueSearch()) {
                break
            }
        }
    }

    protected open fun sampleIndividual(): EvaluatedIndividual<T>? {

        return ff.calculateCoverage(sampler.sample())
                ?.also { archive.addIfNeeded(it) }
    }
}