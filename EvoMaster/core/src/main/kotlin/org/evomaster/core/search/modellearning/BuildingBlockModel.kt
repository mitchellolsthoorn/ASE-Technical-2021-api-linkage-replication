package org.evomaster.core.search.modellearning

import org.evomaster.core.search.service.Randomness

/**
 * This class implements the Bayesian model class.
 * Using either a Bayesian Parent matrix or a FOS the constructors create a list of building blocks.
 *
 * @author Dimitri Stallenberg
 */
class BuildingBlockModel {

    private val buildingBlocks = mutableListOf<MutableSet<Int>>()

    /**
     * Constructor that uses a Bayesian Parent matrix to create building blocks.
     *
     * @param parentMatrix the Bayesian Parent matrix
     */
    constructor (parentMatrix: Array<IntArray>) {
        val varToBlockMap = HashMap<Int, MutableSet<Int>>()

        for (i in parentMatrix.indices) {
            for (j in parentMatrix.indices) {
                if (parentMatrix[i][j] == 2) {
                    if (!varToBlockMap.containsKey(i) && !varToBlockMap.containsKey(j)) {
                        val newBlock = mutableSetOf(i, j)
                        buildingBlocks.add(newBlock)
                        varToBlockMap[i] = newBlock
                        varToBlockMap[j] = newBlock
                    } else if (varToBlockMap.containsKey(i)) {
                        varToBlockMap[i]!!.add(j)
                    } else if (varToBlockMap.containsKey(j)) {
                        varToBlockMap[j]!!.add(i)
                    }
                }
            }
        }

        // Every variable that is not in any building block should be added as a separate building block
        for (i in parentMatrix.indices) {
            var found = false

            for (block in buildingBlocks) {
                for (variable in block) {
                    if (variable == i) {
                        found = true
                        break
                    }
                }
                if (found) {
                    break
                }
            }

            if (!found) {
                buildingBlocks.add(mutableSetOf(i))
            }
        }
    }

    /**
     * Constructor that uses a FOS to create building blocks.
     *
     * @param fos the FOS
     */
    constructor(fos: MutableList<MutableList<Int>>) {
        for (f in fos) {
            if (f.size < 2) {
                continue
            }
            buildingBlocks.add(f.toMutableSet())
        }
    }

    /**
     * Returns a random building block using the given randomness object.
     *
     * @param randomness the randomness object to use
     *
     * @return the randomly chosen building block
     */
    fun getRandomBuildingBlock(randomness: Randomness): Set<Int> {
        return randomness.choose(buildingBlocks).toSet()
    }

    @Override
    override fun toString(): String {
        var str = ""

        for (block in buildingBlocks) {
            str += "{ " + block.joinToString(", ") + " }, "
        }

        return str
    }

    fun buildingBlocks(): MutableList<MutableSet<Int>> {
        return this.buildingBlocks
    }

}