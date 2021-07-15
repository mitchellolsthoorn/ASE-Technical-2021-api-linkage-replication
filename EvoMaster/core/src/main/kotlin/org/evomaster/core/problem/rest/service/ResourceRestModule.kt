package org.evomaster.core.problem.rest.service

import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import org.evomaster.core.problem.rest.RestIndividual
import org.evomaster.core.remote.service.RemoteController
import org.evomaster.core.search.service.Archive
import org.evomaster.core.search.service.FitnessFunction
import org.evomaster.core.search.service.Sampler
import org.evomaster.core.search.service.crossover.Crossover
import org.evomaster.core.search.service.crossover.StructureCrossover
import org.evomaster.core.search.service.mutator.Mutator
import org.evomaster.core.search.service.mutator.StandardMutator
import org.evomaster.core.search.service.mutator.StructureMutator


class ResourceRestModule : AbstractModule(){

    override fun configure() {
        bind(object : TypeLiteral<Sampler<RestIndividual>>() {})
                .to(RestResourceSampler::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<Sampler<*>>() {})
                .to(RestResourceSampler::class.java)
                .asEagerSingleton()

        bind(RestResourceSampler::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<FitnessFunction<RestIndividual>>() {})
                .to(RestResourceFitness::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<AbstractRestFitness<RestIndividual>>() {})
                .to(RestResourceFitness::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<Archive<RestIndividual>>() {})
                .asEagerSingleton()

        bind(object : TypeLiteral<Archive<*>>() {})
                .to(object : TypeLiteral<Archive<RestIndividual>>() {})

        bind(RemoteController::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<Mutator<RestIndividual>>() {})
                .to(ResourceRestMutator::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<StandardMutator<RestIndividual>>() {})
                .to(ResourceRestMutator::class.java)
                .asEagerSingleton()

        bind(ResourceRestMutator::class.java)
                .asEagerSingleton()

        bind(StructureMutator::class.java)
                .to(RestResourceStructureMutator::class.java)
                .asEagerSingleton()

        bind(object : TypeLiteral<Crossover<RestIndividual>>() {})
                .to(object : TypeLiteral<RestResourceCrossover>(){})
                .asEagerSingleton()

        bind(StructureCrossover::class.java)
                .to(RestStructureCrossover::class.java)
                .asEagerSingleton()

        bind(ResourceManageService::class.java)
                .asEagerSingleton()

        bind(ResourceDepManageService::class.java)
                .asEagerSingleton()

    }
}