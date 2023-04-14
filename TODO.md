# List of task that should be done
* Remove the graphql-gradle-plugin/userHome from git
* Manage the below warning, during the Gradle build:
    * Task :graphql-gradle-plugin-samples-Forum-client:compileJava
        * warning: unknown enum constant When.MAYBE
        * reason: class file for javax.annotation.meta.When not found
        * Note: Some input files use or override a deprecated API.
        * Note: Recompile with -Xlint:deprecation for details.
        * warning
    * Task :graphql-gradle-plugin-samples-Forum-client:processResources
        * Execution optimizations have been disabled for task ':graphql-gradle-plugin-samples-Forum-client:processResources' to ensure correctness due to the following reasons:
        * Gradle detected a problem with the following location: 'Y:\graphql-gradle-plugin-samples-Forum-client\build\generated\resources\graphqlGradlePlugin'. Reason: Task ':graphql-gradle-plugin-samples-Forum-client:processResources' uses this output of task ':graphql-gradle-plugin-samples-Forum-client:graphqlGenerateCode' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed. Please refer to https://docs.gradle.org/7.3.1/userguide/validation_problems.html#implicit_dependency for more details about this problem.
* Enhance dependency management (so that it's no more needed to provide two times the version number)
