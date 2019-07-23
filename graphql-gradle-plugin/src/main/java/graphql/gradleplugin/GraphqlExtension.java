package graphql.gradleplugin;

import java.io.File;

import org.gradle.api.Project;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.plugin.Logger;
import com.graphql_java_generator.plugin.Packaging;
import com.graphql_java_generator.plugin.PluginConfiguration;
import com.graphql_java_generator.plugin.PluginMode;

/**
 * Parameters for the GraphQL Gradle plugin.
 * 
 * @author EtienneSF
 *
 */
@Component
public class GraphqlExtension implements PluginConfiguration {

	final Project project;

	/** The packageName in which the generated classes will be created */
	String packageName;

	/** The encoding for the generated source files */
	String sourceEncoding;

	GradleLogger logger;

	/**
	 * The generation mode: either client or server. Choose client to generate the code which can query a graphql server
	 * or server to generate a code for the server side.
	 */
	PluginMode mode;

	/**
	 * The pattern to find the graphql schema file(s). The default value is "/*.graphqls" meaning that the maven plugin
	 * will search all graphqls files in the "src/main/resources" folder. The current version can read only one
	 * file.<BR/>
	 * In the future, it will search for all graphqls file in the root of the classpath.<BR/>
	 * In the future, it will be possible to set in schemaFilePattern values like "myFolder/*.graphqls" to search for
	 * all schemas in the "myFolder" subfolder of src/main/resources (for the plugin execution). At runtime, the path
	 * used for search will then be classpath:/myFolder/*.graphqls".<BR/>
	 * It will also be possible to define one schema, by putting "mySchema.myOtherExtension" in the schemaFilePattern
	 * configuration parameter of the plugin.
	 */
	String schemaFilePattern;

	/**
	 * schemaPersonalizationFile is the file name where the GraphQL maven plugin will find personalization that it must
	 * apply before generating the code. See the doc for more details.<BR/>
	 * The standard file would be something like src/main/graphql/schemaPersonalizationFile.json, which avoid to embed
	 * this compile time file within your maven artefact<BR/>
	 * The default value is a file named "noPersonalization", meaning: no schema personalization.
	 */
	String schemaPersonalizationFile;

	/** The folder where the generated classes will be generated */
	String targetSourceFolder;

	public GraphqlExtension(Project project) {
		this.project = project;
		this.logger = new GradleLogger(project);
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public Logger getLog() {
		return logger;
	}

	@Override
	public PluginMode getMode() {
		return mode;
	}

	@Override
	public Packaging getPackaging() {
		// We calculate as late as possible this packaging. So no precaculation on creation, we wait for a call on this
		// getter. At this time, it should be triggered by the gradle plugin execution (and not its configuration)
		return (project.getTasksByName("war", false).size() >= 1) ? Packaging.war : Packaging.jar;
	}

	@Override
	public File getResourcesFolder() {
		return project.file("./src/main/resources");
	}

	@Override
	public String getSchemaFilePattern() {
		return schemaFilePattern;
	}

	@Override
	public File getSchemaPersonalizationFile() {
		return project.file(schemaPersonalizationFile);
	}

	@Override
	public String getSourceEncoding() {
		return sourceEncoding;
	}

	@Override
	public File getTargetClassFolder() {
		return project.file("$buildDir/classes");
	}

	@Override
	public File getTargetSourceFolder() {
		return project.file("$buildDir/generated/" + GraphqlPlugin.GRAPHQL_GENERATE_CODE_TASK_NAME);
	}

}
