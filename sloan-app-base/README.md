# Sloane-app-base Repo Getting Started

## Prerequisites

Install JDK 18 or later, depending on project requirements.  OpenJDK archives are located [here](https://jdk.java.net/archive/).  Verify that the JDK installed by executing `java -version` at the command line.

Install Gradle, following the instructions located [here](https://gradle.org/install/).  Validate your Gradle installation by executing `gradle` at the command line.  You should see something similar to:

```
Task :help

Welcome to Gradle 8.4.

To run a build, run gradle <task> ...
```

## Gradle Commands

### Compile the Project

Execute `gradle build` at the command line.  Java classes will be created in the `rdsjava/build/classes`

### Run a Program

To run a program in this project, create an entry for the program in the `build.gradle.kts` file.  See the example below and replace the value of `mainClass` with the name of the class you want to run, including the package name.

Example:

```
task("execute", JavaExec::class) {
mainClass = "host.HostRecv"
classpath = sourceSets["main"].runtimeClasspath
}
```

### Create a TAR Distribution, ZIP Distribution or install the distribution.

Execute `gradle distZip` or `gradle distTar` in the root directory of the project.  The distribution file will be placed in the `/rdsjava/build/distributions` directory.  The distribution will contain all the project dependencies and a JAR file containing all the Java classes in the project.

Execute `gradle installDist` to install the distribution.  By default, the files will be placed in the `/rdsjava/build/install/[projectName]`.