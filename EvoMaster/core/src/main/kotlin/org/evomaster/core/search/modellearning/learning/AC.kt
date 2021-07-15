package org.evomaster.core.search.modellearning.learning

import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.modellearning.BuildingBlockModel
import org.evomaster.core.search.modellearning.linkageTreeFOSLearning
import org.evomaster.core.search.modellearning.score.BaseScoring

/**
 * This class implements the Linkage Tree learning class.
 * It extends the BNLearning class.
 *
 * @author Dimitri Stallenberg
 *
 * @param scoringFunction the scoring function to use
 * @param randomness the randomness object to use
 * @param mVariables the amount of variables the Bayesian Network contains
 * @param rValues the amount of values each variable can take on
 */
class AC(
        scoringFunction: BaseScoring,
        randomness: Randomness,
        mVariables: Int,
        rValues: Int,
        populationSize: Int = 0,
        maxParents: Int = 0
): BaseLearning(
        scoringFunction,
        randomness,
        mVariables,
        rValues,
        populationSize,
        maxParents
) {
    override fun search(maxGenerations: Int, data: Array<IntArray>): BuildingBlockModel {
        val parsedData = Array(data.size) {
            Pair(data[it], 0.0)
        }

        val fos = linkageTreeFOSLearning(mVariables, parsedData, randomness)

        return BuildingBlockModel(fos)
    }

//    /**
//     * Learns the Linkage Tree FOS from the data.
//     *
//     * @param mVariables the amount of variables each data row contains
//     * @param data the data to learn the Linkage Tree from
//     * @param wantedClusterCount the amount of clusters to extract
//     *
//     * @return a list of clusters of variables that are similar
//     */
//    private fun linkageTreeFOSLearning(mVariables: Int, data: Array<Pair<IntArray, Double>>, wantedClusterCount: Int): MutableList<MutableList<Int>> {
//        val simMatrix = buildSimilarityMatrix(mVariables, data, rValues)
//
//        var currentState = MutableList(mVariables) {
//            val value = it
//            MutableList(1) {
//                value
//            }
//        }
//
//        currentState = randomness.shuffle(currentState).toMutableList()
//
//        while (true) {
//            var bestIndexA = 0
//            var bestIndexB = 0
//            var bestScore = 0.0
//
//            for (indexA in 0 until currentState.size) {
//                for (indexB in 0 until currentState.size) {
//                    if (indexA == indexB) {
//                        continue
//                    }
//
//                    val clusterA = currentState[indexA]
//                    val clusterB = currentState[indexB]
//
//                    var sum = 0.0
//
//                    for (x in clusterA) {
//                        for (y in clusterB) {
//                            sum += simMatrix[x][y]
//                        }
//                    }
//                    val IUPGMA = (1.0 / (clusterA.size * clusterB.size)) * sum
//
//                    if (bestScore < IUPGMA) {
//                        bestIndexA = indexA
//                        bestIndexB = indexB
//                        bestScore = IUPGMA
//                    }
//                }
//            }
//
//            println(bestScore)
//
//            if (bestScore < 0.005) {
//                break
//            }
//
//            val bestClusterA: MutableList<Int>
//            val bestClusterB: MutableList<Int>
//
//            if (bestIndexA > bestIndexB) {
//                bestClusterA = currentState.removeAt(bestIndexA)
//                bestClusterB = currentState.removeAt(bestIndexB)
//            } else {
//                bestClusterB = currentState.removeAt(bestIndexB)
//                bestClusterA = currentState.removeAt(bestIndexA)
//            }
//
//            val newCluster = bestClusterA.toMutableList()
//            newCluster.addAll(bestClusterB)
//
//            currentState.add(newCluster)
//
////            if (currentState.size == wantedClusterCount) {
////                break
////            }
//        }
//
//        return currentState
//    }
}