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
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.conf.CustomScalarDefinition;
import com.graphql_java_generator.plugin.conf.GenerateServerCodeConfiguration;
import com.graphql_java_generator.plugin.conf.GraphQLConfiguration;
import com.graphql_java_generator.plugin.conf.Logger;
import com.graphql_java_generator.plugin.conf.Packaging;
import com.graphql_java_generator.plugin.conf.PluginMode;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeDocumentParser;
import com.graphql_java_generator.plugin.generate_code.GenerateCodeGenerator;

public class GenerateServerCodeTask extends DefaultTask implements GenerateServerCodeConfiguration {

	/** The Gradle extension, to read the plugin parameters from the script */
	private transient GenerateServerCodeExtension generateServerCodeExtension = null;

	final Project project;

	/**
	 * @param project
	 *            The current Gradle project
	 * @param generateServerCodeExtension
	 *            The Gradle extension, which contains all parameters found in the build script
	 */
	@Inject
	public GenerateServerCodeTask(Project project, GenerateServerCodeExtension generateServerCodeExtension) {
		this.project = project;
		this.generateServerCodeExtension = generateServerCodeExtension;
	}

	@TaskAction
	public void execute() throws IOException {

		getPluginLogger().debug("Starting merging of the given GraphQL schemas");

		// We'll use Spring IoC
		GenerateServerCodeSpringConfiguration.generateServerCodeExtension = generateServerCodeExtension;
		AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(
				GenerateServerCodeSpringConfiguration.class);

		// Let's log the current configuration (this will do something only when in debug mode)
		GenerateServerCodeConfiguration pluginConfiguration = ctx.getBean(GenerateServerCodeConfiguration.class);
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
		return generateServerCodeExtension.getCustomScalars();
	}

	@Override
	@Input
	public String getJavaTypeForIDType() {
		return generateServerCodeExtension.javaTypeForIDType;
	}

	@Override
	@Internal
	public Logger getPluginLogger() {
		return generateServerCodeExtension.getPluginLogger();
	}

	@Override
	@Input
	public PluginMode getMode() {
		return generateServerCodeExtension.getMode();
	}

	@Override
	@Input
	public String getPackageName() {
		return generateServerCodeExtension.getPackageName();
	}

	@Override
	@Input
	public Packaging getPackaging() {
		return generateServerCodeExtension.getPackaging();
	}

	@Override
	public File getProjectDir() {
		return project.getProjectDir();
	}

	@Override
	@Input
	public String getScanBasePackages() {
		return generateServerCodeExtension.getScanBasePackages();
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
		return generateServerCodeExtension.getSchemaFileFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return generateServerCodeExtension.getSchemaFilePattern();
	}

	@Override
	@InputFile
	@Optional
	public File getSchemaPersonalizationFile() {
		return generateServerCodeExtension.getSchemaPersonalizationFile();
	}

	@Override
	@Input
	public String getSourceEncoding() {
		return generateServerCodeExtension.getSourceEncoding();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		return generateServerCodeExtension.getTargetClassFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		return generateServerCodeExtension.getTargetSourceFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetResourceFolder() {
		return generateServerCodeExtension.getTargetResourceFolder();
	}

	@Override
	@Input
	public Map<String, String> getTemplates() {
		return generateServerCodeExtension.getTemplates();
	}

	@Override
	@Input
	public boolean isAddRelayConnections() {
		return generateServerCodeExtension.isAddRelayConnections();
	}

	@Override
	@Input
	public boolean isCopyRuntimeSources() {
		return generateServerCodeExtension.isCopyRuntimeSources();
	}

	@Override
	@Input
	public boolean isGenerateBatchLoaderEnvironment() {
		return generateServerCodeExtension.isGenerateBatchLoaderEnvironment();
	}

	@Override
	@Input
	public boolean isGenerateJPAAnnotation() {
		return generateServerCodeExtension.isGenerateJPAAnnotation();
	}

	@Override
	@Input
	public boolean isSeparateUtilityClasses() {
		return generateServerCodeExtension.isSeparateUtilityClasses();
	}

	@Override
	@Input
	public boolean isSkipGenerationIfSchemaHasNotChanged() {
		return generateServerCodeExtension.isSkipGenerationIfSchemaHasNotChanged();
	}
}
