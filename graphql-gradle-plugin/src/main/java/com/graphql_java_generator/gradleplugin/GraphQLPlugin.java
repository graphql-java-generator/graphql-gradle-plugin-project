/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
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
				logger.info("[in project.afterEvaluate2] Before registering generated folders for project '"
						+ p.getName() + "'");
				// Below is an attempt to automatically say to Gradle, that the codeGeneration must occur before the
				// compilation. But this fails, as doing the code below would activate all the plugin's tasks.
				// And I found no way to identify all the task that will be executed before the Project is evaluated,
				// and the TaskExecutionGraph (that can be retrieved from the Gradle instance) is populated. But at this
				// time, it's too late to change the tasks execution order
				// for (Task task : p.getTasks()) {
				// if (!task.getEnabled()) {
				// logger.info(" Ignoring disabled task '" + task.getName() + "'");
				// } else if (task.getState().getSkipped()) {
				// logger.info(" Ignoring skipped task '" + task.getName() + "'");
				// } else if (task instanceof CommonTask) {
				// logger.info(" Registering generated folders for task '" + task.getName() + "'");
				// ((CommonTask) task).registerGeneratedFolders();
				//
				// // This task generates the code. So it must be executed before the compilation and the resource
				// // processing
				// addDependency("compileJava", task);
				// for (Task t : addDependency("processResources", task)) {
				// // Some resources are generated in double. Here is a workaround
				// ((CopySpec) t).setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
				// logger.info(" Setting property: {}.duplicatesStrategy = {}", t.getName(),
				// DuplicatesStrategy.INCLUDE);
				// }
				// } else {
				// logger.info(" Ignoring task '" + task.getName() + "'");
				// }
				// } // for

				Set<Task> compileJavaTasks = project.getTasksByName("compileJava", false);
				Set<Task> processResourcesTasks = project.getTasksByName("processResources", false);

				Set<Task> allDependingTasks = new HashSet<>();
				allDependingTasks.addAll(compileJavaTasks);
				allDependingTasks.addAll(processResourcesTasks);

				// Add all tasks for this plugin as dependencies for the compileJava and processResources tasks
				for (Task t : project.getTasks()) {
					// GenerateServerCodeExtension e = t.getExtensions().findByType(GenerateServerCodeExtension.class);
					// Object eName = t.getExtensions().findByName(GENERATE_SERVER_CODE_EXTENSION);
					// if (e != null) {
					// logger.info("getTasks(): {}, initialized={}", t.getPath(), e.isInitialized());
					// } else if (eName != null && eName instanceof GenerateServerCodeExtension) {
					// logger.info("getTasks(): {}, initialized={}", t.getPath(), t.property("initialized"));
					// } else if (eName != null) {
					// logger.info("getTasks(): {}, eName.class={}", t.getPath(), eName.getClass().getName());
					// } else if (taskProvider.getName().equals(t.getName())) {
					// // if (taskProvider.isPresent()) {
					// // logger.info("getTasks(): {}, taskProvider.present={}", t.getPath(),
					// // taskProvider.isPresent());
					// // } else if (taskProvider.getName().equals(t.getName()) && !taskProvider.isPresent()) {
					// // logger.info("getTasks(): {}, taskProvider.present={}", t.getPath(),
					// // taskProvider.isPresent());
					// // }
					// logger.info("getTasks(): {}, taskProvider.present={}, initialized={}", t.getPath(),
					// taskProvider.isPresent(), t.property("initialized"));
					if (t.hasProperty("initialized")) {
						logger.debug("getTasks(): {}, taskProvider.present={}, initialized={}", t.getPath(),
								taskProvider.isPresent(), t.property("initialized"));
						if ((boolean) t.property("initialized"))
							addTaskAsADependencyToAnotherTask(p, t, allDependingTasks);
					} else {
						logger.debug("getTasks(): {}", t.getPath());
					}
				}

				// project.getTasks().stream()
				// .forEach(t -> logger.info("getTasks(): {}, extensions={}", t.getPath(), t.getExtensions()));
				// project.getDefaultTasks().stream().forEach(t -> logger.info("getDefaultTasks(): {}", t));
				// addThisTaskAsADependencyToAnotherTask(p, GENERATE_CLIENT_CODE_TASK_NAME, allDependingTasks);
				// addThisTaskAsADependencyToAnotherTask(p, GENERATE_POJO_TASK_NAME, allDependingTasks);
				// addThisTaskAsADependencyToAnotherTask(p, GENERATE_SERVER_CODE_TASK_NAME, allDependingTasks);
				// addThisTaskAsADependencyToAnotherTask(p, GRAPHQL_GENERATE_CODE_TASK_NAME, allDependingTasks);
				// addThisTaskAsADependencyToAnotherTask(p, MERGE_TASK_NAME, allDependingTasks);

				if (processResourcesTasks.size() == 0) {
					throw new RuntimeException(
							"Found no 'processResources' task, when executing project.afterEvaluate()");
				}
				for (Task t : processResourcesTasks) { // There should be one.
					// Some resources are generated in double. This can generate an error when building the project.
					// Here is a workaround:
					((CopySpec) t).setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
					logger.info(" Setting property: {}.duplicatesStrategy = {}", t.getName(),
							DuplicatesStrategy.INCLUDE);
				}

				// For internal tests, to be removed
				String prefix = "    ";
				for (Configuration conf : project.getBuildscript().getConfigurations()) {
					System.out.println("Reading buildScript configuration " + conf.getName() + ":");
					if (!conf.isCanBeResolved()) {
						System.out.println(prefix + "can not be resolved");
					} else if (conf.getName().startsWith("test")) {
						System.out.println(prefix + "test dependencies are ignored");
					} else {
						// TODO: conf: try to add spring-context 6.0.8
						// conf.getDependencies(): list of the declared dependencies
						// conf.getAllDependencies(): list of the declared dependencies + super configuration
						for (Dependency dep : conf.getAllDependencies()) {
							System.out.println(prefix + dep.getGroup() + ":" + dep.getName() + ":" + dep.getVersion());
						}
					}
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
					logger.info("Adding dependency: {}.dependsOn({})", dependingTask.getPath(), task.getPath());
					dependingTask.dependsOn(task.getPath());
				}
			}

		});// project.afterEvaluate
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		if (properties == null) {
			properties = new Properties();
			try {
				try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
					properties.load(is);
				}
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			} // try
		} // if
		return properties;
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
