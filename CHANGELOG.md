# Note for the future 2.x versions

New developments __should not use the graphql Maven goal or generateCode Gradle task__. 
Instead, they should use the new __generateClientCode__ and __generateServerCode__ goals/tasks.
Whether the application uses the _graphql_, the _generateClientCode_ or the _generateServerCode_ goal/task, it should use the parameters below, to be compliant with default values of the 2.0 version:
* generateBatchLoaderEnvironment: true _(server only)_
* generateDeprecatedRequestResponse: false _(client only)_
* separateUtilityClasses: true _(both client and server mode)_
* skipGenerationIfSchemaHasNotChanged: true _(both client and server mode)_

# 1.17

Gradle Plugin:
* Corrected some issues when executing a `gradlew clean build`, whereas `gradlew clean` then `gradlew build` works ok

Client mode:
* The generated code can now attack two (or more) GraphQL servers.
    * More information on [this page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_more_than_one_graphql_servers).
* Added the ability to create __GraphQL Repositories__: like Spring Data Repositories, GraphQL Repositories allow to declare GraphQL requests (query, mutation, subscription) by __just declaring an interface__. The runtime code create dynamic proxies at runtime, that will execute these GraphQL requests.
    * In other words: GraphQL Repositories is a powerful tool that allows to execute GraphQL request, without writing code for that: just declare these requests in the GraphQL Repository interface
    * More information on [this page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).

# 1.16

Both mode:
* The plugin now properly manages GraphQL scalar extensions
* Added a control, that the version of the runtime matches the version of the plugin. Doing such a control can prevent some weird errors.
* Added a check, that the provided custom templates have valid names and match to existing files (or resources in the classpath)

Client mode:
* Issue #82: Using Hard coded Int parameters would raise an exception (for instance in this request: `"{aRequest(intParam: 3) {...}}"`)


# 1.15

