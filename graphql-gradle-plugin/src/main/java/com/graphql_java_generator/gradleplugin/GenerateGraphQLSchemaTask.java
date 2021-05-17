/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchema;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchemaDocumentParser;

/**
 * Generates the code from the given GraphQL schema.
 * 
 * @author EtienneSF
 */
public class GenerateGraphQLSchemaTask extends DefaultTask implements GenerateGraphQLSchemaConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateGraphQLSchemaTask.class);

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GenerateGraphQLSchemaExtension extension = null;

	final Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param generateGraphQLSchemaExtension
	 *            The Gradle extension, which contains all parameters found in the build script
	 */
	@Inject
	public GenerateGraphQLSchemaTask(Project project, GenerateGraphQLSchemaExtension generateGraphQLSchemaExtension) {
		this.project = project;
		this.extension = generateGraphQLSchemaExtension;
	}

	@TaskAction
	public void execute() throws IOException {

		logger.debug("Executing " + this.getClass().getName() + " (extension is an instance of "
				+ extension.getClass().getName());

		// We'll use Spring IoC
		GenerateGraphQLSchemaSpringConfiguration.generateGraphQLSchemaExtension = extension;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateGraphQLSchemaSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateGraphQLSchemaConfiguration pluginConfiguration = ctx.getBean(GenerateGraphQLSchemaConfiguration.class);
		pluginConfiguration.logConfiguration();

		GenerateGraphQLSchemaDocumentParser documentParser = ctx.getBean(GenerateGraphQLSchemaDocumentParser.class);
		documentParser.parseDocuments();

		GenerateGraphQLSchema merge = ctx.getBean(GenerateGraphQLSchema.class);
		merge.generateGraphQLSchema();

		ctx.close();

		// Let's add the folders where the GraphQL schemas have been generated to the project
		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getResources().srcDir(extension.getTargetResourceFolder());

		logger.debug("Finished generation of the merged schema");
	}

	@Override
	@Internal
	public String getDefaultTargetSchemaFileName() {
		return GenerateGraphQLSchemaConfiguration.DEFAULT_TARGET_SCHEMA_FILE_NAME;
	}

	@Override
	@Internal
	public File getProjectDir() {
		return project.getProjectDir();
	}

	@Override
	@Input
	public String getResourceEncoding() {
		return extension.getResourceEncoding();
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
	@InputDirectory
	public File getTargetFolder() {
		return extension.getTargetFolder();
	}

	@Override
	@Input
	public String getTargetSchemaFileName() {
		return extension.getTargetSchemaFileName();
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
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return extension.isSkipGenerationIfSchemaHasNotChanged();
	}

}
