package org.evomaster.core.search.modellearning

import org.evomaster.core.search.service.Randomness
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log2

/**
 * Gets the i-th subset of the Family of Subsets.
 */
fun getIthSubsetOfFOS(i: Int): IntArray {
    val subset = mutableListOf<Int>()

    var bit: Int

    val maxBits = floor(log2(i.toDouble())).toInt()
    for (j in 0 .. maxBits) {
        bit = (i shr j) and 1 // bit value at position j
        if (bit == 1) {
            subset.add(j)
        }
    }

    return subset.toIntArray()
}

/**
 * Builds the similarity matrix using the population as data.
 *
 * @param mVariables the amount of variables
 * @param population the data to build the similarity matrix for
 * @param values the amount of values each variable can take
 *
 * @return an m times m similary matrix for the m variables
 */
fun buildSimilarityMatrix(mVariables: Int, population: Array<Pair<IntArray, Double>>, values: Int): Array<DoubleArray> {
    val miMatrix = Array(mVariables) {
        DoubleArray(mVariables)
    }

    val frequencies = Array(values) {
        DoubleArray(values)
    }

    for (i in 0 until mVariables) {
        for (j in (i + 1) until mVariables) {
            for (p in population) {
                frequencies[p.first[i]][p.first[j]] += 1.0
            }

            var frequency: Double

            for (k in 0 until values) {
                for (l in 0 until values) {
                    frequency = frequencies[k][l]
                    if (frequency > 0) {
                        frequency /= population.size
                        miMatrix[i][j] += -frequency * ln(frequency)
                        frequencies[k][l] = 0.0
                    }
                }
            }

            miMatrix[j][i] = miMatrix[i][j]
        }
        for (p in population) {
            frequencies[p.first[i]][p.first[i]] += 1.0
        }

        var frequency: Double
        for (k in 0 until values) {
            for (l in 0 until values) {
                frequency = frequencies[k][l]
                if (frequency > 0) {
                    frequency /= population.size
                    miMatrix[i][i] += -frequency * ln(frequency)
                    frequencies[k][l] = 0.0
                }
            }
        }
    }

    for (i in 0 until mVariables) {
        for (j in (i + 1) until mVariables) {
            miMatrix[i][j] = miMatrix[i][i] + miMatrix[j][j] - miMatrix[i][j];
            miMatrix[j][i] = miMatrix[i][j];
        }
    }

    return miMatrix
}

/**
 * Learns the Family Of Subsets from the population data.
 *
 * @param mVariables the amount of variables
 * @param population the population to use as data
 * @param randomness the randomness object to use
 *
 * @return the entire Family Of Subsets
 */
