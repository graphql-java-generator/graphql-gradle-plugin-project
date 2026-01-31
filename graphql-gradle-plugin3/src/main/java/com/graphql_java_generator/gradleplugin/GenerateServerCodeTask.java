package com.graphql_java_generator.gradleplugin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.dataloader.BatchLoaderEnvironment;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.plugin.conf.BatchMappingDataFetcherReturnType;
import com.graphql_java_generator.plugin.conf.GenerateServerCodeConfiguration;
import com.graphql_java_generator.plugin.conf.Packaging;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;
import com.graphql_java_generator.plugin.language.DataFetchersDelegate;
import com.graphql_java_generator.util.GraphqlUtils;

/**
 * <P>
 * The <I>generateServerCode</I> Maven goal (and Gradle task) generates the java code for an almost ready to start
 * GraphQL server. The developer has only to develop request to the data.
 * </P>
 * <P>
 * The java code is generated from one or more GraphQL schemas. It allows to work in Java with graphQL, in a schema
 * first approach. These items are generated:
 * </P>
 * <UL>
 * <LI>the main method (in a jar project) or the main servlet (in a war project)</LI>
 * <LI>All the GraphQL wiring, based on graphql-java-spring, itself being build on top of graphql-java</LI>
 * <LI>All the POJOs, that contain the incoming request contents. The request response is written by the user code into
 * these POJO, and the plugin take care of mapping them into the server response.</LI>
 * <LI>An option allows to annotate the POJOs with the standard JPA annotations, to make it easy to link with a
 * database. Please note that a</LI>
 * <LI>All the interfaces for the {@link DataFetchersDelegate} (named providers in the graphql.org presentation) that
 * the server needs to implement</LI>
 * </UL>
 * <P>
 * The specific code that needs to be implemented is the access to the Data: your database, other APIs or web services,
 * or any kind of storage you may have. This is done by implementing the interfaces for the {@link DataFetchersDelegate}
 * into a Spring component, that is:
 * </P>
 * <UL>
 * <LI>Create a class for each generated {@link DataFetchersDelegate} interface</LI>
 * <LI>Make it implement the relevant {@link DataFetchersDelegate} interface</LI>
 * <LI>Mark it with the {@link Component} annotation</LI>
 * </UL>
 * <P>
 * And you're done! :)
 * </P>
 * <P>
 * You'll find more info in the tutorials: take a look at the
 * <A HREF="https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-server">Maven server tutorial</A> or
 * the <A HREF="https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server">Gradle server
 * tutorial</A>
 * </P>
 * <P>
 * <B>Note:</B> The attribute have no default values: their default values is read from the
 * {@link GenerateCodeCommonExtension}, whose attributes can be either the default value, or a value set in the build
 * script.
 * </P>
 * 
 * @author etienne-sf
 */
