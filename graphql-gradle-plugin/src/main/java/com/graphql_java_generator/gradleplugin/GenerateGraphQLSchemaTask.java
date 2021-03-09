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
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.conf.Logger;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchema;
import com.graphql_java_generator.plugin.generate_schema.GenerateGraphQLSchemaDocumentParser;

/**
 * Generates the code from the given GraphQL schema.
 * 
 * @author EtienneSF
 */
public class GenerateGraphQLSchemaTask extends DefaultTask implements GenerateGraphQLSchemaConfiguration {

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GenerateGraphQLSchemaExtension generateGraphQLSchemaExtension = null;

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
		this.generateGraphQLSchemaExtension = generateGraphQLSchemaExtension;
	}

	@TaskAction
	public void execute() throws IOException {

		getPluginLogger().debug("Starting merging of the given GraphQL schemas");

		// We'll use Spring IoC
		GenerateGraphQLSchemaSpringConfiguration.generateGraphQLSchemaExtension = generateGraphQLSchemaExtension;
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

		getPluginLogger().debug("Finished generation of the merged schema");
	}

	@Override
	@Internal
	public Logger getPluginLogger() {
		return generateGraphQLSchemaExtension.getPluginLogger();
	}

	@Override
	@Input
	public String getResourceEncoding() {
		return generateGraphQLSchemaExtension.getResourceEncoding();
	}

	@Override
	@InputDirectory
	@Optional
	public File getSchemaFileFolder() {
		return generateGraphQLSchemaExtension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return generateGraphQLSchemaExtension.getSchemaFilePattern();
	}

	@Override
	@InputDirectory
	public File getTargetFolder() {
		return generateGraphQLSchemaExtension.getTargetFolder();
	}

	@Override
	@Input
	public String getTargetSchemaFileName() {
		return generateGraphQLSchemaExtension.getTargetSchemaFileName();
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return generateGraphQLSchemaExtension.getTemplates();
	}

	@Override
	@Input
	public boolean isAddRelayConnections() {
		return generateGraphQLSchemaExtension.isAddRelayConnections();
	}

	@Override
	@Input
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return generateGraphQLSchemaExtension.isSkipGenerationIfSchemaHasNotChanged();
	}

}
