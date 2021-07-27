/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.util.Map;

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
	 * look at the
	 * <A HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/schema_personalization.html">Schema
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

	/**
	 * <P>
	 * schemaPersonalizationFile is the file name where the GraphQL maven plugin will find personalization that it must
	 * apply before generating the code. This applies to the <B>server</B> mode only. See
	 * <A HREF="https://graphql-maven-plugin-project.graphql-java-generator.com/schema_personalization.html">the doc on
	 * the plugin web site</A> for more details.
	 * </P>
	 * <P>
	 * The standard file would be something like /src/main/graphql/schemaPersonalizationFile.json, which avoids to embed
	 * this compile time file within your maven artifact (as it is not in the /src/main/java nor in the
	 * /src/main/resources folders).
	 * </P>
	 */
	private String schemaPersonalizationFile = GraphQLConfiguration.DEFAULT_SCHEMA_PERSONALIZATION_FILE;

	public GenerateServerCodeExtension(Project project) {
		super(project);
	}

	@Override
	public boolean isGenerateBatchLoaderEnvironment() {
		return generateBatchLoaderEnvironment;
	}

	public void setGenerateBatchLoaderEnvironment(boolean generateBatchLoaderEnvironment) {
		this.generateBatchLoaderEnvironment = generateBatchLoaderEnvironment;
	}

	@Override
	public boolean isGenerateJPAAnnotation() {
		return generateJPAAnnotation;
	}

	public void setGenerateJPAAnnotation(boolean generateJPAAnnotation) {
		this.generateJPAAnnotation = generateJPAAnnotation;
	}

	@Override
	public String getJavaTypeForIDType() {
		return javaTypeForIDType;
	}

	public void setJavaTypeForIDType(String javaTypeForIDType) {
		this.javaTypeForIDType = javaTypeForIDType;
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
	}

	@Override
	public File getSchemaPersonalizationFile() {
		return (GraphQLConfiguration.DEFAULT_SCHEMA_PERSONALIZATION_FILE.equals(schemaPersonalizationFile)) ? null
				: project.file(schemaPersonalizationFile);
	}

	public void setSchemaPersonalizationFile(String schemaPersonalizationFile) {
		this.schemaPersonalizationFile = schemaPersonalizationFile;
	}

}
