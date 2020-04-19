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
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.CodeGenerator;
import com.graphql_java_generator.plugin.CustomScalarDefinition;
import com.graphql_java_generator.plugin.DocumentParser;
import com.graphql_java_generator.plugin.Logger;
import com.graphql_java_generator.plugin.Packaging;
import com.graphql_java_generator.plugin.PluginConfiguration;
import com.graphql_java_generator.plugin.PluginMode;

/**
 * Generates the code from the given GraphQL schema.
 * 
 * @author EtienneSF
 */
public class GraphqlGenerateCodeTask extends DefaultTask implements PluginConfiguration {

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GraphqlExtension graphqlExtension = null;

	final Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param extension
	 *            The GraphQL extension, which contains all parameters found in the build script
	 */
	@Inject
	public GraphqlGenerateCodeTask(Project project, GraphqlExtension graphqlExtension) {
		this.project = project;
		this.graphqlExtension = graphqlExtension;
	}

	@TaskAction
	public void execute() {
		try {

			getLog().debug("Starting generation of java classes from graphqls files");

			// We'll use Spring IoC
			SpringConfiguration.graphqlExtension = graphqlExtension;
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			PluginConfiguration pluginConfiguration = ctx.getBean(PluginConfiguration.class);
			pluginConfiguration.logConfiguration();

			DocumentParser documentParser = ctx.getBean(DocumentParser.class);
			documentParser.parseDocuments();

			CodeGenerator codeGenerator = ctx.getBean(CodeGenerator.class);
			int nbGeneratedClasses = codeGenerator.generateCode();

			ctx.close();

			getLog().info(nbGeneratedClasses + " java classes have been generated from the schema(s) '"
					+ pluginConfiguration.getSchemaFilePattern() + "' in the package '"
					+ pluginConfiguration.getPackageName() + "'");

			// getLog().debug("Source dir added to the main.java.srcDirs: "
			// + pluginConfiguration.getTargetSourceFolder().getAbsolutePath());
			// project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main").getJava()
			// .getSrcDirs().add(pluginConfiguration.getTargetSourceFolder());

			getLog().debug("Finished generation of java classes from graphqls files (5)");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Override
	@Input
	public List<CustomScalarDefinition> getCustomScalars() {
		return graphqlExtension.getCustomScalars();
	}

	@Override
	public Logger getLog() {
		return graphqlExtension.getLog();
	}

	@Override
	@Input
	public PluginMode getMode() {
		return graphqlExtension.getMode();
	}

	@Override
	@Input
	public String getPackageName() {
		return graphqlExtension.getPackageName();
	}

	@Override
	@Input
	public Packaging getPackaging() {
		return graphqlExtension.getPackaging();
	}

	@Override
	@InputDirectory
	@Optional
	public File getSchemaFileFolder() {
		return graphqlExtension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return graphqlExtension.getSchemaFilePattern();
	}

	@Override
	@InputFile
	@Optional
	public File getSchemaPersonalizationFile() {
		return graphqlExtension.getSchemaPersonalizationFile();
	}

	@Override
	@Input
	public String getSourceEncoding() {
		return graphqlExtension.getSourceEncoding();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		return graphqlExtension.getTargetClassFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		return graphqlExtension.getTargetSourceFolder();
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return graphqlExtension.getTemplates();
	}

	@Override
	@Input
	public boolean isCopyRuntimeSources() {
		return graphqlExtension.isCopyRuntimeSources();
	}

	@Override
	@Input
	public boolean isGenerateJPAAnnotation() {
		return graphqlExtension.isGenerateJPAAnnotation();
	}

	@Override
	@Input
	public boolean isSeparateUtilityClasses() {
		return graphqlExtension.isSeparateUtilityClasses();
	}

}