public class GenerateServerCodeTask extends GenerateCodeCommonTask implements GenerateServerCodeConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateServerCodeTask.class);
	/**
	 * <P>
	 * This parameter is used only when generateBatchMappingDataFetchers is set to <i>true</i>. It determines the return
	 * type of the data fetchers, as defined in the <a href=
	 * "https://docs.spring.io/spring-graphql/reference/controllers.html#controllers.batch-mapping.return.values">spring-graphql
	 * documentation</a>.
	 * </P>
	 * <P>
	 * The allowed values are (where K is the key type, that is: the parent object, and V is the value to be loaded in
	 * batch):
	 * </P>
	 * <TABLE>
	 * <TR>
	 * <TH>Value</TH>
	 * <TH>Return type</TH>
	 * </TR>
	 * <TR>
	 * <TD>MONO_MAP</TD></TD>Mono&lt;Map&lt;K,V&gt;&gt;</TD>
	 * </TR>
	 * <TR>
	 * <TD>MAP</TD>
	 * <TD>Map&lt;K,V&gt;</TD>
	 * </TR>
	 * <TR>
	 * <TD>FLUX</TD>
	 * <TD>Flux&lt;V&gt;</TD>
	 * </TR>
	 * <TR>
	 * <TD>COLLECTION</TD>
	 * <TD>Collection&lt;V&gt;</TD>
	 * </TR>
	 * </TABLE>
	 * <P>
	 * The default value is <code>Flux&lt;V&gt;</code>
	 * </P>
	 * <P>
	 * For an easier use of this parameter, the comment of the generated data fetchers details the exact expected type.
	 * </P>
	 */
	BatchMappingDataFetcherReturnType batchMappingDataFetcherReturnType;

	/**
	 * <P>
	 * (only for server mode) Indicates if the plugin should generate add the {@link BatchLoaderEnvironment} parameter
	 * to the <I>batchLoader</I> methods, in DataFetchersDelegate. This parameter allows to get the context of the Batch
	 * Loader, including the context associated to the id, when using the id has been added by the
	 * {@link org.dataloader.DataLoader#load(Object, Object)} method.
	 * </P>
	 * <P>
	 * This parameter is <b>forced to true</b> when the <code>generateBatchMappingDataFetchers</code> parameter is set
	 * to <i>true</i>.
	 * </P>
	 * <P>
	 * For instance, if you have the method below, for a field named <I>oneWithIdSubType</I> in a DataFetcherDelegate:
	 * </P>
	 * 
	 * <PRE>
	 * &#64;Override
	 * public CompletableFuture&lt;AllFieldCasesWithIdSubtype> oneWithIdSubType(
	 * 		DataFetchingEnvironment dataFetchingEnvironment, DataLoader&lt;UUID, AllFieldCasesWithIdSubtype> dataLoader,
	 * 		AllFieldCases source, Boolean uppercase) {
	 * 	return dataLoader.load(UUID.randomUUID());
	 * }
	 * </PRE>
	 * <P>
	 * then, in the <I>AllFieldCasesWithIdSubtype</I> DataFetcherDelegate, you can retrieve the uppercase this way:
	 * </P>
	 * 
	 * <PRE>
	 * &#64;Override
	 * public List&lt;AllFieldCasesWithIdSubtype> batchLoader(List&lt;UUID> keys, BatchLoaderEnvironment environment) {
	 * 	List&lt;AllFieldCasesWithIdSubtype> list = new ArrayList<>(keys.size());
	 * 	for (UUID id : keys) {
	 * 		// Let's manage the uppercase parameter, that was associated with this key
	 * 		Boolean uppercase = (Boolean) environment.getKeyContexts().get(id);
	 * 		if (uppercase != null && uppercase) {
	 * 			item.setName(item.getName().toUpperCase());
	 * 		}
	 * 
	 * 		// Do something with the id and the uppercase value
	 * 	}
	 * 	return list;
	 * }
	 * </PRE>
	 * <P>
	 * For more complex cases, you can store a {@link Map} with all the needed values, instead of just the parameter
	 * value.
	 * </P>
	 * <P>
	 * <B><I>The default value changed since 2.0 version: it is false in 1.x version, and true since the 2.0
	 * version</I></B>
	 * </P>
	 */
	private Boolean generateBatchLoaderEnvironment;

	/**
	 * <P>
	 * If this parameter is set to <i>true</i>, the spring GraphQL controller methods will be annotated with the
	 * <code>@BatchMapping</code> (instead of the <code>@SchemaMapping</code>). This allows to manage the of the N+1
	 * select problem: so this allows much better performances, by highly diminishing the number of executed requests
	 * (avoid to execute several times the same "sub-query")
	 * <P>
	 * </P>
	 * When setting this parameter to <i>true</i>, the main changes are:
	 * </P>
	 * <UL>
	 * <LI>The <code>@BatchMapping</code> annotation may be applied to all data fetchers without argument(s) that return
	 * either a List, a Type, an Interface or an Union.</LI>
	 * <LI>The return type must be defined in the controller: it may not be `Object`, as spring-graphql builds the
	 * proper BatchLoader while loading the controllers, when the server starts. The return type for this method is
	 * managed by the <code>batchMappingMethodReturnType</code> plugin parameter</LI>
	 * <LI>DataLoader is managed transparently by spring (instead of having to declare it in the generated controller,
	 * and having it as a parameter in the generated data fetchers)</LI>
	 * <LI>The batch mapping is generalized on all data fetchers</LI>
	 * <LI>The <code>DataFetchersDelegate</code> method's signature changes</LI>
	 * <LI>The <code>generateBatchLoaderEnvironment</code>, <code>generateDataFetcherForEveryFieldsWithArguments</code>
	 * and <code>generateDataLoaderForLists</code> plugin parameters are ignored</LI>
	 * </UL>
	 * <P>
	 * A typical method signature for a data fetcher would be as below, where the return type is controller by the
	 * <code>batchMappingMethodReturnType</code> plugin parameter :
	 * </P>
	 * 
	 * <PRE>
	 * public Flux<Topic> topics(//
	 * 		BatchLoaderEnvironment batchLoaderEnvironment, //
	 * 		GraphQLContext graphQLContext, //
	 * 		List<Board> boards);
	 * </PRE>
	 * <P>
	 * Please note that the <code>@BatchMapping</code> annotation is a shortcut to avoid boilerplate code, for the most
	 * common cases. See <a href="https://github.com/spring-projects/spring-graphql/issues/232">this discussion</a> for
	 * more information on this. For most complex cases, the use of a DataLoader is recommended by the spring-graphql
	 * case. And in these cases, the plugin will generate a method with the <code>@SchemaMapping</code> annotation
	 * </P>
	 */
	public Boolean generateBatchMappingDataFetchers;
	/**
	 * <P>
	 * (only for server mode, since 2.5) Defines if a data fetcher is needed for every GraphQL field that has input
	 * argument, and add them in the generated POJOs. This allows a better compatibility with spring-graphql, and an
	 * easy access to the field's parameters.
	 * </P>
	 * <P>
	 * This parameter is <b>forced to true</b> when the <code>generateBatchMappingDataFetchers</code> parameter is set
	 * to <i>true</i>.
	 * </P>
	 * <P>
	 * With this argument to false, the data fetchers are generated only for field which type is a type (not a scalar or
	 * an enum), and for the query, mutation and subscription types.
	 * </P>
	 * <P>
	 * With this argument to true, the data fetchers are generated for all GraphQL fields which type is a type (not a
	 * scalar or an enum) <b><i>or</i></b> that has one or arguments
	 * </P>
	 * <P>
	 * This parameter is available since version 2.5:
	 * </P>
	 * <UL>
	 * <LI>From 2.5 to 3.0: Default value is false in 2.x versions for backward compatibility with existing
	 * implementations based on the plugin. But the <b>recommended value is true</b>.</LI>
	 * <LI>From 3.0.1: Default value is true</LI>
	 * </UL>
	 */
	private Boolean generateDataFetcherForEveryFieldsWithArguments;

	/**
	 * <P>
	 * (only for server mode) Defines how the methods in the data fetchers delegates are generated. The detailed
	 * information is available in the
	 * <a href="https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/server">Wiki server page</a>
	 * </P>
	 * <P>
	 * When generateDataLoaderForLists is false (default mode), the data loaders are used only for fields that don't
	 * return a list. In other words, for fields which type is a sub-object with an id, two methods are generated: one
	 * which returns a {@link CompletableFuture}, and one which returns a none {@link CompletableFuture} result (that is
	 * used by the generated code only if no data loader is available).
	 * </P>
	 * <P>
	 * This parameter is <b>forced to true</b> when the <code>generateBatchMappingDataFetchers</code> parameter is set
	 * to <i>true</i>.
	 * </P>
	 * <P>
	 * When generateDataLoaderForLists is true, the above behavior is extended to fields that are a list.
	 * </P>
	 * <P>
	 * Note: if set to true, this plugin parameter make the use of data loader mandatory for every field which type is a
	 * list of GraphQL objects, which have an id. This may not be suitable, for instance when your data is stored in a
	 * relational database, where you would need a first query to retrieve the ids and push them into the data loader,
	 * then another one to retrieve the associated values. If you want to use data loader for only some of particular
	 * fields, you should <b>consider using the <code>generateDataLoaderForLists</code></b>. You'll find more
	 * information on the
	 * <a href="https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/server">Wiki server
	 * page</a>.
	 * </P>
	 * <P>
	 * This parameter is available since version 1.18.4
	 * </P>
	 */
	private Boolean generateDataLoaderForLists;

	/**
	 * Indicates whether the plugin should generate the JPA annotations, for generated objects, when in server mode.
	 */
	private Boolean generateJPAAnnotation;

	/**
	 * The gradle plugin can't easily detect the packaging mode. This parameter allows to choose between jar (the
	 * default packaging) and war.
	 */
	private Packaging packaging;

	/**
	 * <P>
	 * This parameter marks a list of GraphQL mappings as ignored, so that they are not generated by the plugin. These
	 * ignored mappings can then be defined by the specific implementation.
	 * </P>
	 * <P>
	 * The other way to it is to create a spring GraphQL Controller, that overrides the controller generated by the
	 * plugin. But this may lead to this error:
	 * <code>Ambiguous mapping. Cannot map 'xxxController' method [...] to 'Type.field': there is already 'yyy' bean method [...] mapped.</code>
	 * </P>
	 * <P>
	 * The parameter may contain:
	 * </P>
	 * <UL>
	 * <LI>The '*' character: this would mark all controllers and DataFetchersDeleagate to be ignored. That is: none
	 * would be generated, and it's up to the specific implementation to manage them. In this mode, you still benefit of
	 * the POJO generation, the type wiring, the custom scalars mapping...</LI>
	 * <LI>A list of:</LI>
	 * <UL>
	 * <LI>GraphQL type name: The full controller class for this type is ignored, and won't be generated</LI>
	 * <LI>GraphQL type's field name: The method in the controller of this type, for this field, is ignored, and won't
	 * be generated. The field must be written like this: <code>{type name}.{field name}</code></LI>
	 * </UL>
	 * </UL>
	 * <P>
	 * The accepted separators for the values are: comma, space, carriage return, end of line, space, tabulation. At
	 * least one separator must exist between two values in the list. Here is a sample:
	 * </P>
	 * 
	 * <PRE>
	 *          <ignoredSpringMappings>Type1, Type2.field1
	 *          	Type3
	 *          	Type4.field2
	 *          </ignoredSpringMappings>
	 * </PRE>
	 * <P>
	 * For field mapping, there must be no separator other than '.' between the type name and the field name. For
	 * instance, the following type declaration are invalid: 'type .field', 'type. field'
	 * </P>
	 * <P>
	 * To implement the ignored mappings, you'll have to follow the [spring-graphql
	 * documentation](https://docs.spring.io/spring-graphql/reference/controllers.html).
	 * </P>
	 */
	public String ignoredSpringMappings;

	/**
	 * <P>
	 * The <I>javaTypeForIDType</I> is the java class that is used in the generated code for GraphQL fields that are of
	 * the GraphQL ID type. The default value is <I>java.util.UUID</I>. Valid values are: java.lang.String,
	 * java.lang.Long and java.util.UUID.
	 * </P>
	 * <P>
	 * This parameter is only valid for the server mode. When generating the client code, the ID is always generated as
	 * a String type, as recommended in the GraphQL doc.
	 * </P>
	 * <P>
	 * In other words: when in server mode and <I>javaTypeForIDType</I> is not set, all GraphQL ID fields are UUID
	 * attributes in java. When in server mode and <I>javaTypeForIDType</I> is set to the X type, all GraphQL ID fields
	 * are X attributes in java.
	 * </P>
	 * <P>
	 * Note: you can override this, by using the schema personalization capability. For more information, please have a
	 * look at the <A HREF=
	 * "https://github.com/graphql-java-generator.com/graphql-maven-plugin-project/wiki/usage_schema_personalization">Schema
	 * Personalization doc page</A>.
	 * </P>
	 * 
	 */
	private String javaTypeForIDType;

	/**
	 * <P>
	 * (only for server mode) A comma separated list of package names, <B>without</B> double quotes, that will also be
	 * parsed by Spring, to discover Spring beans, Spring repositories and JPA entities when the server starts. You
	 * should use this parameter only for packages that are not subpackage of the package defined in the _packageName_
	 * parameter and not subpackage of <I>com.graphql_java_generator</I>
	 * </P>
	 * <P>
	 * This allows for instance, to set <I>packageName</I> to <I>your.app.package.graphql</I>, and to define your Spring
	 * beans, like the
	 * <A HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/server.html">DataFetcherDelegates</A> or
	 * your Spring data repositories in any other folder, by setting for instance scanBasePackages to
	 * <I>your.app.package.impl, your.app.package.graphql</I>, or just <I>your.app.package</I>
	 * </P>
	 */
	private String scanBasePackages;

	/**
	 * @param projectLayout
	 *            This Gradle service is automatically injected by gradle. It allows to retrieve the project directory,
	 *            as accessing the Gradle {@link Project} is forbidden from a task.
	 */
	@Inject
	public GenerateServerCodeTask(ProjectLayout projectLayout) {
		super(new GenerateServerCodeExtension(projectLayout, Packaging.jar), projectLayout);
	}

	public GenerateServerCodeTask(GenerateServerCodeExtension extension, ProjectLayout projectLayout) {
		super(extension, projectLayout);
	}

	@TaskAction
	public void execute() throws IOException {

		logger.debug("Executing " + this.getClass().getName());

		// We'll use Spring IoC
		GenerateServerCodeSpringConfiguration.generateServerCodeConf = this;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateServerCodeSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateServerCodeConfiguration pluginConfiguration = ctx.getBean(GenerateServerCodeConfiguration.class);
		pluginConfiguration.logConfiguration();

		GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
		documentParser.parseGraphQLSchemas();

		GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
		codeGenerator.generateCode();

		ctx.close();

		logger.debug("Finished generation of java classes from graphqls files (5)");
	}

	@Override
	@Input
	public BatchMappingDataFetcherReturnType getBatchMappingDataFetcherReturnType() {
		return getValue(batchMappingDataFetcherReturnType, getExtension().getBatchMappingDataFetcherReturnType());
	}

	public void setBatchMappingDataFetcherReturnType(
			BatchMappingDataFetcherReturnType batchMappingDataFetcherReturnType) {
		this.batchMappingDataFetcherReturnType = batchMappingDataFetcherReturnType;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	public String getIgnoredSpringMappings() {
		return getValue(ignoredSpringMappings, getExtension().getIgnoredSpringMappings());
	}

	public final void setIgnoredSpringMappings(String ignoredSpringMappings) {
		this.ignoredSpringMappings = ignoredSpringMappings;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	final public String getJavaTypeForIDType() {
		return getValue(javaTypeForIDType, getExtension().getJavaTypeForIDType());
	}

	public final void setJavaTypeForIDType(String javaTypeForIDType) {
		this.javaTypeForIDType = javaTypeForIDType;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Input
	@Override
	public PluginMode getMode() {
		return PluginMode.server;
	}

	@Input
	@Optional
	@Override
	public Packaging getPackaging() {
		return getValue(packaging, ((GenerateServerCodeExtension) extension).getPackaging());
	}

	public void setPackaging(Packaging packaging) {
		this.packaging = packaging;
	}

	@Internal
	@Override
	public String getQuotedScanBasePackages() {
		return GraphqlUtils.graphqlUtils.getQuotedScanBasePackages(getScanBasePackages());
	}

	@Input
	@Override
	final public String getScanBasePackages() {
		return getValue(scanBasePackages, getExtension().getScanBasePackages());
	}

	public final void setScanBasePackages(String scanBasePackages) {
		this.scanBasePackages = scanBasePackages;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	final public boolean isGenerateBatchLoaderEnvironment() {
		return getValue(generateBatchLoaderEnvironment, getExtension().isGenerateBatchLoaderEnvironment());
	}

	public final void setGenerateBatchLoaderEnvironment(boolean generateBatchLoaderEnvironment) {
		this.generateBatchLoaderEnvironment = generateBatchLoaderEnvironment;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	public boolean isGenerateBatchMappingDataFetchers() {
		return getValue(generateBatchMappingDataFetchers, getExtension().isGenerateBatchMappingDataFetchers());
	}

	public void setGenerateBatchMappingDataFetchers(boolean generateBatchMappingDataFetchers) {
		this.generateBatchMappingDataFetchers = generateBatchMappingDataFetchers;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	final public boolean isGenerateDataFetcherForEveryFieldsWithArguments() {
		return getValue(generateDataFetcherForEveryFieldsWithArguments,
				getExtension().isGenerateDataFetcherForEveryFieldsWithArguments());
	}

	public final void setGenerateDataFetcherForEveryFieldsWithArguments(
			boolean generateDataFetcherForEveryFieldsWithArguments) {
		this.generateDataFetcherForEveryFieldsWithArguments = generateDataFetcherForEveryFieldsWithArguments;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Internal
	@Override
	public boolean isGenerateJacksonAnnotations() {
		return false;
	}

	@Override
	@Input
	final public boolean isGenerateJPAAnnotation() {
		return getValue(generateJPAAnnotation, getExtension().isGenerateJPAAnnotation());
	}

	public final void setGenerateJPAAnnotation(boolean generateJPAAnnotation) {
		this.generateJPAAnnotation = generateJPAAnnotation;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	@Input
	public boolean isGenerateDataLoaderForLists() {
		return getValue(generateDataLoaderForLists, getExtension().isGenerateDataLoaderForLists());
	}

	public final void setGenerateDataLoaderForLists(boolean generateDataLoaderForLists) {
		this.generateDataLoaderForLists = generateDataLoaderForLists;
		// This task is now configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	protected GenerateServerCodeExtension getExtension() {
		return (GenerateServerCodeExtension) super.getExtension();
	}
}