fun linkageTreeFOSLearning(mVariables: Int, population: Array<Pair<IntArray, Double>>, randomness: Randomness): MutableList<MutableList<Int>> {
    val simMatrix = buildSimilarityMatrix(mVariables, population, 3)

    var randomOrder = MutableList(mVariables) { it }
    randomOrder = randomness.shuffle(randomOrder).toMutableList()

    var mpm = Array(mVariables) {
        MutableList(1) {
            0
        }
    }

    var mpmNumberOfIndices = Array(mVariables) { 0 }
    var mpmLength = mVariables

    for (i in 0 until mVariables) {
        mpm[i][0] = randomOrder[i]
        mpmNumberOfIndices[i] = 1
    }

    var fos = MutableList(mpmLength) {
        mpm[it].toMutableList()
    }

    var fosIndex = mpmLength

    val sMatrix = Array(mVariables) {
        val i = it
        Array(mVariables) {
            simMatrix[mpm[i][0]][mpm[it][0]]
        }
    }

    for (i in 0 until mpmLength) {
        sMatrix[i][i] = 0.0
    }

    var newMpm: Array<MutableList<Int>>
    var NNChain = Array(mVariables + 2) { 0 }
    var NNChainLength = 0
    var done = false

    while (!done) {
        if (NNChainLength == 0) {
            NNChain[NNChainLength] = randomness.nextInt(mpmLength)
            NNChainLength += 1
        }

        while (NNChainLength < 3) {
            NNChain[NNChainLength] = DetermineNearestNeighbour(NNChain[NNChainLength - 1], sMatrix, mpmNumberOfIndices, mpmLength);
            NNChainLength++;
        }

        while (NNChain[NNChainLength - 3] != NNChain[NNChainLength - 1]) {
            NNChain[NNChainLength] = DetermineNearestNeighbour(NNChain[NNChainLength - 1], sMatrix, mpmNumberOfIndices, mpmLength);
            if (((sMatrix[NNChain[NNChainLength - 1]][NNChain[NNChainLength]] == sMatrix[NNChain[NNChainLength - 1]][NNChain[NNChainLength - 2]])) && (NNChain[NNChainLength] != NNChain[NNChainLength - 2]))
                NNChain[NNChainLength] = NNChain[NNChainLength - 2];
            NNChainLength++;
            if (NNChainLength > mVariables)
                break;
        }
        var r0 = NNChain[NNChainLength - 2];
        var r1 = NNChain[NNChainLength - 1];
        var rswap: Int
        if (r0 > r1) {
            rswap = r0;
            r0 = r1;
            r1 = rswap;
        }
        NNChainLength -= 3;

        if (r1 < mpmLength) { /* This test is required for exceptional cases in which the nearest-neighbor ordering has changed within the chain while merging within that chain */
            var indices = Array(mpmNumberOfIndices[r0] + mpmNumberOfIndices[r1]) { 0 }
            //indices.resize((mpmNumberOfIndices[r0] + mpmNumberOfIndices[r1]));
            //indices.clear();

            var i = 0;
            for (j in 0 until mpmNumberOfIndices[r0]) {
                indices[i] = mpm[r0][j];
                i++;
            }
            for (j in 0 until mpmNumberOfIndices[r1]) {
                indices[i] = mpm[r1][j];
                i++;
            }

            fos.add(indices.toMutableList())
            fosIndex++;

            var mul0 = (mpmNumberOfIndices[r0]).toDouble() / (mpmNumberOfIndices[r0] + mpmNumberOfIndices[r1]).toDouble();
            var mul1 = (mpmNumberOfIndices[r1]).toDouble() / (mpmNumberOfIndices[r0] + mpmNumberOfIndices[r1]).toDouble();
            for (i in 0 until mpmLength) {
                if ((i != r0) && (i != r1)) {
                    sMatrix[i][r0] = mul0 * sMatrix[i][r0] + mul1 * sMatrix[i][r1];
                    sMatrix[r0][i] = sMatrix[i][r0];
                }
            }

            newMpm = Array(mpmLength - 1) {
                MutableList(1) {
                    0
                }
            }

            var newMpmNumberOfIndices = Array(mpmLength - 1) { 0 }
            var mpmNewLength = mpmLength - 1;
            for (i in 0 until mpmNewLength) {
                newMpm[i] = mpm[i];
                newMpmNumberOfIndices[i] = mpmNumberOfIndices[i];
            }

            newMpm[r0] = indices.toMutableList()

            newMpmNumberOfIndices[r0] = mpmNumberOfIndices[r0] + mpmNumberOfIndices[r1];
            if (r1 < mpmLength - 1) {
                newMpm[r1] = mpm[mpmLength - 1];
                newMpmNumberOfIndices[r1] = mpmNumberOfIndices[mpmLength - 1];

                for (i in 0 until r1) {
                    sMatrix[i][r1] = sMatrix[i][mpmLength - 1];
                    sMatrix[r1][i] = sMatrix[i][r1];
                }

                for (j in (r1 + 1) until mpmNewLength) {
                    sMatrix[r1][j] = sMatrix[j][mpmLength - 1];
                    sMatrix[j][r1] = sMatrix[r1][j];
                }
            }

            for (i in 0 until NNChainLength) {
                if (NNChain[i] == mpmLength - 1) {
                    NNChain[i] = r1;
                    break;
                }
            }

            mpm = newMpm;
            mpmNumberOfIndices = newMpmNumberOfIndices;
            mpmLength = mpmNewLength;

            if (mpmLength == 1)
                done = true;
        }
    }

    assert(fos.size == mVariables + mVariables - 1)

    fos.removeAt(fos.size - 1)

//    for (i in 0 until fos.size) {
//        var set = fos[i];
//        auto it = find(set->begin(), set->end(), 0);
//        if (it != set->end()) {
//        set->erase(it);
//    }
//    }
//    // remove empty sets and duplicates
//    set<vector < size_t>> fos_set;
//    for (vector<size_t> set : FOS) {
//        if (!set.empty())
//            fos_set.insert(set);
//    }
//    FOS = vector<vector < size_t >> (fos_set.begin(), fos_set.end());

    var fosSet = mutableSetOf<MutableList<Int>>()

    for (set in fos) {
        if (!set.isEmpty()) {
            fosSet.add(set)
        }
    }

    fos = fosSet.toMutableList()

    return fos
}

