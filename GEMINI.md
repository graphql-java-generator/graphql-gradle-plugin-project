# Project Overview

This project is a Gradle plugin for generating Java code from a GraphQL schema. It simplifies the development of GraphQL clients and servers in Java by generating the necessary boilerplate code. The plugin supports the full GraphQL specification and can be used in both Spring Boot and non-Spring applications.

The project is structured as a multi-project Gradle build. The main plugin logic is in the `graphql-gradle-plugin3` subproject. The other subprojects are samples that demonstrate how to use the plugin in various scenarios.

# Building and Running

The project is built using Gradle. The following commands can be used to build and test the project:

*   `./gradlew build`: Builds the entire project, including the plugin and all the samples.
*   `./gradlew test`: Runs the tests for the entire project.
*   `./gradlew publishToMavenLocal`: Publishes the plugin to the local Maven repository, which is useful for testing the plugin in other projects.

The project also includes tasks for running the sample applications. For example, to run the `allGraphQLCases-server` sample, you can use the following command:

*   `./gradlew :graphql-gradle-plugin-samples-allGraphQLCases-server:bootRun`

# Development Conventions

The project uses the Eclipse code formatter to maintain a consistent code style. The formatter configuration is located in the `graphql-java-generator (eclipse code formatter).xml` file.

The project has a comprehensive test suite, including unit tests, integration tests, and end-to-end tests. The tests are located in the `src/test` directory of each subproject.
