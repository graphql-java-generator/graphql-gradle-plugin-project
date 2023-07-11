/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.dataloader.BatchLoaderEnvironment;
import org.gradle.api.Project;

import com.graphql_java_generator.plugin.conf.GenerateServerCodeConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.Packaging;
import com.graphql_java_generator.plugin.conf.PluginMode;

/**
 * @author etienne-sf
 */
public class GenerateServerCodeExtension extends GenerateCodeCommonExtension
		implements GenerateServerCodeConfiguration {

	/**
	 * <P>
	 * (only for server mode) Indicates if the plugin should generate add the {@link BatchLoaderEnvironment} parameter
	 * to the <I>batchLoader</I> methods, in DataFetchersDelegate. This parameter allows to get the context of the Batch
	 * Loader, including the context associated to the id, when using the id has been added by the
	 * {@link org.dataloader.DataLoader#load(Object, Object)} method.
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
	 * <B><I>Default value is false</I></B>
	 * </P>
	 */
	private boolean generateBatchLoaderEnvironment = GraphQLConfiguration.DEFAULT_GENERATE_BATCH_LOADER_ENVIRONMENT
			.equals("true");

	/**
	 * Indicates whether the plugin should generate the JPA annotations, for generated objects, when in server mode.
	 */
	private boolean generateJPAAnnotation = GraphQLConfiguration.DEFAULT_GENERATE_JPA_ANNOTATION.equals("true");

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
	private boolean generateDataLoaderForLists = GenerateServerCodeConfiguration.DEFAULT_GENERATE_DATA_LOADER_FOR_LISTS
			.equals("true");
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
	public String javaTypeForIDType = GenerateServerCodeConfiguration.DEFAULT_JAVA_TYPE_FOR_ID_TYPE;

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
	private String scanBasePackages = GraphQLConfiguration.DEFAULT_SCAN_BASE_PACKAGES;

	public GenerateServerCodeExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateBatchLoaderEnvironment() {
		return generateBatchLoaderEnvironment;
	}

	public void setGenerateBatchLoaderEnvironment(boolean generateBatchLoaderEnvironment) {
		this.generateBatchLoaderEnvironment = generateBatchLoaderEnvironment;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public boolean isGenerateJPAAnnotation() {
		return generateJPAAnnotation;
	}

	public void setGenerateJPAAnnotation(boolean generateJPAAnnotation) {
		this.generateJPAAnnotation = generateJPAAnnotation;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public boolean isGenerateDataLoaderForLists() {
		return generateDataLoaderForLists;
	}

	public final void setGenerateDataLoaderForLists(boolean generateDataLoaderForLists) {
		this.generateDataLoaderForLists = generateDataLoaderForLists;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	@Override
	public String getJavaTypeForIDType() {
		return javaTypeForIDType;
	}

	public void setJavaTypeForIDType(String javaTypeForIDType) {
		this.javaTypeForIDType = javaTypeForIDType;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

	/** The mode is forced to {@link PluginMode#server} */
	@Override
	public PluginMode getMode() {
		return PluginMode.server;
	}

	@Override
	public Packaging getPackaging() {
		// We calculate as late as possible this packaging. So no precaculation on creation, we wait for a call on this
		// getter. At this time, it should be triggered by the gradle plugin execution (and not its configuration)
		return (project.getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;
	}

	@Override
	public String getQuotedScanBasePackages() {
		String scanBasePackages = getScanBasePackages();

		if (scanBasePackages == null || scanBasePackages.contentEquals("") || scanBasePackages.contentEquals("null")) {
			return "";
		}

		// Let's remove all spaces. It will be easier to insert the good double quotes, afterwards.
		// Let's say scanBasePackages is: a, b, c,d
		scanBasePackages = scanBasePackages.replace(" ", "");// scanBasePackages is now a,b,c,d
		scanBasePackages = scanBasePackages.replace(",", "\",\"");// scanBasePackages is now a","b","c","d
		scanBasePackages = ",\"" + scanBasePackages + "\"";// scanBasePackages is now ,"a","b","c","d"
		return scanBasePackages;
	}

	@Override
	public String getScanBasePackages() {
		return scanBasePackages;
	}

	public void setScanBasePackages(String scanBasePackages) {
		this.scanBasePackages = scanBasePackages;
		// This task as being configured. So we'll mark compileJava and processResources as depending on it
		setInitialized(true);
	}

}
