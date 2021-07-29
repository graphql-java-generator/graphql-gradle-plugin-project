package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.dataloader.BatchLoaderEnvironment;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

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
	private Boolean generateBatchLoaderEnvironment;

	/**
	 * Indicates whether the plugin should generate the JPA annotations, for generated objects, when in server mode.
	 */
	private Boolean generateJPAAnnotation;

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
	public String javaTypeForIDType;

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
	private String schemaPersonalizationFile;

	@Inject
	public GenerateServerCodeTask() {
		super(GenerateServerCodeExtension.class);
	}

	public GenerateServerCodeTask(Class<? extends GenerateServerCodeExtension> extensionClazz) {
		super(extensionClazz);
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
		documentParser.parseDocuments();

		GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
		codeGenerator.generateCode();

		ctx.close();

		registerGeneratedFolders();

		logger.debug("Finished generation of java classes from graphqls files (5)");
	}

	@Override
	@Input
	final public String getJavaTypeForIDType() {
		return getValue(javaTypeForIDType, getExtension().getJavaTypeForIDType());
	}

	public final void setJavaTypeForIDType(String javaTypeForIDType) {
		this.javaTypeForIDType = javaTypeForIDType;
	}

	@Input
	@Override
	public PluginMode getMode() {
		return PluginMode.server;
	}

	@Internal
	@Override
	final public Packaging getPackaging() {
		// We calculate as late as possible this packaging. So no precaculation on creation, we wait for a call on this
		// getter. At this time, it should be triggered by the gradle plugin execution (and not its configuration)
		return (getProject().getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;
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
	}

	@Override
	@InputFile
	@Optional
	final public File getSchemaPersonalizationFile() {
		return getFileValue(schemaPersonalizationFile, getExtension().getSchemaPersonalizationFile());
	}

	public final void setSchemaPersonalizationFile(String schemaPersonalizationFile) {
		this.schemaPersonalizationFile = schemaPersonalizationFile;
	}

	@Override
	@Input
	final public boolean isGenerateBatchLoaderEnvironment() {
		return getValue(generateBatchLoaderEnvironment, getExtension().isGenerateBatchLoaderEnvironment());
	}

	public final void setGenerateBatchLoaderEnvironment(boolean generateBatchLoaderEnvironment) {
		this.generateBatchLoaderEnvironment = generateBatchLoaderEnvironment;
	}

	@Override
	@Input
	final public boolean isGenerateJPAAnnotation() {
		return getValue(generateJPAAnnotation, getExtension().isGenerateJPAAnnotation());
	}

	public final void setGenerateJPAAnnotation(boolean generateJPAAnnotation) {
		this.generateJPAAnnotation = generateJPAAnnotation;
	}

	@Override
	protected GenerateServerCodeExtension getExtension() {
		return (GenerateServerCodeExtension) super.getExtension();
	}
}
