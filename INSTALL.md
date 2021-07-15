# Install instructions

This file contains the instructions for setting up the requirements and needed projects for this study.


## Requirements

- Java version: `JAVA openjdk version "1.8.0_252" OpenJDK Runtime Environment (build 1.8.0_252-8u252-b09-1~18.04-b09) OpenJDK 64-Bit Server VM (build 25.252-b09, mixed mode)`


## Build EvoMaster

To build our adapted version of EvoMaster:

1. Go into the EvoMaster folder
2. Run `mvn clean install -DskipTests`

This produces the `EvoMaster.jar` file needed to run EvoMaster

## Build EMB

To build the benchmark:

1. Open the pom.xml in the EMB folder in IntelliJ IDEA
2. Wait until IntelliJ IDEA is done resolvinf all dependencies
