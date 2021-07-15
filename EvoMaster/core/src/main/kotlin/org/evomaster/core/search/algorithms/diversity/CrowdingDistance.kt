package org.evomaster.core.search.algorithms.diversity

import org.evomaster.core.search.Individual
import org.evomaster.core.search.algorithms.MosaAlgorithm.Data
import org.evomaster.core.search.service.Randomness
import kotlin.math.abs

class CrowdingDistance<T> : DiversityOperator<T> where T : Individual {
    private val randomness = Randomness()

    override fun assignDiversityScore(notCovered: Set<Int>, list: List<Data>){
        val size = list.size
        if (size == 0) return
        if (size == 1) {
            list[0].crowdingDistance = Double.POSITIVE_INFINITY
            return
        }
        if (size == 2) {
            list[0].crowdingDistance = Double.POSITIVE_INFINITY
            list[1].crowdingDistance = Double.POSITIVE_INFINITY
            return
        }

        for (t in list) {
            t.crowdingDistance = 0.0
        }

        for (ff in notCovered) {
            // Sort the population by Fit n
            var orderedList = list.sortedBy { it.ind.fitness.getHeuristic(ff) }
            val objectiveMin = orderedList[0].ind.fitness.getHeuristic(ff)
            val objectiveMax = orderedList[orderedList.size - 1].ind.fitness.getHeuristic(ff)

            val delta = abs(objectiveMax - objectiveMin)

            // No need to compute the crow. distance if there is no diff. between best and worst objective value
            if (delta == 0.0)
                continue

            // set crowding distance
            orderedList[0].crowdingDistance = Double.POSITIVE_INFINITY
            orderedList[orderedList.size - 1].crowdingDistance = Double.POSITIVE_INFINITY

            for (j in 1 until size - 1) {
                val distance = abs(orderedList[j + 1].ind.fitness.getHeuristic(ff) -
                        orderedList[j - 1].ind.fitness.getHeuristic(ff))

                orderedList[j].crowdingDistance += distance / delta
            }
        }
    }

    override fun sortFront(front : List<Data>) : List<Data> {
        //var a = front.sortedWith(compareBy { -it.crowdingDistance })
        return front.sortedWith(compareBy { -it.crowdingDistance })
    }

    override fun isBetter(new: Data, old: Data): Boolean {
        if (new.crowdingDistance > old.crowdingDistance) {
            return true
        }

        if (new.crowdingDistance == old.crowdingDistance && randomness.nextBoolean()) {
            return true
        }

        return false
    }
}