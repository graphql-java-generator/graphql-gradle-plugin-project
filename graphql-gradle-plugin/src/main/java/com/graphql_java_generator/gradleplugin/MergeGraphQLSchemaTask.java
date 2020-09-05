/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.File;
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

import com.graphql_java_generator.plugin.Logger;
import com.graphql_java_generator.plugin.Merge;
import com.graphql_java_generator.plugin.MergeConfiguration;
import com.graphql_java_generator.plugin.MergeDocumentParser;

/**
 * Generates the code from the given GraphQL schema.
 * 
 * @author EtienneSF
 */
public class MergeGraphQLSchemaTask extends DefaultTask implements MergeConfiguration {

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient MergeGraphQLSchemaExtension mergeGraphQLSchemaExtension = null;

	final Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param mergeGraphQLSchemaExtension
	 *            The Gradle extension, which contains all parameters found in the build script
	 */
	@Inject
	public MergeGraphQLSchemaTask(Project project, MergeGraphQLSchemaExtension mergeGraphQLSchemaExtension) {
		this.project = project;
		this.mergeGraphQLSchemaExtension = mergeGraphQLSchemaExtension;
	}

	@TaskAction
	public void execute() {

		getLog().debug("Starting merging of the given GraphQL schemas");

		// We'll use Spring IoC
		MergeGraphQLSchemaSpringConfiguration.mergeGraphQLSchemaExtension = mergeGraphQLSchemaExtension;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				MergeGraphQLSchemaSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		MergeConfiguration pluginConfiguration = ctx.getBean(MergeConfiguration.class);
		pluginConfiguration.logConfiguration();

		MergeDocumentParser documentParser = ctx.getBean(MergeDocumentParser.class);
		documentParser.parseDocuments();

		Merge merge = ctx.getBean(Merge.class);
		merge.generateRelaySchema();

		ctx.close();

		getLog().debug("Finished generation of the merged schema");
	}

	@Override
	@Internal
	public Logger getLog() {
		return mergeGraphQLSchemaExtension.getLog();
	}

	@Override
	@Input
	public String getPackageName() {
		return mergeGraphQLSchemaExtension.getPackageName();
	}

	@Override
	@Input
	public String getResourceEncoding() {
		return mergeGraphQLSchemaExtension.getResourceEncoding();
	}

	@Override
	@InputDirectory
	@Optional
	public File getSchemaFileFolder() {
		return mergeGraphQLSchemaExtension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return mergeGraphQLSchemaExtension.getSchemaFilePattern();
	}

	@Override
	@InputDirectory
	public File getTargetFolder() {
		return mergeGraphQLSchemaExtension.getTargetFolder();
	}

	@Override
	@Input
	public String getTargetSchemaFileName() {
		return mergeGraphQLSchemaExtension.getTargetSchemaFileName();
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return mergeGraphQLSchemaExtension.getTemplates();
	}

}
