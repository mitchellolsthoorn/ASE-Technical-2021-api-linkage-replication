package org.evomaster.core.search.algorithms.diversity

import org.evomaster.core.search.Individual
import org.evomaster.core.search.algorithms.MosaAlgorithm

class SubvectorDominance<T> : DiversityOperator<T> where T : Individual {

    override fun assignDiversityScore(notCovered: Set<Int>, list: List<MosaAlgorithm.Data>) {
        /*
            see:
            Substitute Distance Assignments in NSGA-II for
            Handling Many-Objective Optimization Problems
         */

        list.forEach { i ->
            i.crowdingDistance = 0.0
            list.filter { j -> j!=i }.forEach { j ->
                val v = svd(notCovered, i, j)

                // consider the worst case
                if(v > i.crowdingDistance){
                    i.crowdingDistance = v
                }
            }
        }
    }


    private fun svd(notCovered: Set<Int>, i: MosaAlgorithm.Data, j: MosaAlgorithm.Data) : Double{
        var cnt = 0.0
        for(t in notCovered){
            // count the number objectives j is better than i
            if(i.ind.fitness.getHeuristic(t) < j.ind.fitness.getHeuristic(t)){
                cnt++
            }
        }
        return cnt
    }


    override fun sortFront(front: List<MosaAlgorithm.Data>): List<MosaAlgorithm.Data> {
        return front.sortedWith(compareBy { it.crowdingDistance })
    }

    override fun isBetter(new: MosaAlgorithm.Data, old: MosaAlgorithm.Data): Boolean {
        if (new.crowdingDistance < old.crowdingDistance)
            return true

        return false
    }

}