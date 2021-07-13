package com.graphql_java_generator.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateClientCodeConfiguration;
import com.graphql_java_generator.plugin.conf.GenerateGraphQLSchemaConfiguration;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

public class GenerateClientCodeTask extends DefaultTask implements GenerateClientCodeConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GenerateClientCodeTask.class);

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GenerateClientCodeExtension extension = null;

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
		this.extension = generateClientCodeExtension;
	}

	@TaskAction
	public void execute() throws IOException {

		logger.debug("Executing " + this.getClass().getName());

		// We'll use Spring IoC
		GenerateClientCodeSpringConfiguration.generateClientCodeExtension = extension;
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

		// Let's add the folders where the sources and resources have been generated to the project
		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getJava().srcDir(extension.getTargetSourceFolder());
		main.getResources().srcDir(extension.getTargetResourceFolder());

		logger.info(nbGeneratedClasses + " java classes have been generated from the schema(s) '"
				+ pluginConfiguration.getSchemaFilePattern() + "' in the package '"
				+ pluginConfiguration.getPackageName() + "'");

		logger.debug("Finished generation of java classes from graphqls files (5)");
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
	@Internal
	public File getProjectDir() {
		return project.getProjectDir();
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
	@Input
	public String getSourceEncoding() {
		return extension.getSourceEncoding();
	}

	@Override
	@Input
	public String getSpringBeanSuffix() {
		return extension.getSpringBeanSuffix();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		return extension.getTargetClassFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		return extension.getTargetSourceFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetResourceFolder() {
		return extension.getTargetResourceFolder();
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
	public boolean isGenerateDeprecatedRequestResponse() {
		return extension.isGenerateDeprecatedRequestResponse();
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
