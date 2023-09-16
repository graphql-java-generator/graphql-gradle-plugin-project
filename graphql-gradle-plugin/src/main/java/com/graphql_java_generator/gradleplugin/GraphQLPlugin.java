/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author EtienneSF
 *
 */
public class GraphQLPlugin implements Plugin<Project> {

	private static final Logger logger = LoggerFactory.getLogger(GraphQLPlugin.class);

	/** The extension name to configure the GraphQL plugin, for the client code generation task */
	final public static String GENERATE_CLIENT_CODE_EXTENSION = "generateClientCodeConf";
	/** The name of the task that generates the client code from the given GraphQL schemas */
	final public static String GENERATE_CLIENT_CODE_TASK_NAME = "generateClientCode";

	/** The extension name to configure the GraphQL plugin, for the generatePojo task */
	final public static String GENERATE_POJO_EXTENSION = "generatePojoConf";
	/** The name of the task that generates the POJOs from the given GraphQL schemas */
	final public static String GENERATE_POJO_TASK_NAME = "generatePojo";

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

	/**
	 * The properties loaded from application.properties
	 * 
	 * @see #getProperties()
	 */
	private Properties properties = null;

	@Override
	public void apply(Project project) {
		applyGenerateClientCode(project);
		applyGeneratePojo(project);
		TaskProvider<GenerateServerCodeTask> taskProvider = applyGenerateServerCode(project);
		applyGraphQLGenerateCode(project);
		applyGenerateGraphQLSchema(project);

		// The generated resources must be declared:
		// - Not here as it's too soon: the project has not been evaluated yet
		// - Not in the Task.execute method, as this method is not executed if the task is up-to-date
		// So we create a dedicated Action for that. Let's register it, so that it is executed once the project is
		// evaluated.
		project.afterEvaluate(new Action<Project>() {

			@Override
			public void execute(Project p) {
				Set<Task> compileJavaTasks = project.getTasksByName("compileJava", false);
				Set<Task> processResourcesTasks = project.getTasksByName("processResources", false);
				Set<Task> allDependingTasks = new HashSet<>();
				allDependingTasks.addAll(compileJavaTasks);
				allDependingTasks.addAll(processResourcesTasks);

				Set<String> list = new TreeSet<>();
				for (Task t : p.getTasks()) {
					if (t.hasProperty("initialized")) {
						list.add(String.format(
								"[in project.afterEvaluate (getTasks)]   %1s %2s (initialized=[%3s]%4s, enabled=%5s, extensions=%6s)",
								p.getName(), t.getPath(), t.property("initialized").getClass().getSimpleName(),
								t.property("initialized"), t.getEnabled(), t.getExtensions()));
						if ((boolean) t.property("initialized")) {
							logger.info(
									"Adding the {} task as a dependency for the compileJava and processResources tasks",
									t.getName());
							addTaskAsADependencyToAnotherTask(p, t, allDependingTasks);
						} else {
							logger.debug(
									"Task {} ignored, as its initialized state is {} (it will not be added as a dependency for the compileJava and processResources tasks)",
									t.getName(), t.property("initialized"));
						}
					}
				}
				for (String s : list) {
					logger.debug(s);
				}
			}

			/**
			 * Add a dependency on taskName, like this: <I>taskName.dependsOn(dependsOnTask)</I>.
			 * 
			 * @param taskName
			 * @param dependsOnTask
			 * @return the set of tasks, that has <I>taskName<I> as a name. This set contains at least one item (and
			 *         should not contain more than one)
			 */
			private void addTaskAsADependencyToAnotherTask(Project p, Task task, Set<Task> dependingTasks) {
				for (Task dependingTask : dependingTasks) {
					dependingTask.dependsOn(task.getPath());
				}
			}
		});

	}

	/**
	 * Retrieve the gradle project.version, that has been copied into the 'plugin.version' property of the
	 * 'application.properties' file
	 */
	public String getVersion() {
		return getProperties().get("plugin.version").toString();
	}

	/**
	 * Retrieve the version of the <i>graphql-maven-plugin-logic</i> library, that contains all the plugin logic. This
	 * version has been copied into the 'plugin.version' property of the 'application.properties' file
	 */
	public String getDependenciesVersion() {
		return getProperties().get("graphqlMavenPluginLogic.version").toString();
	}

	/** Manual reading of the application.properties file, as this is not a spring boot project. */
	private Properties getProperties() {
		if (this.properties == null) {
			this.properties = new Properties();
			try {
				try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
					this.properties.load(is);
				}
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} // try
		} // if
		return this.properties;
	}// getProperties()

	/**
	 * Applies the <I>generateClientCode</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateClientCode(Project project) {
		project.getExtensions().create(GENERATE_CLIENT_CODE_EXTENSION, GenerateClientCodeExtension.class, project);
		logger.debug("Applying generateClientCode task");
		project.getTasks().register(GENERATE_CLIENT_CODE_TASK_NAME, GenerateClientCodeTask.class);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

	/**
	 * Applies the <I>generateGraphQLSchema</I> task
	 * 
	 * @param project
	 */
	private void applyGenerateGraphQLSchema(Project project) {
		project.getExtensions().create(MERGE_EXTENSION, GenerateGraphQLSchemaExtension.class, project);
		logger.debug("Applying generateGraphQLSchema task");
		project.getTasks().register(MERGE_TASK_NAME, GenerateGraphQLSchemaTask.class);
	}

	/**
	 * Applies the <I>generatePojo</I> task
	 * 
	 * @param project
	 */
	private void applyGeneratePojo(Project project) {
		project.getExtensions().create(GENERATE_POJO_EXTENSION, GeneratePojoExtension.class, project);
		logger.debug("Applying generatePojo task");
		project.getTasks().register(GENERATE_POJO_TASK_NAME, GeneratePojoTask.class);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

	/**
	 * Applies the <I>generateServerCode</I> task
	 * 
	 * @param project
	 * @return
	 */
	private TaskProvider<GenerateServerCodeTask> applyGenerateServerCode(Project project) {
		project.getExtensions().create(GENERATE_SERVER_CODE_EXTENSION, GenerateServerCodeExtension.class, project);
		logger.info("Applying generateServerCode task");
		TaskProvider<GenerateServerCodeTask> ret = project.getTasks().register(GENERATE_SERVER_CODE_TASK_NAME,
				GenerateServerCodeTask.class);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);

		return ret;
	}

	/**
	 * Applies the <I>graphqlGenerateCode</I> task
	 * 
	 * @param project
	 */
	private void applyGraphQLGenerateCode(Project project) {
		project.getExtensions().create(GRAPHQL_EXTENSION, GraphQLExtension.class, project);
		logger.debug("Applying GraphQL task");
		project.getTasks().register(GRAPHQL_GENERATE_CODE_TASK_NAME, GraphQLGenerateCodeTask.class);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
	}

}
