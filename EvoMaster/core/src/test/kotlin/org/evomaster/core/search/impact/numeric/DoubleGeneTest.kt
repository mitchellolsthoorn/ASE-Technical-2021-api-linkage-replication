package org.evomaster.core.search.impact.numeric

import org.evomaster.core.search.gene.DoubleGene
import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.impact.GeneImpact
import org.evomaster.core.search.impact.GeneImpactTest
import org.evomaster.core.search.impact.MutatedGeneWithContext
import org.evomaster.core.search.impact.value.numeric.DoubleGeneImpact

/**
 * created by manzh on 2019-10-08
 */
class DoubleGeneTest : GeneImpactTest() {

    override fun simulateMutation(original: Gene, geneToMutate: Gene, mutationTag: Int): MutatedGeneWithContext {
        geneToMutate as DoubleGene

        if (geneToMutate.value + 1.0 > Double.MAX_VALUE)
            geneToMutate.value -= 1.0
        else
            geneToMutate.value += 1.0

        return MutatedGeneWithContext(previous = original, current = geneToMutate)
    }

    override fun getGene(): Gene = DoubleGene("i",  value= 1.0)

    override fun checkImpactType(impact: GeneImpact) {
        assert(impact is DoubleGeneImpact)
    }

}