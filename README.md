# Improving Test Case Generation for REST APIs Through Hierarchical Clustering

This is the replication package for our paper "Improving Test Case Generation for REST APIs Through Hierarchical Clustering".

This artifact consists of:
- [EMB | EvoMaster Benchmark version 1.0.1](https://github.com/EMResearch/EMB); located in the folder EMB
- [EvoMaster](https://github.com/EMResearch/EvoMaster) | adapted version located in the folder EvoMaster
- Analysis of the results | located in the folder Analysis  

To replicate this study first follow the installation/setup instructions in [INSTALL.md](INSTALL.md).

## Performing the experiment

To perform the full experiment, for each project:

1. Run the EmbeddedEvoMasterController for that project within IntelliJ IDEA
2. Change the $evomaster variable in runscript.sh to the location of the compiled evomaster.jar
3. Run the runscript.sh

## Results

The results of our study can be found in the Analysis folder, where each project has its own directory:

- catwatch
- features
- ncs
- ocvn
- proxyprint
- scout-api
- scs

## Analyse the results

To analyse the results run the R scripts:

- features-graph.R
- ocvn-graph.R
- auc.R