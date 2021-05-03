/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.conf.GeneratePojoConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.Packaging;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

/**
 * <P>
 * The <I>generatePojo</I> goal generates all the java objects that match the provided GraphQL schema. It allows to work
 * in Java with graphQL, in a schema first approach.
 * </P>
 * This goal generates:
 * <UL>
 * <LI>One java interface for each GraphQL `union` and `interface`</LI>
 * <LI>One java class for each GraphQL `type` and `input` type, including the query, mutation and subscription (if any).
 * If the GraphQL type implements an interface, then its java class implements this same interface</LI>
 * <LI>One java enum for each GraphQL enum</LI>
 * </UL>
 * 
 * <P>
 * Every class, interface and their attributes are marked with the annotation from the <A HREF=
 * "https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/annotation/package-summary.html">GraphQL
 * annotation</A> package. This allows to retrieve the GraphQL information for every class, interface and attribute, at
 * runtime.
 * 
 * <P>
 * It can run in two modes (see the <A HREF=
 * "https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generatePojo-mojo.html#mode">mode
 * plugin parameter</A> for more information):
 * </P>
 * <UL>
 * <LI><B>server</B>: In the server mode, only the GraphQL annotation are added. You can add the JPA annotation, with
 * the <I>generateJPAAnnotation</I> plugin parameter set to true. In this mode, as with the <I>generateServerCode</I>,
 * you need to either add the <I>graphql-java-server-dependencies</I> dependencies, or set the <I>copyRuntimeSources</I>
 * plugin parameter to false and add the <I>graphql-java-runtime</I>.</LI>
 * <LI><B>client</B>: The client mode is the default one. This mode generates the same POJO as in server mode, with the
 * addition of the <A HREF="https://github.com/FasterXML/jackson">Jackson</A> annotations. These annotations allows to
 * serialize and unserialize the GraphQL POJO to and from JSON. And the <I>CustomJacksonDeserializers</I> utility class
 * is generated, that allows to deserialize custom scalars and arrays. In this mode, as with the
 * <I>generateServerCode</I>, you need to either add the <I>graphql-java-client-dependencies</I> dependencies, or set
 * the <I>copyRuntimeSources</I> plugin parameter to false and add the <I>graphql-java-runtime</I>.</LI>
 * </UL>
 * 
 * @author EtienneSF
 */
public class GeneratePojoTask extends DefaultTask implements GeneratePojoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GeneratePojoTask.class);

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GeneratePojoExtension extension = null;

	final private Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param generatePojoExtension
	 *            The generatePojo extension, which contains all parameters found in the build script
	 */
	@Inject
	public GeneratePojoTask(Project project, GeneratePojoExtension generatePojoExtension) {
		this.project = project;
		this.extension = generatePojoExtension;
	}

	@TaskAction
	public void execute() {
		try {

			logger.debug("Executing " + this.getClass().getName());

			// We'll use Spring IoC
			GraphQLGenerateCodeSpringConfiguration.graphqlExtension = extension;
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
					GraphQLGenerateCodeSpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			GraphQLConfiguration pluginConfiguration = ctx.getBean(GraphQLConfiguration.class);
			pluginConfiguration.logConfiguration();

			GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
			documentParser.parseDocuments();

			GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
			int nbGeneratedClasses = codeGenerator.generateCode();

			ctx.close();

			logger.info(nbGeneratedClasses + " java classes have been generated from the schema(s) '"
					+ pluginConfiguration.getSchemaFilePattern() + "' in the package '"
					+ pluginConfiguration.getPackageName() + "'");

			logger.debug("Finished generation of java classes from graphqls files (5)");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Override
	@Input
	public List<CustomScalarDefinition> getCustomScalars() {
		return extension.getCustomScalars();
	}

	@Override
	@Internal
	public String getDefaultTargetSchemaFileName() {
		return GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME;
	}

	@Override
	@Input
	public String getJavaTypeForIDType() {
		return extension.javaTypeForIDType;
	}

	@Override
	@Input
	public PluginMode getMode() {
		return extension.getMode();
	}

	@Override
	@Input
	public String getPackageName() {
		return extension.getPackageName();
	}

	@Override
	@Input
	public Packaging getPackaging() {
		return extension.getPackaging();
	}

	@Override
	@Internal
	public File getProjectDir() {
		return project.getProjectDir();
	}

	@Override
	@Input
	public String getScanBasePackages() {
		return extension.getScanBasePackages();
	}

	@Override
	@Internal
	public String getQuotedScanBasePackages() {
		return ((GraphQLConfiguration) this).getQuotedScanBasePackages();
	}

	@Override
	@InputDirectory
	@Optional
	public File getSchemaFileFolder() {
		return extension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return extension.getSchemaFilePattern();
	}

	@Override
	@InputFile
	@Optional
	public File getSchemaPersonalizationFile() {
		return extension.getSchemaPersonalizationFile();
	}

	@Override
	@Input
	public String getSourceEncoding() {
		return extension.getSourceEncoding();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		// TODO Understand why project.file("$buildDir/classes") doesn't work
		return project.file("build/classes/java/main");
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		// TODO Understand why project.file("$buildDir/classes") doesn't work
		return project.file("build/generated/" + GraphQLPlugin.GENERATE_POJO_TASK_NAME);
	}

	@Override
	@OutputDirectory
	public File getTargetResourceFolder() {
		// TODO Understand why project.file("$buildDir/resources") doesn't work
		return project.file("build/resources/main");
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return extension.getTemplates();
	}

	@Override
	@Input
	public boolean isAddRelayConnections() {
		return extension.isAddRelayConnections();
	}

	@Override
	@Input
	public boolean isCopyRuntimeSources() {
		return extension.isCopyRuntimeSources();
	}

	@Override
	@Input
	public boolean isGenerateBatchLoaderEnvironment() {
		return extension.isGenerateBatchLoaderEnvironment();
	}

	@Override
	@Input
	public boolean isGenerateDeprecatedRequestResponse() {
		return extension.isGenerateDeprecatedRequestResponse();
	}

	@Override
	@Input
	public boolean isGenerateJPAAnnotation() {
		return extension.isGenerateJPAAnnotation();
	}

	@Override
	@Internal
	public boolean isGenerateUtilityClasses() {
		return extension.isGenerateUtilityClasses();
	}

	@Override
	@Input
	public boolean isSeparateUtilityClasses() {
		return extension.isSeparateUtilityClasses();
	}

	@Override
	@Input
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return extension.isSkipGenerationIfSchemaHasNotChanged();
	}
}
