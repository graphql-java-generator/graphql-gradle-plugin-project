/**
 * 
 */
package com.graphql_java_generator.gradleplugin;

import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPlugin;
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

	@Override
	public void apply(Project project) {
		applyGenerateClientCode(project);
		applyGeneratePojo(project);
		applyGenerateServerCode(project);
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

				Set<Task> tasks = project.getTasksByName("processResources", false);
				if (tasks.size() == 0) {
					throw new RuntimeException(
							"Found no 'processResources' task, when executing project.afterEvaluate()");
				}
				for (Task t : tasks) { // There should be one.
					// Some resources are generated in double. This can generate an error when building the project.
					// Here is a workaround:
					((CopySpec) t).setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
					logger.info(" Setting property: {}.duplicatesStrategy = {}", t.getName(),
							DuplicatesStrategy.INCLUDE);
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
			private Set<Task> addDependency(String taskName, Task dependsOnTask) {
				Set<Task> tasks = project.getTasksByName(taskName, false);
				if (tasks.size() == 0) {
					throw new RuntimeException("Found no '" + taskName + taskName + "' task, when trying to execute: "
							+ taskName + ".dependsOn(\"" + dependsOnTask.getName() + "\"");
				}
				for (Task t : tasks) {
					logger.info("    Adding task dependency: {}.dependsOn({})", t.getName(), dependsOnTask.getName());
					t.dependsOn(dependsOnTask);
				}
				return tasks;
			}
		});// project.afterEvaluate
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

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
	 */
	private void applyGenerateServerCode(Project project) {
		project.getExtensions().create(GENERATE_SERVER_CODE_EXTENSION, GenerateServerCodeExtension.class, project);
		logger.debug("Applying generateServerCode task");
		project.getTasks().register(GENERATE_SERVER_CODE_TASK_NAME, GenerateServerCodeTask.class);

		// Apply the java plugin, then add the generated source
		project.getPlugins().apply(JavaPlugin.class);
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
