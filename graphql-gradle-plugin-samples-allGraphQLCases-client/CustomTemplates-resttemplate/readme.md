# Explanation

This template is a copy/paste of the 'client_query_mutation_type.vm.java' template, as it is redefined in the 
`graphql-maven-plugin-samples-CustomTemplates-resttemplate` module of the maven plugin project.

Like in the maven plugin project, this custom template should come from an external project. But custom templates embedded 
in an external library doesn't seem to be readable from a Gradle plugin, so this custom template has been copied into the ./CustomTemplates-resttemplate of this project, so that the JUnit tests coming from the Maven project still work.
