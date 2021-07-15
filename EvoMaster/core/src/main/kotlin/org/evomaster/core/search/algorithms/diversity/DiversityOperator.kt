package org.evomaster.core.search.algorithms.diversity

import org.evomaster.core.search.Individual
import org.evomaster.core.search.algorithms.MosaAlgorithm.Data

interface DiversityOperator<T> where T : Individual {

    fun assignDiversityScore(notCovered: Set<Int>, list: List<Data>)

    fun sortFront(front : List<Data>) : List<Data>

    fun isBetter(new : Data, old : Data) : Boolean
}