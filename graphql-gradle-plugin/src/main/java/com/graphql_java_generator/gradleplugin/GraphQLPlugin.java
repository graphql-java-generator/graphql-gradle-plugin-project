/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 * @author EtienneSF
 *
 */
public class GraphQLPlugin implements Plugin<Project> {

	/** The extension name to configure the GraphQL plugin, for the client code generation task */
	final public static String GENERATE_CLIENT_CODE_EXTENSION = "generateClientCodeConf";
	/** The name of the task that generates the client code from the given GraphQL schemas */
	final public static String GENERATE_CLIENT_CODE_TASK_NAME = "generateClientCode";

	/** The extension name to configure the GraphQL plugin, for the server code generation task */
	final public static String GENERATE_SERVER_CODE_EXTENSION = "generateServerCodeConf";
	/** The name of the task that generates the server code from the given GraphQL schemas */
	final public static String GENERATE_SERVER_CODE_TASK_NAME = "generateServerCode";

	/** The extension name to configure the GraphQL plugin, for the code generation task */
	final public static String GRAPHQL_EXTENSION = "graphql";
	/** The name of the task that generates the code from the given GraphQL schemas */
	final public static String GRAPHQL_GENERATE_CODE_TASK_NAME = "graphqlGenerateCode";

	/** The extension name to configure the GraphQL merge task */
	final public static String MERGE_EXTENSION = "generateGraphQLSchemaConf";
	/** The name of the task that generates a GraphqL that merges several GraphQL schemas */
	final public static String MERGE_TASK_NAME = "generateGraphQLSchema";

	@Override
	public void apply(Project project) {
		applyGenerateClientCode(project);
		applyGenerateServerCode(project);
		applyGraphQLGenerateCode(project);
		applyGenerateGraphQLSchema(project);
	}

	/**
	 * Applies the <I>generateClientCode</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateClientCode(Project project) {
		GenerateClientCodeExtension extension = project.getExtensions().create(GENERATE_CLIENT_CODE_EXTENSION,
				GenerateClientCodeExtension.class, project);
		project.getTasks().register(GENERATE_CLIENT_CODE_TASK_NAME, GenerateClientCodeTask.class, project, extension);

		extension.getPluginLogger().debug("Applying generateClientCode task");

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);

		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getJava().srcDir(extension.getTargetSourceFolder());
	}

	/**
	 * Applies the <I>generateServerCode</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateServerCode(Project project) {
		GenerateServerCodeExtension extension = project.getExtensions().create(GENERATE_SERVER_CODE_EXTENSION,
				GenerateServerCodeExtension.class, project);
		project.getTasks().register(GENERATE_SERVER_CODE_TASK_NAME, GenerateServerCodeTask.class, project, extension);

		extension.getPluginLogger().debug("Applying generateServerCode task");

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);

		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getJava().srcDir(extension.getTargetSourceFolder());
	}

	/**
	 * Applies the <I>graphqlGenerateCode</I> task
	 * 
	 * @param project
	 */
	private void applyGraphQLGenerateCode(Project project) {
		GraphQLExtension extension = project.getExtensions().create(GRAPHQL_EXTENSION, GraphQLExtension.class, project);
		project.getTasks().register(GRAPHQL_GENERATE_CODE_TASK_NAME, GraphQLGenerateCodeTask.class, project, extension);

		extension.getPluginLogger().debug("Applying GraphQL task");

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);

		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		main.getJava().srcDir(extension.getTargetSourceFolder());
	}

	/**
	 * Applies the <I>graphqlGenerateCode</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateGraphQLSchema(Project project) {
		GenerateGraphQLSchemaExtension extension = project.getExtensions().create(MERGE_EXTENSION,
				GenerateGraphQLSchemaExtension.class, project);
		project.getTasks().register(MERGE_TASK_NAME, GenerateGraphQLSchemaTask.class, project, extension);
	}
}