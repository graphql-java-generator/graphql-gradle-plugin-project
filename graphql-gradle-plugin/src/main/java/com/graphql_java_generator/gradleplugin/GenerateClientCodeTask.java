package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.GenerateCodeGenerator;
import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;
import com.graphql_java_generator.plugin.conf.Logger;
import com.graphql_java_generator.plugin.conf.PluginMode;

public class GenerateClientCodeTask extends DefaultTask implements GenerateClientCodeConfiguration {

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GenerateClientCodeExtension generateClientCodeExtension = null;

	final Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param generateClientCodeExtension
	 *            The Gradle extension, which contains all parameters found in the build script
	 */
	@Inject
	public GenerateClientCodeTask(Project project, GenerateClientCodeExtension generateClientCodeExtension) {
		this.project = project;
		this.generateClientCodeExtension = generateClientCodeExtension;
	}

	@TaskAction
	public void execute() throws IOException {

		getPluginLogger().debug("Starting merging of the given GraphQL schemas");

		// We'll use Spring IoC
		GenerateClientCodeSpringConfiguration.generateClientCodeExtension = generateClientCodeExtension;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateClientCodeSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateClientCodeConfiguration pluginConfiguration = ctx.getBean(GenerateClientCodeConfiguration.class);
		pluginConfiguration.logConfiguration();

		GenerateCodeDocumentParser documentParser = ctx.getBean(GenerateCodeDocumentParser.class);
		documentParser.parseDocuments();

		GenerateCodeGenerator codeGenerator = ctx.getBean(GenerateCodeGenerator.class);
		int nbGeneratedClasses = codeGenerator.generateCode();

		ctx.close();

		getPluginLogger().info(nbGeneratedClasses + " java classes have been generated from the schema(s) '"
				+ pluginConfiguration.getSchemaFilePattern() + "' in the package '"
				+ pluginConfiguration.getPackageName() + "'");

		getPluginLogger().debug("Finished generation of java classes from graphqls files (5)");
	}

	@Override
	@Input
	public List<CustomScalarDefinition> getCustomScalars() {
		return generateClientCodeExtension.getCustomScalars();
	}

	@Override
	@Internal
	public Logger getPluginLogger() {
		return generateClientCodeExtension.getPluginLogger();
	}

	@Override
	@Input
	public PluginMode getMode() {
		return generateClientCodeExtension.getMode();
	}

	@Override
	@Input
	public String getPackageName() {
		return generateClientCodeExtension.getPackageName();
	}

	@Override
	@InputDirectory
	@Optional
	public File getSchemaFileFolder() {
		return generateClientCodeExtension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return generateClientCodeExtension.getSchemaFilePattern();
	}

	@Override
	@Input
	public String getSourceEncoding() {
		return generateClientCodeExtension.getSourceEncoding();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		return generateClientCodeExtension.getTargetClassFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		return generateClientCodeExtension.getTargetSourceFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetResourceFolder() {
		return generateClientCodeExtension.getTargetResourceFolder();
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return generateClientCodeExtension.getTemplates();
	}

	@Override
	@Input
	public boolean isAddRelayConnections() {
		return generateClientCodeExtension.isAddRelayConnections();
	}

	@Override
	@Input
	public boolean isCopyRuntimeSources() {
		return generateClientCodeExtension.isCopyRuntimeSources();
	}

	@Override
	@Input
	public boolean isGenerateDeprecatedRequestResponse() {
		return generateClientCodeExtension.isGenerateDeprecatedRequestResponse();
	}

	@Override
	@Input
	public boolean isSeparateUtilityClasses() {
		return generateClientCodeExtension.isSeparateUtilityClasses();
	}

}
