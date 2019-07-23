/**
 * 
 */
package graphql.gradleplugin;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.graphql_java_generator.plugin.CodeGenerator;
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

	final Project project;

	/** The Gradle extension, to read the plugin parameters from the script */
	private GraphqlExtension extension = null;

	@Inject
	public GraphqlGenerateCodeTask(Project project) {
		this.project = project;
	}

	@Override
	@Input
	public String getPackageName() {
		return getGraphqlExtension().getPackageName();
	}

	@Override
	public Logger getLog() {
		return getGraphqlExtension().getLog();
	}

	@Override
	@Input
	public PluginMode getMode() {
		return getGraphqlExtension().getMode();
	}

	@Override
	@Input
	public Packaging getPackaging() {
		return getGraphqlExtension().getPackaging();
	}

	@Override
	@Input
	public File getResourcesFolder() {
		return getGraphqlExtension().getResourcesFolder();
	}

	@Override
	@Input
	public String getSchemaFilePattern() {
		return getGraphqlExtension().getSchemaFilePattern();
	}

	@Override
	@InputFile
	public File getSchemaPersonalizationFile() {
		return getGraphqlExtension().getSchemaPersonalizationFile();
	}

	@Override
	@Input
	public String getSourceEncoding() {
		return getGraphqlExtension().getSourceEncoding();
	}

	@Override
	@OutputDirectory
	public File getTargetClassFolder() {
		return getGraphqlExtension().getTargetClassFolder();
	}

	@Override
	@OutputDirectory
	public File getTargetSourceFolder() {
		return getGraphqlExtension().getTargetSourceFolder();
	}

	@TaskAction
	public void execute() {
		try {

			getLog().debug("Starting generation of java classes from graphqls files");

			// We'll use Spring IoC
			AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);

			// Let's log the current configuration (this will do something only when in debug mode)
			PluginConfiguration pluginConfiguration = ctx.getBean(PluginConfiguration.class);
			pluginConfiguration.logConfiguration();

			DocumentParser documentParser = ctx.getBean(DocumentParser.class);
			documentParser.parseDocuments();

			CodeGenerator codeGenerator = ctx.getBean(CodeGenerator.class);
			int nbGeneratedClasses = codeGenerator.generateCode();

			ctx.close();

			// Add of the generated source
			project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main").getJava()
					.getFiles().add(pluginConfiguration.getTargetSourceFolder());

			getLog().info(nbGeneratedClasses + " java classes have been generated the schema(s) '"
					+ pluginConfiguration.getSchemaFilePattern() + "' in the package '"
					+ pluginConfiguration.getPackageName() + "'");

		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}

	@Input
	public GraphqlExtension getGraphqlExtension() {
		if (extension == null) {
			extension = getProject().getExtensions().findByType(GraphqlExtension.class);
		}
		return extension;
	}
}
