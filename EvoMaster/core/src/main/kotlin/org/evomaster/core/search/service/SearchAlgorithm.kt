package org.evomaster.core.search.service

import com.google.inject.Inject
import com.google.inject.Injector
import org.evomaster.client.java.instrumentation.shared.ObjectiveNaming
import org.evomaster.core.EMConfig
import org.evomaster.core.remote.service.RemoteController
import org.evomaster.core.search.Individual
import org.evomaster.core.search.Solution
import org.evomaster.core.search.service.crossover.Crossover
import org.evomaster.core.search.service.mutator.Mutator
import java.io.File


abstract class SearchAlgorithm<T> where T : Individual {

    @Inject
    protected lateinit var sampler : Sampler<T>

    @Inject
    protected lateinit var ff : FitnessFunction<T>

    @Inject
    protected lateinit var randomness : Randomness

    @Inject
    protected lateinit var time : SearchTimeController

    @Inject
    protected lateinit var archive: Archive<T>

    @Inject
    protected lateinit var apc: AdaptiveParameterControl

    @Inject
    protected lateinit var config: EMConfig

    @Inject
    protected lateinit var injector: Injector

    @Inject(optional = true)
    private lateinit var mutator: Mutator<T>


    @Inject(optional = true)
    private lateinit var crossover: Crossover<T>

    protected fun getMutatator() : Mutator<T> {
        return mutator
    }

    protected fun getCrossover() : Crossover<T> {
        return crossover
    }

    abstract fun search() : Solution<T>

    abstract fun getType() : EMConfig.Algorithm

    fun writeResultHeaders() {
        File("${config.algorithm}-${config.crossoverModel}-${config.modelScoringFunction}-${config.recalculationInterval}-${config.run}.csv").writeText("time, generation, evaluatedTests, evaluatedActions, neededBudget, coveredTargets, faults, lines, avgActionsPerTest, percentImprovedThroughtCrossover\n".trimIndent())
        File("${config.algorithm}-${config.crossoverModel}-${config.modelScoringFunction}-${config.recalculationInterval}-${config.run}.csv").appendText("\n")
    }

    fun writeResults(generation: Int) {
        val stc = injector.getInstance(SearchTimeController::class.java)
        val idMapper = injector.getInstance(IdMapper::class.java)

        val etest = stc.evaluatedIndividuals
        val eactions = stc.evaluatedActions
        val nbudget = stc.neededBudget() + ""

        val solution = archive.extractSolution()
        val faults = solution.overall.potentialFoundFaults(idMapper).size

        val coveredLines = solution.overall.coveredTargets(ObjectiveNaming.LINE, idMapper)
        val coveredTargets = solution.overall.coveredTargets()

        val rc = injector.getInstance(RemoteController::class.java)
        var unitsInfo = rc.getSutInfo()?.unitsInfoDto

        if (unitsInfo == null) {
            unitsInfo = rc.getSutInfo()?.unitsInfoDto
        }

        val totalLines = unitsInfo!!.numberOfLines
        val percentage = String.format("%.0f", (coveredLines / totalLines.toDouble()) * 100)

        val avgTimeAndSize = time.computeExecutedIndividualTimeStatistics()

        val percentImproved = 0

        File("${config.algorithm}-${config.crossoverModel}-${config.modelScoringFunction}-${config.recalculationInterval}-${config.run}.csv").appendText("${time.getElapsedSeconds()}, ${generation}, ${etest}, ${eactions}, ${nbudget.replace("%", "")}, ${coveredTargets}, ${faults}, ${percentage}, ${avgTimeAndSize.second}, ${percentImproved}\n")

    }
}