fun DetermineNearestNeighbour(index: Int, S_matrix: Array<Array<Double>>, mpm_number_of_indices: Array<Int>, mpm_length: Int): Int {
    var result = 0;
    if (result == index)
        result++;
    for (i in 1 until mpm_length) {
        if (((S_matrix[index][i] > S_matrix[index][result]) || ((S_matrix[index][i] == S_matrix[index][result]) && (mpm_number_of_indices[i] < mpm_number_of_indices[result]))) && (i != index))
            result = i;
    }

    return result
}

//fun linkageTreeFOSLearning(mVariables: Int, population: Array<Pair<IntArray, Double>>, randomness: Randomness): MutableList<MutableList<Int>> {
//    val miMatrix = buildSimilarityMatrix(mVariables, population, 3)
//
//    // Univariate FOS
//    val fos = MutableList(mVariables) {
//        val value = it
//        MutableList(1) {
//            value
//        }
//    }
//
//    var currentState = MutableList(mVariables) {
//        val value = it
//        MutableList(1) {
//            value
//        }
//    }
//
//    currentState = randomness.shuffle(currentState).toMutableList()
//
//    val mutualClusterInformation = hashMapOf<MutableList<Int>, HashMap<MutableList<Int>, Double>>()
//
//    while (true) {
//        var bestIndexA = 0
//        var bestIndexB = 0
//        var bestScore = 0.0
//
//        for (indexA in 0 until currentState.size) {
//            for (indexB in (indexA + 1) until currentState.size) {
////                if (indexA == indexB) {
////                    continue
////                }
//
//                val clusterA = currentState[indexA]
//                val clusterB = currentState[indexB]
//
//                var sum = 0.0
//
//                if (mutualClusterInformation.containsKey(clusterA) && mutualClusterInformation[clusterA]!!.containsKey(clusterB)) {
//                    sum = mutualClusterInformation[clusterA]!![clusterB]!!
//                } else {
//                    for (x in clusterA) {
//                        for (y in clusterB) {
//                            sum += miMatrix[x][y]
//                        }
//                    }
//
//                    if (!mutualClusterInformation.containsKey(clusterA)) {
//                        mutualClusterInformation[clusterA] = hashMapOf()
//                    }
//
//                    if (!mutualClusterInformation.containsKey(clusterB)) {
//                        mutualClusterInformation[clusterB] = hashMapOf()
//                    }
//
//                    mutualClusterInformation[clusterA]!![clusterB] = sum
//                    mutualClusterInformation[clusterB]!![clusterA] = sum
//                }
//
//
//                val IUPGMA = (1.0 / (clusterA.size * clusterB.size)) * sum
//
//                if (bestScore < IUPGMA) {
//                    bestIndexA = indexA
//                    bestIndexB = indexB
//                    bestScore = IUPGMA
//                }
//            }
//        }
//
//        val bestClusterA: MutableList<Int>
//        val bestClusterB: MutableList<Int>
//
//        bestClusterB = currentState.removeAt(bestIndexB)
//        bestClusterA = currentState.removeAt(bestIndexA)
//
//        mutualClusterInformation.remove(bestClusterA)
//        mutualClusterInformation.remove(bestClusterB)
//
//        val newCluster = bestClusterA.toMutableList()
//        newCluster.addAll(bestClusterB)
//
//        mutualClusterInformation[newCluster] = hashMapOf()
//
//        for (index in 0 until currentState.size) {
//            val cluster = currentState[index]
//
//            val valueA = mutualClusterInformation[cluster]!!.remove(bestClusterA)!!
//            val valueB = mutualClusterInformation[cluster]!!.remove(bestClusterB)!!
//
//            mutualClusterInformation[newCluster]!![cluster] = valueA + valueB
//            mutualClusterInformation[cluster]!![newCluster] = valueA + valueB
//
//        }
//
//        currentState.add(newCluster)
//        fos.add(newCluster)
//
//        if (currentState.size == 1) {
//            break
//        }
//    }
//
//    assert(fos.size == mVariables + mVariables - 1)
//
//    fos.removeAt(fos.size - 1)
//
//    return fos
//}