Client mode:
* The queries, mutations and subscriptions now __accept GraphQL aliases__, like specified in [GraphQL spec](http://spec.graphql.org/June2018/#sec-Field-Alias)
    * Once the request is executed, The alias value can be retrieved with the `Object getAliasValue(String: aliasname)` method that has been added to every generated objects and interfaces. This method returns the alias value, parsed into the relevant Java Object.
* The default `QueryExecutor` provided by the plugin is now a Spring bean. If you want to override it, you should not mark your own `QueryExecutor` with the `@Primary` annotation, to ignore the default one.

Gradle only:
* Since 1.15, the files are generated in these folders:
    * Resources are generated in: `build/generated/resources/graphqlGradlePlugin`
    * Sources are generated in: `build/generated/sources/graphqlGradlePlugin`
    

# 1.14.2

New goal/task added:
* generatePojo: This goal allows to only generate the Java classes and interfaces that match the provided GraphQL schema.

Server mode:
* Workaround to build a project with Gradle 7 without the gradle wrapper (this commit prevents a strange error during the build)

Internal:
* The plugin now uses slf4j as the logging frontend framework, as do Gradle and Maven (since maven 3.1). This means that, when using Maven, the minimum release version is the 3.1 version.


# 1.14.1

Both mode:
* Upgrade of _com.google.guava_, to version 30.1.1-jre, to remove a vulnerability
* Upgrade of Spring boot from 2.4.0 to 2.4.4
* Upgrade of Spring framework from 5.3.0 to 5.3.5
* Upgrade of Spring security from 5.4.1 to 5.4.5
* Upgrade of graphql-java-extended-scalars version from 1.0.1 to 16.0.1
* Upgrade of commons-io from 2.6 to 2.8.0
* Upgrade of dozer-core from 6.5.0 to 6.5.2
* Upgrade of h2 from 1.4.199 to 1.4.200

Client mode:
* Issue #65: When using requests with the parameters in the request (no GraphQL Variables and no Bind Parameter), the request is properly encoded when these parameters are or contain strings
* Dependency order changed in the graphql-java-client-dependencies module, to make sure the right spring's dependencies are used (this could prevent a Spring app to start)
* Removed the use of the `reactor.core.publisher.Sinks` class, to restore compatibility for those who uses an older version of Spring Boot

Gradle:
* Update in the build.gradle to make it compatible with gradle 7


# 1.14

Both mode:
* Upgrade of _com.google.guava_, to remove a vulnerability

Client mode:
* Request with GraphQL variable are now accepted. Of course, this works only with full requests. You can find more information on GraphQL Variables in the [GraphQL spec](http://spec.graphql.org/June2018/index.html#sec-Language.Variables). 
* Subscription can now be executed as full request. They were previously limited to partial requests.

Custom templates:
* The following templates have been updated : client_DirectiveRegistryInitializer.vm.java, client_GraphQLRequest.vm.java, client_jackson_deserializers.vm.java, client_query_mutation_executor.vm.java, client_subscription_executor.vm.java, client_subscription_type.vm.java, server_GraphQLDataFetchers.vm.java
* These updates are due to:
    * The `GraphQLInputParameters` has been updated, to now embed all the GraphQL information (prerequisite for GraphQL variable)
    * The list level is now better managed (not just a boolean, but the real depth when there are lists of lists)
    * The input parameters now typed with an enum, not just a boolean for mandatory/optional.
* The _client_query_target_type.vm.java_ was not used and has been removed.



# 1.13

Both mode:
* Custom Templates can now be defined as a resource in the current project (no need to embed them in a jar prior to use them). See the [CustomTemplates-client pom file](https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-CustomTemplates-client/pom.xml) as a sample for that.

Client mode:
* The _extensions_ field on the root of the GraphQL server response can be retrieved by using Full Queries. More information on the [Client page about request execution](https://graphql-maven-plugin-project.graphql-java-generator.com/exec_graphql_requests.html)
* Issue #55 : the _extensions_ field of the GraphQL errors is now properly managed. It's possible to receive any GraphQL valid response for this field. And the `Error` class has now the proper getters to manage it (including deserialization of the _extensions_ map values in any Java classes)
* Issue #65: It's now possible to provide a full query that contains all GraphQL parameters (without runtime parameters). More info in the [Client FAQ](https://graphql-maven-plugin-project.graphql-java-generator.com/client_faq.html)

Server mode:
* The Query/Mutation/Subscription are now available on the same URL (/graphql by default). This is the standard GraphQL behavior, but it was tricky to build, due to a Java limitation.


# 1.12.5

Client mode:
* Issue #53: custom scalars would not be properly managed when they are a parameter of a query, mutation or subscription.

# 1.12.4

Server mode:
* It's now possible to override the type wiring, thanks to the new `GraphQLWiring` class.
* It's now possible to override the DataFetcher definitions, by overriding `GraphQLDataFetchers`. This allows, for instance, to change the DataLoader behavior.
* [Internal] The `GraphQLProvider` class has been removed. The Spring beans it created are now created by the `GraphQLServerMain` class. The type wiring has been moved in the new `GraphQLWiring` class. This allows an easier overriding of the generated type wiring.


# 1.12.3


Both mode:
* Corrected a multithreading issue with the provided custom scalars GraphQLScalarTypeDate and GraphQLScalarTypeDateTime

Server mode:
* Issue #72: The subscription notifications would not be properly sent when more than one client subscribed to a subscription.

Client mode:
* The GraphQL server response could not be deserialized, when it contains the (non standard) _extensions_ entry


# 1.12.2

Both modes (client and server):
* Added the _skipGenerationIfSchemaHasNotChanged_ parameter. It's in beta version. It prevents code and resource generation, of the schema file(s) are older than these generated sources or resources. It's default value is false in 1.x releases.
* When the _addRelayConnections_ parameter is true, the XxxConnection types of the fields marked with @RelayConnection are now non mandatory.
* The comments read in the provided schema are reported into the generated code and the generated GraphQL schemas.

Server mode:
* The graphql-java version has been upgraded to 16.2 (the latest version at this time)
* The generated code would not allow the specific implementation to override the GraphQLInvocation Spring Bean




# 1.12.1

Both modes (client and server):
* Correction for the _addRelayConnections_: the _Node_ interface was not properly copied to all implementing subtypes and subinterfaces

Server mode:
* The server won't start when the _graphql-java-runtime_ dependency is in the classpath (that is when the _copyRuntimeSources_ plugin parameter is set to false)
* When a DataFetcher has a BatchLoader, two datafetchers would be wired, instead of just one. This is internal to the generated code, and has no impact on the "user's" code.
* The cache of the DataLoader is now managed for per request.
* When the _addRelayConnections_ plugin parameter is set to true, the _generateServerCode_ task/goal (and _graphql_ task/goal when in server mode) copies the generated schema file in the _/classes_ folder, so that the graphql-java engine has a proper access to it, at runtime.


# 1.12

Both modes (client and server):
* Added support for OAuth 2
* Removed all dependencies to log4j
* [internal] The GraphqlUtils class has been moved into the com.graphql_java_generator.util package


Client mode:
* The client can now be a Spring Boot app (and that's now the recommended way to build a GraphQL app). see the [plugin web site](https://graphql-maven-plugin-project.graphql-java-generator.com/client_spring.html) for more info on this
* The subscription management has been updated.
==> Spring reactive WebSocketClient
==> The _SubscriptionClient_ interface has a new method: _WebSocketSession getSession()_, which allows to retrieve the Spring reactive _WebSocketSession_. 

Server mode:
* Corrected a regression in 1.11.2, due to _generateBatchLoaderEnvironment_ plugin parameter (see issue #64)

# 1.11.2

server mode:
* The generated code would not compile for fields with parameters (when the field's type is an entity with an id)
* Add of the _generateBatchLoaderEnvironment_ parameter. When in server mode, it allows the batch loader to retrieve the context, for instance the field parameters associated to this id.


# 1.11.1

Both modes (client and server):
* Upgrade of spring boot from 2.3.3 to 2.4.0
* Issue 54: The generated code would not compile for subscriptions that return a list

Gradle plugin:
* The plugin is now compatible with a JDK/JRE 8 (it previously needed java 13)

# 1.11

Both modes (client and server):
* Changes in goal(maven)/task(gradle) names, to make them clear and homogeneous between the gradle and the maven plugin:
    * The _graphql_ maven goal (and _graphqlGenerateCode_ gradle task) are deprecated, but they will be maintained in future 2.x versions
    * The goal(maven)/task(gradle) are now:
        * __generateClientCode__ : generates the Java code on client side, to access a GraphQL server, based on its GraphQL schema. It is the same as the deprecated _graphql_ maven goal (or _graphqlGenerateCode_ gradle task), with the _mode_ parameter removed (it is internally forced to _client_).
        * __generateServerCode__ : generates the Java code on server side, to access a GraphQL server, based on its GraphQL schema. It is the same as the deprecated _graphql_ maven goal (or _graphqlGenerateCode_ gradle task), with the _mode_ parameter removed (it is forced to _server_).
        * __generateGraphQLSchema__ : new, see below
        * __graphql__ (maven) / ___graphqlGenerateCode__ (gradle) : deprecated and maintained. It's the same as the new _generateClientCode_ and _generateServerCode_ goals/tasks, with the _mode_ plugin parameter that allows to choose between the client or the server mode.  
* New _generateGraphQLSchema_ goal/task that allows to generate the GraphQL schema file. It's interesting when:
    * There are several GraphQL schema files in input (for instance with the extends GraphQL capability)
    * The _addRelayConnections_ is used, that adds the _Node_ interface, and the _Edge_ and _Connection_ types to the schema.


Client Mode (generateClientCode):
* Corrected issue 50: could not work with nested arrays (for instance [[Float]])
* Corrected issue 51: error with argument being a list of ID ([ID]) 



# 1.10

Both modes (client and server):
* Upgrade of graphql-java from v14.0 to v15.0
* The main improvement for this is: the plugin now accepts interfaces that implement one or more interfaces
* Attributes of input types that are enum are now properly managed

Server mode:
* The generated code would not compile if a GraphQL interface is defined, but not used, in the given GraphQL schema


# 1.9

Both modes (client and server):
* The GraphQL schema can now be split into separate files, including one file containing GraphQL extend keyword on the other file's objects
* Add of the _merge_ goal/task: it generates a GraphQL schema file, based on the source GraphQL schemas. It can be used to merge several GraphQL schema files into one file, or to reformat the schema files.

Client mode:
* Fixes #46: Strings not properly escaped for JSON

# 1.8.1

Both modes (client and server):
* The generated code was not compatible with Java 8 (only with Java 9 and above)

# 1.8

Both modes (client and server):
* Corrected issue #43: GraphQL Float was mapped to Float, instead of Double

Client mode:
* a XxxxExecutor class is now generated for each query, mutation and subscription types, for better separation of __GraphQL objects and utility classes__ . They contains the methods to prepare and execute the queries/mutations/subscriptions that were in the query/mutation/subscription classes. These the query/mutation/subscription classes are still generated, but their use to prepare and execute the queries/mutations/subscriptions is now deprecated.
    * In other word: existing code CAN remain as is. It'll continue to work.
    * New code SHOULD use the XxxExecutor classes.
* Add of the generateDeprecatedRequestResponse. Default value is true, which makes it transparent for existing code. If set to false, the XxxResponse classes are not generated (where Xxx is the GraphQL name for the query, mutation and subscription objects), nor the Xxxx classes in the util subpackage (only if separateUtilityClasses is true). If generated, these classes are marked as Deprecated, and should not be used any more.
* Solved issue #44: no more need to add an _extension_ bloc in the _build_ pom's bloc, to make the plugin work. 
* Named Fragments would not work if seperateUtilityClass was false

Server mode:
* Added the _scanBasePackages_ plugin parameter. It allows to create our Spring Beans, including the DataFetchersDelegateXxx implementation in another place than a subpackage of the package define in the _packageName_ plugin parameter
* Corrected a dependency issue (for server implemented from the code generated by the plugin)

# 1.7.0

Both modes (client and server):
* The plugin now manages __subscription__
* For custom templates: the QUERY_MUTATION_SUBSCRIPTION template has been split into two templates, QUERY_MUTATION and SUBSCRIPTION

Server mode:
* In some cases, the DataFetcherDelegate now have another DataFetcher that must be implemented
* Some server templates rename, to respect java standards. An underscore has been added to these templates name: BATCH_LOADER_DELEGATE, BATCH_LOADER_DELEGATE_IMPL, DATA_FETCHER and DATA_FETCHER_DELEGATE

# 1.6.1

Both modes (client and server):
* Default value for input parameters (fields and directives) that are null, array or an object are now properly managed.
* Plugin parameter _copyGraphQLJavaSources_ renamed to _copyRuntimeSources_
* New Plugin parameter _separateUtilityClasses_: it allows to separate the generated utility classes (including GraphQLRequest, query/mutation/subscription) in a subfolder. This avoids collision in the generated code, between utility classes and the classes directly generated from the GraphQL schema.

Client mode:
* Thanks to the _separateUtilityClasses_ plugin parameter, the plugin can generate the code for the shopify and github GraphQL schema (needs some additional tests: tester need here...)
   

# 1.6.0

Both modes (client and server):
* Added a _templates_ plugin parameter, that allows to override the default templates. This allows the developper to control exactly what code is generated by the developer.
* Added a _copyGraphQLJavaSources_ that allows to control whether the runtime code is embedded in the generated (previous behavior, set as the default behavior), or not. ==> _copyGraphQLJavaSources_ renamed to _copyRuntimeSources_ in 1.7.0 version
* Removed the unused java annotation GraphQLCustomScalar
* Solved issue #35: argument for directive could not be CustomScalar, Boolean, Int, Float or enum (enum are still not valid on the server side of this plugin, due to a graphql-java 14.0 limitation, see https://github.com/graphql-java/graphql-java/issues/1844 for more details)

Client mode:
* Added GraphQL __fragment__ capability. Query/mutation/subscription can now contain Fragment(s) and inline Fragment(s), including directives on fragments.
* Added support for GraphQL __Union__
* The client code has been highly simplified :
    * The Builder support is now limited to the  _withQueryResponseDef(String query)_  method.
    * The support for the  _withField()_, _withSubObject()_ (...) methods has been removed
    * The  ___IntrospectionQuery_  class is no more generated: the ___schema_  and  ___type_  queries has been added to the GraphQL schema's query, as defined in the GraphQL spec. A default query is added if no query was defined in the GraphQL schema.  
    * The client code should now use the  _GraphQLRequest_  that is now generated along with the GraphQL java classes.
    * A _GraphQLConfiguration_ class has been added. The generated query/mutation/subscription classes create an instance of this class, so there is no impact on existing code. But this class must be used to configure the _GraphQLRequest_.
    * Please check the behavior of your full queries: the mutation keyword is now mandatory for mutations, as specified in the GraphQL specification. The query keyword remains optional. 
    * Please the [client mode doc](https://graphql-maven-plugin-project.graphql-java-generator.com/client.html) for more information.

# 1.5.0

Both modes (client and server):
- GraphQL __Directives__ are now managed
- GraphQL types can implement multiple interfaces
- Upgrade of graphql-java from v13.0 to v14.0

Client mode:
- Directives can be added in the query, on query and fields (fragment is for a next release, coming soon)
- The query/subscription/mutation classes have now a collection of __exec__ methods, which allows to execute several queries/mutations/subscriptions in one server call. This allows to add directive on the queries/mutations/subscriptions. Please the [client mode doc](https://graphql-maven-plugin-project.graphql-java-generator.com/client.html) for more information. 
- Added a queryName/mutationName/subscriptionName that accept bind parameters, for each query/mutation/subscription. Please have a look at the allGraphQLCases client tests, in the _org.allGraphQLCases.FullQueriesDirectIT_ class
- interfaces are properly deserialized, thanks to GraphQL introspection. 
(caution: code impact. Previously, for each interface, the plugin would generated a concrete class that doesn't exist in the GraphQL schema. This is not the case any more, and only GraphQL types are now generated
- The __typename is added to the list of scalar fields, for every request GraphQL nonscalar type. This allow to properly deserialize interfaces and unions.
  


# 1.4.0

Both modes (client and server):
- The plugin is compatible again with java 8
- The provided Date and DateTime scalars are now provided as a static field (instead of the class itself), due to a graphql-java change) 

Client mode:
- Can now invoke GraphQL introspection queries  (it was already the case on server side, thanks to graphql-java)

# 1.3.2

Both modes (client and server):
- Input parameters are now managed for scalar fields (custom or not)
- Removed the dependency to log4j, replaced by slf4j
- the GraphQL schema may now use java keywords (if the GraphQL schema uses identifiers that are java keywords, these identifiers are prefixed by an underscore in the generated code)

Client mode:
- Added a constructor in the generated query/mutation/subscription with a preconfigured Jersey client instance to support customization of the rest request

# 1.3.1

Both modes (client and server):
- The project now compiles up to JDK 13 (the generated code is still compatible with java 8 and higher)
- Unknown GraphQL concept are now ignored (instead of blocking the plugin work by throwing an error)
 


# 1.3


Both modes (client and server):
- Custom Scalars are now properly managed. You can provide your own Custom Scalars, or used the ones defined by graphql-java
- Fixed issue 8: Problem when using Boolean Type with property prefix "is"


# 1.2


Both modes (client and server):
- Corrected a bad dependency version, which prevents the released plugin to work
- Input object types are now accepted
- [CAUTION, code impact] All GraphQL exceptions have been moved into the com.graphql_java_generator.exception package

Client mode:
- Connection to https is made simpler (just declare the https URL)
- Input parameters are properly managed for queries, mutations and regular GraphQL type's field. It's possible to prepare query with Bind variables, like in JPA
- Only for request prepared by the Builder: simplification and change in the way to construct ObjectResponse objects
- Exception GraphQLExecutionException renamed to GraphQLRequestExecutionException

Server mode:
- XxxDataFetchersDelegate interfaces renamed as DataFetchersDelegateXxxx (code is easier to read, as the DataFetchersDelegates are grouped together in the classes list)
- Input parameters are accepted for queries, mutations and object's field.  
- Add of the generateJPAAnnotation plugin parameter (default to false)